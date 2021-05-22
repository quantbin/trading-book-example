package ba;

import java.util.*;

public class Book {
    // NOTE we are not dealing with cross orders here when bid and ask prices cross;
    // we do not remove price levels when they are empty; the tree will go through re-balancing in the beginning,
    // but when all levels are created, there won't be performance hit due to re-balancing
    TreeMap<Double, Orders> bidLevels = new TreeMap<>();
    TreeMap<Double, Orders> askLevels = new TreeMap<>();

    public void processLimitOrder(Order order) {
        Orders level;
        // pick a side (assuming only two sides possible)
        // find level; if does not exist - create it
        TreeMap<Double, Orders> levels = OrderSide.BUY == order.side ? bidLevels : askLevels;
        if (OrderType.ADD == order.type) {
            // (A)dd order;
            if (!levels.containsKey(order.price)) {
                // add new price level
                level = new Orders();
                levels.put(order.price, level);
            } else {
                level = levels.get(order.price);
            }
            // add order to price level and to the map
            level.appendOrder(order);
        } else {
            // (R)educe order - find this order and reduce its size or cancel
            Order origOrder = Orders.getOrder(order.id);
            if (order.size < origOrder.size ) {
                // after reduction the order size is greater than 0
                origOrder.size -= order.size;
            } else {
                // after reduction the order size is less than or equal to 0,
                // order canceled - remove it;
                // get side of the book where order is located
                levels = OrderSide.BUY == origOrder.side ? bidLevels : askLevels;
                // get the price level where this order is located
                level = levels.get(origOrder.price);
                level.removeOrder(order.id);
            }
        }
    }

    public OrderResponse processMarketOrder(Order order) {
        OrderResponse resp = new OrderResponse();
        // pick a side
        TreeMap<Double, Orders> levels = OrderSide.BUY == order.side ? askLevels : bidLevels;
        // start filling the order from the most favourable levels
        NavigableSet<Double> prices = OrderSide.BUY == order.side ? levels.navigableKeySet() : levels.descendingKeySet();
        long filled = 0;
        double totalPrice = 0.0;
        for(Double levelPrice : prices) {
            long filledOnLevel = 0;
            Orders level = levels.get(levelPrice);
            Order order_ = level.first;
            while(null != order_) {
                if (order.size <= filled + order_.size) {
                    // we are done
                    filledOnLevel = order.size - filled;
                    break;
                } else {
                    filledOnLevel += order_.size;
                }
                order_ = order_.next;
            }
            totalPrice += filledOnLevel * levelPrice;
            filled += filledOnLevel;
            if (filled == order.size) {
                resp.isFilled = true;
                break;
            }
        }
        resp.side = order.side;
        resp.total = totalPrice;

        return resp;
    }

    @SuppressWarnings("unused")
    public void print() {
        System.out.println("---");
        System.out.println("ASK");
        for(Double levelPrice : askLevels.descendingKeySet()) {
            System.out.print(levelPrice + "=");
            Orders level = askLevels.get(levelPrice);
            Order order_ = level.first;
            while(null != order_) {
                System.out.print(order_.id + ":" + order_.size + " ");
                order_ = order_.next;
            }
            System.out.println();
        }
        System.out.println("BID");
        for(Double levelPrice : bidLevels.navigableKeySet()) {
            System.out.print(levelPrice + "=");
            Orders level = bidLevels.get(levelPrice);
            Order order_ = level.first;
            while(null != order_) {
                System.out.print(order_.id + ":" + order_.size + " ");
                order_ = order_.next;
            }
            System.out.println();
        }
        System.out.println();
    }
}
