package at.reisisoft.SoS;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.function.Consumer;

/**
 * Created by Florian on 11.12.2016.
 */
public abstract class AbstractAgent<T> extends Agent {

    public abstract T getData();

    public ACLMessage getMessage(Consumer<Object> onNoMessage) {
      /*  MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate m2 = MessageTemplate.MatchLanguage("java");
        MessageTemplate m3 = MessageTemplate.MatchOntology("ReceiveTest");
        MessageTemplate m1andm2 = MessageTemplate.and(m1, m2);
        MessageTemplate notm3 = MessageTemplate.not(m3);
        MessageTemplate m1andm2_and_notm3 = MessageTemplate.and(m1andm2, notm3);
        return blockingReceive(m1andm2_and_notm3);*/
        ACLMessage message;
        do {
            message = receive();
            if (message == null) {
                onNoMessage.accept(null);
            } else System.out.printf("%n%n%n Received message! %n%n%n");

        } while (message == null);
        return message;
    }
}
