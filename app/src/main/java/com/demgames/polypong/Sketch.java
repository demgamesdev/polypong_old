package com.demgames.polypong;

import android.app.Activity;
import android.content.Context;

//importing processing libraries
import processing.core.*;
import oscP5.*;
import netP5.*;
import processing.data.IntList;
import processing.event.MouseEvent;
import processing.event.KeyEvent;
//test

public class Sketch extends PApplet {
    //Globals globalVariables = (Globals) getApplicationContext;

    //declare oscp5 object for sending and receiving messages
    OscP5 oscP5;
    NetAddress myRemoteLocation;

    //listening port of application
    int port=12000;
    String myipadress;
    String remoteipadress;
    String clientname="test";
    String mode;
    float zoom=1;
    int myplayerscreen=0;
    Float frict;


    //constructor which is called in gamelaunch

    Sketch(String mode_, String myipadress_,String remoteipadress_, String numberofballs_, Float friction_) {
        mode=mode_;
        myipadress=myipadress_;
        remoteipadress=remoteipadress_;
        numberofballs=Integer.parseInt(numberofballs_);
        println(remoteipadress);
        frict=friction_;

        myRemoteLocation=new NetAddress(remoteipadress,port);

        if(mode.equals("client")) {
            myplayerscreen=1;
        }

    }
    /********* VARIABLES *********/

    //declare game variables
    //float frict=(float)0.0;
    float batfrict=(float)0.05;
    float grav;
    float inelast=(float)0.6;
    float ballspring=3;
    float batspring=6;
    float mousevelocity=1;
    float mouseattraction;//200;
    float amp=20;
    float width0,height0;

    int value=255;
    int framecounter=0;
    int gameScreen=1;

    int numberofballs;

    IntList ballstocompute;

    //define pvector of last touch event
    PVector mouselast=new PVector(0,0);
    PVector zoompoint;
    PVector zoomoffset=new PVector(0,0);
    Bat mybat;
    Bat otherbat;
    //declare array of balls and buttons
    Ball[] balls;
    Button gravbutton,attractbutton,zoombutton;

    //set size of canvas
    public void settings() {
        fullScreen();
        //smooth();
        //frameRate(60);

    }

    public void setup() {

        frameRate(60);
        //initialize oscp5 object for sending and receiving messages
        oscP5 = new OscP5(this,port);

        //set mode of rect drawings
        rectMode(CENTER);


        //initialize bat and balls objects
        mybat=new Bat(width/2,height,width/4,height/25,true,myplayerscreen);
        otherbat=new Bat(width/2,-height,width/4,height/25,true,(myplayerscreen+1)%2);

        //initialize buttons
        gravbutton = new Button("Gravity",width/3,height/8,width/3,height/15,false);
        attractbutton = new Button("Attract",2*width/3,height/8,width/3,height/15,false);
        zoombutton=new Button("Zoom",width/2,height/4,width/3,height/15,false);

        zoompoint=new PVector(width/2,height);

        width0=width;
        height0=height;

        if(mode.equals("client")) {
            createBallsClient();
        } else if(mode.equals("host")) {
            createBallsHost();
        }
    }

    //loop for drawing
    public void draw() {
        background(0);

        //show different screens
        switch(gameScreen) {
            case 0:
                break;
            case 1:
                //testScreen();
                showGameScreen();
                break;
            case 2:
                //showGameOverScreen();
                break;
        }



        framecounter++;
    }

    /********* SCREENCONTENT *********/


    void showGameScreen() {
        //display and check buttons for pressing
        background(0);
        /*if (gravbutton.pressed()) {
            grav=(float)0.7;
        } else {
            grav=0;
        }
        if (attractbutton.pressed()) {
            mouseattraction=200;
        } else {
            mouseattraction=0;
        }
        if (zoombutton.pressed()) {
            zoom=(float)0.5;
        } else {
            zoom=1;
        }*/


        //checkZoom("out");
        fill(255);
        rect(width/2,0,width,2*height0);
        float linethickness=height/50;
        fill(0);
        rect(width/2,-linethickness/2,width,linethickness,linethickness/2);
        textMode(CORNER);
        fill(0);
        text(str(parseInt(frameRate)),width*(float)0.9,height*(float)0.05);
        checkZoom("in");
        //gravbutton.display();

        //attractbutton.display();

        //zoombutton.display();

        //checkZoom("out");




        //move and display bat
        mybat.move();
        sendBat(mybat);
        mybat.display();
        otherbat.display();

        //first check all balls for collisions and then update all balls
        for (Ball ball : balls) {
            if(ball.playerScreen==myplayerscreen) {
                ball.checkPlayerScreenChange();
                ball.checkBallCollision();
                ball.checkExternalForce();
                ball.checkBatCollision(mybat);
                ball.checkBoundaryCollision();
            }
        }
        for (Ball ball : balls) {
            if(ball.playerScreen==myplayerscreen) {
                ball.update();
                sendBall(ball);
            }

            ball.display();
        }


        //update mouselast
        if (mousePressed) {
            mouselast=new PVector(mouseX,mouseY);
        }
    }

