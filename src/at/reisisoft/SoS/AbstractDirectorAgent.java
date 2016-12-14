package at.reisisoft.SoS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

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
    private final BufferedWriter bufferedWriter;


    public AbstractDirectorAgent(List<Class<? extends T>> classes) throws IOException {
        this.classes = classes;
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\Desktop\\sos.csv", false)));
    }

    @Override
    protected void setup() {
        addBehaviour(new SimpleBehaviour() {
                         private AgentState agentState = AgentState.INIT;
                         private List<T> registeredAgents;
                         private int curIterationCount = 0, maxIterations = -1;
                         private List<AgentData> agentData;
                         //How many collect messages got received
                         private int collectMessages = 0;

                         @Override
                         public void action() {
                             //Wait for an init message
                             while (!AgentState.END.equals(agentState)) {
                                 if (AgentState.INIT.equals(agentState) && (initMessage = (String) getArguments()[0]) != null) {
                                     agentController = (AgentContainer) getArguments()[1];
                                     init(initMessage);
                                     if (!AgentState.LOOP_EMIT.equals(agentState))
                                         throw new IllegalStateException("Should be in LOOP_EMIT state");
                                     try {
                                         stepEmit();
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                     }
                                 } else {
                                     if (AgentState.LOOP_EMIT.equals(agentState))
                                         try {
                                             stepEmit();
                                         } catch (IOException e) {
                                             e.printStackTrace();
                                         }
                                 }
                             }
                             try {
                                 end();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         }

                         @Override
                         public boolean done() {
                             return AgentState.END.equals(agentState);
                         }

                         private void end() throws IOException {
                             printWorld(agentData);
                             bufferedWriter.close();
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
                                 maxIterations = Integer.parseInt(splitted[0]);
                                 registeredAgents = getRandomAgents(splitted);
                                 StringJoiner sj = new StringJoiner(";");
                                 sj.add("Iteration");
                                 for (int i = 1; i <= registeredAgents.size(); i++)
                                     sj.add("Agent " + i);
                                 bufferedWriter.append(sj.toString());
                                 bufferedWriter.newLine();
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
                             agentData = Collections.unmodifiableList(agentData);
                         }

                         private void stepEmit() throws IOException {
                             final AID director = getAID();
                             collectMessages = 0;
                             if (curIterationCount >= maxIterations) {
                                 agentState = AgentState.END;
                             } else {
                                 List<AgentData> newAgentData = new ArrayList<>(agentData.size());
                                 printWorld(agentData);
                                 for (T agent : registeredAgents)
                                     newAgentData.add(agent.apply(agentData));

                                 //The last thing: Increase iteration count
                                 curIterationCount++;
                                 agentData = Collections.unmodifiableList(newAgentData);
                                 agentState = AgentState.LOOP_EMIT;

                             }
                         }

                         private void printWorld(List<AgentData> world) throws IOException {
                             StringJoiner stringJoiner = new StringJoiner(";");
                             stringJoiner.add(Integer.toString(curIterationCount));
                             for (AgentData ad : world)
                                 stringJoiner.add(ad.toString());
                             final String csvLine = stringJoiner.toString();
                             bufferedWriter.write(csvLine);
                             bufferedWriter.newLine();
                             System.out.format("%n%n%n === World at #%d of %d iterations ===%n%n%s%n%n          === End ===%n%n%n", curIterationCount, maxIterations, csvLine);
                         }
                     }
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

    private List<T> getRandomAgents(String[] config) throws IllegalAccessException, InstantiationException, StaleProxyException {
        if (config.length < 2)
            throw new IllegalStateException("Not applicable");
        @SuppressWarnings("unchecked")
        List<T> agents = new ArrayList<>();
        Class<? extends T> aClass;
        for (int i = 1; i < config.length; i++) {
            int numberOfInstances = Integer.parseInt(config[i]);
            aClass = classes.get(i - 1);
            for (int j = 0; j < numberOfInstances; j++) {
                T cur = aClass.newInstance();
                agentController.acceptNewAgent(aClass.getName() + '_' + i + '-' + j, cur);
                agents.add(cur);
            }
        }
        return agents;
    }
}
