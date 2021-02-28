drop database if exists order_management;
create database order_management;
use order_management;

create table client (
    name varchar(45) unique primary key,
    city varchar(45)
);

create table product (
	name varchar(45) unique primary key,
    quantity int,
    price double
);

create table orderView (
	name varchar(45),
    product varchar(45),
    quantity int,
    foreign key (name) references client(name) on delete cascade,
    foreign key (product) references product(name) on delete cascade,
    primary key (name, product)
);

drop procedure if exists add_new_order;
delimiter //
create procedure add_new_order(clientName varchar(45), productName varchar(45), quantity double)
begin
	declare clientID, productID int;
    set clientID:=(select id from client where client.name like clientName);
    set productID:=(select id from product where product.name like productName);
    insert into orders(idClient, idProduct, quantity) values (clientID, productID, quantity) on duplicate key update quantity = values(quantity);
end; //
delimiter ;

drop trigger if exists
delimiter //
create trigger update_stock
after insert on orderView for each row
begin
	declare currentQuantity int;
	set currentQuantity:=(select product.quantity from product where product.name like new.product);
    if new.quantity > currentQuantity then
		update orderView set quantity = currentQuantity;
        update product set quantity = 0;
	else update product set quantity = product.quantity - new.quantity;
	end if;
end; // 
delimiter ;