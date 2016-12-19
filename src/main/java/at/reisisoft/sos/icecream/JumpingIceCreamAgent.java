package at.reisisoft.sos.icecream;

import java.util.Arrays;

/**
 * Created by Florian on 19.12.2016.
 */
public class JumpingIceCreamAgent extends IceCreamAgent {
    @Override
    protected synchronized double calculateNext(double self, double[] world) {
        final int maxIndex = world.length - 2;
        Arrays.sort(world);
        double maxDiff = Double.MIN_VALUE;
        double curDiff;
        int maxDiffIndex = -1;
        for (int i = 0; i <= maxIndex; i++) {
            curDiff = world[i + 1] - world[i];
            if (curDiff > maxDiff) {
                maxDiff = curDiff;
                maxDiffIndex = i;
            }
        }
        double nextPos;
        double randCheckWorldBorders = Math.random();
        if (randCheckWorldBorders > 0.75) {
            double lowestWorld = world[0], maximumWorld = 1000 - world[world.length - 1];
            if (Math.max(lowestWorld, maximumWorld) > maxDiff) {

                if (lowestWorld > maximumWorld) {
                    nextPos = world[0] / 2;
                } else
                    nextPos = world[world.length - 1] + (1000 - world[world.length - 1]) / 2;

            } else
                nextPos = world[maxDiffIndex] + (world[maxDiffIndex + 1] - world[maxDiffIndex]) / 2;
        } else nextPos = world[maxDiffIndex] + (world[maxDiffIndex + 1] - world[maxDiffIndex]) / 2;
        double variable = 1 + (Math.random() - 0.5) / 5d;
        double nextWithRand = nextPos * variable;
        if (0 <= nextWithRand && nextWithRand <= 1000)
            return nextWithRand;
        return nextPos;
    }
}
