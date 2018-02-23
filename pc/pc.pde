//network stuff
import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;
int port=12000;
String remoteip="192.168.1.116";

/********* VARIABLES *********/

float frict=0.01;
float batfrict=0.05;
float grav;
float inelast=0.6;
float spring=2;
float mouseattraction;//200;

boolean moveUp,moveDown,moveLeft,moveRight,setMovemouse=false;
int moveStep=1;
int gameScreen = 1;

Ball[] balls;

int numberofballs=100;

float amp=20;

PVector mouselast=new PVector(0,0);

int value=0;

void setup() {
  size(540,960);
  frameRate(60);
  
  balls=new Ball[numberofballs];
  balls[0]=new Ball(width/2,height/2,-10,0,100,true,0,0);
  for(int i=1;i<balls.length;i++){
    balls[i]=new Ball(random(width*0.1,width*0.9),random(height*0.1,height*0.9),random(-amp,amp),random(-amp,amp),random(5,20),false,i,0);
  }
  
  oscP5 = new OscP5(this,port);
  myRemoteLocation = new NetAddress(remoteip,port);
}


void draw() {
  background(value);
  for (int j=0;j<balls.length;j++) {
    balls[j].display();
  }
}

/* incoming osc message are forwarded to the oscEvent method. */
void oscEvent(OscMessage theOscMessage) {
  switch(theOscMessage.addrPattern()) {
    case "/background":
      value=theOscMessage.get(0).intValue();
      //println("background: ",value);
      break;
  }
  for (int i=0;i<balls.length;i++) { //balls.length
    if(theOscMessage.addrPattern().equals("/ball/"+str(i)+"/position")) {
      balls[i].position.x=theOscMessage.get(0).floatValue()/2;
      balls[i].position.y=theOscMessage.get(1).floatValue()/2;
      //println("position: ",balls[i].position.x,balls[i].position.y);
    }
    if(theOscMessage.addrPattern().equals("/ball/"+str(i)+"/attributes")) {
      balls[i].radius=theOscMessage.get(0).floatValue()/2;
      //println("radius: ",balls[i].radius," m: ",balls[i].m);
    }
  }
}


/********* CLASSES *********/

class Ball {
  PVector position;
  PVector velocity;
  PVector correctvelocity;
  PVector lastposition;
  
  int playerScreen;
  
  float radius,m;
  
  color ballcolor;
  int ballnumber;
  boolean controlled;
  
  Ball(float x, float y, float xvel, float yvel, float r_,boolean controlled_,int ballnumber_,int playerScreen_) {
    position = new PVector(x,y);
    velocity = new PVector(xvel,yvel);
    radius=r_;
    m=radius*0.1;
    ballcolor=color(random(255),random(255),random(255));
    controlled=controlled_;
    ballnumber=ballnumber_; 
    playerScreen=playerScreen_;
  }
  
  void update() {
      
    PVector forces = new PVector(0,grav);
    if (mousePressed) {
      //println("pressed");
      PVector mousepos=new PVector(mouseX,mouseY);
      PVector distanceV=PVector.sub(mousepos,position);
      float dist=distanceV.mag();
      forces.add(PVector.mult(distanceV,mouseattraction/pow(dist,2)));
    }
    velocity=PVector.add(PVector.mult(velocity,(1-frict)),forces);
    velocity.add(PVector.mult(correctvelocity,inelast));
    
    position.add(velocity);
    
    if (controlled) {
      if (mousePressed) {
        position=new PVector(mouseX,mouseY);
        velocity=PVector.sub(position,mouselast);
        mouselast=new PVector(mouseX,mouseY);
      }
      
      /*if (moveUp) {
        velocity.y-=moveStep;
      } if (moveDown) {
        velocity.y+=moveStep;
      } if (moveLeft){
        velocity.x-=moveStep;
      } if (moveRight){
        velocity.x+=moveStep;
      }*/
    }    
  }
  
  void display() {
    fill(ballcolor);
    ellipse(position.x,position.y,radius*2,radius*2);
  }
  
  void checkBoundaryCollision() {
    if (position.x+radius+velocity.x>width) {
      position.x=width-radius;
      velocity.x*=-inelast;
    } if (position.x-radius+velocity.x<0) {
      position.x=radius;
      velocity.x*=-inelast;
    } if (position.y+radius+velocity.y>height) {
      position.y=height-radius;
      velocity.y*=-inelast;
    } if (position.y-radius+velocity.y<0) {
      position.y=radius;
      velocity.y*=-inelast;
    }
  }
  
  void checkBallCollision() {
    correctvelocity=new PVector(0,0);
    PVector distanceV;
    float dist;
    float mindist;
    float scalar;
    for (int i=0;i<balls.length;i++) {
        if (i!=ballnumber) {
          distanceV=PVector.sub(balls[i].position,position);
          dist=distanceV.mag();
    
          mindist=radius+balls[i].radius;
          
          if (dist<mindist) {
              PVector equil=PVector.add(position,PVector.mult(distanceV,mindist/dist));
              correctvelocity.add(PVector.mult(PVector.sub(balls[i].position,equil),spring/m));
              //correctvelocity.add(PVector.mult(distanceV,-spring/(m*dist*dist)));
              //newcollision[i]=false;           
            
          }
        }
    }
  }
}