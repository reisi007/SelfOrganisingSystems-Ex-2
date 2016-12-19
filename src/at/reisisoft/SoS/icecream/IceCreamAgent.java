package at.reisisoft.SoS.icecream;


import at.reisisoft.SoS.AbstractAgent;
import at.reisisoft.SoS.AbstractCyclicBehaviour;
import at.reisisoft.SoS.Gson;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * Created by Florian on 11.12.2016.
 */
public class IceCreamAgent extends AbstractAgent<Double> {

    protected double x;

    public IceCreamAgent() {
        this(1000 * Math.random());
    }


    protected IceCreamAgent(double x) {
        this.x = x;
    }

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(getBehaviour());
    }

    protected Behaviour getBehaviour() {
        return new AbstractCyclicBehaviour(this) {

            @Override
            public void action() {
                ACLMessage message = receive(template);
                if (message == null)
                    block();
                else
                    try {
                        Double[] world = Gson.getInstance().fromJson(message.getContent(), Double[].class);
                        apply(world); // result can be ignored here
                        final ACLMessage respone = prepareACLMessage(message);
                        respone.setSender(getAID());
                        send(respone);

                    } catch (NumberFormatException e) {
                        e.printStackTrace(System.err);
                        System.err.printf("%n%n%nMessage didn't contain a valid number%n");
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
            }
        };
    }

    private int findClosest(Double[] world) {
        double minDistance = Double.MAX_VALUE;
        double distance;
        int bestIndex = -1;
        for (int i = 0; i < world.length; i++) {
            double aWorld = world[i];
            distance = Math.abs(aWorld - x);
            if (distance < minDistance && distance != 0) {
                minDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public double getPosition() {
        return x;
    }

    @Override
    public Double getData() {
        return x;
    }

    private static final double MAX_JUMP_SIZE = 25;
    private static final double MIN_JUMP_SIZE = 3;

    @Override
    public Double apply(Double[] world) {
        int other = findClosest(world);
        synchronized (this) {
            double otherPos = world[other];
            double dist = otherPos - x;
            if (Math.abs(dist) >= MAX_JUMP_SIZE) {
                if (dist > 0)
                    dist = MAX_JUMP_SIZE;
                else
                    dist = -MAX_JUMP_SIZE;
            } else if (Math.abs(dist) <= MIN_JUMP_SIZE) {
                if (dist > 0)
                    dist = MIN_JUMP_SIZE;
                else
                    dist = -MIN_JUMP_SIZE;
            }
            dist *= 1 + (Math.random() - 0.5) / 5;
            x += dist;
            return x;
        }
    }
}
