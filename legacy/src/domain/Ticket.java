/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jeffrey
 */
@Entity
@Table(name = "TICKET")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Ticket.findAll", query = "SELECT t FROM Ticket t"),
    @NamedQuery(name = "Ticket.findByTicketId", query = "SELECT t FROM Ticket t WHERE t.ticketId = :ticketId"),
    @NamedQuery(name = "Ticket.findBySeatNo", query = "SELECT t FROM Ticket t WHERE t.seatNo = :seatNo"),
    @NamedQuery(name = "Ticket.findByTicketType", query = "SELECT t FROM Ticket t WHERE t.ticketType = :ticketType"),
    @NamedQuery(name = "Ticket.findByTicketPrice", query = "SELECT t FROM Ticket t WHERE t.ticketPrice = :ticketPrice"),
    @NamedQuery(name = "Ticket.findByGoodsServiceTax", query = "SELECT t FROM Ticket t WHERE t.goodsServiceTax = :goodsServiceTax"),
    @NamedQuery(name = "Ticket.findByEntertainmentTax", query = "SELECT t FROM Ticket t WHERE t.entertainmentTax = :entertainmentTax")})
public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "TICKET_ID")
    private String ticketId;
    @Basic(optional = false)
    @Column(name = "SEAT_NO")
    private String seatNo;
    @Basic(optional = false)
    @Column(name = "TICKET_TYPE")
    private String ticketType;
    @Basic(optional = false)
    @Column(name = "TICKET_PRICE")
    private String ticketPrice;
    @Basic(optional = false)
    @Column(name = "GOODS_SERVICE_TAX")
    private String goodsServiceTax;
    @Basic(optional = false)
    @Column(name = "ENTERTAINMENT_TAX")
    private String entertainmentTax;
    @JoinColumn(name = "PAYMENT_ID", referencedColumnName = "PAYMENT_ID")
    @ManyToOne(optional = false)
    private Payment paymentId;

    public Ticket() {
    }

    public Ticket(String ticketId) {
        this.ticketId = ticketId;
    }

    public Ticket(String ticketId, String seatNo, String ticketType, String ticketPrice, String goodsServiceTax, String entertainmentTax, Payment payment) {
        this.ticketId = ticketId;
        this.seatNo = seatNo;
        this.ticketType = ticketType;
        this.ticketPrice = ticketPrice;
        this.goodsServiceTax = goodsServiceTax;
        this.entertainmentTax = entertainmentTax;
        this.paymentId = payment;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getGoodsServiceTax() {
        return goodsServiceTax;
    }

    public void setGoodsServiceTax(String goodsServiceTax) {
        this.goodsServiceTax = goodsServiceTax;
    }

    public String getEntertainmentTax() {
        return entertainmentTax;
    }

    public void setEntertainmentTax(String entertainmentTax) {
        this.entertainmentTax = entertainmentTax;
    }

    public Payment getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Payment paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ticketId != null ? ticketId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Ticket)) {
            return false;
        }
        Ticket other = (Ticket) object;
        if ((this.ticketId == null && other.ticketId != null) || (this.ticketId != null && !this.ticketId.equals(other.ticketId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Ticket[ ticketId=" + ticketId + " ]";
    }
    
}
