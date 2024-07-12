package kz.wonder.wonderuserrepository.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PackagingCost {
    private double totalCost;
    private int packagingMinutes;
    private int packagingSeconds;
}
