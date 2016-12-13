package at.reisisoft.SoS.icecream;

import at.reisisoft.SoS.AbstractDirectorAgent;

import java.util.Arrays;

/**
 * Created by Florian on 12.12.2016.
 */
public class IceCreamDirectorAgent extends AbstractDirectorAgent<Double, IceCreamAgent> {
    public IceCreamDirectorAgent() throws NoSuchMethodException {
        super(
                Arrays.asList(
                        IceCreamAgent.class,
                        JumpingIceCreamAgent.class
                )
        );
    }
}
