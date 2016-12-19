package at.reisisoft.sos.icecream;

/**
 * Created by Florian on 19.12.2016.
 */
public class SendWorldMessage implements IceCreamMessage<double[]> {
    private final double[] data;

    public SendWorldMessage(double[] data) {
        this.data = data;
    }

    public double[] getData() {
        return data;
    }
}
