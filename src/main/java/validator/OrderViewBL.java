package validator;

import com.itextpdf.text.pdf.PdfPTable;
import dataAccess.OrderViewDA;
import model.OrderView;

import java.util.NoSuchElementException;

public class OrderViewBL {
    private OrderViewDA orderViewDA;

    public OrderViewBL() {
        this.orderViewDA = new OrderViewDA();
    }

    public OrderView findOrderViewByName(String name) {
        OrderView st = orderViewDA.findByName(name);
        if (st == null) {
            System.out.println("name not found");
            throw new NoSuchElementException("The student with name =" + name + " was not found!");
        }
        return st;
    }

    public PdfPTable selectOrderView() {
        return orderViewDA.select("OrderView");
    }

    public void insertOrderView(OrderView s) {
        orderViewDA.insert(s);
    }

    public void deleteOrderView(String name) {
        orderViewDA.deleteByName(name);
    }

    public void updateOrderView(OrderView s, String condition) {
        orderViewDA.updateByName(s, condition);
    }
}
