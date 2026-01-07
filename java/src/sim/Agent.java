package sim;

import java.util.List;
import java.util.Random;

/**
 * Agent for Couzin-style flocking (plain Java version of your Processing Agent).
 * Display / color code removed. Use update(others, width, height, rng).
 */
public class Agent {
    public Vec2 pos;       // position
    public Vec2 vel;       // direction * speed
    public int informed;   // 0 = naive, 1/2 = informed types
    public Vec2 g;         // preference vector (unit vector), zero for naive

    public double speed = 1.0;
    public double maxTurn = 0.3;   // maximum angle per step (radians)
    // feedback weights not used currently
    public double weight_inc = 0.008;
    public double weight_dec = 0.0006;
    public boolean useFeedback;

    // Zone radii
    public double R_rep = 2*12;   // zone of repulsion
    public double R_ori = 2.5*92;   // zone of orientation + attraction
    public double w = 0.35;     // weight for preference vector (was 0.20 before))
    public double w_max = 0.45; // maximum weight for preference vector
 
    //public double noise = 0.0; // angular noise standard deviation


    public Agent(
        double x, 
        double y, 
        int informed, 
        Vec2 g,
        boolean useFeedback) 
        
        {
        this.pos = new Vec2(x, y);
        double angle = Math.random() * 2 * Math.PI;
        this.vel = Vec2.fromAngle(angle);
        this.informed = informed;
        if (informed != 0 && g != null) {
            this.g = g.copy().normalize();
        } else {
            this.g = new Vec2(0, 0); // no preference
        }

        this.useFeedback = useFeedback;
        if (useFeedback) {
            w = 0.10; // lower initial weight
        }
    }

    /**
     * Update the agent for one timestep.
     * @param others list of all agents in the flock (including this one)
     * @param width toroidal width of simulation
     * @param height toroidal height of simulation
     * @param rng Random instance (used for gaussian noise)
     */
    public void update(List<Agent> others, double width, double height, Random rng) {
        // 1. REPULSION
        Vec2 repulsion = new Vec2(0, 0);
        boolean hasRepulsion = false;

        // 2. SOCIAL ZONE: ORIENTATION + ATTRACTION
        Vec2 orientation = new Vec2(0, 0);
        Vec2 attraction = new Vec2(0, 0);
        int socialCount = 0;

        for (Agent o : others) {
            double d = periodicDist(this.pos, o.pos, width, height);

            if (o != this && d < R_rep) {
                // vector away from neighbor (periodic)
                Vec2 away = periodicVector(this.pos, o.pos, width, height).mult(-1).normalize();
                repulsion.add(away);
                hasRepulsion = true;
            } else if (d < R_ori) {
                // attraction: unit vector toward neighbor (skip self)
                if (o != this) {
                    Vec2 toNbr = periodicVector(this.pos, o.pos, width, height).copy().normalize();
                    attraction.add(toNbr);
                }

                // orientation: add neighbor heading (unit) - includes self
                Vec2 oDir = o.vel.copy().normalize();
                orientation.add(oDir);

                socialCount++;
            }
        }

        // 3. SOCIAL DECISION (before preference)
        Vec2 desired;
        if (hasRepulsion) {
            desired = repulsion.copy().normalize();
        } else if (socialCount > 0) {
            Vec2 att = attraction.copy().normalize();
            Vec2 ori = orientation.copy().normalize();

            desired = new Vec2(0, 0);
            desired.add(att.mult(1.0));  // attraction no weighting
            desired.add(ori.mult(1.0));  // orientation no weighting
            desired.normalize();
        } else {
            desired = this.vel.copy().normalize();
        }

        // 4. ADD PREFERENCE (informed individuals only)
        if (this.informed != 0) {

            Vec2 pref = this.g.copy();

            // use feedback to adjust weight
            if (useFeedback) {
                double angleBetween = angleBetweenVectors(this.vel, pref);
                // compute alignment with group direction and increase/decrease weight w if
                // aligned/misaligned (threshold is 10 degrees )
                if (Math.abs(angleBetween) < 0.17 && w < w_max ) {           // 10 degrees = 0.17 rad
                    this.w += weight_inc;
                } else if(w > 0.0) { // only decrease if above zero
                    this.w -= weight_dec;
                }

            }
            
            pref.mult(this.w); // weight preference vector multiplied by w

            desired.add(pref);
            desired.normalize();
        }

        // 5. MAX TURNING RATE
        double angleBetween = angleBetweenVectors(this.vel, desired);
        double sign = turnSign(this.vel, desired);
        if (Math.abs(angleBetween) > maxTurn) {
            this.vel.rotate(sign * maxTurn);
        } else {
            this.vel = desired.copy();
        }

        // 6. ADD ANGULAR NOISE
        //double noiseAngle = rng.nextGaussian() * noise;
        //this.vel.rotate(noiseAngle);

        this.vel.normalize();
        this.vel.mult(speed);

        // 7. UPDATE POSITION + WRAP
        this.pos.add(this.vel);
        wrap(width, height); 
    
    }

