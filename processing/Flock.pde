class Flock {
  ArrayList<Agent> agents;

  Flock() {
    agents = new ArrayList<Agent>();
  }

  void add(Agent a) {
    agents.add(a);
  }

  void update() {
    for (Agent a : agents) {
      a.update(agents);
    }
  }


  PVector calculateGroupCentroid() {
    float sumCosX = 0;
    float sumSinX = 0;
    float sumCosY = 0;
    float sumSinY = 0;

    for (Agent a : agents) {
      float angleX = (a.pos.x / width) * TWO_PI;
      float angleY = (a.pos.y / height) * TWO_PI;

      sumCosX += cos(angleX);
      sumSinX += sin(angleX);
      sumCosY += cos(angleY);
      sumSinY += sin(angleY);
    }

    float meanAngleX = atan2(sumSinX, sumCosX);
    float meanAngleY = atan2(sumSinY, sumCosY);

    // convert back to torus coordinates
    float cx = (meanAngleX / TWO_PI) * width;
    float cy = (meanAngleY / TWO_PI) * height;

    // keep inside bounds
    if (cx < 0) cx += width;
    if (cx > width) cx -= width;
    if (cy < 0) cy += height;
    if (cy > height) cy -= height;

    return new PVector(cx, cy);
 }

//  // calculate bounding box around agents along group direction
//   PVector computeBoundingBox(PVector groupDirection) {

//     float maxAlong = -Float.MAX_VALUE; // initialize to very small value
//     float minAlong = Float.MAX_VALUE;  // initialize to very large value
//     float maxPerpendicular = -Float.MAX_VALUE;
//     float minPerpendicular = Float.MAX_VALUE;
    
//     // project each agent's position onto the group direction vector
//     for (Agent a : agents) {
//       PVector toAgent = PVector.sub(a.pos, new PVector(0, 0)); // vector from origin to agent
//       float along = toAgent.dot(groupDirection);
//       float perpendicular = toAgent.cross(groupDirection).z; // 2D cross product

//       if (along > maxAlong) maxAlong = along;
//       if (along < minAlong) minAlong = along;
//       if (perpendicular > maxPerpendicular) maxPerpendicular = perpendicular;
//       if (perpendicular < minPerpendicular) minPerpendicular = perpendicular;
//     }

//     // return bounding box dimensions
//     return new PVector(maxAlong - minAlong, maxPerpendicular - minPerpendicular);

//   }
// compute minimum-image displacement on a torus

PVector toroidalDisplacement(PVector a, PVector b, float worldWidth, float worldHeight) {
  float dx = a.x - b.x;
  float dy = a.y - b.y;

  // wrap x
  if (dx >  worldWidth/2)  dx -= worldWidth;
  if (dx < -worldWidth/2)  dx += worldWidth;

  // wrap y
  if (dy >  worldHeight/2) dy -= worldHeight;
  if (dy < -worldHeight/2) dy += worldHeight;

  return new PVector(dx, dy);
}


// calculate bounding box along and perpendicular to group direction (toroidal)
PVector computeBoundingBox(PVector groupDirection, PVector centroid2) {

  // normalize to avoid projection distortion
  PVector dir = groupDirection.copy().normalize();
  PVector perp = new PVector(-dir.y, dir.x); // perpendicular direction

  // 1. Compute toroidal flock centroid (better reference point than origin)
  PVector center = centroid2;

  // 2. Find min/max extents in along/perp coordinates
  float maxAlong = -Float.MAX_VALUE;
  float minAlong =  Float.MAX_VALUE;
  float maxPerp  = -Float.MAX_VALUE;
  float minPerp  =  Float.MAX_VALUE;

  for (Agent a : agents) {
    // compute toroidal displacement from center → agent
    PVector d = toroidalDisplacement(a.pos, center, width, height);

    // project onto directions
    float along = d.dot(dir);
    float perpendicular = d.dot(perp);

    if (along > maxAlong) maxAlong = along;
    if (along < minAlong) minAlong = along;
    if (perpendicular > maxPerp) maxPerp = perpendicular;
    if (perpendicular < minPerp) minPerp = perpendicular;
  }

  return new PVector(maxAlong - minAlong, maxPerp - minPerp);
}


  void display() {
    for (Agent a : agents) {
      a.display();
    }
  }
}
