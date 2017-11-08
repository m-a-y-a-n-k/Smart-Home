package com.solutions.isecpowify.smarthome;

/**
 * Created by mayank on 7/11/17.
 */

class State {
    public Double temp,humidity,light,ax,ay,az,timestamp,latestIndoorMovement,latestOutdoorMovement;
    public boolean inDoorMotion,outDoorMotion;
    State(){
        // empty constructor
    }
}
