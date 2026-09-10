package ui;

import java.util.regex.Pattern;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Jeffrey
 */
public class Validation {

    //Date format
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");

    public boolean isValidName(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (Character.isWhitespace(name.charAt(i))) {

            }
            else if (!Character.isLetter(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidPassword(String pass) {
        int letter = 0;
        int digit = 0;

        if (pass.length() > 7) {
            for (int i = 0; i < pass.length(); i++) {
                if (!Character.isLetterOrDigit(pass.charAt(i))) {
                    return false;
                }
                else if (Character.isDigit(pass.charAt(i))) {
                    digit++;
                }
                else if (Character.isLetter(pass.charAt(i))) {
                    letter++;
                }
            }
            return (digit > 0 && letter > 0);
        }
        else {
            return false;
        }
    }

    public boolean isValidDate(String date) {
        try {
            LocalDate parseLocalDate = dtf.parseLocalDate(date);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isValidIC(int year, int month, int day, String ic) {
        year += 1969;

        String str = String.format("%02d", year % 100)
                + String.format("%02d", month) + String.format("%02d", day);

        if (ic.isEmpty() || ic.length() < 6) {
            return false;
        }

        else if (str.equals(ic.substring(0, 6))) {
            boolean isTrue = Pattern.matches("\\d{6}-\\d{2}-\\d{4}", ic);
            return isTrue;
        }
        return false;
    }

    public boolean isValidPhoneNumber(String phone) {
        boolean isTrue = Pattern.matches("\\d{3}-\\d{7}", phone);
        return isTrue;
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public boolean isReleaseDateValid(String relDate) {
        LocalDate releaseDate = dtf.parseLocalDate(relDate);
        LocalDate today = new LocalDate();
        return !releaseDate.isBefore(today.plusDays(8));
    }

    public boolean isOfflineDateValid(String relDate, String offDate) {
        LocalDate releaseDate = dtf.parseLocalDate(relDate);
        LocalDate offlineDate = dtf.parseLocalDate(offDate);
        LocalDate today = new LocalDate();

        return !(offlineDate.isBefore(today) || offlineDate.isBefore(releaseDate));
    }

}
