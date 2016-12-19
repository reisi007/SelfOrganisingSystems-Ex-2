package at.reisisoft.sos;

import java.io.IOException;

/**
 * Created by Florian on 19.12.2016.
 */
public class UnknownMessageTypeException extends IOException {
    public UnknownMessageTypeException(Class<?> class1) {
        super("Unknown message class:" + class1.getCanonicalName());
    }
}
