package at.reisisoft.sos.stockmarket.bidder;

import at.reisisoft.sos.stockmarket.Bidder;
import at.reisisoft.sos.stockmarket.BuyAndSellRequest;
import at.reisisoft.sos.stockmarket.NoBid;
import at.reisisoft.sos.stockmarket.StockMarketRequest;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;

/**
 * Created by Florian on 19.12.2016.
 */
public class BuyDownSellUpBidder extends Bidder {
    private static final int GRANULARITY = 10;
    private int cnt = (int) (GRANULARITY * Math.random());
    private final BigDecimal wantedWin = new BigDecimal(1 + Math.random() / 4);
    private final int LOOK_AT_LAST_X = 3 + (int) (10 * Math.random());
    private final int BUY_ONCE_MAX = 20 + (int) (1000 * Math.random());
    private BigDecimal currentStockPrice;

    @Override
    protected Map<BigDecimal, Integer> sellResponse(Map<BigDecimal, Integer> bought, int amount) {
        try {
            final SortedSet<Map.Entry<BigDecimal, Integer>> collectedSet = bought.entrySet().stream().filter(isWin()).collect(Collector.of(
                    () -> new TreeSet<>(Comparator.comparing(Map.Entry::getKey)),
                    TreeSet::add,
                    (a, b) -> {
                        a.addAll(b);
                        return a;
                    }
            ));

            int totalValue = 0;
            final Iterator<Map.Entry<BigDecimal, Integer>> iterator = collectedSet.iterator();
            Map.Entry<BigDecimal, Integer> entry;
            Map<BigDecimal, Integer> returnMap = new HashMap<>();
            do {
                entry = iterator.next();
                int needed = amount - totalValue;
                int available = entry.getValue();
                int takeX = Math.min(needed, available);
                totalValue += takeX;
                returnMap.put(entry.getKey(), takeX);
            } while (totalValue < amount);
            return returnMap;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    protected StockMarketRequest doBuyOrSell(Map<BigDecimal, Integer> myStock, List<Map.Entry<Integer, BigDecimal>> stockPrice, BigDecimal currentStockPrice) {
        cnt = (cnt + 1) % GRANULARITY;
        if (cnt < 0)
            cnt += GRANULARITY;
        if (cnt == 0) return tryBid(myStock, stockPrice, currentStockPrice);
        else return NoBid.getInstance();
    }

    private StockMarketRequest tryBid(Map<BigDecimal, Integer> myStock, List<Map.Entry<Integer, BigDecimal>> stockPrice, BigDecimal currentStockPrice) {
        this.currentStockPrice = currentStockPrice;
        int canBuy;
        if ((canBuy = canBuy(currentStockPrice)) > 0 && lastX_Down(stockPrice)) {
            canBuy = Math.min(canBuy, BUY_ONCE_MAX);
        }
        int canSell = getCurrentSellAmount(myStock);
        return new BuyAndSellRequest(canBuy, canSell);
    }


    private boolean lastX_Down(List<Map.Entry<Integer, BigDecimal>> stockPrice) {
        if (stockPrice.size() < LOOK_AT_LAST_X + 1) return false;
        for (int i = stockPrice.size() - (LOOK_AT_LAST_X + 1), y = stockPrice.size() - LOOK_AT_LAST_X;
             y < stockPrice.size();
             i++, y++) {
            if (stockPrice.get(i).getValue().compareTo(stockPrice.get(y).getValue()) < 0)
                return false;
        }
        return true;
    }

    private int getCurrentSellAmount(Map<BigDecimal, Integer> myStock) {
        return myStock.entrySet().stream()
                .filter(isWin())
                .map(Map.Entry::getKey)
                .map(myStock::get)
                .reduce(0, Integer::sum);
    }

    private Predicate<Map.Entry<BigDecimal, ?>> isWin() {
        return kvp -> {
            BigDecimal wantedWinValue = kvp.getKey().multiply(wantedWin);
            return wantedWinValue.compareTo(currentStockPrice) >= 0;
        };
    }

}
