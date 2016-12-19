package at.reisisoft.sos;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

/**
 * Created by Florian on 19.12.2016.
 */
public abstract class AbstractUntypedActor extends UntypedActor {
    private static final Timeout DEFAULT_TIMEOUT = new Timeout(1, TimeUnit.MINUTES);

    public void tell(ActorRef to, Object message) {
        to.tell(message, getSelf());
    }

    public Object ask(ActorRef to, Object message) throws Exception {
        return ask(to, message, DEFAULT_TIMEOUT);
    }

    public Object ask(ActorRef to, Object message, Timeout timeout) throws Exception {
        Future<Object> future = Patterns.ask(to, message, timeout);
        return Await.result(future, timeout.duration());
    }

}
