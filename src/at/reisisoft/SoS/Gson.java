package at.reisisoft.SoS;

/**
 * Created by Florian on 14.12.2016.
 */
public class Gson {
    private static com.google.gson.Gson instance;

    public static com.google.gson.Gson getInstance() {
        if (instance == null)
            instance = new com.google.gson.Gson();
        return instance;
    }
}
