package control;

import domain.Creditcard;
import database.CreditCardDA;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainCreditCard {

    private final CreditCardDA da;

    public MaintainCreditCard() {
        da = new CreditCardDA();
    }

    public Creditcard selectRecord(String id) {
        return da.getRecord(id);
    }

    public void addRecord(Creditcard cus) {
        da.addRecord(cus);
    }

    public void updateRecord(Creditcard cus) {
        da.updateRecord(cus);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Creditcard> getAll() {
        return da.getAllCreditCard();
    }

    public String lastID() {
        return da.lastID();
    }
}