    void showMirrorScreen() {
        mybat.move();
        mybat.display();

        for (Ball ball : balls) {
            ball.checkPlayerScreenChange();
            ball.display();
        }
    }

    void showGameOverScreen() {
        for (int j=0;j<balls.length;j++) {
            balls[j].display();
        }
        fill(0);
        line(width/2,height,balls[0].position.x,balls[0].position.y);
    }

    /********* EVENTHANDLER *********/

    //check for incoming osc messages
    void oscEvent(OscMessage theOscMessage) {
        for (int i=0;i<balls.length;i++) { //balls.length
            if(theOscMessage.addrPattern().equals("/ball/"+str(i)+"/position")) {
                balls[i].position.x=width-theOscMessage.get(0).floatValue()*width;
                balls[i].position.y=-theOscMessage.get(1).floatValue()*height;
                //println("position: ",balls[i].position.x,balls[i].position.y);
            }
            if(theOscMessage.addrPattern().equals("/ball/"+str(i)+"/attributes")) {
                balls[i].radius=theOscMessage.get(0).floatValue()*width;
                //println("radius: ",balls[i].radius," m: ",balls[i].m);
            }
            if(theOscMessage.addrPattern().equals("/ball/"+str(i)+"/playerscreen")) {
                balls[i].playerScreen=theOscMessage.get(0).intValue();
                balls[i].velocity.x=-theOscMessage.get(1).floatValue()*width;
                balls[i].velocity.y=-theOscMessage.get(2).floatValue()*height;
                //println("radius: ",balls[i].radius," m: ",balls[i].m);
            }
        }
        for (int i=0;i<2;i++) {
            if(i!=myplayerscreen) {
                if(theOscMessage.addrPattern().equals("/bat/"+str(i)+"/position")) {
                    otherbat.position.x=theOscMessage.get(0).floatValue()*width;
                    otherbat.position.y=theOscMessage.get(1).floatValue()*height;
                    otherbat.orientation=theOscMessage.get(2).floatValue();
                    //println("position: ",balls[i].position.x,balls[i].position.y);
                }
            }
        }


    }



