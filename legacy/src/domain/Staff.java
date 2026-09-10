/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
@Table(name = "STAFF")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Staff.findAll", query = "SELECT s FROM Staff s"),
    @NamedQuery(name = "Staff.findByStaffId", query = "SELECT s FROM Staff s WHERE s.staffId = :staffId"),
    @NamedQuery(name = "Staff.findByStaffName", query = "SELECT s FROM Staff s WHERE s.staffName = :staffName"),
    @NamedQuery(name = "Staff.findByStaffPosition", query = "SELECT s FROM Staff s WHERE s.staffPosition = :staffPosition"),
    @NamedQuery(name = "Staff.findByStaffPassword", query = "SELECT s FROM Staff s WHERE s.staffPassword = :staffPassword"),
    @NamedQuery(name = "Staff.findByStaffGender", query = "SELECT s FROM Staff s WHERE s.staffGender = :staffGender"),
    @NamedQuery(name = "Staff.findByStaffBirthday", query = "SELECT s FROM Staff s WHERE s.staffBirthday = :staffBirthday"),
    @NamedQuery(name = "Staff.findByStaffIc", query = "SELECT s FROM Staff s WHERE s.staffIc = :staffIc"),
    @NamedQuery(name = "Staff.findByStaffPhone", query = "SELECT s FROM Staff s WHERE s.staffPhone = :staffPhone"),
    @NamedQuery(name = "Staff.findByStaffAddress", query = "SELECT s FROM Staff s WHERE s.staffAddress = :staffAddress"),
    @NamedQuery(name = "Staff.findByStaffEmail", query = "SELECT s FROM Staff s WHERE s.staffEmail = :staffEmail"),
    @NamedQuery(name = "Staff.findByStaffStatus", query = "SELECT s FROM Staff s WHERE s.staffStatus = :staffStatus"),
    @NamedQuery(name = "Staff.findByStaffQuestion", query = "SELECT s FROM Staff s WHERE s.staffQuestion = :staffQuestion"),
    @NamedQuery(name = "Staff.findByStaffAnswer", query = "SELECT s FROM Staff s WHERE s.staffAnswer = :staffAnswer")})
public class Staff implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "STAFF_ID")
    private String staffId;
    @Basic(optional = false)
    @Column(name = "STAFF_NAME")
    private String staffName;
    @Basic(optional = false)
    @Column(name = "STAFF_POSITION")
    private String staffPosition;
    @Basic(optional = false)
    @Column(name = "STAFF_PASSWORD")
    private String staffPassword;
    @Basic(optional = false)
    @Column(name = "STAFF_GENDER")
    private String staffGender;
    @Basic(optional = false)
    @Column(name = "STAFF_BIRTHDAY")
    private String staffBirthday;
    @Basic(optional = false)
    @Column(name = "STAFF_IC")
    private String staffIc;
    @Basic(optional = false)
    @Column(name = "STAFF_PHONE")
    private String staffPhone;
    @Basic(optional = false)
    @Column(name = "STAFF_ADDRESS")
    private String staffAddress;
    @Basic(optional = false)
    @Column(name = "STAFF_EMAIL")
    private String staffEmail;
    @Basic(optional = false)
    @Column(name = "STAFF_STATUS")
    private String staffStatus;
    @Basic(optional = false)
    @Column(name = "STAFF_QUESTION")
    private int staffQuestion;
    @Basic(optional = false)
    @Column(name = "STAFF_ANSWER")
    private String staffAnswer;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "staffId")
    private Collection<Payment> paymentCollection;

    public Staff() {
    }

    public Staff(String staffId) {
        this.staffId = staffId;
    }

    public Staff(String staffId, String staffName, String staffPosition, String staffPassword, String staffGender, String staffBirthday, String staffIc, String staffPhone, String staffAddress, String staffEmail, String staffStatus, int staffQuestion, String staffAnswer) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.staffPosition = staffPosition;
        this.staffPassword = staffPassword;
        this.staffGender = staffGender;
        this.staffBirthday = staffBirthday;
        this.staffIc = staffIc;
        this.staffPhone = staffPhone;
        this.staffAddress = staffAddress;
        this.staffEmail = staffEmail;
        this.staffStatus = staffStatus;
        this.staffQuestion = staffQuestion;
        this.staffAnswer = staffAnswer;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffPosition() {
        return staffPosition;
    }

    public void setStaffPosition(String staffPosition) {
        this.staffPosition = staffPosition;
    }

    public String getStaffPassword() {
        return staffPassword;
    }

    public void setStaffPassword(String staffPassword) {
        this.staffPassword = staffPassword;
    }

    public String getStaffGender() {
        return staffGender;
    }

    public void setStaffGender(String staffGender) {
        this.staffGender = staffGender;
    }

    public String getStaffBirthday() {
        return staffBirthday;
    }

    public void setStaffBirthday(String staffBirthday) {
        this.staffBirthday = staffBirthday;
    }

    public String getStaffIc() {
        return staffIc;
    }

    public void setStaffIc(String staffIc) {
        this.staffIc = staffIc;
    }

    public String getStaffPhone() {
        return staffPhone;
    }

    public void setStaffPhone(String staffPhone) {
        this.staffPhone = staffPhone;
    }

    public String getStaffAddress() {
        return staffAddress;
    }

    public void setStaffAddress(String staffAddress) {
        this.staffAddress = staffAddress;
    }

    public String getStaffEmail() {
        return staffEmail;
    }

    public void setStaffEmail(String staffEmail) {
        this.staffEmail = staffEmail;
    }

    public String getStaffStatus() {
        return staffStatus;
    }

    public void setStaffStatus(String staffStatus) {
        this.staffStatus = staffStatus;
    }

    public int getStaffQuestion() {
        return staffQuestion;
    }

    public void setStaffQuestion(int staffQuestion) {
        this.staffQuestion = staffQuestion;
    }

    public String getStaffAnswer() {
        return staffAnswer;
    }

    public void setStaffAnswer(String staffAnswer) {
        this.staffAnswer = staffAnswer;
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
        hash += (staffId != null ? staffId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Staff)) {
            return false;
        }
        Staff other = (Staff) object;
        if ((this.staffId == null && other.staffId != null) || (this.staffId != null && !this.staffId.equals(other.staffId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Staff[ staffId=" + staffId + " ]";
    }
    
}
