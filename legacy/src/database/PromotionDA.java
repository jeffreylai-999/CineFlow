/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import domain.Promotion;
import domain.Movie;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Jeffrey
 */
public class PromotionDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "promotion";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private MovieDA movieDA = new MovieDA();
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public PromotionDA() {
        createConnection();
    }

    public void addRecord(Promotion promotion) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, promotion.getPromotionId());
            stmt.setString(2, promotion.getPromotionStart());
            stmt.setString(3, promotion.getPromotionEnd());
            stmt.setInt(4, promotion.getPromotionDiscount());
            stmt.setString(5, promotion.getMovieId().getMovieId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Promotion getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE PROMOTION_ID = ?";
        Movie movie = null;
        Promotion promotion = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                movie = movieDA.getRecord(rs.getString("MOVIE_ID"));
                promotion = new Promotion(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), movie);
            }

        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return promotion;
    }

    public void updateRecord(Promotion promotion) {
        String insertStr = "UPDATE " + tableName + " SET PROMOTION_START = ?, PROMOTION_END = ?, PROMOTION_DISCOUNT = ?, MOVIE_ID = ? WHERE PROMOTION_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, promotion.getPromotionStart());
            stmt.setString(2, promotion.getPromotionEnd());
            stmt.setInt(3, promotion.getPromotionDiscount());
            stmt.setString(4, promotion.getMovieId().getMovieId());
            stmt.setString(5, promotion.getPromotionId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRecord(String id) {
        String deleteStr = "DELETE FROM " + tableName + " WHERE PROMOTION_ID = ?";
        try {
            stmt = conn.prepareStatement(deleteStr);
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Promotion lastID() //throws SQLException
    {
        Promotion promotion = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName;
            stmt = conn.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            promotion = new Promotion(rs.getString(1));
        }
        catch (SQLException ex) {
            return promotion;
        }
        return promotion;
    }

    public ArrayList<Promotion> getAllPromotion() {
        ArrayList<Promotion> promotion = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                promotion.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return promotion;
    }

    public Promotion getRecordID() {

        Movie movie = null;
        Promotion promotion = null;
        try {
            movie = movieDA.getRecord(rs.getString("MOVIE_ID"));
            promotion = new Promotion(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), movie);
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return promotion;
    }

    private void createConnection() {
        try {
            conn = DriverManager.getConnection(host, user, password);
            System.out.println("***TRACE: Connection established.");
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void shutDown() {
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
