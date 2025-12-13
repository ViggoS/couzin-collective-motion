package sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Flock {

    private final List<Agent> agents;

    public Flock() {
        this.agents = new ArrayList<>();
    }

    public void add(Agent a) {
        agents.add(a);
    }

    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Update all agents one timestep.
     */
    public void update(double width, double height, Random rng) {
        for (Agent a : agents) {
            a.update(agents, width, height, rng);
        }
    }

    /**
     * Compute the centroid on a torus (mean circular coordinate mapping).
     * Equivalent to your Processing version.
     */
    public Agent.Vec2 calculateGroupCentroid(double width, double height) {
        double sumCosX = 0;
        double sumSinX = 0;
        double sumCosY = 0;
        double sumSinY = 0;

        for (Agent a : agents) {
            double angleX = (a.pos.x / width) * 2 * Math.PI;
            double angleY = (a.pos.y / height) * 2 * Math.PI;

            sumCosX += Math.cos(angleX);
            sumSinX += Math.sin(angleX);
            sumCosY += Math.cos(angleY);
            sumSinY += Math.sin(angleY);
        }

        double meanAngleX = Math.atan2(sumSinX, sumCosX);
        double meanAngleY = Math.atan2(sumSinY, sumCosY);

        // convert back to toroidal coordinates
        double cx = (meanAngleX / (2 * Math.PI)) * width;
        double cy = (meanAngleY / (2 * Math.PI)) * height;

        // wrap
        if (cx < 0) cx += width;
        if (cx >= width) cx -= width;
        if (cy < 0) cy += height;
        if (cy >= height) cy -= height;

        return new Agent.Vec2(cx, cy);
    }

    // compute minimal periodic displacement between two points on a torus
    public Agent.Vec2 periodicDist(Agent.Vec2 a, Agent.Vec2 b, double width, double height) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        if (dx > width / 2.0) dx -= width;
        if (dx < -width / 2.0) dx += width;
        if (dy > height / 2.0) dy -= height;
        if (dy < -height / 2.0) dy += height;
        return new Agent.Vec2(dx, dy);

    }

    // compute bounding box along and perpendicular to group direction
    public double[] computeBoundingBox(Agent.Vec2 groupDir, Agent.Vec2 groupCentroid, double width, double height) {
        double minAlong = Double.POSITIVE_INFINITY;
        double maxAlong = Double.NEGATIVE_INFINITY;
        double minPerp = Double.POSITIVE_INFINITY;
        double maxPerp = Double.NEGATIVE_INFINITY;  

        for (Agent a : agents) {


            Agent.Vec2 d = periodicDist(a.pos, groupCentroid, width, height);

            double along = (d.x * groupDir.x + d.y * groupDir.y); // projection along group direction
            double perp = (d.x * -groupDir.y + d.y * groupDir.x) ; // projection perpendicular to group direction  
        

            if (along < minAlong) minAlong = along;
            if (along > maxAlong) maxAlong = along;
            if (perp < minPerp) minPerp = perp;
            if (perp > maxPerp) maxPerp = perp;
        }

        return new double[]{maxAlong - minAlong, maxPerp - minPerp}; // return lengths (list of two doubles)

    }
    
}
