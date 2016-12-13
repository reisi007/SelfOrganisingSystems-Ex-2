package at.reisisoft.SoS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Florian on 12.12.2016.
 */
public abstract class AbstractDirectorAgent<AgentData extends Serializable, T extends AbstractAgent<AgentData>> extends Agent {

    private final List<Class<? extends T>> classes;
    private boolean lookedForArgs = false;
    private String initMessage;
    private AgentContainer agentController;
    private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

    public AbstractDirectorAgent(List<Class<? extends T>> classes) {
        this.classes = classes;
    }

    @Override
    protected void setup() {
        setEnabledO2ACommunication(true, 1);
        addBehaviour(
                tbf.wrap(
                        new CyclicBehaviour(this) {
                            @Override
                            public void action() {
                                //Wait for an init message
                                if (!lookedForArgs && (initMessage = (String) getArguments()[0]) != null) {
                                    agentController = (AgentContainer) getArguments()[1];
                                    doSomethingWithRawMessage(initMessage);
                                    lookedForArgs = true;
                                } else {
                                    final ACLMessage message = waitForInitMessage();
                                    if (message != null)
                                        doSomethingWithRawMessage(message.getContent());
                                    else
                                        block();
                                }
                            }

                            private void doSomethingWithRawMessage(String rawContent) {
                                int agents, maxIterations = -1;
                                T[] registeredAgents = null;
                                try {
                                    final String[] splitted = rawContent.split(",");
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
                                        sendWorldToAgent.addReplyTo(director);
                                        sendWorldToAgent.setSender(director);
                                        sendWorldToAgent.setLanguage("java");
                                        agentData = new Serializable[registeredAgents.length];
                                        for (int agentIndex = 0; agentIndex < registeredAgents.length; agentIndex++) {
                                            T agent = registeredAgents[agentIndex];
                                            message2Actor = (ACLMessage) sendWorldToAgent.clone();
                                            message2Actor.setContentObject(lastAgentData);
                                            curActor = agent.getAID();
                                            message2Actor.addReceiver(curActor);
                                            System.out.printf("%n%n%nSending message to agent %d%n%n%n", agentIndex);
                                            send(message2Actor);
                                            message2Actor = null;
                                            MessageTemplate messageTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchSender(curActor));
                                            do {
                                                //  messageFromActor = blockingReceive(messageTemplate);
                                                messageFromActor = receive();
                                                if (messageFromActor == null)
                                                    block();
                                            } while (messageFromActor == null);
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
                        })
        );
    }

    protected final ACLMessage waitForInitMessage() {
        MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate m2 = MessageTemplate.MatchLanguage("PlainText");
        MessageTemplate m3 = MessageTemplate.MatchOntology("ReceiveTest");
        MessageTemplate m1andm2 = MessageTemplate.and(m1, m2);
        MessageTemplate notm3 = MessageTemplate.not(m3);
        MessageTemplate m1andm2_and_notm3 = MessageTemplate.and(m1andm2, notm3);
        return receive(m1andm2_and_notm3);

    }

    private T[] getRandomAgents(int cnt) throws IllegalAccessException, InstantiationException, StaleProxyException {
        if (cnt < 3)
            throw new IllegalArgumentException("Number of agents too low. Minimum 2. Is: " + cnt);
        @SuppressWarnings("unchecked")
        T[] agents = (T[]) new AbstractAgent[cnt];
        int index;
        Class<? extends T> aClass;
        for (int i = 0; i < cnt; i++) {
            double rand = Math.random();
            index = (int) (rand * classes.size());
            aClass = classes.get(index);
            T cur = aClass.newInstance();
            agentController.acceptNewAgent(aClass.getName() + '_' + i, cur);
            agents[i] = cur;
        }
        return agents;
    }
}
