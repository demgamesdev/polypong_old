package com.demgames.polypong;
import processing.event.MouseEvent;
import processing.core.*;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

//importing processing libraries
import processing.core.*;
import oscP5.*;
import netP5.*;

//Todo Gegner Schläger hinzufügen
//Todo Datenübertragung für ball und schläger von host auf client
//Todo Datenübertragung schläger von client zu host

public class SketchRaphael extends PApplet {

    private static final String TAG = "MyActivity";

    Activity myActivity;
    Context myContext;

    OscP5 oscP5;
    NetAddress myRemoteLocation;

    //listening port of application
    int port = 12000;
    String myipadress;
    String remoteipadress;
    String clientname = "test";
    String mode;
    float zoom = 1;
    int myplayerscreen = 0;
    Float frict;

    /********* VARIABLES *********/

    //declare game variables
    float batfrict = (float) 0.05;
    float gravityMag = (float) 0.6;
    float attractionMag = 200;
    float inelast = (float) 0.6;
    float ballspring = 3;
    float batspring = 6;
    float mousevelocity = 1;

    float width0, height0;

    boolean gravityState;
    boolean attractionState;

    int framecounter = 0;
    int gameScreen = 1;

    int numberofballs;

    //define pvector of last touch event
    PVector mouselast = new PVector(0, 0);
    PVector zoompoint;

    //constructor which is called in gamelaunch


    //PVector contains x, y, z components for each of the elements listed. Used for positioning.
    PVector player, enemy, ball;

    //speeds for things that are moving automatically (the ball and the enemy)
    float ballSpeedX, ballSpeedY, enemySpeed;

    //declare and initializing scores
    int playerScore = 0;
    int enemyScore = 0;

    //declaring ball size
    float ballSize;


    //sensor stuff
    float accelerometerX, accelerometerY, accelerometerZ;

    SketchRaphael(String mode_, String myipadress_, String remoteipadress_, String numberofballs_, Float friction_) {

        mode = mode_;
        Log.d(TAG, "SketchRaphael: mode" + mode);


        if (mode.equals("client")) {
            myplayerscreen = 1;
        }

    }


    //set size of canvas
    public void settings() {
        fullScreen();
        smooth();
        //frameRate(60);

    }


    public void setup() {

        myActivity=this.getActivity();
        myContext = myActivity.getApplicationContext();
        Globals globalVariables = (Globals) myContext;

        myipadress=globalVariables.getMyIpAdress();
        remoteipadress=globalVariables.getRemoteIpAdress();

        myRemoteLocation=new NetAddress(remoteipadress,port);

        //initialize oscp5 object for sending and receiving messages
        oscP5 = new OscP5(this,port);

        ball = new PVector(width / 2, height / 2);
        player = new PVector(width, height);
        enemy = new PVector(0, height / 2);
         /*
    Instead of using actual numbers, we're using ratios of width and height for different objects here.
    This is to ensure that visual elements retain their relative sizes when running on multiple resolutions.
    */

         /*
    Same idea as using ratios for sizes. We want the ball to move at relatively same speed across multiple resolutions, so we're using ratios.
    */
        ballSpeedX = width / 100;
        ballSpeedY = width / 100;
        enemySpeed = width / 150;
        ballSize = width / 20;
        rectMode(CENTER);
        centerLine();
    }

    public void draw() {
        //background is important for clearing the frame every frame, so that there is nothing remaining from the previous frame drawn
        background(0);

        //calling methods for drawing the ball, the player, the enemy, and the scores
        if (mode.equals("host")){
            drawBallHost();
            drawPlayer();
            drawEnemy();
            scoreText();
            sendBall();
            //drawEnemy();
        }
        else if (mode.equals("client")){
            drawBallClient();
            drawPlayer();
            scoreText();
            drawEnemy();
        }

    }

    void drawBallHost() {
        pushMatrix();
        translate(ball.x, ball.y);
        fill(255);
        noStroke();
        ellipse(0, 0, width / 20, width / 20);
        popMatrix();
        ball.x += ballSpeedX;
        ball.y += ballSpeedY;
        ballBoundary();

    }

    void drawBallClient() {
        pushMatrix();
        translate(ball.x, ball.y);
        fill(255);
        noStroke();
        ellipse(0, 0, width / 20, width / 20);
        popMatrix();
        Log.d(TAG, "drawBallClient: Ich stelle den Client ball dar");
    }

    void ballBoundary()
    {

    //left
    if (ball.x < 0) {
        ball.x = 0;
        ballSpeedX *= -1;
    }

//
//      //right
    if (ball.x > width) {
        ball.x = width;
        ballSpeedX *= -1;
    }

    float playerDist = ball.dist(player);
    float enemyDist = ball.dist(enemy);

    if (ball.y > height) {  //Ball ist unten
        enemyScore ++;
        ball.y = height/2;
        ballSpeedY = 0;
        Thread timer = new Thread(){
            public void  run(){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    ball.y = height/2;
                    ballSpeedY = width / 100;
                    ballSpeedY *= -1;
                }
            }
        };
        timer.start();
    }

