package at.reisisoft.SoS;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Created by Florian on 13.12.2016.
 */
public abstract class AbstractCyclicBehaviour extends CyclicBehaviour {

    public AbstractCyclicBehaviour(Agent a) {
        super(a);
    }

    public abstract void action();

    protected final static MessageTemplate template =
            MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchOntology("sos")
            );

    public ACLMessage prepareACLMessage(ACLMessage original) {
        final ACLMessage reply = original.createReply();
        reply.setContent("Done!");
        reply.setPerformative(ACLMessage.CONFIRM);
        return reply;
    }
}
