package ba;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // TODO validate params
        int orderSize = Integer.parseInt(args[0]);
        Book book = new Book();

        //BufferedReader ordersIn = new BufferedReader(new InputStreamReader(System.in));

        // TEST on win >>>
        File file=new File("D:\\1\\pulpsoft\\texas-test\\book_analyzer.in");
        FileReader fr=new FileReader(file);
        BufferedReader ordersIn =new BufferedReader(fr);
        // <<<

        Order mktBuyOrder = new Order();
        mktBuyOrder.side = OrderSide.BUY;
        mktBuyOrder.size = orderSize;

        Order mktSellOrder = new Order();
        mktSellOrder.side = OrderSide.SELL;
        mktSellOrder.size = orderSize;

        String orderStr = ordersIn.readLine();
        OrderResponse respBuyPrev = null;
        OrderResponse respSellPrev = null;
        long start = System.nanoTime();
        long count = 0;
        long timeParse = 0;
        while (null != orderStr) {
            try {
                long startParse = System.nanoTime();
                Order order = Order.parse(orderStr);
                long endParse = System.nanoTime();
                timeParse += endParse - startParse;

                //System.out.println(order);
                if (order.timestamp == 47382589)
                    System.out.println();
                book.processLimitOrder(order);
                //book.print();

                OrderResponse respBuy = book.processMarketOrder(mktBuyOrder);
                respBuy.timestamp = order.timestamp;
                //checkOrder(respBuy, respBuyPrev);

                OrderResponse respSell = book.processMarketOrder(mktSellOrder);
                respSell.timestamp = order.timestamp;
                //checkOrder(respSell, respSellPrev);

                respBuyPrev = respBuy;
                respSellPrev = respSell;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                // read next order
                orderStr = ordersIn.readLine();
                count++;
            }
        }
        long end = System.nanoTime();
        long lat = (end - start)/count;
        long latParse = timeParse/count;
        System.out.println("nanos per iteration: " + lat);
        System.out.println("nanos per parsing: " + latParse);
    }

    // check and print order if there is a change from previous order
    static void checkOrder(OrderResponse resp, OrderResponse respPrev) {
        if (null == respPrev) {
            // first order; was it filled?
            if (resp.isFilled) {
                System.out.println(resp);
            }
        } else {
            // this is not the first order; was there a change?
            if (respPrev.isFilled ^ resp.isFilled) {
                // only one order is filled: we went from NA to filled or other way around
                System.out.println(resp);
            } else if (respPrev.isFilled && resp.isFilled){
                // both order were filled; did the amount change?
                if (0.0000000001 < Math.abs(respPrev.total - resp.total)) {
                    System.out.println(resp);
                }
            }
        }
    }
}