    //check mouse for being released
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);
        gravbutton.flip=true;
        attractbutton.flip=true;
        zoombutton.flip=true;
    }

    /********* SENDING FUNCTIONS *********/

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

    void sendPlayerScreenChange(Ball theBall) {
        OscMessage playerscreenMessage = new OscMessage("/ball/"+str(theBall.ballnumber)+"/playerscreen");
        playerscreenMessage.add(theBall.playerScreen);
        playerscreenMessage.add(theBall.velocity.x/width);
        playerscreenMessage.add(theBall.velocity.y/height);
        oscP5.send(playerscreenMessage, myRemoteLocation);
    }

    void sendBat(Bat theBat) {
        OscMessage batMessage = new OscMessage("/bat/"+str(myplayerscreen)+"/position");
        batMessage.add(theBat.position.x/width);
        batMessage.add(theBat.position.y/height);
        batMessage.add(theBat.orientation);
        oscP5.send(batMessage, myRemoteLocation);

    }

    /********* OTHER FUNCTIONS *********/

    void createBallsHost() {
        balls=new Ball[numberofballs];

        //balls[0]=new Ball(bat.origin.x,bat.origin.y,0,0,width/20,false,0,0);
        for(int i=0;i<balls.length;i++) {
            if(i%2==0) {
                balls[i]=new Ball(mybat.origin.x,mybat.origin.y-mybat.moveradius,0,0,width/30,false,i,0);

            } else {
                balls[i]=new Ball(mybat.origin.x,-mybat.origin.y+mybat.moveradius,0,0,width/30,false,i,1);
            }

            //balls[i] = new Ball(random((float) (width * 0.1), (float) (width * 0.9)), random((float) (height * 0.1), (float) (height * 0.5)), random(-amp, amp), random(-amp, amp), random(width/100, width/50), false, i, 0);
        }
    }

    void createBallsClient() {
        balls=new Ball[numberofballs];

        //balls[0]=new Ball(bat.origin.x,bat.origin.y,0,0,width/20,false,0,0);
        for(int i=0;i<balls.length;i++) {
            if(i%2==0) {
                balls[i]=new Ball(mybat.origin.x,-mybat.origin.y+mybat.moveradius,0,0,width/30,false,i,0);

            } else {
                balls[i]=new Ball(mybat.origin.x,mybat.origin.y-mybat.moveradius,0,0,width/30,false,i,1);
            }

            //balls[i] = new Ball(random((float) (width * 0.1), (float) (width * 0.9)), random((float) (height * 0.1), (float) (height * 0.5)), random(-amp, amp), random(-amp, amp), random(width/100, width/50), false, i, 0);
        }
    }

    void checkZoom(String zoommode) {
        translate(zoompoint.x,zoompoint.y);
        switch(zoommode) {
            case "out":
                scale(zoom);
                break;
            case "in":
                scale(1/zoom);
                break;
            default:
                scale(1);
        }
        translate(-zoompoint.x,-zoompoint.y);

    }

    void rotatePlayerScreen(int playerScreen,String rotateMode) {
        float rotationangle=0;
        if(playerScreen==(myplayerscreen+1)%2) {
            rotationangle=PI;
        }

        translate(width/2,0);
        switch(rotateMode) {
            case "cw":
                rotate(rotationangle);
                break;
            case "acw":
                rotate(-rotationangle);
                break;

        }
        translate(-width/2,0);
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
            } /*else if (position.y-radius+velocity.y<0) {
                position.y=radius;
                velocity.y*=-inelast;
            }*/
            else if (position.y-radius+velocity.y<-height) {
                position.y=-height+radius;
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

            //print("------");
            if(abs(positionparallel)<(thebat.wid/2+radius)*part) {
                //println("parallel fit");
                //upper side
                if(positionnormal>=0 && positionnormal<thebat.hei/2+radius) {
                    //println("up");

                    PVector normalforce = PVector.mult(thebat.ornormal, (thebat.hei / 2 + radius - positionnormal)*batspring/m);
                    velocity.add(normalforce);
                    velocity.add(PVector.mult(thebat.ornormal,PVector.dot(thebat.ornormal,thebat.velocity)));
                    velocity.mult(inelast);

                    //lower side
                } else if(positionnormal<0 && positionnormal>-(thebat.hei/2+radius)) {

                    //println("down");
                    PVector normalforce = PVector.mult(thebat.ornormal, -(thebat.hei / 2 + radius + positionnormal) * batspring/m);
                    velocity.add(normalforce);
                    velocity.add(PVector.mult(thebat.ornormal,PVector.dot(thebat.ornormal,thebat.velocity)));
                    velocity.mult(inelast);
                }
            } else if(abs(positionnormal)<(thebat.hei/2+radius)*part) {
                //println("normal fit");
                //right side
                if(positionparallel>=0 && positionparallel<thebat.wid/2+radius) {
                    //println("right");
                    PVector parallelforce = PVector.mult(thebat.orparallel, (thebat.wid/2+radius-positionparallel) *batspring/m);
                    velocity.add(parallelforce);
                    velocity.add(PVector.mult(thebat.orparallel,PVector.dot(thebat.orparallel,thebat.velocity)));
                    velocity.mult(inelast);
                    //left side
                } else if(positionparallel<0 && positionparallel>-(thebat.wid/2+radius)) {

                    //println("left");
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



            //print("------");

        }

        //add external force such as gravity, mouse attraction and friction
        void checkExternalForce() {
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

        void checkPlayerScreenChange() {
            if(position.y>0 && position.y+velocity.y<0) {
                playerScreen=(playerScreen+1)%2;
                sendPlayerScreenChange(this);
            }
        }
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
            rotatePlayerScreen(playerScreen,"acw");
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
            rotatePlayerScreen(playerScreen,"cw");

        }


        //move bat to new position
        void move() {
            origin= new PVector(width/2,height);
            if(controlled) {
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
                }

                orientation=getangle(position.x,position.y);
                setOrvectors();
            }
        }

        //display bat also rotating
        void display() {
            //pushMatrix();
            rotatePlayerScreen(playerScreen,"acw");

            fill(batcolor[0],batcolor[1],batcolor[2],50);
            ellipse(width/2,height,2*moveradius,2*moveradius);

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
            rotatePlayerScreen(playerScreen,"cw");
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
                    if (mouseX >= (position.x - wid / 2)*zoom+(1-zoom)*zoompoint.x && mouseX <= (position.x + wid / 2)*zoom+(1-zoom)*zoompoint.x &&
                            mouseY >= (position.y - hei / 2)*zoom+(1-zoom)*zoompoint.y && mouseY <= (position.y + hei / 2)*zoom+(1-zoom)*zoompoint.y) {
                        value = true;
                    }
                } else {
                    value = false;
                }

            } else {
                if (mousePressed) {
                    if (flip && mouseX >= (position.x - wid / 2)*zoom+(1-zoom)*zoompoint.x && mouseX <= (position.x + wid / 2)*zoom+(1-zoom)*zoompoint.x &&
                            mouseY >= (position.y - hei / 2)*zoom +(1-zoom)*zoompoint.y && mouseY <= (position.y + hei / 2)*zoom+(1-zoom)*zoompoint.y) {
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

