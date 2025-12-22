package kz.itzhiti.orderservice.model.enums;

public enum TransactionType {
    EARNED("Заработано из заказа"),
    SPENT("Потрачено на скидку"),
    REFUNDED("Возвращено"),
    BONUS("Бонус");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

