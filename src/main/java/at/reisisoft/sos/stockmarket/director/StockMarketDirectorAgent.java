package at.reisisoft.sos.stockmarket.director;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import at.reisisoft.sos.AbstractUntypedActor;
import at.reisisoft.sos.directormessage.DirectorInit;
import at.reisisoft.sos.stockmarket.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Florian on 19.12.2016.
 */
public class StockMarketDirectorAgent extends AbstractUntypedActor {

    private enum State {
        PRE_INIT, INIT, ANNOUNCE_STOCK_PRICE, WAIT_FOR_RESPONSE, FINISHED;

        private boolean isInitState() {
            return INIT.equals(this) || PRE_INIT.equals(this);
        }
    }

    private State state = State.PRE_INIT;
    private final List<ActorRef> actors = new ArrayList<>();
    private final List<Transacation> transactions = new ArrayList<>();
    private int gotStockMarketRequest, curTick, maxIterations = -1;
    private BigDecimal stockPrice;
    private int amountAvailable, initialAmountAvailable;
    private final List<Map.Entry<Integer, BigDecimal>> stockPriceHistory = new ArrayList<>();
    private PrintWriter outFile = null;

    @Override
    public synchronized void onReceive(Object o) throws Throwable {
        o = Objects.requireNonNull(o);
        if (state.isInitState() && (o instanceof DirectorInit)) {
            init((DirectorInit) o);
        } else if (o instanceof StockMarketRequest) {
            if (!State.WAIT_FOR_RESPONSE.equals(state))
                throw new IllegalStateException(String.format("We need to be in state '%s', but we were in state '%s'!", State.WAIT_FOR_RESPONSE, state));
            handleStockMarketRequests(getSender(), (StockMarketRequest) o);
        }

        if (State.ANNOUNCE_STOCK_PRICE.equals(state)) {
            if (curTick < maxIterations) {
                gotStockMarketRequest = 0;
                transactions.clear();
                final StockValueBroadcast stockValueBroadcast = new StockValueBroadcast(Collections.unmodifiableList(stockPriceHistory), stockPrice);
                actors.forEach(actorRef -> tell(actorRef, stockValueBroadcast));
                state = State.WAIT_FOR_RESPONSE;
            } else state = State.FINISHED;
        }

        if (State.FINISHED.equals(state))
            cleanup();
    }

    private void init(DirectorInit initMessage) throws FileNotFoundException {
        outFile = new PrintWriter(new FileOutputStream(initMessage.getOutFile(), false), true);
        outFile.println("Iteration;Stock price;Amount available");
        for (Map.Entry<Class<? extends UntypedActor>, Integer> entry : initMessage.getEntrySet()) {
            int max = entry.getValue();
            for (int c = 0; c < max; c++) {
                final ActorRef worker = getContext().actorOf(Props.create(entry.getKey()));
                actors.add(worker);
            }
        }
        maxIterations = initMessage.getMaxIterations();
        //init Stock DB
        stockPrice = new BigDecimal(10 + 100 * Math.random()).setScale(10, BigDecimal.ROUND_FLOOR);
        initialAmountAvailable = amountAvailable = 20000 + (int) (50000 * Math.random());
        doTick();
        state = State.ANNOUNCE_STOCK_PRICE;
    }

    private void doTick() {
        String s = curTick + ";" + stockPrice + ';' + amountAvailable;
        System.out.println(s);
        outFile.println(s);
        stockPriceHistory.add(new AbstractMap.SimpleImmutableEntry<>(curTick, stockPrice));
        curTick++;
    }

