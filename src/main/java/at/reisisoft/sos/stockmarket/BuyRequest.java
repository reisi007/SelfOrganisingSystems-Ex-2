package at.reisisoft.sos.stockmarket;

/**
 * Created by Florian on 19.12.2016.
 */
public class BuyRequest implements StockMarketRequest {

    private final int amount;

    public int getAmount() {

        return amount;
    }

    public BuyRequest(int amount) {

        this.amount = amount;
    }

}
