package com.demgames.polypong;

//importing processing libraries
import processing.core.*;
import oscP5.*;
import netP5.*;
import processing.event.MouseEvent;
import processing.event.KeyEvent;


public class Sketch extends PApplet {

    //declare oscp5 object for sending and receiving messages
    OscP5 oscP5;
    NetAddress myRemoteLocation;
    boolean connectstate=false;
    boolean readystate=false;

    //listening port of application
    int port=12000;
    String myipadress;
    String hostipadress;
    String remoteipadress;
    String clientname="test";
    String mode;
    int numberofballs;

    //constructor which is called in gamelaunch
    Sketch(String mode_, String myipadress_, String remoteipadress_,String ballnumber_) {
        mode=mode_;
        myipadress=myipadress_;
        remoteipadress=remoteipadress_;
        numberofballs=Integer.parseInt(ballnumber_);
    }
    /********* VARIABLES *********/

    //declare game variables
    float frict=(float)0.02;
    float batfrict=(float)0.05;
    float grav;
    float inelast=(float)0.6;
    float ballspring=3;
    float batspring=6;
    float mousevelocity=1;
    float mouseattraction;//200;
    float amp=20;

    int value=255;
    int framecounter=0;
    int gameScreen=0;

    //define pvector of last touch event
    PVector mouselast=new PVector(0,0);

    Bat bat;
    //declare array of balls and buttons
    Ball[] balls;
    Button gravbutton,attractbutton;

    //set size of canvas
    public void settings() {
        fullScreen();
        //frameRate(60);

    }

    public void setup() {

        //initialize oscp5 object for sending and receiving messages
        oscP5 = new OscP5(this,port);

        //set mode of rect drawings
        rectMode(CENTER);


        //initialize bat and balls objects
        bat=new Bat(width/2,height,width/4,height/25,true);

        balls=new Ball[numberofballs];
        balls[0]=new Ball(width/2,height/2,-10,0,50,false,0,0);
        for(int i=1;i<balls.length;i++) {
            balls[i] = new Ball(random((float) (width * 0.1), (float) (width * 0.9)), random((float) (height * 0.1), (float) (height * 0.5)), random(-amp, amp), random(-amp, amp), random(width/100, width/50), false, i, 0);
        }

        //initialize buttons
        gravbutton = new Button("Gravity",width/3,height/8,width/3,height/15,false);
        attractbutton = new Button("Attract",2*width/3,height/8,width/3,height/15,false);
    }

    //loop for drawing
    public void draw() {
        background(value);

        //show different screens
        switch(gameScreen) {
            case 0:
                showConnectScreen();
                break;
            case 1:
                //testScreen();
                if(mode.equals("host")) {
                    showGameScreen();
                } else {
                    showMirrorScreen();
                }

                break;
            case 2:
                showGameOverScreen();
                break;
        }



        framecounter++;
    }

    /********* SCREENCONTENT *********/
    void showConnectScreen() {
        PFont font;
        font = createFont("SansSerif", (float) (height / 40), true);
        fill(0);
        textFont(font);
        textAlign(CENTER, CENTER);
        text("Connecting", width/2, height*(float)0.2);
        switch(mode) {
            case "host":
                text("You are Host", width/2, height*(float)0.4);


                text("Checking for connection in local network", width/2, height*(float)0.6);

                if(!connectstate) {
                    String[] myipparts = split(myipadress, ".");

                    for (int i = 0; i < 256; i++) {
                        String checkipadress = myipparts[0] + "." + myipparts[1] + "." + myipparts[2] + "." + str(i);
                        if(!checkipadress.equals(myipadress)) {
                            NetAddress checkRemoteLocation = new NetAddress(checkipadress, port);
                            OscMessage connectMessage = new OscMessage("/hostconnect");
                            connectMessage.add(myipadress);
                            oscP5.send(connectMessage, checkRemoteLocation);
                        }
                    }
                    text(str(framecounter), width/2, height*(float)0.65);
                } else {
                    text("Connected to "+remoteipadress, width/2, height*(float)0.7);
                    sendSettings();
                    if(readystate) {
                        text(clientname+" ready", width/2, height*(float)0.8);
                        delay(2000);
                        gameScreen = 1;
                    }
                }

                break;

            case "client":
                text("You are Client", width/2, height*(float)0.4);
                text("Your Ip-Adress is: "+myipadress, width/2, height*(float)0.5);

                if(!connectstate) {
                    text("Waiting for connection in local network", width/2, height*(float)0.6);

                } else {
                    text("Connected to "+remoteipadress, width/2, height*(float)0.6);
                    delay(3500);
                    gameScreen=1;
                }

                break;

        }


    }

