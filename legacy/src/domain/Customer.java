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
@Table(name = "CUSTOMER")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Customer.findAll", query = "SELECT c FROM Customer c"),
    @NamedQuery(name = "Customer.findByCusId", query = "SELECT c FROM Customer c WHERE c.cusId = :cusId"),
    @NamedQuery(name = "Customer.findByCusName", query = "SELECT c FROM Customer c WHERE c.cusName = :cusName"),
    @NamedQuery(name = "Customer.findByCusGender", query = "SELECT c FROM Customer c WHERE c.cusGender = :cusGender"),
    @NamedQuery(name = "Customer.findByCusBirthday", query = "SELECT c FROM Customer c WHERE c.cusBirthday = :cusBirthday"),
    @NamedQuery(name = "Customer.findByCusIc", query = "SELECT c FROM Customer c WHERE c.cusIc = :cusIc"),
    @NamedQuery(name = "Customer.findByCusPhone", query = "SELECT c FROM Customer c WHERE c.cusPhone = :cusPhone"),
    @NamedQuery(name = "Customer.findByCusAddress", query = "SELECT c FROM Customer c WHERE c.cusAddress = :cusAddress"),
    @NamedQuery(name = "Customer.findByCusEmail", query = "SELECT c FROM Customer c WHERE c.cusEmail = :cusEmail")})
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CUS_ID")
    private String cusId;
    @Basic(optional = false)
    @Column(name = "CUS_NAME")
    private String cusName;
    @Basic(optional = false)
    @Column(name = "CUS_GENDER")
    private String cusGender;
    @Basic(optional = false)
    @Column(name = "CUS_BIRTHDAY")
    private String cusBirthday;
    @Basic(optional = false)
    @Column(name = "CUS_IC")
    private String cusIc;
    @Basic(optional = false)
    @Column(name = "CUS_PHONE")
    private String cusPhone;
    @Basic(optional = false)
    @Column(name = "CUS_ADDRESS")
    private String cusAddress;
    @Basic(optional = false)
    @Column(name = "CUS_EMAIL")
    private String cusEmail;
    @OneToMany(mappedBy = "cusId")
    private Collection<Payment> paymentCollection;

    public Customer() {
    }

    public Customer(String cusId) {
        this.cusId = cusId;
    }

    public Customer(String cusId, String cusName, String cusGender, String cusBirthday, String cusIc, String cusPhone, String cusAddress, String cusEmail) {
        this.cusId = cusId;
        this.cusName = cusName;
        this.cusGender = cusGender;
        this.cusBirthday = cusBirthday;
        this.cusIc = cusIc;
        this.cusPhone = cusPhone;
        this.cusAddress = cusAddress;
        this.cusEmail = cusEmail;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getCusGender() {
        return cusGender;
    }

    public void setCusGender(String cusGender) {
        this.cusGender = cusGender;
    }

    public String getCusBirthday() {
        return cusBirthday;
    }

    public void setCusBirthday(String cusBirthday) {
        this.cusBirthday = cusBirthday;
    }

    public String getCusIc() {
        return cusIc;
    }

    public void setCusIc(String cusIc) {
        this.cusIc = cusIc;
    }

    public String getCusPhone() {
        return cusPhone;
    }

    public void setCusPhone(String cusPhone) {
        this.cusPhone = cusPhone;
    }

    public String getCusAddress() {
        return cusAddress;
    }

    public void setCusAddress(String cusAddress) {
        this.cusAddress = cusAddress;
    }

    public String getCusEmail() {
        return cusEmail;
    }

    public void setCusEmail(String cusEmail) {
        this.cusEmail = cusEmail;
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
        hash += (cusId != null ? cusId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Customer)) {
            return false;
        }
        Customer other = (Customer) object;
        if ((this.cusId == null && other.cusId != null) || (this.cusId != null && !this.cusId.equals(other.cusId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Customer[ cusId=" + cusId + " ]";
    }
    
}
