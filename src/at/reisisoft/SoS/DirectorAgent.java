package at.reisisoft.SoS;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

/**
 * Created by Florian on 12.12.2016.
 */
public class DirectorAgent extends Agent {
    @Override
    protected void setup() {
        addBehaviour(new SimpleBehaviour() {
            private final Boolean b = Boolean.FALSE;

            @Override
            public void action() {
                synchronized (b) {

                }
            }

            @Override
            public synchronized boolean done() {
                return b;
            }
        });
    }
}
