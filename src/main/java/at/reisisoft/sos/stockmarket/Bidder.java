package at.reisisoft.sos.stockmarket;

import akka.actor.ActorRef;
import at.reisisoft.sos.AbstractUntypedActor;
import at.reisisoft.sos.directormessage.Acknowledge;
import at.reisisoft.sos.directormessage.GetID;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Florian on 19.12.2016.
 */
public abstract class Bidder extends AbstractUntypedActor {

    private static final BigDecimal MAX_MONEY = new BigDecimal(1000000);
    private BigDecimal money;
    private Map<BigDecimal, Integer> bought = new TreeMap<>();
    private int totalStock = 0;

    @Override
    public final synchronized void onReceive(Object o) throws Throwable {
        o = Objects.requireNonNull(o);
        if (o instanceof GetID)
            initialize(getSender(), (GetID) o);
        else if (o instanceof StockValueBroadcast)
            doBid(getSender(), (StockValueBroadcast) o);
        else if (o instanceof BuyResponse)
            buyResponse((BuyResponse) o);
        else if (o instanceof SellResponse)
            sellResponseInternal((SellResponse) o);
        else unhandled(o);
    }

    private void initialize(ActorRef to, GetID getID) {
        double rand = Math.random();
        rand = rand * rand * rand; // rand^3
        money = MAX_MONEY.multiply(new BigDecimal(rand));

        tell(to, Acknowledge.getInstance());
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
        money = money.add(value);
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
