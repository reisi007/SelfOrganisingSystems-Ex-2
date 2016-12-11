package at.reisisoft.SoS;


import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Created by Florian on 11.12.2016.
 */
public class IceCreamAgent extends AbstractAgent {

    protected double x;

    public IceCreamAgent() {
        this(IceCreamAgent.class);
    }

    protected IceCreamAgent(Class<? extends IceCreamAgent> class1) {
        this(1000 * Math.random());
    }

    public IceCreamAgent(double x) {
        this(IceCreamAgent.class, x);
    }

    protected IceCreamAgent(Class<? extends IceCreamAgent> class1, double x) {
        super(class1);
        this.x = x;
    }

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(getBehaviour());
    }

    protected Behaviour getBehaviour() {
        return new SimpleBehaviour(this) {
            private boolean finished;

            @Override
            public void action() {
                finished = false;
                ACLMessage message = waitForMessage();
                if (message != null)
                    try {
                        double other = Double.parseDouble(message.getContent());
                        synchronized (this) {
                            double dist = other - x;
                            if (Math.abs(dist) >= 5) {
                                if (dist > 0)
                                    dist = 5;
                                else
                                    dist = -5;
                            }
                            x += dist;
                            sendMessageToAll(Double.toString(x));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace(System.err);
                        System.err.printf("%n%n%nMessage didn't contain a valid number%n");
                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }
                finished = true;
            }

            @Override
            public boolean done() {
                return finished;
            }
        };
    }
}
