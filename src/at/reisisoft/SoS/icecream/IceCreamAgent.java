package at.reisisoft.SoS.icecream;


import at.reisisoft.SoS.AbstractAgent;
import at.reisisoft.SoS.AbstractCyclicBehaviour;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.Serializable;

/**
 * Created by Florian on 11.12.2016.
 */
public class IceCreamAgent extends AbstractAgent<Double> {

    protected double x;

    public IceCreamAgent() {
        this(1000 * Math.random());
    }

    private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

    protected IceCreamAgent(double x) {
        this.x = x;
    }

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(tbf.wrap(getBehaviour()));
    }

    protected Behaviour getBehaviour() {
        return new AbstractCyclicBehaviour(this) {
            private boolean finished;

            @Override
            public void action() {
                finished = false;
                ACLMessage message = getMessage(o -> block());
                if (message != null)
                    try {
                        Serializable serializable = message.getContentObject();
                        double[] world = (double[]) serializable;
                        double other = findClosest(world);
                        synchronized (this) {
                            double dist = other - x;
                            if (Math.abs(dist) >= 5) {
                                if (dist > 0)
                                    dist = 5;
                                else
                                    dist = -5;
                            }
                            x += dist;
                            final ACLMessage respone = prepareACLMessage(message);
                            respone.setSender(getAID());
                            send(respone);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace(System.err);
                        System.err.printf("%n%n%nMessage didn't contain a valid number%n");
                    } catch (UnreadableException | ClassCastException e) {
                        e.printStackTrace();
                    }
                finished = true;
            }
        };
    }

    private double findClosest(double[] world) {
        double minDistance = Double.MAX_VALUE;
        double distance;
        for (double aWorld : world) {
            distance = Math.abs(aWorld - x);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    public double getPosition() {
        return x;
    }

    @Override
    public Double getData() {
        return x;
    }
}
