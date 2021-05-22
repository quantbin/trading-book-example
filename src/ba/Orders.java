package ba;

import java.util.HashMap;

// double linked list of orders on a price level
public class Orders {
    // store orders in the map as well to have a quick access to be able to reduce the size
    static HashMap<String, Order> orderMap = new HashMap<>();
    Order first;
    Order last;

    void appendOrder(Order order) {
        if (null == first) {
            first = order;
            last = order;
        } else {
            order.prev = last;
            last.next = order;
            last = order;
        }
        orderMap.put(order.id, order);
    }

    static Order getOrder(String id) {
        return orderMap.get(id);
    }

    void removeOrder(String id) {
        Order order = orderMap.get(id);
        orderMap.remove(id);
        if (null == order.next && null == order.prev) {
            // this is the only order in the list
            first = null;
            last = null;
        } else if (null == order.next) {
            // this is last order
            last = order.prev;
            last.next = null;
        } else if (null == order.prev) {
            // this is the first order
            first = order.next;
            first.prev = null;
        } else {
            // this order is in the middle
            Order before = order.prev;
            Order after = order.next;
            before.next = after;
            after.prev = before;
        }
    }
}
