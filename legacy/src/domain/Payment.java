/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Jeffrey
 */
@Entity
@Table(name = "PAYMENT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Payment.findAll", query = "SELECT p FROM Payment p"),
    @NamedQuery(name = "Payment.findByPaymentId", query = "SELECT p FROM Payment p WHERE p.paymentId = :paymentId"),
    @NamedQuery(name = "Payment.findByCusType", query = "SELECT p FROM Payment p WHERE p.cusType = :cusType"),
    @NamedQuery(name = "Payment.findByAdult", query = "SELECT p FROM Payment p WHERE p.adult = :adult"),
    @NamedQuery(name = "Payment.findByChild", query = "SELECT p FROM Payment p WHERE p.child = :child"),
    @NamedQuery(name = "Payment.findBySeatNo", query = "SELECT p FROM Payment p WHERE p.seatNo = :seatNo"),
    @NamedQuery(name = "Payment.findByTotalPrice", query = "SELECT p FROM Payment p WHERE p.totalPrice = :totalPrice"),
    @NamedQuery(name = "Payment.findByReservationId", query = "SELECT p FROM Payment p WHERE p.reservationId = :reservationId"),
    @NamedQuery(name = "Payment.findByPaymentType", query = "SELECT p FROM Payment p WHERE p.paymentType = :paymentType"),
    @NamedQuery(name = "Payment.findByPaymentDate", query = "SELECT p FROM Payment p WHERE p.paymentDate = :paymentDate")})
public class Payment implements Serializable {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "paymentId")
    private Collection<Ticket> ticketCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "PAYMENT_ID")
    private String paymentId;
    @Basic(optional = false)
    @Column(name = "CUS_TYPE")
    private String cusType;
    @Basic(optional = false)
    @Column(name = "ADULT")
    private int adult;
    @Basic(optional = false)
    @Column(name = "CHILD")
    private int child;
    @Basic(optional = false)
    @Column(name = "SEAT_NO")
    private String seatNo;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @Column(name = "TOTAL_PRICE")
    private BigDecimal totalPrice;
    @Column(name = "RESERVATION_ID")
    private String reservationId;
    @Column(name = "PAYMENT_TYPE")
    private String paymentType;
    @Column(name = "PAYMENT_DATE")
    private String paymentDate;
    @JoinColumn(name = "CARD_ID", referencedColumnName = "CARD_ID")
    @ManyToOne
    private Creditcard cardId;
    @JoinColumn(name = "CUS_ID", referencedColumnName = "CUS_ID")
    @ManyToOne
    private Customer cusId;
    @JoinColumn(name = "SHOW_ID", referencedColumnName = "SHOW_ID")
    @ManyToOne(optional = false)
    private Showtime showId;
    @JoinColumn(name = "STAFF_ID", referencedColumnName = "STAFF_ID")
    @ManyToOne(optional = false)
    private Staff staffId;

    public Payment() {
    }

    public Payment(String paymentId) {
        this.paymentId = paymentId;
    }

    public Payment(String paymentId, Staff staff, Showtime showtime,
            String cusType, int adult, int child, String seatNo,
            BigDecimal totalPrice, String reservation, Customer customer,
            String paymentType, Creditcard creditcard, String paymentDate) {
        this.paymentId = paymentId;
        this.staffId = staff;
        this.showId = showtime;
        this.cusType = cusType;
        this.adult = adult;
        this.child = child;
        this.seatNo = seatNo;
        this.totalPrice = totalPrice;
        this.reservationId = reservation;
        this.cusId = customer;
        this.paymentType = paymentType;
        this.cardId = creditcard;
        this.paymentDate = paymentDate;
    }
    
    public Payment(String paymentId, Staff staff, Showtime showtime,
            String cusType, int adult, int child, String seatNo,
            BigDecimal totalPrice, String reservation) {
        this.paymentId = paymentId;
        this.staffId = staff;
        this.showId = showtime;
        this.cusType = cusType;
        this.adult = adult;
        this.child = child;
        this.seatNo = seatNo;
        this.totalPrice = totalPrice;
        this.reservationId = reservation;
    }
    
    public Payment(String paymentId, Customer customer, String paymentType, Creditcard creditcard, String payment) {
        this.paymentId = paymentId;
        this.cusId = customer;
        this.paymentType = paymentType;
        this.cardId = creditcard;
        this.paymentDate = payment;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getCusType() {
        return cusType;
    }

    public void setCusType(String cusType) {
        this.cusType = cusType;
    }

    public int getAdult() {
        return adult;
    }

    public void setAdult(int adult) {
        this.adult = adult;
    }

    public int getChild() {
        return child;
    }

    public void setChild(int child) {
        this.child = child;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Creditcard getCardId() {
        return cardId;
    }

    public void setCardId(Creditcard cardId) {
        this.cardId = cardId;
    }

    public Customer getCusId() {
        return cusId;
    }

    public void setCusId(Customer cusId) {
        this.cusId = cusId;
    }

    public Showtime getShowId() {
        return showId;
    }

    public void setShowId(Showtime showId) {
        this.showId = showId;
    }

    public Staff getStaffId() {
        return staffId;
    }

    public void setStaffId(Staff staffId) {
        this.staffId = staffId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (paymentId != null ? paymentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Payment)) {
            return false;
        }
        Payment other = (Payment) object;
        if ((this.paymentId == null && other.paymentId != null) || (this.paymentId != null && !this.paymentId.equals(other.paymentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Payment[ paymentId=" + paymentId + " ]";
    }

    @XmlTransient
    public Collection<Ticket> getTicketCollection() {
        return ticketCollection;
    }

    public void setTicketCollection(Collection<Ticket> ticketCollection) {
        this.ticketCollection = ticketCollection;
    }
    
}
