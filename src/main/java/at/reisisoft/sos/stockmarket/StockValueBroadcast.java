package at.reisisoft.sos.stockmarket;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Florian on 19.12.2016.
 */
public class StockValueBroadcast {
    private final List<Map.Entry<Integer, BigDecimal>> stockPrice;
    private final BigDecimal currentStockPrice;

    public StockValueBroadcast(List<Map.Entry<Integer, BigDecimal>> stockPrice, BigDecimal currentStockPrice) {
        this.stockPrice = stockPrice;
        this.currentStockPrice = currentStockPrice;
    }

    public List<Map.Entry<Integer, BigDecimal>> getStockPrice() {
        return stockPrice;
    }

    public BigDecimal getCurrentStockPrice() {
        return currentStockPrice;
    }
}