    // ---------------- helper math & geometry ----------------

    private double turnSign(Vec2 a, Vec2 b) {
        // sign of cross product a x b
        double cross = a.x * b.y - a.y * b.x;
        return cross > 0 ? 1.0 : -1.0;
    }

    private double angleBetweenVectors(Vec2 a, Vec2 b) {
        double adot = a.copy().normalize().dot(b.copy().normalize());
        adot = clamp(adot, -1.0, 1.0);
        return Math.acos(adot);
    }

    private double clamp(double v, double lo, double hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    private double periodicDist(Vec2 a, Vec2 b, double width, double height) {
        double dx = Math.abs(a.x - b.x);
        double dy = Math.abs(a.y - b.y);
        if (dx > width / 2.0) dx = width - dx;
        if (dy > height / 2.0) dy = height - dy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private Vec2 periodicVector(Vec2 from, Vec2 to, double width, double height) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        if (Math.abs(dx) > width / 2.0) dx = dx > 0 ? dx - width : dx + width;
        if (Math.abs(dy) > height / 2.0) dy = dy > 0 ? dy - height : dy + height;
        return new Vec2(dx, dy);
    }

    private void wrap(double width, double height) {
        if (pos.x < 0) pos.x += width;
        if (pos.x >= width) pos.x -= width;
        if (pos.y < 0) pos.y += height;
        if (pos.y >= height) pos.y -= height;
    }

    // ---------------- small 2D vector helper class ----------------
    public static class Vec2 {
        public double x, y;

        public Vec2(double x, double y) { 
            this.x = x; 
            this.y = y; 
        }

        public Vec2 copy() { 
            return new Vec2(x, y); 
        }

        public Vec2 add(Vec2 other) { 
            this.x += other.x; 
            this.y += other.y; 
            return this; 
        }
        public Vec2 sub(Vec2 other) {
            this.x -= other.x; 
            this.y -= other.y; 
            return this; }

        // scalar multiplication
        public Vec2 mult(double s) { 
            this.x *= s; 
            this.y *= s; 
            return this; 
        }

        public Vec2 normalize() {
            double len = length(); 
            if (len == 0) return this;
            this.x /= len; 
            this.y /= len; 
            return this;
        }

        // rotate vector by angle (radians)
        public Vec2 rotate(double ang) {
            double ca = Math.cos(ang), sa = Math.sin(ang);
            double nx = ca * x - sa * y;
            double ny = sa * x + ca * y;
            this.x = nx; this.y = ny; 
            return this;
        }

        public double length() { 
            return Math.hypot(x, y);
        }

        // dot product between this and other
        public double dot(Vec2 other) { 
            return this.x * other.x + this.y * other.y; 
        }
        // create unit vector from angle
        public static Vec2 fromAngle(double ang) {
            return new Vec2(Math.cos(ang), Math.sin(ang)); 
        }

        // angle of 2D vector to x-axis
        public double angle() { 
            return Math.atan2(y, x); 
        }
    }
}
