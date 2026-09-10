/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "CREDITCARD")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Creditcard.findAll", query = "SELECT c FROM Creditcard c"),
    @NamedQuery(name = "Creditcard.findByCardId", query = "SELECT c FROM Creditcard c WHERE c.cardId = :cardId"),
    @NamedQuery(name = "Creditcard.findByCardNo", query = "SELECT c FROM Creditcard c WHERE c.cardNo = :cardNo"),
    @NamedQuery(name = "Creditcard.findByCardHoldName", query = "SELECT c FROM Creditcard c WHERE c.cardHoldName = :cardHoldName"),
    @NamedQuery(name = "Creditcard.findByCardType", query = "SELECT c FROM Creditcard c WHERE c.cardType = :cardType"),
    @NamedQuery(name = "Creditcard.findByCardBank", query = "SELECT c FROM Creditcard c WHERE c.cardBank = :cardBank"),
    @NamedQuery(name = "Creditcard.findByCardExp", query = "SELECT c FROM Creditcard c WHERE c.cardExp = :cardExp")})
public class Creditcard implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CARD_ID")
    private String cardId;
    @Basic(optional = false)
    @Column(name = "CARD_NO")
    private long cardNo;
    @Basic(optional = false)
    @Column(name = "CARD_HOLD_NAME")
    private String cardHoldName;
    @Basic(optional = false)
    @Column(name = "CARD_TYPE")
    private String cardType;
    @Basic(optional = false)
    @Column(name = "CARD_BANK")
    private String cardBank;
    @Basic(optional = false)
    @Column(name = "CARD_EXP")
    private String cardExp;
    @OneToMany(mappedBy = "cardId")
    private Collection<Payment> paymentCollection;

    public Creditcard() {
    }

    public Creditcard(String cardId) {
        this.cardId = cardId;
    }

    public Creditcard(String cardId, long cardNo, String cardHoldName, String cardType, String cardBank, String cardExp) {
        this.cardId = cardId;
        this.cardNo = cardNo;
        this.cardHoldName = cardHoldName;
        this.cardType = cardType;
        this.cardBank = cardBank;
        this.cardExp = cardExp;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public long getCardNo() {
        return cardNo;
    }

    public void setCardNo(long cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardHoldName() {
        return cardHoldName;
    }

    public void setCardHoldName(String cardHoldName) {
        this.cardHoldName = cardHoldName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardBank() {
        return cardBank;
    }

    public void setCardBank(String cardBank) {
        this.cardBank = cardBank;
    }

    public String getCardExp() {
        return cardExp;
    }

    public void setCardExp(String cardExp) {
        this.cardExp = cardExp;
    }

    @XmlTransient
    public Collection<Payment> getPaymentCollection() {
        return paymentCollection;
    }

    public void setPaymentCollection(Collection<Payment> paymentCollection) {
        this.paymentCollection = paymentCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cardId != null ? cardId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Creditcard)) {
            return false;
        }
        Creditcard other = (Creditcard) object;
        if ((this.cardId == null && other.cardId != null) || (this.cardId != null && !this.cardId.equals(other.cardId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Creditcard[ cardId=" + cardId + " ]";
    }
    
}
