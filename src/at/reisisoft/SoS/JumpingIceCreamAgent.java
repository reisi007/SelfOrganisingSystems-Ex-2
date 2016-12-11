package at.reisisoft.SoS;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Created by Florian on 11.12.2016.
 */
public class JumpingIceCreamAgent extends IceCreamAgent {

    private double lastPos = Double.MIN_VALUE;

    public JumpingIceCreamAgent() {
        super(JumpingIceCreamAgent.class);
    }

    public JumpingIceCreamAgent(double x) {
        super(JumpingIceCreamAgent.class, x);
    }

    @Override
    protected Behaviour getBehaviour() {
        return new SimpleBehaviour() {
            boolean finished;

            @Override
            public void action() {
                finished = false;
                ACLMessage message = waitForMessage();
                if (message != null)
                    try {
                        double other = Double.parseDouble(message.getContent());
                        synchronized (this) {
                            if (lastPos < 0) {
                                lastPos = other;
                            } else {
                                x = Math.abs(lastPos - other);
                                lastPos = other;
                            }
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
