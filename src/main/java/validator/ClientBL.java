package validator;

import com.itextpdf.text.pdf.PdfPTable;
import dataAccess.ClientDA;
import model.Client;

import java.util.NoSuchElementException;

public class ClientBL {
    private ClientDA clientDA;

    public ClientBL() {
        this.clientDA = new ClientDA();
    }

    public Client findClientByName(String name) {
        Client st = clientDA.findByName(name);
        if (st == null) {
            System.out.println("sid not found");
            throw new NoSuchElementException("The student with name =" + name + " was not found!");
        }
        return st;
    }

    public PdfPTable selectClient() {
        return clientDA.select("client");
    }

    public void insertClient(Client s) {
        clientDA.insert(s);
    }

    public void deleteClient(String name) {
        clientDA.deleteByName(name);
    }

    public void updateClient(Client s, String condition) {
        clientDA.updateByName(s, condition);
    }
}
