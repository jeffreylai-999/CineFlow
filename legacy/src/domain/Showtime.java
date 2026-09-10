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
@Table(name = "SHOWTIME")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Showtime.findAll", query = "SELECT s FROM Showtime s"),
    @NamedQuery(name = "Showtime.findByShowId", query = "SELECT s FROM Showtime s WHERE s.showId = :showId"),
    @NamedQuery(name = "Showtime.findByShowDate", query = "SELECT s FROM Showtime s WHERE s.showDate = :showDate"),
    @NamedQuery(name = "Showtime.findByShowTime", query = "SELECT s FROM Showtime s WHERE s.showTime = :showTime")})
public class Showtime implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "SHOW_ID")
    private String showId;
    @Basic(optional = false)
    @Column(name = "SHOW_DATE")
    private String showDate;
    @Basic(optional = false)
    @Column(name = "SHOW_TIME")
    private String showTime;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "showId")
    private Collection<Payment> paymentCollection;
    @JoinColumn(name = "MOVIE_ID", referencedColumnName = "MOVIE_ID")
    @ManyToOne(optional = false)
    private Movie movieId;

    public Showtime() {
    }

    public Showtime(String showId) {
        this.showId = showId;
    }

    public Showtime(String showId, String showDate, String showTime, Movie movie) {
        this.showId = showId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.movieId = movie;
    }

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    @XmlTransient
    public Collection<Payment> getPaymentCollection() {
        return paymentCollection;
    }

    public void setPaymentCollection(Collection<Payment> paymentCollection) {
        this.paymentCollection = paymentCollection;
    }

    public Movie getMovieId() {
        return movieId;
    }

    public void setMovieId(Movie movieId) {
        this.movieId = movieId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (showId != null ? showId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Showtime)) {
            return false;
        }
        Showtime other = (Showtime) object;
        if ((this.showId == null && other.showId != null) || (this.showId != null && !this.showId.equals(other.showId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Showtime[ showId=" + showId + " ]";
    }
    
}
