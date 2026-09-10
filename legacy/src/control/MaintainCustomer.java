package control;

import domain.Customer;
import database.CustomerDA;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainCustomer {

    private final CustomerDA da;

    public MaintainCustomer() {
        da = new CustomerDA();
    }

    public Customer selectRecord(String id) {
        return da.getRecord(id);
    }

    public void addRecord(Customer cus) {
        da.addRecord(cus);
    }

    public void updateRecord(Customer cus) {
        da.updateRecord(cus);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Customer> getAll() {
        return da.getAllMember();
    }

    public Customer lastCustomerID() {
        return da.lastID();
    }
}