    void testScreen() {
        ellipse(width/2,height/2,width/2,width/2);

    }

    void showGameScreen() {
        //display and check buttons for pressing
        gravbutton.display();
        if (gravbutton.pressed()) {
            grav=(float)0.7;
        } else {
            grav=0;
        }
        attractbutton.display();
        if (attractbutton.pressed()) {
            mouseattraction=200;
        } else {
            mouseattraction=0;
        }

        //move and display bat
        bat.move();
        bat.display();

        //first check all balls for collisions and then update all balls
        for (Ball ball : balls) {
            ball.checkBallCollision();
            ball.checkExternalFroce();
            ball.checkBatCollision(bat);
            ball.checkBoundaryCollision();
        }
        for (Ball ball : balls) {
            ball.update();
            ball.display();
        }

        //value = (value + 1) % 255;


        //send data every n frames
        if(framecounter%1==0) {
            for (Ball ball : balls) {
                sendBall(ball);
            }
        }


        //update mouselast
        if (mousePressed) {
            mouselast=new PVector(mouseX,mouseY);
        }
    }

    void showMirrorScreen() {
        for (int j=0;j<balls.length;j++) {
            balls[j].display();
        }
    }

    void showGameOverScreen() {
        for (int j=0;j<balls.length;j++) {
            balls[j].display();
        }
    }

    /********* EVENTHANDLER *********/

    //check for incoming osc messages
    void oscEvent(OscMessage theOscMessage) {
        switch(theOscMessage.addrPattern()) {
            case "/settings":
                value=theOscMessage.get(0).intValue();
                print(value);
                OscMessage readyMessage = new OscMessage("/clientready");
                readyMessage.add("paty");
                oscP5.send(readyMessage, myRemoteLocation);
                //println("background: ",value);
                break;

            case "/clientconnect":
                connectstate=true;
                remoteipadress=theOscMessage.get(0).stringValue();
                myRemoteLocation=new NetAddress(remoteipadress,port);

                break;

            case "/clientready":
                readystate=true;
                //clientname=theOscMessage.get(0).stringValue();

                break;

            case "/hostconnect":
                if(!theOscMessage.get(0).stringValue().equals(myipadress)) {
                    remoteipadress=theOscMessage.get(0).stringValue();
                    connectstate=true;
                    println("hostipadress: ", remoteipadress);
                    myRemoteLocation = new NetAddress(remoteipadress, port);
                    OscMessage connectMessage = new OscMessage("/clientconnect");
                    connectMessage.add(myipadress);
                    oscP5.send(connectMessage, myRemoteLocation);
                }
                break;

            default:
                for (int i=0;i<balls.length;i++) { //balls.length
                    if(theOscMessage.addrPattern().equals("/ball/"+str(i)+"/position")) {
                        balls[i].position.x=theOscMessage.get(0).floatValue()*width;
                        balls[i].position.y=theOscMessage.get(1).floatValue()*height;
                        //println("position: ",balls[i].position.x,balls[i].position.y);
                    }
                    if(theOscMessage.addrPattern().equals("/ball/"+str(i)+"/attributes")) {
                        balls[i].radius=theOscMessage.get(0).floatValue()*width;
                        //println("radius: ",balls[i].radius," m: ",balls[i].m);
                    }
                }
                break;

        }

    }

