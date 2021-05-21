package ba;

import java.util.*;

public class Book {
    // NOTE we are not dealing with cross orders here when bid and ask prices cross;
    // we do not remove price levels when they are empty; the tree will go through re-balancing in the beginning,
    // but when all levels are created, there won't be performance hit due to re-balancing
    TreeMap<Double, LinkedList<Order>> bidLevels = new TreeMap<>();
    TreeMap<Double, LinkedList<Order>> askLevels = new TreeMap<>();
    // store orders in the map as well to have a quick access to be able to reduce the size
    HashMap<String, Order> orderMap = new HashMap<>();

    public void processLimitOrder(Order order) {
        LinkedList<Order> level;
        if (OrderType.ADD == order.type) {
            // (A)dd order;
            // pick a side (assuming only two sides possible)
            // find level; if does not exist - create it
            TreeMap<Double, LinkedList<Order>> levels = OrderSide.BUY == order.side ? bidLevels : askLevels;
            if (!levels.containsKey(order.price)) {
                // add new price level
                level = new LinkedList<>();
                levels.put(order.price, level);
            } else {
                level = levels.get(order.price);
            }
            // add order to price level and to the map
            level.add(order);
            orderMap.put(order.id, order);
        } else {
            // (R)educe order - find this order and reduce its size or cancel
            Order origOrder = orderMap.get(order.id);
            if (order.size < origOrder.size ) {
                // after reduction the order size is greater than 0
                origOrder.size -= order.size;
            } else {
                // after reduction the order size is less than or equal to 0,
                // order canceled - remove it;
                // get side of the book where order is located
                TreeMap<Double, LinkedList<Order>> levels = OrderSide.BUY == origOrder.side ? bidLevels : askLevels;
                // get the price level where this order is located
                level = levels.get(origOrder.price);
                // iterate over the price level, find order and remove it
                // NOTE this is the only place where we do scan - if performance is a concern, this needs to be optimized:
                // we have to use an explicit linked list - maintain Orders with links to neighbours; then from the order map
                // we jump directly to the oder in the list and rearrange the links to exclude this order; this eliminates
                // the level scan;
                // NOTE that we are maintaining orders in the proper sequence since this is a fundamental property of the book
                for(int i = 0; i < level.size(); i++) {
                    Order order_ = level.get(i);
                    if (order_.id.equals(origOrder.id)) {
                        // order found - remove it from the price level and from the map
                        level.remove(i);
                        orderMap.remove(order_.id);
                        return;
                    }
                }
            }
        }
    }

    public OrderResponse processMarketOrder(Order order) {
        OrderResponse resp = new OrderResponse();
        // pick a side
        TreeMap<Double, LinkedList<Order>> levels = OrderSide.BUY == order.side ? askLevels : bidLevels;
        // start filling the order from the most favourable levels
        NavigableSet<Double> prices = OrderSide.BUY == order.side ? levels.navigableKeySet() : levels.descendingKeySet();
        long filled = 0;
        double totalPrice = 0.0;
        for(Double levelPrice : prices) {
            long filledOnLevel = 0;
            LinkedList<Order> level = levels.get(levelPrice);
            for (Order order_ : level) {
                if (order.size <= filled + order_.size) {
                    // we are done
                    filledOnLevel = order.size - filled;
                    break;
                } else {
                    filledOnLevel += order_.size;
                }
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
            LinkedList<Order> level = askLevels.get(levelPrice);
            for (Order order : level) {
                System.out.print(order.id + ":" + order.size + " ");
            }
            System.out.println();
        }
        System.out.println("BID");
        for(Double levelPrice : bidLevels.navigableKeySet()) {
            System.out.print(levelPrice + "=");
            LinkedList<Order> level = bidLevels.get(levelPrice);
            for (Order order : level) {
                System.out.print(order.id + ":" + order.size + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
