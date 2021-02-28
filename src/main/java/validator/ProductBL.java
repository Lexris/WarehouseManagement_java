package validator;

import com.itextpdf.text.pdf.PdfPTable;
import dataAccess.ProductDA;
import model.Product;

import java.util.NoSuchElementException;

public class ProductBL {
    private ProductDA productDA;

    public ProductBL() {
        this.productDA = new ProductDA();
    }

    public Product findProductByName(String name) {
        Product st = productDA.findByName(name);
        if (st == null) {
            System.out.println("sid not found");
            throw new NoSuchElementException("The student with name =" + name + " was not found!");
        }
        return st;
    }

    public PdfPTable selectProduct() {
        return productDA.select("product");
    }

    public void insertProduct(Product s) {
        productDA.insert(s);
    }

    public void deleteProduct(String name) {
        productDA.deleteByName(name);
    }

    public void updateProduct(Product s, String condition) {
        productDA.updateByName(s, condition);
    }
}
