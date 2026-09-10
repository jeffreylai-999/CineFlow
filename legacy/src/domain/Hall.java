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
@Table(name = "HALL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Hall.findAll", query = "SELECT h FROM Hall h"),
    @NamedQuery(name = "Hall.findByHallId", query = "SELECT h FROM Hall h WHERE h.hallId = :hallId"),
    @NamedQuery(name = "Hall.findByHallLocation", query = "SELECT h FROM Hall h WHERE h.hallLocation = :hallLocation"),
    @NamedQuery(name = "Hall.findByHallStatus", query = "SELECT h FROM Hall h WHERE h.hallStatus = :hallStatus"),
    @NamedQuery(name = "Hall.findByHallPeriod", query = "SELECT h FROM Hall h WHERE h.hallPeriod = :hallPeriod")})
public class Hall implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "HALL_ID")
    private String hallId;
    @Basic(optional = false)
    @Column(name = "HALL_LOCATION")
    private String hallLocation;
    @Column(name = "HALL_STATUS")
    private String hallStatus;
    @Column(name = "HALL_PERIOD")
    private String hallPeriod;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hallId")
    private Collection<Movie> movieCollection;

    public Hall() {
    }

    public Hall(String hallId) {
        this.hallId = hallId;
    }

    public Hall(String hallId, String hallLocation, String hallStatus, String hallPeriod) {
        this.hallId = hallId;
        this.hallLocation = hallLocation;
        this.hallStatus = hallStatus;
        this.hallPeriod = hallPeriod;
    }

    public String getHallId() {
        return hallId;
    }

    public void setHallId(String hallId) {
        this.hallId = hallId;
    }

    public String getHallLocation() {
        return hallLocation;
    }

    public void setHallLocation(String hallLocation) {
        this.hallLocation = hallLocation;
    }

    public String getHallStatus() {
        return hallStatus;
    }

    public void setHallStatus(String hallStatus) {
        this.hallStatus = hallStatus;
    }

    public String getHallPeriod() {
        return hallPeriod;
    }

    public void setHallPeriod(String hallPeriod) {
        this.hallPeriod = hallPeriod;
    }

    @XmlTransient
    public Collection<Movie> getMovieCollection() {
        return movieCollection;
    }

    public void setMovieCollection(Collection<Movie> movieCollection) {
        this.movieCollection = movieCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (hallId != null ? hallId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Hall)) {
            return false;
        }
        Hall other = (Hall) object;
        if ((this.hallId == null && other.hallId != null) || (this.hallId != null && !this.hallId.equals(other.hallId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Hall[ hallId=" + hallId + " ]";
    }
    
}
