package at.reisisoft.SoS;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Florian on 11.12.2016.
 */
public class GlobalId {
    private static AtomicInteger id = new AtomicInteger(0);

    public static int getGlobalId() {
        return id.getAndIncrement();
    }
}
