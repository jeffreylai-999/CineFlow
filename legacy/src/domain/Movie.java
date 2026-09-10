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
@Table(name = "MOVIE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Movie.findAll", query = "SELECT m FROM Movie m"),
    @NamedQuery(name = "Movie.findByMovieId", query = "SELECT m FROM Movie m WHERE m.movieId = :movieId"),
    @NamedQuery(name = "Movie.findByMovieName", query = "SELECT m FROM Movie m WHERE m.movieName = :movieName"),
    @NamedQuery(name = "Movie.findByMoviePrice", query = "SELECT m FROM Movie m WHERE m.moviePrice = :moviePrice"),
    @NamedQuery(name = "Movie.findByRunningTime", query = "SELECT m FROM Movie m WHERE m.runningTime = :runningTime"),
    @NamedQuery(name = "Movie.findByMovieGenre", query = "SELECT m FROM Movie m WHERE m.movieGenre = :movieGenre"),
    @NamedQuery(name = "Movie.findByReleaseDate", query = "SELECT m FROM Movie m WHERE m.releaseDate = :releaseDate"),
    @NamedQuery(name = "Movie.findByOfflineDate", query = "SELECT m FROM Movie m WHERE m.offlineDate = :offlineDate")})
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "MOVIE_ID")
    private String movieId;
    @Basic(optional = false)
    @Column(name = "MOVIE_NAME")
    private String movieName;
    @Basic(optional = false)
    @Column(name = "MOVIE_PRICE")
    private String moviePrice;
    @Basic(optional = false)
    @Column(name = "RUNNING_TIME")
    private String runningTime;
    @Basic(optional = false)
    @Column(name = "MOVIE_GENRE")
    private String movieGenre;
    @Basic(optional = false)
    @Column(name = "RELEASE_DATE")
    private String releaseDate;
    @Basic(optional = false)
    @Column(name = "OFFLINE_DATE")
    private String offlineDate;
    @JoinColumn(name = "HALL_ID", referencedColumnName = "HALL_ID")
    @ManyToOne(optional = false)
    private Hall hallId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "movieId")
    private Collection<Promotion> promotionCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "movieId")
    private Collection<Showtime> showtimeCollection;

    public Movie() {
    }

    public Movie(String movieId) {
        this.movieId = movieId;
    }

    public Movie(String movieId, String movieName, String moviePrice, String runningTime, String movieGenre, String releaseDate, String offlineDate, Hall hall) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.moviePrice = moviePrice;
        this.runningTime = runningTime;
        this.movieGenre = movieGenre;
        this.releaseDate = releaseDate;
        this.offlineDate = offlineDate;
        this.hallId = hall;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMoviePrice() {
        return moviePrice;
    }

    public void setMoviePrice(String moviePrice) {
        this.moviePrice = moviePrice;
    }

    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }

    public String getMovieGenre() {
        return movieGenre;
    }

    public void setMovieGenre(String movieGenre) {
        this.movieGenre = movieGenre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getOfflineDate() {
        return offlineDate;
    }

    public void setOfflineDate(String offlineDate) {
        this.offlineDate = offlineDate;
    }

    public Hall getHallId() {
        return hallId;
    }

    public void setHallId(Hall hallId) {
        this.hallId = hallId;
    }

    @XmlTransient
    public Collection<Promotion> getPromotionCollection() {
        return promotionCollection;
    }

    public void setPromotionCollection(Collection<Promotion> promotionCollection) {
        this.promotionCollection = promotionCollection;
    }

    @XmlTransient
    public Collection<Showtime> getShowtimeCollection() {
        return showtimeCollection;
    }

    public void setShowtimeCollection(Collection<Showtime> showtimeCollection) {
        this.showtimeCollection = showtimeCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (movieId != null ? movieId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Movie)) {
            return false;
        }
        Movie other = (Movie) object;
        if ((this.movieId == null && other.movieId != null) || (this.movieId != null && !this.movieId.equals(other.movieId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Movie[ movieId=" + movieId + " ]";
    }
    
}
