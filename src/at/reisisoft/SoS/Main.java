package at.reisisoft.SoS;

import at.reisisoft.SoS.icecream.IceCreamDirectorAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;

/**
 * Created by Florian on 13.12.2016.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        start(
                IceCreamDirectorAgent.class,
                3,
                3
        );
    }

    private static void start(Class<? extends AbstractDirectorAgent<?, ?>> class1, int instances, int cycles) throws Exception {
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();

// Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");

// Create a default profile
        Profile profile = new ProfileImpl(null, 1200, null);
        System.out.print("profile created\n");

        System.out.println("Launching a whole in-process platform..." + profile);
        jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(profile);

// now set the default Profile to start a container
        ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
        System.out.println("Launching the agent container ..." + pContainer);

        jade.wrapper.AgentContainer cont = rt.createAgentContainer(pContainer);
        System.out.println("Launching the agent container after ..." + pContainer);

        System.out.println("containers created");
        System.out.println("Launching the rma agent on the main container ...");
        AgentController rma = mainContainer.createNewAgent("rma",
                "jade.tools.rma.rma", new Object[0]);
        rma.start();
        String message = instances + "," + cycles;
        AgentController director = mainContainer.createNewAgent("director", class1.getName(), new Object[]{message, mainContainer});
        director.start();
    }
}
