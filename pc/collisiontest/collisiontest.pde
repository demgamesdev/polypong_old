Ball myBall;
Bat myBat;
float zoom=1;
float mousevelocity=1;
float inelast=0.6;
float batspring=6;
float spring=3;
float frict=0.2;
float gravityMag=0.6;
float attractionMag=200;

PVector zoompoint=new PVector(width/2,height);
PVector mouselast=new PVector(0,0);

boolean gravityState=false;
boolean attractionState=false;

void setup() {
  size(540,960);
  myBall=new Ball(width/2,height/2,0,1,20,false,0,0);
  myBat=new Bat(width/2,height*0.9,100,10,true,0);
}


void draw() {
  background(255);
  myBat.move();
  
  myBall.checkBoundaryCollision();
  myBall.checkBatCollision(myBat);
  myBall.update();
  
  myBall.display();
  myBat.display();
  
  
}

void mousePressed() {
  myBat.position=new PVector(mouseX,mouseY);
  mouselast=new PVector(mouseX,mouseY);
  myBat.velocity=new PVector(0,0);
}

void mouseMoved() {
  
  myBat.position=new PVector(mouseX,mouseY);
  myBat.velocity=PVector.sub(new PVector(mouseX,mouseY),mouselast);
  mouselast=new PVector(mouseX,mouseY);
  
}

void mouseReleased() {
  myBat.velocity=new PVector(0,0);
}

class Ball {
        PVector position;
        PVector velocity;

        int playerScreen;

        float radius,m;

        int[] ballcolor=new int[3];
        int ballnumber;
        boolean controlled;
        boolean updateState=true;

        //constructor for Ball
        Ball(float x, float y, float xvel, float yvel, float r_,boolean controlled_,int ballnumber_,int playerScreen_) {
            position = new PVector(x,y);
            velocity = new PVector(xvel,yvel);
            radius=r_;
            m=(float)(radius*0.1);
            controlled=controlled_;
            ballnumber=ballnumber_;
            playerScreen=playerScreen_;

            ballcolor[0] =0;
            ballcolor[1]=0;
            ballcolor[2]=255;

            if (playerScreen!=0) {
                ballcolor[0] =255;
                ballcolor[2]=0;
            }
            //(int)random(255);
        }

        //update position of ball with velocity
        void update() {

            position.add(velocity);

            if (controlled) {
                if (mousePressed) {
                    position = new PVector(mouseX, mouseY);
                    velocity = PVector.sub(position, mouselast);
                }

            }
        }

        //display ball
        void display() {

            fill(ballcolor[0],ballcolor[1],ballcolor[2]);
            ellipse(position.x,position.y,radius*2,radius*2);
        }

        //check for collision with border of screen
        void checkBoundaryCollision() {
            if (position.x+radius+velocity.x>width) {
                position.x=width-radius;
                velocity.x*=-inelast;
            } else if (position.x-radius+velocity.x<0) {
                position.x=radius;
                velocity.x*=-inelast;
            } if (position.y+radius+velocity.y>height) {
                position.y=height-radius;
                velocity.y*=-inelast;
            } else if (position.y-radius+velocity.y<0) {
                position.y=radius;
                velocity.y*=-inelast;
            }
            else if (position.y-radius+velocity.y<-height) {
                position.y=-height+radius;
                velocity.y*=-inelast;
            }
        }


