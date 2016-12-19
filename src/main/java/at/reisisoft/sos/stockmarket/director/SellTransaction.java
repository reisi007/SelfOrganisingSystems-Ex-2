package at.reisisoft.sos.stockmarket.director;

import java.math.BigDecimal;

/**
 * Created by Florian on 19.12.2016.
 */
public class SellTransaction extends AbstractTransaction {
    public SellTransaction(BigDecimal value) {
        super(value.negate());
    }

    @Override
    public BigDecimal getAbsolutValue() {
        return getValue().negate();
    }
}
