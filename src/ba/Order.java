package ba;

public class Order {
    // avoiding getters and setters for clarity
    public long timestamp;
    public OrderType type;
    public String id;
    public OrderSide side;
    public double price;
    public long size;
    public Order next;
    public Order prev;

    @Override
    public String toString() {
        return "Order{" +
                "timestamp=" + timestamp +
                ", type=" + type +
                ", id='" + id + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", size=" + size +
                '}';
    }

    public static Order parse(String orderStr) throws Exception {
        // this produces lot of litter;
        // pooling and reusing objects may need to be considered, but overkill for this exercise
        String[] tok = orderStr.split(" ");
        if (tok.length < 2)
            throw new Exception("order: invalid num of fields");
        Order order = new Order();
        order.timestamp = Long.parseLong(tok[0]);
        switch (tok[1]) {
            case "A":
                order.type = OrderType.ADD;
                if (6 != tok.length)
                    throw new Exception("order: invalid num of fields");
                order.id = tok[2];
                switch (tok[3]) {
                    case "B":
                        order.side = OrderSide.BUY;
                        break;
                    case "S":
                        order.side = OrderSide.SELL;
                        break;
                    default:
                        throw new Exception("order: unrecognized side");
                }
                order.price = Double.parseDouble(tok[4]);
                order.size = Long.parseLong(tok[5]);
                break;
            case "R":
                if (4 != tok.length)
                    throw new Exception("order: invalid num of fields");
                order.type = OrderType.REDUCE;
                order.id = tok[2];
                order.size = Long.parseLong(tok[3]);
                break;
            default:
                throw new Exception("order: unrecognized type");
        }
        return order;
    }
}
