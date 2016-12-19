package at.reisisoft.sos.icecream;

import java.io.Serializable;

/**
 * Created by Florian on 19.12.2016.
 */
public class ReturnWorldMessage implements IceCreamMessage<ReturnWorldMessage.Data> {
    private final Data data;

    public ReturnWorldMessage(int id, double data) {
        this.data = new Data(id, data);
    }

    public Data getData() {
        return data;
    }

    public class Data implements Serializable {
        private final double data;
        private final int id;

        public double getData() {
            return data;
        }

        public int getId() {
            return id;
        }

        private Data(int id, double data) {
            this.data = data;
            this.id = id;
        }
    }
}
