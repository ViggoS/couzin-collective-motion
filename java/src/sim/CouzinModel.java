package sim;

import java.util.Random;

public class CouzinModel {

    /** Simple container for simulation outputs. */
    public static class SimulationResult {
        public final Agent.Vec2 groupDirection;
        public final double[] boundingBox;

        public SimulationResult(Agent.Vec2 groupDirection, double[] boundingBox) {
            this.groupDirection = groupDirection;
            this.boundingBox = boundingBox;
        }
    }

    private Flock flock;

    // Parameters
    private int N = 100;        // total number of agents
    private int n1 = 0;        // number of informed agents (g1)
    private int n2 = 30;         // informed agents with g2

    private Agent.Vec2 g1, g2;     // preference vectors
    private double angle1 = 0;
    private double angle2 = Math.toRadians(90);

    private int runTime = 2000;
    private int width = 1400;
    private int height = 1000;

    private int initialBoxSize = 50;

    private Agent.Vec2 centroid1;
    private Agent.Vec2 centroid2;
    private Agent.Vec2 groupDirection;
    private double[] boundingBox; 

    private int timeToMeasure = 250;
    private boolean useFeedback = false;


    private final Random rng = new Random(); // for noise and randomness

    public CouzinModel(int N, int n1, int n2, double angle1, double angle2, int runTime, Random rng, boolean useFeedback) {
        

        this.N = N;
        this.n1 = n1;
        this.n2 = n2;
        this.angle1 = angle1;
        this.angle2 = angle2;

        this.g1 = Agent.Vec2.fromAngle(angle1).normalize();
        this.g2 = Agent.Vec2.fromAngle(angle2).normalize();
        this.useFeedback = useFeedback;
        
    }

    /** Runs one full simulation and returns the resulting group direction */
    public SimulationResult runSimulation() {
        flock = new Flock(); // create new flock

        // Spawn agents
        for (int i = 0; i < N; i++) {
            int informed = 0;
            Agent.Vec2 g = new Agent.Vec2(0, 0);

            if (i < n1) {
                informed = 1;
                g = g1.copy();
            } else if (i < n1 + n2) {
                informed = 2;
                g = g2.copy();
            }

            // Random position in initial box
            double x = randInRange(width / 2 - initialBoxSize / 2,
                                   width / 2 + initialBoxSize / 2);
            double y = randInRange(height / 2 - initialBoxSize / 2,
                                   height / 2 + initialBoxSize / 2);

            Agent a = new Agent(x, y, informed, g, useFeedback);
            flock.add(a);
        }

        // Iterate simulation
        for (int t = 0; t < runTime; t++) {

            if (t == runTime - timeToMeasure - 1) {
                centroid1 = flock.calculateGroupCentroid(width, height);
            }
            if (t == runTime - 1) {
                centroid2 = flock.calculateGroupCentroid(width, height);
            }

            flock.update(width, height, rng);
        }

        groupDirection = computeGroupDirection(centroid1, centroid2);
        boundingBox = flock.computeBoundingBox(groupDirection, centroid2, width, height);
        return new SimulationResult(groupDirection, boundingBox);
    }

    // --- Utility methods ---

    // random double in [a, b)
    private double randInRange(double a, double b) {
        return a + rng.nextDouble() * (b - a);
    }

    // --- Compute group direction based on centroid displacement ---
    private Agent.Vec2 computeGroupDirection(Agent.Vec2 c1, Agent.Vec2 c2) {
        Agent.Vec2 diff = periodicVector(c1, c2).normalize(); 
        return diff;
    }
    

    // --- periodic vector on a torus ---
    private Agent.Vec2 periodicVector(Agent.Vec2 from, Agent.Vec2 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;

        if (Math.abs(dx) > width / 2) dx = dx > 0 ? dx - width : dx + width;
        if (Math.abs(dy) > height / 2) dy = dy > 0 ? dy - height : dy + height;

        return new Agent.Vec2(dx, dy);
    }

    public Agent.Vec2 getGroupDirection() {
        return groupDirection;
    }
}
