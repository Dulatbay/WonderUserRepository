package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import lombok.Data;

@Data
public class DailyStats {
    private int count = 0;
    private Double income = 0.0;
    private String localDate;

    public DailyStats(String date) {
        this.localDate = date;
    }

    public void addOrder(KaspiOrder order) {
        this.income += order.getTotalPrice();
        this.count++;
    }
}