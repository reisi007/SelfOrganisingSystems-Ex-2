package at.reisisoft.SoS;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Florian on 11.12.2016.
 */
public abstract class AbstractAgent extends Agent {
    private Class<? extends AbstractAgent> superClass;

    protected AbstractAgent(Class<? extends AbstractAgent> superClass) {
        this.superClass = superClass;
    }

    @Override
    protected void setup() {
        try {
            AMSAgentDescription description = new AMSAgentDescription();
            description.setName(new AID(superClass.getCanonicalName() + '_' + GlobalId.getGlobalId(), true));
            AMSService.register(this, description);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private List<AID> getAllKnownReceivers() throws FIPAException {
        AID myAid = getAID();
        SearchConstraints c = new SearchConstraints();
        c.setMaxResults((long) -1);
        AMSAgentDescription agentDescription = new AMSAgentDescription();
        final AMSAgentDescription[] search = AMSService.search(this, agentDescription, c);
        return Arrays.stream(search)
                .map(AMSAgentDescription::getName)
                .filter(a -> !myAid.equals(a))
                .collect(Collectors.toList());
    }

    protected void sendMessageToAll(String content) throws FIPAException {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent(content);
        getAllKnownReceivers().forEach(message::addReceiver);
    }

    protected ACLMessage waitForMessage() {
        System.out.println("\nAgent " + getLocalName() + " in state 1.1 is waiting for a message");
        MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate m2 = MessageTemplate.MatchLanguage("PlainText");
        MessageTemplate m3 = MessageTemplate.MatchOntology("ReceiveTest");
        MessageTemplate m1andm2 = MessageTemplate.and(m1, m2);
        MessageTemplate notm3 = MessageTemplate.not(m3);
        MessageTemplate m1andm2_and_notm3 = MessageTemplate.and(m1andm2, notm3);
        return blockingReceive(m1andm2_and_notm3);

    }
}
