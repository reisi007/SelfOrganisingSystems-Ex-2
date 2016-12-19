package at.reisisoft.sos.stockmarket.director;

import java.math.BigDecimal;

/**
 * Created by Florian on 19.12.2016.
 */
public interface Transacation {

    BigDecimal getValue();

    default BigDecimal getAbsolutValue() {
        return getValue();
    }
}
