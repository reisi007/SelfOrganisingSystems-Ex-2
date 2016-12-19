package at.reisisoft.sos;

import at.reisisoft.sos.directormessage.DirectorInit;
import at.reisisoft.sos.icecream.IceCreamAgent;
import at.reisisoft.sos.icecream.IceCreamDirectorAgent;
import at.reisisoft.sos.icecream.JumpingIceCreamAgent;

import java.io.File;

/**
 * Created by Florian on 19.12.2016.
 */
public class IceMain {

    public static void main(String[] args) {
        DirectorInit initMessage = new DirectorInit(
                new File("D:\\Desktop\\sos.csv"),
                500
        );

        initMessage.addData(IceCreamAgent.class, 8);
        initMessage.addData(JumpingIceCreamAgent.class, 2);

        MyAkkaMain.startActorSystem(IceCreamDirectorAgent.class, initMessage);
    }
}