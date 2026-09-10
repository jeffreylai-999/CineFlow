package control;

import database.StaffDA;
import domain.Staff;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainStaff {

    private final StaffDA da;

    public MaintainStaff() {
        da = new StaffDA();
    }

    public Staff selectRecord(String id) {
        return da.getRecord(id);
    }

    public void addRecord(Staff staff) {
        da.addRecord(staff);
    }

    public void updateRecord(Staff staff) {
        da.updateRecord(staff);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Staff> getAll() {
        return da.getAllStaff();
    }

    public Staff lastStaffID() {
        return da.lastID();
    }
}
