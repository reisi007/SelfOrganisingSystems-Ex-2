package at.reisisoft.SoS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Florian on 12.12.2016.
 */
public abstract class AbstractDirectorAgent<AgentData extends Serializable, T extends AbstractAgent<AgentData>> extends Agent {

    private enum AgentState {
        INIT,
        LOOP_EMIT,
        LOOP_COLLECT,
        END
    }

    private final List<Class<? extends T>> classes;
    private String initMessage;
    private AgentContainer agentController;
    private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

    public AbstractDirectorAgent(List<Class<? extends T>> classes) {
        this.classes = classes;
    }

    @Override
    protected void setup() {
        addBehaviour(
                tbf.wrap(
                        new SimpleBehaviour() {

                            private AgentState agentState = AgentState.INIT;
                            private List<T> registeredAgents;
                            private int agents, curIterationCount = 0, maxIterations = -1;
                            private List<AgentData> agentData;
                            //How many collect messages got received
                            private int collectMessages = 0;

                            @Override
                            public void action() {
                                //Wait for an init message
                                if (AgentState.INIT.equals(agentState) && (initMessage = (String) getArguments()[0]) != null) {
                                    agentController = (AgentContainer) getArguments()[1];
                                    init(initMessage);
                                    if (!AgentState.LOOP_EMIT.equals(agentState))
                                        throw new IllegalStateException("Should be in LOOP_EMIT state");
                                    stepEmit();
                                } else {
                                    final ACLMessage message = waitForInitMessage();
                                    if (message != null) {
                                        if (AgentState.INIT.equals(agentState))
                                            init(message.getContent());
                                        if (AgentState.LOOP_EMIT.equals(agentState))
                                            stepEmit();
                                        else if (AgentState.LOOP_COLLECT.equals(agentState)) {
                                            try {
                                                final Serializable contentObject = message.getContentObject();
                                                @SuppressWarnings("unchecked")
                                                AgentData agentData = (AgentData) contentObject;
                                                stepCollect(agentData);
                                            } catch (UnreadableException e) {
                                                throw new RuntimeException(e);
                                            }

                                        }
                                        if (AgentState.END.equals(agentState))
                                            end();
                                    }
                                    block();
                                }
                            }

                            @Override
                            public boolean done() {
                                return AgentState.END.equals(agentState);
                            }

                            private void end() {
                                System.out.printf("%n%n%n == End of simulation ==");
                            }

                            private void stepCollect(AgentData cur) {
                                if (collectMessages <= 0) {
                                    collectMessages = 0;
                                    agentData.clear();
                                }
                                agentData.add(cur);
                                collectMessages++;
                                if (collectMessages >= registeredAgents.size()) {
                                    System.out.printf("%n%n%n Positions: %s %n%n%n", agentData.toString());
                                    agentState = AgentState.LOOP_EMIT;
                                }
                            }

                            private void init(String rawContent) {
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
                                agentData = new ArrayList<>(registeredAgents.size());
                                //Setup world
                                for (T cur : registeredAgents)
                                    agentData.add(cur.getData());
                                agentState = AgentState.LOOP_EMIT;
                            }

                            private void stepEmit() {
                                final AID director = getAID();
                                collectMessages = 0;
                                if (curIterationCount >= maxIterations) {
                                    agentState = AgentState.END;
                                } else
                                    try {
                                        final ACLMessage sendWorldToAgents = new ACLMessage(ACLMessage.REQUEST);
                                        sendWorldToAgents.setDefaultEnvelope();
                                        sendWorldToAgents.addReplyTo(director);
                                        sendWorldToAgents.setSender(director);
                                        sendWorldToAgents.setLanguage("java");
                                        sendWorldToAgents.setContentObject(agentData.toArray());
                                        agentData = new ArrayList<>(agentData.size());

                                        for (T agent : registeredAgents)
                                            sendWorldToAgents.addReceiver(agent.getAID());

                                        System.out.printf("%n%n%nSending message to all agents %n%n%n");
                                        send(sendWorldToAgents);
                                       /* agentData = agentData;
                                        TODO print summary -> different method*/
                                        //The last thing: Increase iteration count
                                        curIterationCount++;
                                        agentState = AgentState.LOOP_COLLECT;
                                    } catch (IOException e) {
                                        System.err.printf("Error while talking to actors in iteration %d.", curIterationCount);
                                        e.printStackTrace();
                                        System.exit(141523);
                                    }
                            }

                        }
                )
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

    private List<T> getRandomAgents(int cnt) throws IllegalAccessException, InstantiationException, StaleProxyException {
        if (cnt < 3)
            throw new IllegalArgumentException("Number of agents too low. Minimum 2. Is: " + cnt);
        @SuppressWarnings("unchecked")
        List<T> agents = new ArrayList<T>(cnt);
        int index;
        Class<? extends T> aClass;
        for (int i = 0; i < cnt; i++) {
            double rand = Math.random();
            index = (int) (rand * classes.size());
            aClass = classes.get(index);
            T cur = aClass.newInstance();
            agentController.acceptNewAgent(aClass.getName() + '_' + i, cur);
            agents.add(cur);
        }
        return agents;
    }
}
