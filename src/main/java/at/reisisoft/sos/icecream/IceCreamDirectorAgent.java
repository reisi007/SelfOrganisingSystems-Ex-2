package at.reisisoft.sos.icecream;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import at.reisisoft.sos.AbstractUntypedActor;
import at.reisisoft.sos.directormessage.Acknowledge;
import at.reisisoft.sos.directormessage.Deny;
import at.reisisoft.sos.directormessage.DirectorInit;
import at.reisisoft.sos.directormessage.GetID;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Florian on 19.12.2016.
 */
public class IceCreamDirectorAgent extends AbstractUntypedActor {
    private enum State {
        PRE_INIT, INIT, WORLD_FETCH_FIRST, WORLD_FETCH_WAIT, FINISHED;

        private boolean isInitState() {
            return INIT.equals(this) || PRE_INIT.equals(this);
        }
    }

    private State state = State.PRE_INIT;
    private final List<ActorRef> actors = new ArrayList<>();
    private int id = 0, ackReceived = 0, curIteration = 0;
    private double[] world;
    private int maxIterations = -1;
    private PrintWriter outFile = null;

    @Override
    public synchronized void onReceive(Object o) throws Throwable {
        o = Objects.requireNonNull(o);
        if (state.isInitState() && (o instanceof DirectorInit)) {
            init((DirectorInit) o);
        } else if (o instanceof Acknowledge) {
            ackReceived++;
            if (ackReceived >= actors.size()) {
                ackReceived = 0;
                switch (state) {
                    case WORLD_FETCH_FIRST:
                        //Fetch world for the first time
                        world = new double[actors.size()];
                        for (ActorRef actorRef : actors)
                            tell(actorRef, new SendWorldMessage(null));

                        state = State.WORLD_FETCH_WAIT;
                        break;
                    default:
                        throwNewIllegalStateException(state + " does not expect acknowledges");
                }
            }
        } else if (o instanceof Deny) {
            Deny d = (Deny) o;
            throwNewIllegalStateException(d.getErrorMessage());
        } else if (o instanceof ReturnWorldMessage) {
            ReturnWorldMessage rwMessage = (ReturnWorldMessage) o;
            ReturnWorldMessage.Data data = rwMessage.getData();
            world[data.getId()] = data.getData();
            ackReceived++;
            if (ackReceived >= actors.size()) {
                ackReceived = 0;
                switch (state) {
                    case WORLD_FETCH_WAIT:
                        curIteration++;
                        StringJoiner sj = new StringJoiner(";");
                        sj.add(Integer.toString(curIteration));
                        for (double d : world)
                            sj.add(Double.toString(d));
                        outFile.println(sj.toString());

                        if (curIteration < maxIterations)
                            for (ActorRef actorRef : actors)
                                tell(actorRef, new SendWorldMessage(world));
                        else
                            state = State.FINISHED;

                        break;
                    default:
                        throwNewIllegalStateException(state + " does not expect acknowledges");
                }
            }
        }

        if (State.FINISHED.equals(state))
            cleanup();
    }

    private void throwNewIllegalStateException(String message) {
        System.err.printf("%n%n%n%s%n%n%n", message);
        throw new IllegalStateException(message);
    }

    private void init(DirectorInit initMessage) throws FileNotFoundException {
        maxIterations = initMessage.getMaxIterations();
        StringJoiner sj = new StringJoiner(";");
        sj.add("Iterations");
        int i = 1;
        outFile = new PrintWriter(new FileOutputStream(initMessage.getOutFile(), false), true);
        for (Map.Entry<Class<? extends UntypedActor>, Integer> entry : initMessage.getEntrySet()) {
            int max = entry.getValue();
            for (int c = 0; c < max; c++) {
                final ActorRef worker = getContext().actorOf(Props.create(entry.getKey()));
                actors.add(worker);
                tell(worker, new GetID(id));
                id++;
                sj.add("Agent " + (i++));
            }
        }
        outFile.println(sj.toString());
        state = State.WORLD_FETCH_FIRST;
    }

    private void cleanup() throws IOException {
        for (ActorRef actorRef : actors)
            getContext().stop(actorRef);
        actors.clear();
        state = State.INIT;
        id = ackReceived = curIteration = 0;
        if (outFile != null)
            outFile.close();
        System.out.println("Finished job");
        // Comment the following line if you want to process more messages
        getContext().system().terminate();
    }
}
