package com.solutions.isecpowify.smarthome;

/**
 * Created by mayank on 10/11/17.
 */

public class Alert {

    private int icon;
    public String title,type,message;
    public Long timestamp;

    public Alert(){
        // empty constructor
    }

    void setIcon(){
        switch(type){
            case "FIRE_ALERT":
                icon = R.drawable.ic_flame;
                break;
            case "INTRUDER_ALERT":
                icon = R.drawable.ic_intruder;
                break;
            case "POWER_ALERT":
                icon = R.drawable.ic_power;
                break;
            default:
                icon = R.drawable.ic_notifs;
        }
    }

    int getIconResource(){
        return icon;
    }
}
