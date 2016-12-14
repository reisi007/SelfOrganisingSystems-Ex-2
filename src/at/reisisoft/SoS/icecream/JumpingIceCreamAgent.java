package at.reisisoft.SoS.icecream;

import at.reisisoft.SoS.AbstractCyclicBehaviour;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Florian on 11.12.2016.
 */
public class JumpingIceCreamAgent extends IceCreamAgent {

    private double lastPos = Double.MIN_VALUE;

    public JumpingIceCreamAgent() {
    }

    public JumpingIceCreamAgent(double x) {
        super(x);
    }

    @Override
    protected Behaviour getBehaviour() {
        return new AbstractCyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage message = receive(template);
                if (message == null)
                    block();
                else
                    try {
                        Serializable serializable = message.getContentObject();
                        double[] world = (double[]) serializable;
                        synchronized (this) {
                            x = nextPosition(world);
                        }
                        final ACLMessage response = prepareACLMessage(message);
                        response.setSender(getAID());
                        send(response);
                    } catch (NumberFormatException e) {
                        e.printStackTrace(System.err);
                        System.err.printf("%n%n%nMessage didn't contain a valid number%n");
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
            }
        };
    }

    private double nextPosition(double[] world) {
        final int maxIndex = world.length - 2;
        Arrays.sort(world);
        double maxDiff = Double.MIN_VALUE;
        double curDiff;
        int maxDiffIndex = -1;
        for (int i = 0; i <= maxIndex; i++) {
            curDiff = world[i + 1] - world[i];
            if (curDiff > maxDiff) {
                maxDiff = curDiff;
                maxDiffIndex = i;
            }
        }
        double randCheckWorldBorders = Math.random();
        if (randCheckWorldBorders > 0.75) {
            double lowestWorld = world[0], maximumWorld = 1000 - world[world.length - 1];
            if (Math.max(lowestWorld, maximumWorld) > maxDiff) {
                double nextPos;
                if (lowestWorld > maximumWorld) {
                    nextPos = world[0] / 2;
                } else
                    nextPos = world[world.length - 1] + (1000 - world[world.length - 1]) / 2;
                return nextPos;
            }
        }
        return world[maxDiffIndex] + (world[maxDiffIndex + 1] - world[maxDiffIndex]) / 2;
    }
}
