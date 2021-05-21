package ba;

public class OrderResponse {
    long timestamp;
    OrderSide side;
    Double total;
    boolean isFilled;

    public String toString() {
        return String.format("%d %s %s", timestamp, OrderSide.BUY == side ? "B": "S", isFilled ? total.toString() : "NA");
    }
}
