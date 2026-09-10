/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import control.MaintainTicket;
import domain.Payment;
import domain.Showtime;
import domain.Ticket;
import ui.Payment.CalculatePrice;

/**
 *
 * @author Jeffrey
 */
public class AddTicket {

    public Ticket ticket = new Ticket();
    private final int discount, adult, child;
    private final String seatNo;
    private final CalculatePrice cp = new CalculatePrice();
    public final MaintainTicket progControl = new MaintainTicket();

    public AddTicket(String seatNo, int adult, int child, int discount, Showtime showtime, Payment payment) {
        this.adult = adult;
        this.child = child;
        this.discount = discount;
        this.seatNo = seatNo;

        String strServer = seatNo.substring(1, seatNo.length() - 1);
        String[] a = strServer.split(", ");
        StringBuilder seatCode = new StringBuilder();

        for (String a1 : a) {
            String strRow = String.valueOf(a1.charAt(0)) + a1.charAt(1);
            String strCol = String.valueOf(a1.charAt(2)) + a1.charAt(3);
            int row = Integer.parseInt(strRow);
            int col = Integer.parseInt(strCol);

            String seat = Character.toString((char) (row + 65)) + String.format("%02d", col + 1) + ", ";
            seatCode.append(seat);
        }
        String code = seatCode.toString().substring(0, seatCode.length() - 2);
        String[] seats = code.split(", ");

        int n = 0;
        double adultPrice = cp.calculateSubPrice(showtime, 1, 0, discount);
        double adultGST = cp.calculateGST(adultPrice);
        double adultEntertainment = cp.calculateEntertainmentTax(adultPrice);
        double childPrice = cp.calculateSubPrice(showtime, 0, 1, discount);
        double childGST = cp.calculateGST(childPrice);
        double childEntertainment = cp.calculateEntertainmentTax(childPrice);

        while (adult > 0) {
            ticket = new Ticket(ticketID(), seats[n], "Adult",
                    String.format("%.2f", adultPrice), String.valueOf(adultGST),
                    String.format("%.2f", adultEntertainment), payment);
            progControl.addRecord(ticket);
            n += 1;
            adult -= 1;
        }

        while (child > 0) {
            ticket = new Ticket(ticketID(), seats[n], "Child",
                    String.format("%.2f", childPrice), String.valueOf(childGST),
                    String.format("%.2f", childEntertainment), payment);
            progControl.addRecord(ticket);
            n += 1;
            child -= 1;
        }

    }

    private String ticketID() {
        ticket = progControl.lastID();

        try {
            String id = ticket.getTicketId().substring(1);
            int intID = Integer.parseInt(id) + 1;
            return "T" + String.format("%09d", intID);
        }
        catch (Exception e) {
            return "T000000001";
        }
    }
}
