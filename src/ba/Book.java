package ba;

import java.util.*;

public class Book {
    class Entry {
        Entry(String orderId, long size) {
            this.orderId = orderId;
            this.size = size;
        }
        String orderId;
        long size;
    }

    String symbol = "DEMO";

    // NOTE we are not dealing with cross orders here when bid and ask prices cross
    TreeMap<Double, LinkedList<Entry>> bidLevels = new TreeMap<>();
    TreeMap<Double, LinkedList<Entry>> askLevels = new TreeMap<>();
    HashMap<String, Order> orderMap = new HashMap<>();

    public void processOrder(Order order) {
        // pick a side (assuming only two sides possible)
        // find level; if does not exist - create it
        LinkedList<Entry> level;
        Entry newEntry = new Entry(order.id, order.size);
        if (OrderType.ADD == order.type) {
            TreeMap<Double, LinkedList<Entry>> levels = OrderSide.BUY == order.side ? bidLevels : askLevels;
            if (!levels.containsKey(order.price)) {
                level = new LinkedList<>();
                levels.put(order.price, level);
            } else {
                level = levels.get(order.price);
            }
            level.add(newEntry);
            orderMap.put(order.id, order);
            // added order; we are done
        } else {
            // (R)educe order - find this order reduce the size
            Double price = orderMap.get(order.id).price;
            TreeMap<Double, LinkedList<Entry>> levels = OrderSide.BUY == orderMap.get(order.id).side ? bidLevels : askLevels;
            level = levels.get(price);
            if (level.isEmpty()) {
                // nothing to reduce on empty level; log warning
                return;
            }
            for(int idx = 0; idx < level.size(); idx++) {
                Entry entry = level.get(idx);
                if (entry.orderId.equals(newEntry.orderId)) {
                    // found order; correct the amount
                    if (newEntry.size < entry.size ) {
                        entry.size -= newEntry.size;
                    } else {
                        // order canceled - remove it
                        level.remove(idx);
                        orderMap.remove(order.id);
                    }
                    return;
                }
            }
            // if we are here then order was not found; nothing to reduce; log warning
        }
    }

    public OrderResponse fillMarketOrder(Order order) {
        OrderResponse resp = new OrderResponse();
        // pick a side (assuming only two sides possible)
        TreeMap<Double, LinkedList<Entry>> levels = OrderSide.BUY == order.side ? askLevels : bidLevels;
        NavigableSet<Double> prices = OrderSide.BUY == order.side ? levels.navigableKeySet() : levels.descendingKeySet();
        // start filling the order from the most favourable levels
        long filled = 0;
        double totalPrice = 0.0;
        for(Double levelPrice : prices) {
            long filledOnLevel = 0;
            LinkedList<Entry> level = levels.get(levelPrice);
            for (Entry entry : level) {
                if (order.size <= filled + entry.size) {
                    // we are done
                    filledOnLevel = order.size - filled;
                } else {
                    filledOnLevel += entry.size;
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

    public void print() {
        System.out.println("---");
        System.out.println("ASK");
        for(Double levelPrice : askLevels.descendingKeySet()) {
            System.out.print(levelPrice + "=");
            LinkedList<Entry> level = askLevels.get(levelPrice);
            for (Entry entry : level) {
                System.out.print(entry.orderId + ":" + entry.size + " ");
            }
            System.out.println();
        }
        System.out.println("BID");
        for(Double levelPrice : bidLevels.navigableKeySet()) {
            System.out.print(levelPrice + "=");
            LinkedList<Entry> level = bidLevels.get(levelPrice);
            for (Entry entry : level) {
                System.out.print(entry.orderId + ":" + entry.size + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
