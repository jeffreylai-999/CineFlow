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
@Table(name = "PROMOTION")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Promotion.findAll", query = "SELECT p FROM Promotion p"),
    @NamedQuery(name = "Promotion.findByPromotionId", query = "SELECT p FROM Promotion p WHERE p.promotionId = :promotionId"),
    @NamedQuery(name = "Promotion.findByPromotionStart", query = "SELECT p FROM Promotion p WHERE p.promotionStart = :promotionStart"),
    @NamedQuery(name = "Promotion.findByPromotionEnd", query = "SELECT p FROM Promotion p WHERE p.promotionEnd = :promotionEnd"),
    @NamedQuery(name = "Promotion.findByPromotionDiscount", query = "SELECT p FROM Promotion p WHERE p.promotionDiscount = :promotionDiscount")})
public class Promotion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "PROMOTION_ID")
    private String promotionId;
    @Basic(optional = false)
    @Column(name = "PROMOTION_START")
    private String promotionStart;
    @Basic(optional = false)
    @Column(name = "PROMOTION_END")
    private String promotionEnd;
    @Basic(optional = false)
    @Column(name = "PROMOTION_DISCOUNT")
    private int promotionDiscount;
    @JoinColumn(name = "MOVIE_ID", referencedColumnName = "MOVIE_ID")
    @ManyToOne(optional = false)
    private Movie movieId;

    public Promotion() {
    }

    public Promotion(String promotionId) {
        this.promotionId = promotionId;
    }

    public Promotion(String promotionId, String promotionStart, String promotionEnd, int promotionDiscount, Movie movie) {
        this.promotionId = promotionId;
        this.promotionStart = promotionStart;
        this.promotionEnd = promotionEnd;
        this.promotionDiscount = promotionDiscount;
        this.movieId = movie;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionStart() {
        return promotionStart;
    }

    public void setPromotionStart(String promotionStart) {
        this.promotionStart = promotionStart;
    }

    public String getPromotionEnd() {
        return promotionEnd;
    }

    public void setPromotionEnd(String promotionEnd) {
        this.promotionEnd = promotionEnd;
    }

    public int getPromotionDiscount() {
        return promotionDiscount;
    }

    public void setPromotionDiscount(int promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
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
        hash += (promotionId != null ? promotionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Promotion)) {
            return false;
        }
        Promotion other = (Promotion) object;
        if ((this.promotionId == null && other.promotionId != null) || (this.promotionId != null && !this.promotionId.equals(other.promotionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Promotion[ promotionId=" + promotionId + " ]";
    }
    
}
