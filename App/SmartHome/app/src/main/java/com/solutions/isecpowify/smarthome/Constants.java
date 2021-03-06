package com.solutions.isecpowify.smarthome;

/**
 * Created by mayank on 5/11/17.
 */

class Constants {
    static final String TAG = "LOG_V";
    static final int SMART_HOME_OP = 1;
    static final int SENSOR_OP_CODE = 2;
    static final int ALERTS_OP_CODE = 3;
    static final String[] OptionTitles = new String[]{"SMART HOME", "SENSORS", "ALERTS"};
    static final String ARG_OPTION_NUMBER = "OPTION_INDEX";
    static final String DEVICE_REG_SERVER = "http://isecpowify-server.herokuapp.com/u/device";
}
