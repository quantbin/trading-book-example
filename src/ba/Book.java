package ba;

import java.util.*;

public class Book {
    // do not remove price levels when they are empty; the tree will go through re-balancing in the beginning
    // until all price levels are created
    //TreeMap<Double, Orders> bidLevels = new TreeMap<>();
    //TreeMap<Double, Orders> askLevels = new TreeMap<>();
    Levels bidLevels = new Levels();
    Levels askLevels = new Levels();

    public void processLimitOrder(Order order) {
        // book side
        Levels levels;
        // price level
        Orders level;
        if (OrderType.ADD == order.type) {
            levels = OrderSide.BUY == order.side ? bidLevels : askLevels;
            // (A)dd order;
            if (!levels.containsKey(order.priceInt)) {
                // add new price level
                level = new Orders();
                levels.put(order.priceInt, level);
            } else {
                level = levels.get(order.priceInt);
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
                level = levels.get(origOrder.priceInt);
                level.removeOrder(order.id);
            }
        }
    }

    public OrderResponse processMarketOrder(Order order) {
        OrderResponse resp = new OrderResponse();
        // pick a side
        Levels levels = OrderSide.BUY == order.side ? askLevels : bidLevels;
        if (levels.isEmpty())
            return resp;
        // start filling the order from the most favourable levels
        //NavigableSet<Double> prices = OrderSide.BUY == order.side ? levels.navigableKeySet() : levels.descendingKeySet();
        int startPrice = OrderSide.BUY == order.side ? askLevels.lowPrice : bidLevels.topPrice;
        int endPrice = OrderSide.BUY == order.side ? askLevels.topPrice : bidLevels.lowPrice;

        int step = OrderSide.BUY == order.side ? 1 : -1;
        long filled = 0;
        double totalPrice = 0.0;
        // eat levels one by one until order is filled or no more levels left
        int price = startPrice;
        while(true) {
            Orders level = levels.get(price);
            if (null != level) {
                long filledOnLevel = 0;
                Order order_ = level.first;
                while (null != order_) {
                    if (order.size <= filled + order_.size) {
                        // adding this limit order exceeds market order size; we are done
                        filledOnLevel = order.size - filled;
                        break;
                    } else {
                        filledOnLevel += order_.size;
                    }
                    order_ = order_.next;
                }
                totalPrice += filledOnLevel * price / 100.0;
                filled += filledOnLevel;
                if (filled == order.size) {
                    // order is filled
                    resp.isFilled = true;
                    break;
                }
            }
            if (endPrice == price) {
                break;
            }
            price += step;
        }
        resp.side = order.side;
        resp.total = totalPrice;

        return resp;
    }

    /*@SuppressWarnings("unused")
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
    }*/
}
