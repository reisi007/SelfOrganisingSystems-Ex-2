package at.reisisoft.sos.stockmarket;

/**
 * Created by Florian on 19.12.2016.
 */
public class BuyAndSellRequest implements StockMarketRequest {

    private final int buyAmount, sellAmount;

    public BuyAndSellRequest(int buyAmount, int sellAmount) {
        this.buyAmount = buyAmount;
        this.sellAmount = sellAmount;
    }

    public int getBuyAmount() {
        return buyAmount;
    }

    public int getSellAmount() {
        return sellAmount;
    }
}
