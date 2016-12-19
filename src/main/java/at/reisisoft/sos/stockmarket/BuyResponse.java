package at.reisisoft.sos.stockmarket;

import java.math.BigDecimal;

/**
 * Created by Florian on 19.12.2016.
 */
public class BuyResponse implements BidMarker {
    private final int amount;
    private final BigDecimal pricePerPiece;

    public BuyResponse(int amount, BigDecimal pricePerPiece) {
        this.amount = amount;
        this.pricePerPiece = pricePerPiece;
    }

    public int getAmount() {
        return amount;
    }

    public BigDecimal getPricePerPiece() {
        return pricePerPiece;
    }
}
