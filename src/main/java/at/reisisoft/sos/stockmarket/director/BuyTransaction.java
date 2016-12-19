package at.reisisoft.sos.stockmarket.director;

import at.reisisoft.sos.AbstractUntypedActor;

import java.math.BigDecimal;

/**
 * Created by Florian on 19.12.2016.
 */
public class BuyTransaction extends AbstractTransaction {
    public BuyTransaction(BigDecimal value) {
        super(value);
    }
}