        //check for collision with Bat thebat and again behave like a spring
        void checkBatCollision(Bat thebat) {
            PVector relPosition=PVector.sub(position,thebat.position);
            PVector relVelocity=PVector.sub(velocity,thebat.velocity);
            float dist =relPosition.mag();
            //distanceV.mult(1/dist);
            float positionparallel=PVector.dot(thebat.orparallel,relPosition);
            float positionnormal=PVector.dot(thebat.ornormal,relPosition);
            //distanceV.mult(1/dist);

            //print("projections:", positionparallel,positionnormal);
            float velocityparallel=PVector.dot(thebat.orparallel,relVelocity);
            float velocitynormal=PVector.dot(thebat.ornormal,relVelocity);

            float factor=(float)10;
            float part=(float)0.9;

            //print("------");
            if(abs(positionparallel+velocityparallel)<(thebat.wid/2+radius) ) {
                //println("parallel fit");
                //upper side
                if(positionnormal>0 && positionnormal+velocitynormal<=(thebat.hei/2+radius)*part) {
                    //println("up");

                    PVector normalforce = PVector.mult(thebat.ornormal, (thebat.hei / 2 + radius - positionnormal)*batspring/m*factor);
                    velocity.add(normalforce);
                    velocity.add(PVector.mult(thebat.ornormal,PVector.dot(thebat.ornormal,thebat.velocity)));

                    //velocity=PVector.add(PVector.mult(thebat.orparallel,velocityparallel),PVector.mult(thebat.ornormal,-velocitynormal));
                    velocity.mult(inelast/factor);

                    //lower side
                } else if(positionnormal<0 && positionnormal+velocitynormal>=-(thebat.hei/2+radius)*part) {

                    //println("down");
                    PVector normalforce = PVector.mult(thebat.ornormal, -(thebat.hei / 2 + radius + positionnormal) * batspring/m*factor);
                    velocity.add(normalforce);
                    velocity.add(PVector.mult(thebat.ornormal,PVector.dot(thebat.ornormal,thebat.velocity)));

                    //velocity=PVector.add(PVector.mult(thebat.orparallel,velocityparallel),PVector.mult(thebat.ornormal,-velocitynormal));
                    velocity.mult(inelast/factor);
                }
            } else if(abs(positionnormal+velocitynormal)<(thebat.hei/2+radius)) {
                //println("normal fit");
                //right side
                if(positionparallel>0 && positionparallel+velocityparallel<=(thebat.wid/2+radius)*part) {
                    //println("right");
                    PVector parallelforce = PVector.mult(thebat.orparallel, (thebat.wid/2+radius-positionparallel) *batspring/m*factor);
                    velocity.add(parallelforce);
                    velocity.add(PVector.mult(thebat.orparallel,PVector.dot(thebat.orparallel,thebat.velocity)));

                    //velocity=PVector.add(PVector.mult(thebat.orparallel,-velocityparallel),PVector.mult(thebat.ornormal,velocitynormal));
                    velocity.mult(inelast/factor);
                    //left side
                } else if(positionparallel<0 && positionparallel+velocityparallel>=-(thebat.wid/2+radius)*part) {

                    //println("left");
                    PVector parallelforce = PVector.mult(thebat.orparallel, -(thebat.wid/2+radius+positionparallel) *batspring/m*factor);
                    velocity.add(parallelforce);
                    velocity.add(PVector.mult(thebat.orparallel,PVector.dot(thebat.orparallel,thebat.velocity)));

                    //velocity=PVector.add(PVector.mult(thebat.orparallel,-velocityparallel),PVector.mult(thebat.ornormal,velocitynormal));
                    velocity.mult(inelast/factor);

                }
            } /*else if(dist<sqrt(thebat.wid*thebat.wid+thebat.hei*thebat.hei)/2){
                println("diagonal");
                velocity.add(PVector.mult(distanceV, (sqrt(thebat.wid*thebat.wid+thebat.hei*thebat.hei)/2-dist)*batspring/m/dist));
                velocity.add(PVector.mult(distanceV,PVector.dot(distanceV,thebat.velocity)/dist));
                velocity.mult(inelast);

            }*/



            //print("------");

        }

        //add external force such as gravity, mouse attraction and friction
        void checkExternalForce() {
            if (gravityState) {
                velocity.add(new PVector(0, gravityMag));

            }
            if(attractionState) {
                if (mousePressed) {
                    //println("pressed");
                    PVector mousepos = new PVector(mouseX, mouseY);
                    PVector distanceV = PVector.sub(mousepos, position);
                    float dist = distanceV.mag();
                    velocity.add(PVector.mult(distanceV, attractionMag / pow(dist, 2)));
                }
            }
            velocity.add(PVector.mult(velocity,-frict/m));
        }

