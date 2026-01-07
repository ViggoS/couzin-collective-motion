class Agent {
  PVector pos;       // position
  PVector vel;       // direction * speed
  int informed;  // whether the agent is informed
  PVector g;         // preference vector (unit vector)
  
  float speed = 2.0;
  float maxTurn = 0.4;   // maximum angle per step (radians)
  // No feedback weights at this stage
  float weight_inc = 0.0; //0.008; // weight increment for informed agents # 0.012
  float weight_dec = 0.0;//0.0006; // weight decrement for uninformed agents
  float w_max = 0.45; // maximum weight for preference vector

  // Zone radii
  float R_rep = 2*12;   // zone of repulsion (alpha in Couzin 2005) (*1.5)
  float R_ori = 2.5*92;   // zone of orientation + attraction (ro in Couzin 2005) (*2.5)
  float w = 0.25;      // weight for preference vector (omega in Couzin 2005) 0.12 is good for cohesion
  float noise = 0.0;  // noise standard deviation
  color col;
  color blue = color(58, 110, 165);
  color orange = color(242, 140, 40);
  color white = color(255);

  Agent(float x, float y, int informed, PVector g) {
    pos = new PVector(x, y);
    // random initial direction
    float angle = random(TWO_PI);
    vel = PVector.fromAngle(angle);
    this.informed = informed;

    if (informed != 0) {
      this.g = g.copy().normalize();
      if (informed == 1) {
        col = orange;  
      } else if (informed == 2) {
        col = blue;  
      }
    } else {
      // non-informed (or naive) agents have no preference
      this.g = new PVector(0, 0);
      col = white;  
    }
  }

  // -------------------------------------------------------
  // UPDATE
  // -------------------------------------------------------
  void update(ArrayList<Agent> others) {

    // 1. REPULSION (highest priority)
    PVector repulsion = new PVector();
    boolean hasRepulsion = false;

    // 2. SOCIAL ZONE: ORIENTING + ATTRACTION
    PVector orientation = new PVector();
    PVector attraction = new PVector();
    int socialCount = 0;

    for (Agent o : others) {
      if (o == this) continue;

      float d = periodicDist(pos, o.pos);

      // ---------------------------
      // REPULSION
      // ---------------------------
      if (d < R_rep) {
        // periodic vector to o, then reverse it
        PVector away = periodicVector(pos, o.pos);
        away.mult(-1);
        away.normalize();
        repulsion.add(away);
        hasRepulsion = true;
      }

      // ---------------------------
      // ORIENTATION + ATTRACTION
      // ---------------------------
      else if (d < R_ori) {
        // attraction: unit vector toward neighbor (periodic!)
        PVector toNbr = periodicVector(pos, o.pos);
        toNbr.normalize();
        attraction.add(toNbr);

        // orientation: align with neighbor's heading
        //PVector oDir = o.vel.copy().normalize();
        //orientation.add(oDir);

        socialCount++;
      }
    }

    for (Agent o : others) {
   
      // include self in orientation zone
      float d = periodicDist(pos, o.pos);

      // ---------------------------
      // ORIENTATION + ATTRACTION
      // ---------------------------
      if (d < R_ori) {

        PVector oDir = o.vel.copy().normalize();
        orientation.add(oDir);

        socialCount++;
      }
    }

    // -------------------------------------------------------
    // 3. SOCIAL DECISION (before preference)
    // -------------------------------------------------------
    PVector desired;

    if (hasRepulsion) {
      // repulsion overrides all social/goal cues
      desired = repulsion.copy();
    } 
    else if (socialCount > 0) {

      // IMPORTANT:
      // Normalize attraction and orientation separately.
      // This ensures they are unit sources, not dependent on neighbor count.
      PVector att = attraction.copy();
      PVector ori = orientation.copy();
      att.normalize();
      ori.normalize();

      // Couzin 2005: attraction tends to be slightly stronger in practice,
      // so we make attraction have a bit more influence.
      desired = new PVector();
      desired.add(PVector.mult(att, 1.0));  // slightly stronger pull inward
      desired.add(PVector.mult(ori, 1.0));  // orientation weaker
      desired.normalize();
    } 
    else {
      // no neighbors → continue current heading
      desired = vel.copy().normalize();
    }

    // -------------------------------------------------------
    // 4. ADD PREFERENCE (informed individuals only)
    // -------------------------------------------------------
    if (informed != 0) {
      
      // update weight w based on feedback mechanism (increase if moving toward goal, decrease otherwise, 20 degrees threshold)
      if (angleBetweenVectors(vel, g) < radians(10) && w < w_max) {
        w += weight_inc;
      } else if(w > 0){ 
        w -= weight_dec;
      }
      w = constrain(w, 0, w_max);

      // Combine desired direction with preference vector g
      // A small w ensures the flock stays coherent.
      PVector pref = PVector.mult(g, w);
      desired.add(pref);
      desired.normalize();
    }

    // -------------------------------------------------------
    // 5. MAX TURNING RATE
    // -------------------------------------------------------
    float angleBetween = angleBetweenVectors(vel, desired);
    float sign = turnSign(vel, desired);

    if (abs(angleBetween) > maxTurn) {
      vel.rotate(sign * maxTurn);
    } else {
      vel = desired.copy();
    }

    // -------------------------------------------------------
    // 6. ADD ANGULAR NOISE
    // -------------------------------------------------------
    float noiseAngle = randomGaussian() * noise;
    vel.rotate(noiseAngle);

    vel.normalize();
    vel.mult(speed);

    // -------------------------------------------------------
    // 7. UPDATE POSITION + WRAP
    // -------------------------------------------------------
    pos.add(vel);
    wrap();
  }


  // -------------------------------------------------------
  // DISPLAY
  // -------------------------------------------------------
  void display() {
    pushMatrix();
    translate(pos.x, pos.y);

    float angle = atan2(vel.y, vel.x);
    rotate(angle);

    // draw as a small triangle
    fill(col); 
    noStroke();
    beginShape();
    vertex(8, 0);
    vertex(-6, 4);
    vertex(-6, -4);
    endShape(CLOSE);

    popMatrix();
  }

  double getW() {
    if(informed != 0) {
      return w;
    } else {
      return 0;
    }
  }

  // -------------------------------------------------------
  // HELPER FUNCTIONS
  // -------------------------------------------------------

  // Calculates the sign of the cross product → indicates which way to turn
  float turnSign(PVector a, PVector b) {
    return (a.x * b.y - a.y * b.x) > 0 ? 1 : -1;
  }

  // Help function to calculate angle between two vectors
  float angleBetweenVectors(PVector a, PVector b) {
    float dot = a.copy().normalize().dot(b.copy().normalize());
    dot = constrain(dot, -1, 1);
    return acos(dot);
  }

  // periodic distance (for toroidal space)
  float periodicDist(PVector a, PVector b) {
    float dx = abs(a.x - b.x);
    float dy = abs(a.y - b.y);

    if (dx > width/2)  dx = width - dx;
    if (dy > height/2) dy = height - dy;

    return sqrt(dx*dx + dy*dy);
  }

  // periodic vector (for toroidal space)
  PVector periodicVector(PVector from, PVector to) {
    float dx = to.x - from.x;
    float dy = to.y - from.y;

    if (abs(dx) > width/2)  dx = dx > 0 ? dx - width : dx + width;
    if (abs(dy) > height/2) dy = dy > 0 ? dy - height : dy + height;

    return new PVector(dx, dy);
  }

  // screen-wrap
  void wrap() {
    if (pos.x < 0) pos.x += width;
    if (pos.x > width) pos.x -= width;
    if (pos.y < 0) pos.y += height;
    if (pos.y > height) pos.y -= height;
  }
}
