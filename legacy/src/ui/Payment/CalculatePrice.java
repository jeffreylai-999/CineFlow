/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Payment;

import domain.Showtime;

/**
 *
 * @author Jeffrey
 */
public class CalculatePrice {

    double adultPrice = 0, childPrice = 0, sum = 0;

    public double calculateSubPrice(Showtime s, int adult, int child, int discount) {
        adultPrice = Double.parseDouble(s.getMovieId().getMoviePrice());
        childPrice = adultPrice - 4;
        sum = (adultPrice * adult) + (childPrice * child);
        sum = sum - (sum * discount / 100);
        return sum;
    }

    public double calculateEntertainmentTax(double sum) {
        double tax = (double) Math.round(sum * 25) / 100;
        return tax;
    }

    public double calculateGST(double sum) {
        double gst = (double) Math.round(sum * 6) / 100;
        return gst;
    }

    public double calculateTotalPrice(double sum, double gst, double tax) {
        double doubleSum = (double) Math.round((sum + gst + tax) * 10) / 10;
        return doubleSum;
    }
}
