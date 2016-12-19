package at.reisisoft.sos.stockmarket;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by Florian on 19.12.2016.
 */
public abstract class SellLowestPriceBidder extends Bidder {
    @Override
    protected Map<BigDecimal, Integer> sellResponse(Map<BigDecimal, Integer> bought, final int neededAmount) {
        Map<BigDecimal, Integer> returnMap = new HashMap<>();
        int selectedAmount = 0;
        for (BigDecimal key : new TreeSet<>(bought.keySet())) {
            int val = bought.get(key);
            int needed = neededAmount - selectedAmount;
            if (val >= needed) {
                returnMap.put(key, needed);
                return returnMap;
            } else {
                returnMap.put(key, val);
                selectedAmount += val;
            }
        }
        throw new IllegalStateException("Nothing mopre to sell");
    }
}
