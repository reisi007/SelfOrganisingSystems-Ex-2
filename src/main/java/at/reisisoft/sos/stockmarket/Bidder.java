package at.reisisoft.sos.stockmarket;

import akka.actor.ActorRef;
import at.reisisoft.sos.AbstractUntypedActor;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Florian on 19.12.2016. FIXME People have too much money
 */
public abstract class Bidder extends AbstractUntypedActor {

    private static final BigDecimal MAX_MONEY = new BigDecimal(10000);
    private BigDecimal money;
    private Map<BigDecimal, Integer> bought = new TreeMap<>();
    private int totalStock = 0;
    private boolean isInitialized = false;

    @Override
    public final synchronized void onReceive(Object o) throws Throwable {
        o = Objects.requireNonNull(o);
        if (!isInitialized)
            initialize();
        if (o instanceof StockValueBroadcast)
            doBid(getSender(), (StockValueBroadcast) o);
        else if (o instanceof BuyResponse)
            buyResponse((BuyResponse) o);
        else if (o instanceof SellResponse)
            sellResponseInternal((SellResponse) o);
        else unhandled(o);
    }

    private void initialize() {
        double rand = Math.random();
        rand = rand * rand * rand; // rand^3
        money = MAX_MONEY.multiply(new BigDecimal(rand));
        isInitialized = true;
    }

    private void doBid(ActorRef to, StockValueBroadcast stockValueBroadcast) {
        StockMarketRequest request = doBuyOrSell(
                Collections.unmodifiableMap(bought),
                stockValueBroadcast.getStockPrice(),
                stockValueBroadcast.getCurrentStockPrice()
        );
        tell(to, request);
    }

    private void buyResponse(BuyResponse response) {
        BigDecimal value = response.getPricePerPiece().multiply(new BigDecimal(response.getAmount())).negate();
        money = money.subtract(value);
        bought.compute(response.getPricePerPiece(), (k, v) -> (v == null ? 0 : v) + response.getAmount());
        totalStock += response.getAmount();
    }

    private void sellResponseInternal(SellResponse response) {
        BigDecimal value = response.getPricePerPiece().multiply(new BigDecimal(response.getAmount()));
        money = money.add(value);
        final Map<BigDecimal, Integer> bigDecimalIntegerMap = sellResponse(Collections.unmodifiableMap(bought), response.getAmount());
        //Check amount
        totalStock -= response.getAmount();
        if (totalStock < 0)
            throw new IllegalArgumentException("Selling to few");
        for (Map.Entry<BigDecimal, Integer> sellItems : bigDecimalIntegerMap.entrySet()) {
            final BigDecimal key = sellItems.getKey();
            bought.computeIfPresent(key, (k, v) -> v - sellItems.getValue());
            int newVal = bought.get(key);
            if (newVal < 0)
                throw new IllegalArgumentException("Selling to much of a piece");
            else if (newVal == 0)
                bought.remove(key);
        }
    }

    protected abstract Map<BigDecimal, Integer> sellResponse(Map<BigDecimal, Integer> bought, int amount);

    protected abstract StockMarketRequest doBuyOrSell(
            Map<BigDecimal, Integer> myStock,
            List<Map.Entry<Integer, BigDecimal>> stockPrice,
            BigDecimal currentStockPrice
    );


    protected final int canBuy(BigDecimal stockPrice) {
        return money.divide(stockPrice, BigDecimal.ROUND_FLOOR).intValue();
    }

    protected final int getTotalStock() {
        return totalStock;
    }
}
