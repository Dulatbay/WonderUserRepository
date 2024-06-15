package kz.wonder.wonderuserrepository.entities.enums;

import lombok.Getter;

@Getter
public enum KaspiProductUnitType {
    MEASURABLE_PIECES("Цельно-весовой"),
    MEASURABLE("Весовой"),
    PIECES("Штучный");

    private final String description;

    KaspiProductUnitType(String description) {
        this.description = description;
    }

    public static KaspiProductUnitType getByDescription(String description) {
        return switch (description.toLowerCase()) {
            case "цельно-весовой": case "measurable_pieces": yield MEASURABLE_PIECES;
            case "весовой": case "measurable": yield MEASURABLE;
            case "штучный": case "pieces": yield PIECES;
            default:
                throw new IllegalStateException("Unexpected value: " + description.toLowerCase());
        };
    }

}
