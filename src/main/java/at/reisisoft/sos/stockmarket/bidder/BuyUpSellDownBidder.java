package at.reisisoft.sos.stockmarket.bidder;

import at.reisisoft.sos.stockmarket.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Florian on 19.12.2016.
 */
public class BuyUpSellDownBidder extends SellLowestPriceBidder {
    private static final int GRANULARITY = 10;
    private int cnt = (int) (GRANULARITY * Math.random());

    @Override
    protected StockMarketRequest doBuyOrSell(Map<BigDecimal, Integer> myStock, List<Map.Entry<Integer, BigDecimal>> stockPrice, BigDecimal currentStockPrice) {
        cnt = (cnt + 1) % GRANULARITY;
        if (cnt < 0)
            cnt += GRANULARITY;
        if (cnt == 0) return tryBid(stockPrice, currentStockPrice);
        else return NoBid.getInstance();
    }

    private StockMarketRequest tryBid(List<Map.Entry<Integer, BigDecimal>> stockPrice, BigDecimal currentStockPrice) {
        int canBuy;
        if (isStockRising(stockPrice)) {
            if ((canBuy = canBuy(currentStockPrice)) > 0) {
                canBuy = Math.max(1, (int) (canBuy * Math.random()));

                return new BuyRequest(canBuy);
            } else return NoBid.getInstance();
        } else {
            long canSell = getTotalStock();
            if (canSell > 0) {
                canSell = (int) (canSell * Math.random());
                if (canSell < 1)
                    canSell = 1;
                return new SellRequest((int)Math.min(Integer.MAX_VALUE,canSell));
            }
        }
        return NoBid.getInstance();
    }

    private boolean isStockRising(List<Map.Entry<Integer, BigDecimal>> stockPrice) {
        if (stockPrice.size() < 2)
            return false;
        BigDecimal last = stockPrice.get(stockPrice.size() - 2).getValue(),
                current = stockPrice.get(stockPrice.size() - 1).getValue();
        return last.compareTo(current) < 0;
    }
}