    private void handleStockMarketRequests(ActorRef to, StockMarketRequest request) {
        gotStockMarketRequest++;
        if (request instanceof SellRequest)
            handleSellRequests(to, (SellRequest) request);
        else if (request instanceof BuyRequest)
            handleBuyRequests(to, (BuyRequest) request);
        else if (request instanceof BuyAndSellRequest) {
            BuyAndSellRequest buyAndSellRequest = (BuyAndSellRequest) request;
            handleBuyRequests(to, new BuyRequest(buyAndSellRequest.getBuyAmount()));
            handleSellRequests(to, new SellRequest(buyAndSellRequest.getSellAmount()));
        } else if (!(request instanceof NoBid)) {
            gotStockMarketRequest--;
            throw new IllegalArgumentException(String.format("'%s' is not a known implementation of '%s'", request.getClass(), StockMarketRequest.class));
        }
        if (gotStockMarketRequest >= actors.size()) {
            calculateNextStockPrice();
            doTick();
            state = State.ANNOUNCE_STOCK_PRICE;
        }
    }

    private void handleBuyRequests(ActorRef to, BuyRequest buyRequest) {
        if (buyRequest.getAmount() > 0) {
            int allowedAmount = Math.min(buyRequest.getAmount(), amountAvailable);
            amountAvailable -= allowedAmount;
            BigDecimal value = stockPrice.multiply(new BigDecimal(buyRequest.getAmount()));
            transactions.add(new BuyTransaction(value));
            tell(to, new BuyResponse(allowedAmount, stockPrice));
        }
    }

    private void handleSellRequests(ActorRef to, SellRequest sellRequest) {
        if (sellRequest.getAmount() > 0) {
            BigDecimal value = stockPrice.multiply(new BigDecimal(sellRequest.getAmount()));
            transactions.add(new SellTransaction(value));
            amountAvailable += sellRequest.getAmount();
            tell(to, new SellResponse(sellRequest.getAmount(), stockPrice));
        }
    }

    private void cleanup() throws IOException {
        for (ActorRef actorRef : actors)
            getContext().stop(actorRef);
        actors.clear();
        stockPriceHistory.clear();
        transactions.clear();
        state = State.INIT;
        gotStockMarketRequest = maxIterations = 0;
        if (outFile != null)
            outFile.close();
        System.out.println("Finished job");
        // Comment the following line if you want to process more messages
        getContext().system().terminate();
    }

    private final static BigDecimal MINIMAL_STOCK_PRICE = BigDecimal.ONE.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);


    private void calculateNextStockPrice() {

        final double buyTransactionValue = transactions.stream().filter(e -> e instanceof BuyTransaction).map(Transacation::getValue).mapToDouble(BigDecimal::doubleValue).sum();
        final double sellTransactionValue = transactions.stream().filter(e -> e instanceof SellTransaction).map(Transacation::getValue).mapToDouble(BigDecimal::doubleValue).sum();


        double stockSold = buyTransactionValue / stockPrice.doubleValue();
        double buyPopulrity = Math.log(1 + 20 * stockSold / amountAvailable);
        if (Double.isInfinite(buyPopulrity))
            buyPopulrity = Double.MAX_VALUE;
        buyPopulrity = Math.min(2, buyPopulrity);
        double sellPopularity = (sellTransactionValue / (10 * (buyTransactionValue - sellTransactionValue)));
        if (Double.isNaN(sellPopularity))
            sellPopularity = 0;
        if (Double.isNaN(buyPopulrity))
            buyPopulrity = 0;
        double initPopularity = (buyTransactionValue == 0 && sellTransactionValue == 0) ? 0.855 /*Most significant parameter*/ : 1.2;
        double popularity = initPopularity + buyPopulrity + sellPopularity;

        double oracle = 1 + oracle();
        double change = oracle * popularity;
        // System.out.printf("buyPopularity: %s, sellPopularity: %s, popularity: %s, oracle: %s, change: %s%n", buyPopulrity, sellPopularity, popularity, oracle, change);
        stockPrice = stockPrice.multiply(
                new BigDecimal(change).setScale(10, BigDecimal.ROUND_HALF_UP)
        ).setScale(10, BigDecimal.ROUND_FLOOR);
        if (stockPrice.compareTo(MINIMAL_STOCK_PRICE) <= 0)
            stockPrice = MINIMAL_STOCK_PRICE;
    }

    private double oracle() {
        double rand = Math.random() - 0.5;
        return (rand * rand * rand);
    }
}