    if (ball.y < 0) {
        playerScore ++;
        ball.y = height/2;
        ballSpeedY = 0;
        Thread timer = new Thread(){
            public void  run(){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    ball.y = height/2;
                    ballSpeedY = width / 100;
                    ballSpeedY *= 1;

                }
            }
        };
        timer.start();

    }

    //Schläger kollision
        if (ball.y > height - height/40 - ballSize && ball.y < height && Math.abs(ball.x - player.x) < width/10) {
            ball.y = height - height/40 - ballSize;
            ballSpeedY *= 1.1;
            ballSpeedY *= -1;
        }

        if (ball.y < height/40 + ballSize && ball.y > 0 && Math.abs(ball.x - enemy.x) < width/10) {
            ball.y = height/40 + ballSize;
            ballSpeedY *= 1.1;
            ballSpeedY *= -1;
        }
    }


    void drawPlayer()
    {
        player.x = mouseX;

        OscMessage posMessage = new OscMessage("/bat");
        posMessage.add(player.x/width);
        oscP5.send(posMessage, myRemoteLocation);

        pushMatrix();
        translate(player.x , height -width/20);
        stroke(0);
        fill(255);
        rect(0, 0, width/5,width/20); //Größe des Schlägers
        //box(width/20, width/5, width/50);
        popMatrix();
    }

    void drawEnemy()
    {
        //player.x = mouseX;

        pushMatrix();
        translate(enemy.x ,  width/20);
        stroke(0);
        fill(255);
        rect(0, 0, width/5,width/20); //Größe des Schlägers
        //box(width/20, width/5, width/50);
        popMatrix();
        Log.d(TAG, "drawEnemy: zeichne");
    }

    void scoreText()
    {
        textSize(width/20);
        if (mode.equals("host") ){
            fill(255);
            textSize(width/20);
            text(playerScore, width/2, height - width /2);
            text(enemyScore, width/2, height/5);
            OscMessage scoreMessage = new OscMessage("/score");
            scoreMessage.add(playerScore);
            scoreMessage.add(enemyScore);
            oscP5.send(scoreMessage, myRemoteLocation);
            //Log.d(TAG, "scoreText: Ich sende den Score");
        }
        if (mode.equals("client") ){
            text(enemyScore, width/2, height - width /2);
            text(playerScore, width/2, height/5);
            //Log.d(TAG, "oscEvent: Ich stelle den score dar");
        }

    }

    //send ball data to remotelocation
    void sendBall() {
        OscMessage posMessage = new OscMessage("/position");
        posMessage.add(ball.x/width);
        posMessage.add(ball.y/height);
        oscP5.send(posMessage, myRemoteLocation);
        Log.d(TAG, "sendBall: " + Float.toString(ball.x) + " " + Float.toString(ball.y));
        //OscMessage attrMessage = new OscMessage("/ball/"+str(theBall.ballnumber)+"/attributes");
        //attrMessage.add(theBall.radius/width);
        //attrMessage.add(theBall.m);
        //oscP5.send(attrMessage,myRemoteLocation);
    }

    void centerLine()
    {
        int numberOfLines = 20;
        Log.d(TAG, "centerLine: Ich maledie center line");
        strokeWeight(width/100);
        stroke(255);
        line(0, height/2, width, height/2);
        for (int i = 0; i < numberOfLines; i++) {
            strokeWeight(width/100);
            stroke(255);
            line(i * width/numberOfLines, width/2, (i+1) * width/numberOfLines - width/40, height/2);
            stroke(0, 0);
            line((i+1) * width/numberOfLines - width/40, width/2, (i+1) * width/numberOfLines,height/2);
            
        }
    }


    //check for incoming osc messages
    void oscEvent(OscMessage theOscMessage) {

            if(theOscMessage.addrPattern().equals("/position")) {
                ball.x=-((theOscMessage.get(0).floatValue()*width)-width);
                ball.y=-((theOscMessage.get(1).floatValue()*height)-height);
                Log.d(TAG, "receiveBall: " + Float.toString(ball.x) + " " + Float.toString(ball.y));
                //Log.d(TAG, "oscEvent: Ich empfange den ball");
                //println("position: ",balls[i].position.x,balls[i].position.y);
            }
            if(theOscMessage.addrPattern().equals("/score")) {
                playerScore=theOscMessage.get(0).intValue();
                enemyScore=theOscMessage.get(1).intValue();
                //Log.d(TAG, "oscEvent: Ich empfange den score");
            }

            if(theOscMessage.addrPattern().equals("/bat")) {
                Log.d(TAG, "Bat: Ich empfange den x Wert" + Float.toString(enemy.x));
                enemy.x = -((theOscMessage.get(0).floatValue()*width)-width);
            }
    }

}
