package at.reisisoft.SoS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Florian on 12.12.2016.
 */
public abstract class AbstractDirectorAgent<AgentData extends Serializable, T extends AbstractAgent<AgentData>> extends Agent {

    private final List<Constructor<? extends T>> constructors;

    public AbstractDirectorAgent(List<Constructor<? extends T>> constructors) {
        this.constructors = constructors;
    }

    @Override
    protected void setup() {
        addBehaviour(new SimpleBehaviour() {
            private final Boolean b = Boolean.FALSE;

            @Override
            public void action() {
                synchronized (b) {
                    //Wait for an init message
                    final ACLMessage message = waitForMessage();
                    final String[] splitted = message.getContent().split(",");
                    int agents, maxIterations = -1;
                    T[] registeredAgents = null;
                    try {
                        agents = Integer.parseInt(splitted[0]);
                        maxIterations = Integer.parseInt(splitted[1]);
                        registeredAgents = getRandomAgents(agents);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-141521);
                    }
                    if (registeredAgents == null || maxIterations < 1) {
                        System.err.println("Agent creation failed or number of maxIterations too low!");
                        System.exit(141522);
                    }
                    int curIterationCount = 0;
                    Serializable[] agentData, lastAgentData = new Serializable[registeredAgents.length];
                    //Setup world
                    for (int i = 0; i < lastAgentData.length; i++) {
                        lastAgentData[i] = registeredAgents[i].getData();
                    }
                    final AID director = getAID();
                    AID curActor;
                    ACLMessage message2Actor, messageFromActor;
                    try {
                        do {
                            ACLMessage sendWorldToAgent = new ACLMessage(ACLMessage.REQUEST);
                            sendWorldToAgent.setContentObject(lastAgentData);
                            sendWorldToAgent.addReplyTo(director);
                            agentData = new Serializable[registeredAgents.length];
                            for (int agentIndex = 0; agentIndex < registeredAgents.length; agentIndex++) {
                                T agent = registeredAgents[agentIndex];
                                message2Actor = (ACLMessage) sendWorldToAgent.clone();
                                curActor = agent.getAID();
                                message.setSender(curActor);
                                message.setLanguage("java");
                                send(message2Actor);
                                message2Actor = null;
                                MessageTemplate messageTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchSender(curActor));
                                messageFromActor = blockingReceive(messageTemplate);
                                //Ignore message object
                                agentData[agentIndex] = agent.getData();
                            }
                            lastAgentData = agentData;
                            //TODO print summary
                        } while (++curIterationCount <= maxIterations);
                    } catch (IOException e) {
                        System.err.printf("Error while talking to actors in iteration %d.", curIterationCount);
                        e.printStackTrace();
                        System.exit(141523);
                    }
                }
            }

            @Override
            public synchronized boolean done() {
                return b;
            }
        });
    }

    protected final ACLMessage waitForMessage() {
        MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate m2 = MessageTemplate.MatchLanguage("PlainText");
        MessageTemplate m3 = MessageTemplate.MatchOntology("ReceiveTest");
        MessageTemplate m1andm2 = MessageTemplate.and(m1, m2);
        MessageTemplate notm3 = MessageTemplate.not(m3);
        MessageTemplate m1andm2_and_notm3 = MessageTemplate.and(m1andm2, notm3);
        return blockingReceive(m1andm2_and_notm3);

    }

    private T[] getRandomAgents(int cnt) throws FIPAException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (cnt < 3)
            throw new IllegalArgumentException("Number of agents too low. Minimum 2. Is: " + cnt);
        @SuppressWarnings("unchecked")
        T[] agents = (T[]) new AbstractAgent[cnt];
        int index;
        Constructor<? extends T> constructor;
        for (int i = 0; i < cnt; i++) {
            index = (int) (Math.random() * constructors.size());
            constructor = constructors.get(index);
            AMSAgentDescription description = new AMSAgentDescription();
            description.setName(new AID(constructor.getDeclaringClass().getCanonicalName() + '_' + i, true));
            T cur = constructor.newInstance();
            AMSService.register(cur, description);
            agents[i] = cur;
        }
        return agents;
    }
}
