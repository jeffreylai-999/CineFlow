package control;

import database.PaymentDA;
import domain.Payment;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainPayment {

    private final PaymentDA da;

    public MaintainPayment() {
        da = new PaymentDA();
    }

    public Payment selectRecord(String id) {
        return da.getRecord(id);
    }

    public void addRecord(Payment payment) {
        da.addPayment(payment);
    }

    public void addPaymentWithout(Payment payment) {
        da.addPaymentWithout(payment);
    }

    public void updateCustomer(Payment payment) {
        da.updateCustomer(payment);
    }

    public void updateCard(Payment payment) {
        da.updateCard(payment);
    }

    public void updatePaymentType(Payment payment) {
        da.updatePaymentType(payment);
    }

    public void updatePaymentDate(Payment payment) {
        da.updatePaymentDate(payment);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Payment> getReservationPayments() {
        return da.getReservationPayments();
    }

    public ArrayList<Payment> getAll() {
        return da.getAll();
    }

    public ArrayList<Payment> getSeats(String type, String showtime) {
        return da.getSeats(type, showtime);
    }

    public String paymentLastID() {
        return da.paymentLastID();
    }

    public String reservationLastID() {
        return da.reservationLastID();
    }
}
