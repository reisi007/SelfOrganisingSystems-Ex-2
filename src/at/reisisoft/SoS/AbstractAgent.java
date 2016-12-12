package at.reisisoft.SoS;

import jade.core.Agent;

/**
 * Created by Florian on 11.12.2016.
 */
public abstract class AbstractAgent<T> extends Agent {

    public abstract T getData();
}
