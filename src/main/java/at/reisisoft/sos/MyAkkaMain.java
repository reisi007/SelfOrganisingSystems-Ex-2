package at.reisisoft.sos;

import akka.actor.*;

/**
 * Created by Florian on 19.12.2016.
 */
public class MyAkkaMain {

    public static void startActorSystem(Class<? extends UntypedActor> startActor, Object messageToStartActor) {
        ActorSystem system = ActorSystem.create("SoS-System");

        final ActorRef mainActor = system.actorOf(
                Props.create(startActor),
                "director"
        );
        final Inbox inbox = Inbox.create(system);
        inbox.send(mainActor, messageToStartActor);
    }
}
