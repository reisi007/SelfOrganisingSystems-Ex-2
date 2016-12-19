package at.reisisoft.sos.stockmarket.director;

import java.math.BigDecimal;

/**
 * Created by Florian on 19.12.2016.
 */
public class AbstractTransaction implements Transacation {
    private final BigDecimal value;

    public AbstractTransaction(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }
}
