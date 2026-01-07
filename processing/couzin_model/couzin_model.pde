Flock flock;

// Parameters
int N = 50;              // total number of agents
int n1 = 10;              // number of informed agents with preference g1
int n2 = 0;              // number of informed agents with preference g2

PVector g1, g2;           // preference vectors
float angle1 = 0;         // 0°
float angle2 = radians(90); // e.g. 60° difference

boolean displayInfo = true;
int runTime = 1000; // iterations to run
int iterations = 0;

int initialBoxSize = 50; // size of initial spawning box

PVector centroid1;
PVector centroid2;
PVector groupDirection;
PVector boundingBox;
float elongationRatio;
boolean displayDirection = false;
boolean displayBoundingBox = false;

void setup() {
  size(1400, 1000);
  flock = new Flock();

  // define preference vectors
  g1 = PVector.fromAngle(angle1).normalize();
  g2 = PVector.fromAngle(angle2).normalize();

  // create agents
  for (int i = 0; i < N; i++) {
    int informed = 0;
    PVector g = new PVector(0, 0);

    if (i < n1) { // make first n1 agents informed with g1
      informed = 1;
      g = g1.copy();
    }
    else if (i < n1 + n2) { // next n2 agents informed with g2
      informed = 2;
      g = g2.copy();
    }

    float x = random(width/2 - initialBoxSize/2, width/2 + initialBoxSize/2);
    float y = random(height/2 - initialBoxSize/2, height/2 + initialBoxSize/2);

    Agent a = new Agent(x, y, informed, g);
    flock.add(a);
  }
}

void draw() {
  if (iterations >= runTime) {
    noLoop();
  } else if (frameCount == runTime - 250) {
    // store first centroid
    centroid1 = flock.calculateGroupCentroid();
    iterations++;
  } else if (frameCount == runTime-1) {
    // store second centroid and compute group direction
    centroid2 = flock.calculateGroupCentroid();
    groupDirection = computeGroupDirection(centroid1, centroid2);
    displayDirection = true;
    // compute bounding box 
    boundingBox = flock.computeBoundingBox(groupDirection, centroid2); // use second centroid as reference point
    displayBoundingBox = true;
    elongationRatio = boundingBox.x / boundingBox.y;
    iterations++;
    // print weights for debugging
    flock.printWeights();
  } else{
    iterations++;
  }
  background(30);

  flock.update();
  flock.display();


  displayInfo();
}

void displayInfo() {
  if (!displayInfo) return;

  fill(255);
  textSize(14);
  text("N = " + N, 10, 20);
  fill(242, 140, 40);
  text("g1-angle = " + degrees(angle1) + "° | n1 = " + n1, 10, 40);
  fill(58, 110, 165);
  text("g2-angle = " + degrees(angle2) + "° | n2 = " + n2, 10, 60);
  // show iteration count
  fill(255);
  text("Iteration: " + iterations + " / " + runTime, 10, 80);

  // display g1 and g2 vectors
    pushMatrix();
    translate(width - 80, 30);
    stroke(242, 140, 40);
    strokeWeight(2);
    line(0, 0, g1.x * 50, g1.y * 50);
    fill(242, 140, 40);
    text("g1", g1.x * 50 + 5, g1.y * 50 + 5);
    popMatrix();
    pushMatrix();
    translate(width - 80, 30);
    stroke(58, 110, 165);
    strokeWeight(2);
    line(0, 0, g2.x * 50, g2.y * 50);
    fill(58, 110, 165);
    text("g2", g2.x * 50 + 5, g2.y * 50 + 5);
    popMatrix();

  // display group direction
  if (displayDirection) {
    pushMatrix();
    translate(width/2, height/2);
    stroke(242, 140, 40);
    strokeWeight(4);
    line(0, 0, groupDirection.x * 100, groupDirection.y * 100);
    // arrowhead
    strokeWeight(6);
    line(groupDirection.x * 100, groupDirection.y * 100, groupDirection.x * 90 - groupDirection.y * 10, groupDirection.y * 90 + groupDirection.x * 10);
    line(groupDirection.x * 100, groupDirection.y * 100, groupDirection.x * 90 + groupDirection.y * 10, groupDirection.y * 90 - groupDirection.x * 10);
    fill(242, 140, 40);
    // make text size larger
    textSize(16);
    text("Group Direction", groupDirection.x * 50 + 50, groupDirection.y * 50 + 5);
    popMatrix();
  }

  // display bounding box
  if (displayBoundingBox) {
    pushMatrix();
    translate(centroid2.x, centroid2.y); // center the box for now
    rotate(atan2(groupDirection.y, groupDirection.x));
    strokeWeight(2);
    stroke(255, 0, 255);
    noFill();
    rectMode(CENTER);
    rect(0, 0, boundingBox.x, boundingBox.y);
    fill(255, 0, 255);
    text("Bounding Box", boundingBox.x/2 + 10, boundingBox.y/2 + 10);
    popMatrix();
    fill(255); // white for text
    text("Elongation Ratio: " + nf(elongationRatio, 1, 2), 10, 100);
  }
    

}

// Compute group direction based on movement of centroids
PVector computeGroupDirection(PVector centroid1, PVector centroid2) {
    PVector diff = periodicVector(centroid1, centroid2).normalize();
    groupDirection = diff.normalize();
    return groupDirection;
}

// periodic vector (for toroidal space)
PVector periodicVector(PVector from, PVector to) {
    float dx = to.x - from.x;
    float dy = to.y - from.y;

    if (abs(dx) > width/2)  dx = dx > 0 ? dx - width : dx + width;
    if (abs(dy) > height/2) dy = dy > 0 ? dy - height : dy + height;

    return new PVector(dx, dy);
  }

// Press 'i' to toggle info display
void keyPressed() {
    if (key == 'i' || key == 'I') {
        displayInfo = !displayInfo;
        }
}