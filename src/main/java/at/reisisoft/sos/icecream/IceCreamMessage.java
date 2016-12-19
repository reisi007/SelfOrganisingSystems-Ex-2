package at.reisisoft.sos.icecream;

import java.io.Serializable;

/**
 * Created by Florian on 19.12.2016.
 */
public interface IceCreamMessage<T extends Serializable> {

    T getData();
}
