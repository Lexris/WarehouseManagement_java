package start;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import model.Client;
import model.OrderView;
import model.Product;
import validator.ClientBL;
import validator.OrderViewBL;
import validator.ProductBL;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Start {
    protected static final Logger LOGGER = Logger.getLogger(Start.class.getName());


    /**
     * set System.in to file
     * generate pdf objects for reports
     * loop a scanner in order to get every command from input file
     * log exceptions into logger
     *
     * @param args commands from the shell
     */
    public static void main(String[] args) {
        try {
            System.setIn(new FileInputStream(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClientBL clientBL = new ClientBL();
        Client selectedClient = null;
        ProductBL productBL = new ProductBL();
        Product selectedProduct = null;
        OrderViewBL orderViewBL = new OrderViewBL();
        OrderView selectedOrderView = null;
        int pdfNumber = 1;
        int orderNumber = 1;

        Scanner stdin = new Scanner(System.in);
        String buffer = "";
        while ((buffer = stdin.nextLine()) != null) {
            try {
                if (buffer.contains("Insert client")) {
                    buffer = buffer.replaceAll("[A-Za-z ]*: ", "");
                    System.out.println(buffer);
                    String[] bufferSplit = buffer.split(", ");
                    if (bufferSplit.length != 2) {
                        continue;
                    }
                    clientBL.insertClient(new Client(bufferSplit[0], bufferSplit[1]));
                } else if (buffer.contains("Insert product")) {
                    buffer = buffer.replaceAll("[A-Za-z ]*: ", "");
                    System.out.println(buffer);
                    String[] bufferSplit = buffer.split(", ");
                    if (bufferSplit.length != 3) {
                        continue;
                    }
                    productBL.insertProduct(new Product(bufferSplit[0], Integer.parseInt(bufferSplit[1]), Double.parseDouble(bufferSplit[2])));
                } else if (buffer.contains("Delete client")) {
                    buffer = buffer.replaceAll("[A-Za-z ]*: ", "");
                    System.out.println(buffer);
                    clientBL.deleteClient(buffer);
                } else if (buffer.contains("Delete product")) {
                    buffer = buffer.replaceAll("[A-Za-z ]*: ", "");
                    System.out.println(buffer);
                    productBL.deleteProduct(buffer);
                } else if (buffer.contains("Order: ")) {
                    buffer = buffer.replaceAll("[A-Za-z ]*: ", "");
                    System.out.println(buffer);
                    String[] bufferSplit = buffer.split(", ");

                    if (bufferSplit.length != 3) {
                        continue;
                    }

                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream("order" + orderNumber + ".pdf"));
                    PdfPTable pdfTable = new PdfPTable(3);
                    pdfTable.addCell("name");
                    pdfTable.addCell("product");
                    pdfTable.addCell("quantity");
                    pdfTable.addCell(bufferSplit[0]);
                    pdfTable.addCell(bufferSplit[1]);
                    pdfTable.addCell(bufferSplit[2]);
                    orderNumber++;
                    document.open();
                    document.add(pdfTable);
                    document.close();

                    orderViewBL.insertOrderView(new OrderView(bufferSplit[0], bufferSplit[1], Integer.parseInt(bufferSplit[2])));
                } else if (buffer.contains("Report ")) {
                    buffer = buffer.replace("Report ", "");
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream("report" + pdfNumber + ".pdf"));
                    pdfNumber++;
                    document.open();

                    switch (buffer) {
                        case "client":
                            document.add(clientBL.selectClient());
                            break;
                        case "product":
                            document.add(productBL.selectProduct());
                            break;
                        case "order":
                            document.add(orderViewBL.selectOrderView());
                            break;
                    }
                    document.close();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, ex.getMessage());
            }
        }
    }
}
