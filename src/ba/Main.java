package ba;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // validate params
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
        while (null != orderStr) {
            try {
                Order order = Order.parse(orderStr);
                //System.out.println(order);
                book.processOrder(order);
                //book.print();
                OrderResponse respBuy = book.fillMarketOrder(mktBuyOrder);
                respBuy.timestamp = order.timestamp;
                if ((null != respBuyPrev && respBuyPrev.isFilled ^ respBuy.isFilled)  || (null == respBuyPrev || 0.0000000001 < Math.abs(respBuyPrev.total - respBuy.total)))
                    System.out.println(respBuy);
                OrderResponse respSell = book.fillMarketOrder(mktSellOrder);
                respSell.timestamp = order.timestamp;
                if ((null != respSellPrev && respSellPrev.isFilled ^ respSell.isFilled) || (null == respSellPrev || 0.0000000001 < Math.abs(respSellPrev.total - respSell.total)))
                    System.out.println(respSell);
                respBuyPrev = respBuy;
                respSellPrev = respSell;
            } catch (Exception e) {
                // handle error - log, recover etc
                System.out.println(e.getMessage());
            } finally {
                // read next order
                orderStr = ordersIn.readLine();
            }
        }
    }
}
