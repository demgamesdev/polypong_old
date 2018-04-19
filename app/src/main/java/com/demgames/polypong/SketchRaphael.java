package com.demgames.polypong;
import processing.event.MouseEvent;
import processing.core.*;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

//importing processing libraries
import com.demgames.polypong.network.sendclasses.SendBallKinetics;
import com.demgames.polypong.network.sendclasses.SendBat;
import com.demgames.polypong.network.sendclasses.SendScore;

import java.util.ArrayList;
import java.util.List;

import processing.core.*;
import oscP5.*;
import netP5.*;

//Todo Ball wie im anderen Sketch von einem Bildschirm zum nächsten schießen lassen (als option)

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

    Globals globalVariables;

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

    int interval = 5;
    int t;
    String time = "5";

    //define pvector of last touch event
    PVector mouselast = new PVector(0, 0);
    PVector zoompoint;


    SendBallKinetics sendBallKinetics=new SendBallKinetics();
    SendBat sendBat=new SendBat();
    SendScore sendScore=new SendScore();



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
        globalVariables=(Globals)myContext;
        //Globals globalVariables = (Globals) myContext;

        myipadress=globalVariables.getMyIpAdress();
        remoteipadress=globalVariables.getRemoteIpAdress();

        myRemoteLocation=new NetAddress(remoteipadress,port);

        //initialize oscp5 object for sending and receiving messages
        oscP5 = new OscP5(this,port);

        ball = new PVector(width / 2, height / 2);
        player = new PVector(width, height);
        enemy = new PVector(0, height / 2);

        ballSpeedX = width / 100;
        ballSpeedY = width / 100;
        enemySpeed = width / 150;
        ballSize = width / 20;
        rectMode(CENTER);

        //Countdown
        fill(255);
        textSize(width/10);
        textAlign(CENTER);
        text("3", width/2, height/2);
    }

    public void draw() {
        t = interval-(millis()/1000);
        time = nf(t , 1);
        background(0);
        if(t <= 0) {
            if (mode.equals("host")){
                drawBallHost();
                drawPlayer();
                drawEnemy();
                scoreText();
                sendBall();
                centerLine();
            }
            else if (mode.equals("client")){
                drawBallClient();
                drawPlayer();
                scoreText();
                drawEnemy();
                centerLine();
            }
        }

        else if (t >= 0){
            //Todo Countown zwischen Server und Client synchronisieren
            //Todo Client start mit Serverstart abgleichen bei Countdown
            text("Spiel beginnt in " + time, width/2, height/2);
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
        ball.x = -((globalVariables.getBallsPositions()[0].x)*width-width);
        ball.y = -((globalVariables.getBallsPositions()[0].y*height)-height);

        translate(ball.x, ball.y);
        fill(255);
        noStroke();
        ellipse(0, 0, width / 20, width / 20);
        popMatrix();
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
        sendScore();
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
        sendScore();
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
            //MediaPlayer pongSound = MediaPlayer.create(getContext(), R.raw.pong_bat);
            //pongSound.start();
            ball.y = height - height/40 - ballSize;
            ballSpeedY *= 1.1;
            ballSpeedY *= -1;
        }

        if (ball.y < height/40 + ballSize && ball.y > 0 && Math.abs(ball.x - enemy.x) < width/10) {
            //MediaPlayer pongSound = MediaPlayer.create(getContext(), R.raw.pong_bat);
            //pongSound.start();
            ball.y = height/40 + ballSize;
            ballSpeedY *= 1.1;
            ballSpeedY *= -1;
        }
    }


    void drawPlayer()
    {
        player.x = mouseX;
        sendBat();
        pushMatrix();
        translate(player.x , height -width/20);
        stroke(0);
        fill(255);
        rect(0, 0, width/5,width/20); //Größe des Schlägers
        popMatrix();
    }

    void drawEnemy()
    {
        pushMatrix();
        if (globalVariables.getBatPosition()!=null){
            enemy.x=-(width*globalVariables.getBatPosition().x-width);
        }
        else{
            enemy.x=width/2;
        }
        translate(enemy.x ,  width/20);
        stroke(0);
        fill(255);
        rect(0, 0, width/5,width/20); //Größe des Schlägers
        popMatrix();
    }

    void scoreText()
    {
        List<String> nameList = new ArrayList<String>();
        nameList=globalVariables.getPlayerNamesList();
        String player = nameList.get(0) + ": "; //Eigener Name
        String enemy = nameList.get(1) + ": ";
        if (mode.equals("host") ){
            fill(255);
            textSize(width/22);
            textAlign(CENTER);
            text(player + playerScore, width/2, height - width /2);
            text( enemy +enemyScore, width/2, height/5);
        }
        if (mode.equals("client") ){
            textSize(width/22);
            textAlign(CENTER);
            text(player + globalVariables.getOtherScore(), width/2, height - width /2);
            text(enemy + globalVariables.getMyScore(), width/2, width /2);
        }
    }

    //send ball data to remotelocation
    void sendBall() {
        //Globals globalVariables = (Globals) myContext;
        sendBallKinetics.ballNumber=0;
        sendBallKinetics.ballPosition=new PVector(ball.x/width,ball.y/height);
        sendBallKinetics.ballVelocity=new PVector(0,0);
        if(mode.equals("host")) {
            globalVariables.getConnectionList()[0].sendTCP(sendBallKinetics);
        }
        else if(mode.equals("client")) {
            globalVariables.getClient().sendUDP(sendBallKinetics);
        }
    }

    void sendBat() {
        //Globals globalVariables = (Globals) myContext;
        sendBat.batPosition=new PVector(player.x/width,player.y/height);
        sendBat.batOrientation=0;
        if(mode.equals("host")) {
            globalVariables.getConnectionList()[0].sendTCP(sendBat);
        } else if(mode.equals("client")) {
            globalVariables.getClient().sendUDP(sendBat);
        }
    }


    void sendScore() {
        //Globals globalVariables = (Globals) myContext;
        sendScore.myScore=playerScore;
        sendScore.otherScore=enemyScore;

        if(mode.equals("host")) {
            globalVariables.getConnectionList()[0].sendTCP(sendScore);
        } else if(mode.equals("client")) {
            globalVariables.getClient().sendTCP(sendScore);
        }
    }

    void centerLine()
    {
        int numberOfLines = 20;
        strokeWeight(width/100);
        stroke(255);
        line(0, height/2, width, height/2);
        /*for (int i = 0; i < numberOfLines; i++) {
            //strokeWeight(width/100);
            ///stroke(255);
            line(i * width/numberOfLines, height/2, (i+1) * width/numberOfLines - width/40, height/2);
            stroke(0, 0);
            line((i+1) * width/numberOfLines - width/40, height/2, (i+1) * width/numberOfLines,height/2);
            
        }*/
    }



}