package at.reisisoft.SoS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Florian on 12.12.2016.
 */
public abstract class AbstractDirectorAgent<AgentData extends Serializable, T extends AbstractAgent<AgentData>> extends Agent implements Function<String, AgentData> {

    private enum AgentState {
        INIT,
        LOOP_EMIT,
        LOOP_COLLECT,
        END
    }

    private final List<Class<? extends T>> classes;
    private String initMessage;
    private final BufferedWriter bufferedWriter;


    public AbstractDirectorAgent(List<Class<? extends T>> classes) throws IOException {
        this.classes = classes;
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\Desktop\\sos.csv", false)));
    }


    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
                         private AgentState agentState = AgentState.INIT;
                         private List<T> registeredAgents;
                         private List<AID> aidsInAgentOrder;
                         private int curIterationCount = 0, maxIterations = -1;
                         private AgentData[] agentData;
                         //How many collect messages got received
                         private int collectMessages = 0;

                         @Override
                         public void action() {
                             //Wait for an init message
                             if (!AgentState.END.equals(agentState)) {
                                 if (AgentState.INIT.equals(agentState) && ((getArguments().length >= 1) && (initMessage = (String) getArguments()[0]) != null)) {
                                     init(initMessage);
                                     if (!AgentState.LOOP_EMIT.equals(agentState))
                                         throw new IllegalStateException("Should be in LOOP_EMIT state");
                                     try {
                                         stepEmit();
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                     }
                                 } else {
                                     ACLMessage message = receive();
                                     if (message == null)
                                         block();
                                     else {
                                         if (AgentState.INIT.equals(agentState))
                                             init(message.getContent());
                                         if (AgentState.LOOP_EMIT.equals(agentState))
                                             try {
                                                 stepEmit();
                                             } catch (IOException e) {
                                                 e.printStackTrace();
                                             }
                                         else if (AgentState.LOOP_COLLECT.equals(agentState)) {
                                             stepCollect(apply(message.getContent()), message.getSender());
                                         }
                                     }
                                 }
                             }
                             if (AgentState.END.equals(agentState))
                                 try {
                                     end();
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
                             else
                                 block();
                         }

                         private void end() throws IOException {
                             printWorld(agentData);
                             bufferedWriter.close();
                             System.out.printf("%n%n%n == End of simulation ==");
                         }

                         private void stepCollect(AgentData cur, AID current) {
                             if (collectMessages <= 0) {
                                 collectMessages = 0;
                                 agentData = (AgentData[]) new Serializable[registeredAgents.size()];
                             }
                             agentData[getAidIndex(current)] = cur;
                             collectMessages++;
                             if (collectMessages >= registeredAgents.size())
                                 agentState = AgentState.LOOP_COLLECT;
                         }

                         private int getAidIndex(AID current) {
                             return aidsInAgentOrder.indexOf(current);
                         }

                         private void init(String rawContent) {
                             try {
                                 final String[] splitted = rawContent.split(",");
                                 maxIterations = Integer.parseInt(splitted[0]);
                                 registeredAgents = getRandomAgents(splitted);
                                 aidsInAgentOrder = registeredAgents.stream().sequential().map(Agent::getAID).collect(Collectors.toList());
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

                             agentData = (AgentData[]) new Serializable[registeredAgents.size()];
                             //Setup world
                             for (int i = 0; i < registeredAgents.size(); i++) {
                                 T cur = registeredAgents.get(i);
                                 agentData[i] = cur.getData();
                             }
                             agentState = AgentState.LOOP_EMIT;
                         }

                         private void stepEmit() throws IOException {
                             final AID director = getAID();
                             collectMessages = 0;
                             if (curIterationCount >= maxIterations) {
                                 agentState = AgentState.END;
                             } else {
                                 printWorld(agentData);
                                 String content = at.reisisoft.SoS.Gson.getInstance().toJson(agentData);
                                 ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                                 message.addReplyTo(director);
                                 //  message.setOntology(BasicOntology.getInstance().getName());
                                 message.setContent(content);
                                 message.setSender(director);
                                 for (T agent : registeredAgents) {
                                     message.addReceiver(agent.getAID());
                                 }
                                 send(message);
                                 //The last thing: Increase iteration count
                                 curIterationCount++;
                                 agentState = AgentState.LOOP_COLLECT;

                             }
                         }

                         private void printWorld(AgentData[] world) throws IOException {
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
                getContainerController().acceptNewAgent(aClass.getName() + '_' + i + '-' + j, cur);
                agents.add(cur);
            }
        }
        return agents;
    }
}
