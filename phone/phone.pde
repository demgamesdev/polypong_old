/********* NETWORKSTUFF *********/
import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;
int port=12000;
String remoteip="192.168.1.17";

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
Button gravbutton,attractbutton;

int numberofballs=20;

float amp=20;

PVector mouselast=new PVector(0,0);

int value=0;


int framecounter=0;

void setup() {
  //size(540,960);
  fullScreen();
  //size(displayWidth,displayHeight);
  frameRate(60);
  
  oscP5 = new OscP5(this,port);
  myRemoteLocation = new NetAddress(remoteip,port);
  
  rectMode(CENTER);
  balls=new Ball[numberofballs];
  balls[0]=new Ball(width/2,height/2,-10,0,100,true,0,0);
  for(int i=1;i<balls.length;i++){
    balls[i]=new Ball(random(width*0.1,width*0.9),random(height*0.1,height*0.9),random(-amp,amp),random(-amp,amp),random(5,20),false,i,0);
  }
  
  gravbutton = new Button("Gravity",width/3,height/8,width/3,height/15,false);
  attractbutton = new Button("Attract",2*width/3,height/8,width/3,height/15,false);
  
}


void draw() {
  
  if (gameScreen == 0) {
    initScreen();
  } else if (gameScreen == 1) {
    gameScreen();
  } else if (gameScreen == 2) {
    gameOverScreen();
  }
  framecounter++;
}

/********* SCREEN CONTENTS *********/

void initScreen() {
  background(0);
  textAlign(CENTER);
  text("Press key to start",width/2,height/2);
}

void gameScreen() {
  background(value);
    
  gravbutton.display();
  if (gravbutton.pressed()) {
    grav=0.7;
  } else {
    grav=0;
  }
  attractbutton.display();
  if (attractbutton.pressed()) {
    mouseattraction=200;
  } else {
    mouseattraction=0;
  }
  
  //bat.move();
  //bat.display();
  for (int j=0;j<balls.length;j++) {
    balls[j].checkBallCollision();
    balls[j].checkBoundaryCollision();
  }
  for (int j=0;j<balls.length;j++) {
    balls[j].update();
    balls[j].display();
  }
  
  value = (value + 1) % 255;
  
  if(framecounter%1==0) {
    sendSettings();
    for(int i=0;i<balls.length;i++){
      sendBall(balls[i]); 
    }
  }
  
}

void gameOverScreen() {
  // codes for game over screen
}

  

/********* EVENTHANDLER *********/

/* incoming osc message are forwarded to the oscEvent method.*/ 
void oscEvent(OscMessage theOscMessage) {
  /* print the address pattern and the typetag of the received OscMessage */
  //print("### received an osc message.");
  //print(" addrpattern: "+theOscMessage.addrPattern());
  //println(" typetag: "+theOscMessage.typetag());
}

void keyPressed() {
  if (gameScreen==0) {
    startGame();
  }
  setMove(keyCode, true);
}
 
void keyReleased() {
  setMove(keyCode, false);
}

void mousePressed() {
  setMovemouse=true;
}

void mouseReleased() {
  setMovemouse=false;
  gravbutton.flip=true;
  attractbutton.flip=true;
}

/********* OTHER FUNCTIONS *********/

void sendBall(Ball theBall) {
  OscMessage posMessage = new OscMessage("/ball/"+str(theBall.ballnumber)+"/position");
  posMessage.add(theBall.position.x);
  posMessage.add(theBall.position.y);
  oscP5.send(posMessage, myRemoteLocation); 
  OscMessage attrMessage = new OscMessage("/ball/"+str(theBall.ballnumber)+"/attributes");
  attrMessage.add(theBall.radius);
  //attrMessage.add(theBall.m);
  oscP5.send(attrMessage,myRemoteLocation);
}

void sendSettings() {
  OscMessage bgMessage = new OscMessage("/background");  
  bgMessage.add(value); /* add an int to the osc message */
  oscP5.send(bgMessage, myRemoteLocation);  
}

// This method sets the necessary variables to start the game  
void startGame() {
  gameScreen=1;
}

void setMove(int k, boolean b) {
  switch (k) {
  case UP:
    moveUp=b;
    break;     
  case DOWN:
    moveDown=b;
    break; 
  case LEFT:
    moveLeft=b;
    break; 
  case RIGHT:
    moveRight=b;
    break;
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


class Button {
  PVector position;
  boolean value=false;
  boolean holdbutton;
  boolean flip=true;
  
  float wid,hei;
  String name;
  PFont font;
  
  Button(String name_,float x, float y, float wid_, float hei_, boolean holdbutton_) {
    position = new PVector(x,y);
    name=name_;
    wid=wid_;
    hei=hei_;
    holdbutton=holdbutton_;
  }
  
  boolean pressed() {
    if(holdbutton) {
      if(mousePressed) {
        if(mouseX>=position.x-wid/2 && mouseX<=position.x+wid/2 && mouseY>=position.y-hei/2 && mouseY<=position.y+hei/2) {
          value=true;
        } 
      } else {
        value=false;
      }

    } else {
      if(mousePressed) {
        if(flip && mouseX>=position.x-wid/2 && mouseX<=position.x+wid/2 && mouseY>=position.y-hei/2 && mouseY<=position.y+hei/2) {
          value=!value;
          flip=false;
        } 
      }
    }
    return(value);
  }
  
  void display() {
    if (this.pressed()) {
      fill(128,0,0);
    } else {
      fill(0,128,0,128);
    }
    rect(position.x,position.y,wid,hei,0);
    fill(255);
    font=createFont("SansSerif",hei/1.5,true);
    textFont(font);
    textAlign(CENTER,CENTER);
    text(name,position.x,position.y);
  }
  
}