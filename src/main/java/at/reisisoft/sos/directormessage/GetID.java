package at.reisisoft.sos.directormessage;

/**
 * Created by Florian on 19.12.2016.
 */
public class GetID implements DirectorMessageMarker {

    private final int id;

    public GetID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