        /*void checkPlayerScreenChange() {
            if(position.y+velocity.y<0) {
                updateState=false;
                sendPlayerScreenChange(this);
            }
        }*/
    }


    //create class bat
    class Bat {
        PVector origin,position,velocity,orparallel,ornormal;

        float orientation,moveradius,wid,hei;

        int[] batcolor=new int[3];
        boolean controlled;

        int playerScreen;

        //cunstructor for bat
        Bat(float x, float y, float wid_,float hei_, boolean controlled_,int playerScreen_) {
            origin= new PVector(x,y);
            position = PVector.add(origin,new PVector(0,-height/10));
            velocity = new PVector(0,0);

            orientation=getangle(position.x,position.y);
            setOrvectors();

            moveradius=width*(float)0.7;
            wid=wid_;
            hei=hei_;

            playerScreen=playerScreen_;



            batcolor[0] =0;
            batcolor[1]=0;
            batcolor[2]=255;

            if (playerScreen!=0) {
                batcolor[0] =255;
                batcolor[2]=0;
            }

            controlled=controlled_;
    

        }


        //move bat to new position
        void move() {
            origin= new PVector(width/2,height);
            if(controlled) {/*
                if (mousePressed) {
                    if (PVector.sub(new PVector(mouseX,mouseY), origin).mag() <= moveradius*zoom) {
                        if(abs(mouseX-origin.x)<=zoom*width/2) {
                            position = new PVector((mouseX - (1 - zoom) * zoompoint.x) / zoom, (mouseY - (1 - zoom) * zoompoint.y) / zoom);
                        } else {

                        }
                    } else if(PVector.sub(new PVector(mouseX,mouseY), origin).mag() <= moveradius*(float)1.1*zoom){
                        if(abs(mouseX-origin.x)<=zoom*width/2) {
                            float posangle = getangle(mouseX, mouseY);
                            position = PVector.mult(PVector.add(origin, new PVector((cos(posangle) * moveradius * zoom - (1 - zoom) * zoompoint.x),
                                    (sin(posangle) * moveradius * zoom - (1 - zoom) * zoompoint.y))), 1 / zoom);
                        } else {

                        }
                    }
                    velocity = PVector.sub(position, mouselast);
                    velocity.mult(mousevelocity);
                }*/

                orientation=getangle(position.x,position.y);
                setOrvectors();
            }
        }

        //display bat also rotating
        void display() {
            //pushMatrix();

            fill(batcolor[0],batcolor[1],batcolor[2],50);
            //ellipse(width/2,height,2*moveradius,2*moveradius);

            rectMode(CORNERS);
            fill(0);

            rect(width, height - moveradius, width + moveradius, height + moveradius);
            rect(-moveradius, height - moveradius, 0, height + moveradius);
            rect(0, height, width, height + moveradius);

            rectMode(CENTER);

            fill(batcolor[0],batcolor[1],batcolor[2]);
            translate(position.x,position.y);
            rotate(orientation-PI/2);
            rect(0,0,wid,hei,hei/2);
            rotate(-(orientation-PI/2));
            translate(-(position.x),-(position.y));

            /*fill(255);
            ellipse(position.x+wid*ornormal.x,position.y+wid*ornormal.y,50,50);
            fill(255,0,0);
            ellipse(position.x+wid*orparallel.x,position.y+wid*orparallel.y,50,50);*/
            //popMatrix();

        }

        //get angle by position relative to origin
        float getangle(float x, float y) {
            float angle=-atan((-y + origin.y) / (x - origin.x))+PI;
            if (x>=origin.x) {
                angle=-atan((-y + origin.y) / (x - origin.x));
            }

            return(angle);
        }

        //set parallel and perpendicular unit vectors for colllision management
        void setOrvectors() {
            ornormal=PVector.add(PVector.mult(new PVector(1,0),cos(orientation)),
                    PVector.mult(new PVector(0,1),sin(orientation)));
            orparallel=new PVector(-ornormal.y,ornormal.x);

        }

    }