package at.reisisoft.sos.directormessage;

/**
 * Created by Florian on 19.12.2016.
 */
public class Deny implements DirectorMessageResponseMarker {
    private final String errorMessage;

    public Deny(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
