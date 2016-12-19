package at.reisisoft.sos.directormessage;

/**
 * Created by Florian on 19.12.2016.
 */
public class Acknowledge implements DirectorMessageResponseMarker {
    private static DirectorMessageResponseMarker instance;

    public static DirectorMessageResponseMarker getInstance() {
        if (instance == null)
            instance = new Acknowledge();
        return instance;
    }

    private Acknowledge() {
    }
}
