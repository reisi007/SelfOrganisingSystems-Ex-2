package at.reisisoft.sos.directormessage;

import akka.actor.UntypedActor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Florian on 19.12.2016.
 */
public class DirectorInit implements DirectorMessageResponseMarker {
    private final Map<Class<? extends UntypedActor>, Integer> data = new HashMap<>();
    private final File outFile;
    private final int maxIterations;

    public DirectorInit(File outFile, int maxIterations) {
        this.outFile = outFile;
        this.maxIterations = maxIterations;
    }

    public void addData(Class<? extends UntypedActor> actor, int count) {
        Integer oldVal = data.put(actor, count);
        if (oldVal != null) {
            data.replace(actor, count + oldVal);
        }
    }

    public Stream<Map.Entry<Class<? extends UntypedActor>, Integer>> stream() {
        return data.entrySet().stream();
    }

    public Set<Map.Entry<Class<? extends UntypedActor>, Integer>> getEntrySet() {
        return data.entrySet();
    }

    public File getOutFile() {
        return outFile;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
}
