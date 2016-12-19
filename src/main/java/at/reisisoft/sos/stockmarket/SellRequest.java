package at.reisisoft.sos.stockmarket;

/**
 * Created by Florian on 19.12.2016.
 */
public class SellRequest implements StockMarketRequest {

    public int getAmount() {
        return amount;
    }

    public SellRequest(int amount) {
        this.amount = amount;
    }

    private final int amount;
}