    //check mouse for being released
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);
        gravbutton.flip=true;
        attractbutton.flip=true;
    }

    /********* OTHER FUNCTIONS *********/

    //send ball data to remotelocation
    void sendBall(Ball theBall) {
        OscMessage posMessage = new OscMessage("/ball/"+str(theBall.ballnumber)+"/position");
        posMessage.add(theBall.position.x/width);
        posMessage.add(theBall.position.y/height);
        oscP5.send(posMessage, myRemoteLocation);
        OscMessage attrMessage = new OscMessage("/ball/"+str(theBall.ballnumber)+"/attributes");
        attrMessage.add(theBall.radius/width);
        //attrMessage.add(theBall.m);
        oscP5.send(attrMessage,myRemoteLocation);
    }

    //send settings data to remote location
    void sendSettings() {
        OscMessage settingsMessage = new OscMessage("/settings");
        settingsMessage.add(value); /* add an int to the osc message */
        oscP5.send(settingsMessage, myRemoteLocation);
    }


    /********* CLASSES *********/


    //create class Ball
    class Ball {
        PVector position;
        PVector velocity;

        int playerScreen;

        float radius,m;

        int[] ballcolor=new int[3];
        int ballnumber;
        boolean controlled;

        //constructor for Ball
        Ball(float x, float y, float xvel, float yvel, float r_,boolean controlled_,int ballnumber_,int playerScreen_) {
            position = new PVector(x,y);
            velocity = new PVector(xvel,yvel);
            radius=r_;
            m=(float)(radius*0.1);
            ballcolor[0]=(int)random(255);
            ballcolor[1]=(int)random(255);
            ballcolor[2]=(int)random(255);
            controlled=controlled_;
            ballnumber=ballnumber_;
            playerScreen=playerScreen_;
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
        }


        //check for collision with other balls and push away like a spring
        void checkBallCollision() {
            PVector distanceV;
            float dist;
            float mindist;
            for (int i=0;i<balls.length;i++) {
                if (i != ballnumber) {
                    distanceV = PVector.sub(balls[i].position, position);
                    dist = distanceV.mag();

                    mindist = radius + balls[i].radius;

                    if (dist < mindist) {
                        PVector equil = PVector.add(position, PVector.mult(distanceV, mindist / dist));
                        velocity.add(PVector.mult(PVector.sub(balls[i].position, equil), ballspring/m));
                        velocity.mult(inelast);
                    }
                }
            }
        }


        //check for collision with Bat thebat and again behave like a spring
        void checkBatCollision(Bat thebat) {
            PVector distanceV=PVector.sub(position,thebat.position);
            float dist =distanceV.mag();
            //distanceV.mult(1/dist);
            float positionparallel=PVector.dot(thebat.orparallel,distanceV);
            float positionnormal=PVector.dot(thebat.ornormal,distanceV);
            //distanceV.mult(1/dist);

            //print("projections:", positionparallel,positionnormal);
            float velocityparallel=PVector.dot(thebat.orparallel,velocity);
            float velocitynormal=PVector.dot(thebat.ornormal,velocity);

            float factor=(float)2;
            float part=(float)0.95;

            print("------");
            if(abs(positionparallel)<(thebat.wid/2+radius)*part) {
                println("parallel fit");
                //upper side
                if(positionnormal>=0 && positionnormal<thebat.hei/2+radius) {
                    println("up");

                    PVector normalforce = PVector.mult(thebat.ornormal, (thebat.hei / 2 + radius - positionnormal)*batspring/m);
                    velocity.add(normalforce);
                    velocity.add(PVector.mult(thebat.ornormal,PVector.dot(thebat.ornormal,thebat.velocity)));
                    velocity.mult(inelast);

                //lower side
                } else if(positionnormal<0 && positionnormal>-(thebat.hei/2+radius)) {

                    println("down");
                    PVector normalforce = PVector.mult(thebat.ornormal, -(thebat.hei / 2 + radius + positionnormal) * batspring/m);
                    velocity.add(normalforce);
                    velocity.add(PVector.mult(thebat.ornormal,PVector.dot(thebat.ornormal,thebat.velocity)));
                    velocity.mult(inelast);
                }
            } else if(abs(positionnormal)<(thebat.hei/2+radius)*part) {
                println("normal fit");
                //right side
                if(positionparallel>=0 && positionparallel<thebat.wid/2+radius) {
                    println("right");
                    PVector parallelforce = PVector.mult(thebat.orparallel, (thebat.wid/2+radius-positionparallel) *batspring/m);
                    velocity.add(parallelforce);
                    velocity.add(PVector.mult(thebat.orparallel,PVector.dot(thebat.orparallel,thebat.velocity)));
                    velocity.mult(inelast);
                //left side
                } else if(positionparallel<0 && positionparallel>-(thebat.wid/2+radius)) {

                    println("left");
                    PVector parallelforce = PVector.mult(thebat.orparallel, -(thebat.wid/2+radius+positionparallel) *batspring/m);
                    velocity.add(parallelforce);
                    velocity.add(PVector.mult(thebat.orparallel,PVector.dot(thebat.orparallel,thebat.velocity)));
                    velocity.mult(inelast);

                }
            } /*else if(dist<sqrt(thebat.wid*thebat.wid+thebat.hei*thebat.hei)/2){
                println("diagonal");
                velocity.add(PVector.mult(distanceV, (sqrt(thebat.wid*thebat.wid+thebat.hei*thebat.hei)/2-dist)*batspring/m/dist));
                velocity.add(PVector.mult(distanceV,PVector.dot(distanceV,thebat.velocity)/dist));
                velocity.mult(inelast);

            }*/



            print("------");





        }

        //add external force such as gravity, mouse attraction and friction
        void checkExternalFroce() {
            velocity.add(new PVector(0,grav));
            if (mousePressed) {
                //println("pressed");
                PVector mousepos=new PVector(mouseX,mouseY);
                PVector distanceV=PVector.sub(mousepos,position);
                float dist=distanceV.mag();
                velocity.add(PVector.mult(distanceV,mouseattraction/pow(dist,2)));
            }

            velocity.add(PVector.mult(velocity,-frict/m));
        }
    }


    //create class bat
    class Bat {
        PVector origin,position,velocity,orparallel,ornormal;

        float orientation,moveradius,wid,hei;

        int[] batcolor=new int[3];
        boolean controlled;

        //cunstructor for bat
        Bat(float x, float y, float wid_,float hei_, boolean controlled_) {
            origin= new PVector(x,y);
            position = PVector.add(origin,new PVector(0,-height/10));
            velocity = new PVector(0,0);

            orientation=getangle(position.x,position.y);
            setOrvectors();

            moveradius=width*(float)0.7;
            wid=wid_;
            hei=hei_;


            batcolor[0]=(int)random(255);
            batcolor[1]=(int)random(255);
            batcolor[1]=(int)random(255);

            controlled=controlled_;

        }


        //move bat to new position
        void move() {
            if(controlled) {
                if (mousePressed) {
                    if (PVector.sub(new PVector(mouseX,mouseY), origin).mag() <= moveradius) {
                        position = new PVector(mouseX,mouseY);
                    } else {
                        float posangle=getangle(mouseX,mouseY);
                        position=PVector.add(origin,PVector.mult(new PVector(cos(posangle),sin(posangle)),moveradius));
                    }
                    velocity = PVector.sub(position, mouselast);
                    velocity.mult(mousevelocity);
                }

                orientation=getangle(position.x,position.y);
                setOrvectors();
            }
        }

        //display bat also rotating
        void display() {
            fill(128,128,128);
            ellipse(origin.x,origin.y,2*moveradius,2*moveradius);
            fill(batcolor[0],batcolor[1],batcolor[2]);
            pushMatrix();
            translate(position.x,position.y);
            rotate(orientation-PI/2);
            rect(0,0,wid,hei,hei/2);
            popMatrix();

            /*fill(255);
            ellipse(position.x+wid*ornormal.x,position.y+wid*ornormal.y,50,50);
            fill(255,0,0);
            ellipse(position.x+wid*orparallel.x,position.y+wid*orparallel.y,50,50);*/

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

    //create class button
    class Button {
        PVector position;
        boolean value = false;
        boolean holdbutton;
        boolean flip = true;

        float wid, hei;
        String name;
        PFont font;

        //constructor for button
        Button(String name_, float x, float y, float wid_, float hei_, boolean holdbutton_) {
            position = new PVector(x, y);
            name = name_;
            wid = wid_;
            hei = hei_;
            holdbutton = holdbutton_;
        }

        //check for being pressed, if holdbutton=false flip is released in mouseReleased()
        boolean pressed() {
            if (holdbutton) {
                if (mousePressed) {
                    if (mouseX >= position.x - wid / 2 && mouseX <= position.x + wid / 2 && mouseY >= position.y - hei / 2 && mouseY <= position.y + hei / 2) {
                        value = true;
                    }
                } else {
                    value = false;
                }

            } else {
                if (mousePressed) {
                    if (flip && mouseX >= position.x - wid / 2 && mouseX <= position.x + wid / 2 && mouseY >= position.y - hei / 2 && mouseY <= position.y + hei / 2) {
                        value = !value;
                        flip = false;
                    }
                }
            }
            return (value);
        }


        //display button
        void display() {
            if (this.pressed()) {
                fill(128, 0, 0);
            } else {
                fill(0, 128, 0, 128);
            }
            rect(position.x, position.y, wid, hei, 0);
            fill(255);
            font = createFont("SansSerif", (float) (hei / 1.5), true);
            textFont(font);
            textAlign(CENTER, CENTER);

            text(name, position.x, position.y);
        }
    }

}

