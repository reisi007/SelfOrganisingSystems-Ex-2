package at.reisisoft.SoS;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Created by Florian on 13.12.2016.
 */
public abstract class AbstractCyclicBehaviour extends CyclicBehaviour {

    public AbstractCyclicBehaviour(Agent a) {
        super(a);
    }

    public abstract void action();

    public ACLMessage prepareACLMessage(ACLMessage original) {
        final ACLMessage reply = original.createReply();
        reply.setContent("Done!");
        reply.setPerformative(ACLMessage.CONFIRM);
        return reply;
    }
}
