package com.solutions.isecpowify.smarthome;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by mayank on 5/11/17.
 */

class Helpers {

    static SharedPreferences getSharedPreferences(Context appContext) {
        return appContext.getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    static void fixBackgroundRepeat(View view) {
        Drawable bg = view.getBackground();
        if (bg != null) {
            if (bg instanceof BitmapDrawable) {
                BitmapDrawable bmp = (BitmapDrawable) bg;
                bmp.mutate();
                bmp.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            }
        }
    }

    static void hideKeyboard(View view, Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    static void restartApplication(Activity current) {

        Intent i = current.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( current.getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        current.finish();
        current.startActivity(i);
    }

    static void configSmartHome(View rootView, MainActivity current) {

        new DeviceTokenUpdater(current);
        DeviceTokenUpdater.sendDetailsToServer(FirebaseInstanceId.getInstance().getToken(),current);

        current.tv = rootView.findViewById(R.id.welcome);
        current.tv.setText("NO INTERNET CONNECTION");
        current.statusCard = rootView.findViewById(R.id.statusCard);
        rootView.findViewById(R.id.homePage)
                .setBackgroundDrawable(current.getResources().getDrawable(R.drawable.app_background));
        Helpers.fixBackgroundRepeat(rootView.findViewById(R.id.homePage));

        final MainActivity finalCurrent = current;
        final View v = rootView;

        current.userDB.child(current.sp.getString(current.getString(R.string.OTRK),"")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User regUser = dataSnapshot.getValue(User.class);
                if(regUser != null ){
                    finalCurrent.tv.setText("Registered User :\nName- " + regUser.name + "\nPhone- " + regUser.contact);
                    finalCurrent.statusCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finalCurrent.selectItem(Constants.SENSOR_OP_CODE);
                        }
                    });
                    setRoomEvent(v,finalCurrent);

                } else{
                    finalCurrent.tv.setText("Welcome !. You are not registered in our DataBase ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(Constants.TAG,"The read failed: " + databaseError.getCode());
            }
        });
    }

    private static void setRoomEvent(View rootView, MainActivity curr) {

        curr.msg = rootView.findViewById(R.id.join);
        curr.img = rootView.findViewById(R.id.room);
        curr.roomCard = rootView.findViewById(R.id.roomCard);

        final MainActivity current = curr;

        class roomJoin extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if( getSharedPreferences(current.getApplicationContext())
                        .contains(current.getString(R.string.joined))){
                    current.msg.setText("Exit");
                    current.img.setBackgroundDrawable(current.getDrawable(R.drawable.ic_exit));
                } else {
                    current.msg.setText("Enter");
                    current.img.setBackgroundDrawable(current.getDrawable(R.drawable.ic_enter));
                }
            }
        }

        new roomJoin().execute();
        current.sensorDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                State latestSensor = dataSnapshot.getValue(State.class);
                if( latestSensor != null ){

                    if( getSharedPreferences(current.getApplicationContext())
                            .contains(current.getString(R.string.joined))){
                        current.sensorDB
                                .child(dataSnapshot.getKey())
                                .child("members")
                                .child(
                                        getSharedPreferences(current.getApplicationContext())
                                                .getString(current.getString(R.string.OTRK),"NONE")
                                )
                                .setValue(true);
                    } else {

                        current.sensorDB
                                .child(dataSnapshot.getKey())
                                .child("members")
                                .child(
                                        getSharedPreferences(current.getApplicationContext())
                                                .getString(current.getString(R.string.OTRK),"NONE")
                                )
                                .setValue(null);
                    }

                    current.sensorDB
                            .child(dataSnapshot.getKey())
                            .child("members")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snap) {
                            if( snap.child(
                                    getSharedPreferences(current.getApplicationContext())
                                            .getString(current.getString(R.string.OTRK),"NONE")).exists() ){
                                getSharedPreferences(current.getApplicationContext())
                                        .edit()
                                        .putBoolean(current.getString(R.string.joined),true)
                                .apply();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    current.roomCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if( getSharedPreferences(current.getApplicationContext())
                                    .contains(current.getString(R.string.joined))){
                                getSharedPreferences(current.getApplicationContext())
                                        .edit()
                                        .remove(current.getString(R.string.joined))
                                        .apply();
                            } else {
                                getSharedPreferences(current.getApplicationContext())
                                        .edit()
                                        .putBoolean(current.getString(R.string.joined),true)
                                        .apply();
                            }
                        }
                    });
                    new roomJoin().execute();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static void configSensorData(View rootView, MainActivity current) {
        showLatestSensorReadings(rootView, current);
    }

    private static void showLatestSensorReadings(View rootView,MainActivity current) {

        rootView.findViewById(R.id.sensorsStatus)
                .setBackgroundDrawable(current.getResources().getDrawable(R.drawable.app_background));
        fixBackgroundRepeat(rootView.findViewById(R.id.sensorsStatus));

        current.lightValue = rootView.findViewById(R.id.lightValue);
        current.tempValue = rootView.findViewById(R.id.tempValue);
        current.humValue = rootView.findViewById(R.id.humValue);
        current.inStatus = rootView.findViewById(R.id.inSwitch);
        current.outStatus = rootView.findViewById(R.id.outSwitch);

        final MainActivity finalCurrent = current;

        current.sensorDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snap, String s) {
//                Log.v(Constants.TAG, String.valueOf(snap.getValue()));
                State latestSensor = snap.getValue(State.class);
                if( latestSensor != null ){
                    finalCurrent.lightValue.setText(String.valueOf(latestSensor.light));
                    finalCurrent.humValue.setText(String.valueOf(latestSensor.humidity));
                    finalCurrent.tempValue.setText(String.valueOf(latestSensor.temp));
                    finalCurrent.inStatus.setChecked(latestSensor.inDoorMotion);
                    finalCurrent.outStatus.setChecked(latestSensor.outDoorMotion);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        CardView back = rootView.findViewById(R.id.backCard);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack(finalCurrent);
            }
        });

    }

    private static void goBack(MainActivity current){
        current.selectItem(Constants.SMART_HOME_OP);
    }
}
