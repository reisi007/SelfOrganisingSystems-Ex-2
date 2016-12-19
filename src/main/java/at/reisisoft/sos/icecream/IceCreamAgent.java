package at.reisisoft.sos.icecream;

import akka.actor.ActorRef;
import at.reisisoft.sos.AbstractUntypedActor;
import at.reisisoft.sos.directormessage.Acknowledge;
import at.reisisoft.sos.directormessage.Deny;
import at.reisisoft.sos.directormessage.GetID;

import java.util.Objects;

/**
 * Created by Florian on 19.12.2016.
 */
public class IceCreamAgent extends AbstractUntypedActor {
    private int id = -1;
    private double next;

    private static final long AREA = 1000;

    public final void onReceive(Object o) throws Throwable {
        o = Objects.requireNonNull(o);
        if (o instanceof GetID)
            initialize(getSender(), (GetID) o);
        else if (o instanceof SendWorldMessage)
            receiveWorld(getSender(), (SendWorldMessage) o);
        else unhandled(o);
    }

    private void initialize(ActorRef to, GetID getID) {
        id = getID.getId();
        next = AREA * Math.random();
        tell(to, Acknowledge.getInstance());
    }

    private void receiveWorld(ActorRef to, SendWorldMessage message) {
        if (id < 0)
            tell(to, new Deny("ID has not been set"));

        final double[] data = message.getData();

        if (data != null)
            next = calculateNext(next, data);

        tell(to, new ReturnWorldMessage(id, next));
    }

    private static final double MAX_JUMP_SIZE = 25;
    private static final double MIN_JUMP_SIZE = 3;

    protected synchronized double calculateNext(double self, double[] world) {
        int other = findClosest(self, world);
        double otherPos = world[other];
        double dist = otherPos - self;
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
        self += dist;
        return self;
    }

    private int findClosest(double self, double[] world) {
        double minDistance = Double.MAX_VALUE;
        double distance;
        int bestIndex = -1;
        for (int i = 0; i < world.length; i++) {
            double aWorld = world[i];
            distance = Math.abs(aWorld - self);
            if (distance < minDistance && distance != 0) {
                minDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
