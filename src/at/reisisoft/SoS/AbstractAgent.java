package at.reisisoft.SoS;

import jade.core.Agent;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Florian on 11.12.2016.
 */
public abstract class AbstractAgent<T> extends Agent implements Function<List<T>, T> {

    public abstract T getData();
}
