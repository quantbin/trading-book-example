package ba;

public class Levels {
    Orders[] levels = new Orders[1000000];

    int topPrice = -1;
    int lowPrice = -1;

    void put(int priceInt, Orders orders) {
        levels[priceInt] = orders;
        if (-1 == topPrice) {
            topPrice = priceInt;
            lowPrice = priceInt;
        } else {
            if (topPrice < priceInt) {
                topPrice = priceInt;
            } else if (priceInt < lowPrice) {
                lowPrice = priceInt;
            }
        }
    }

    boolean containsKey(int priceInt) {
        return null != levels[priceInt];
    }

    Orders get(int priceInt) {
        return levels[priceInt];
    }

    boolean isEmpty() {
        return  -1 == topPrice;
    }
}
