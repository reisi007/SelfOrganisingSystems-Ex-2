package at.reisisoft.sos.stockmarket;

/**
 * Created by Florian on 19.12.2016.
 */
public class NoBid implements StockMarketRequest {
    private static StockMarketRequest instance;

    public static StockMarketRequest getInstance() {
        if (instance == null)
            instance = new NoBid();
        return instance;
    }

    private NoBid() {

    }
}
