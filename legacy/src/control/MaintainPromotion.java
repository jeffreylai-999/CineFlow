package control;

import database.PromotionDA;
import domain.Promotion;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainPromotion {

    private final PromotionDA da;

    public MaintainPromotion() {
        da = new PromotionDA();
    }

    public Promotion selectRecord(String id) {
        return da.getRecord(id);
    }

    public void addRecord(Promotion promotion) {
        da.addRecord(promotion);
    }

    public void updateRecord(Promotion promotion) {
        da.updateRecord(promotion);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Promotion> getAllPromotion() {
        return da.getAllPromotion();
    }

    public Promotion lastID() {
        return da.lastID();
    }
}
