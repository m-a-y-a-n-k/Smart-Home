package com.solutions.isecpowify.smarthome;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sp;
    EditText OTRK;
    TextView tv;
    DatabaseReference userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = Helpers.getSharedPreferences(getApplicationContext());
        userDB = FirebaseDatabase.getInstance().getReference().child("users");

        if( sp.contains(getString(R.string.OTRK)) ){
            setContentView(R.layout.activity_main);

            tv = (TextView)findViewById(R.id.welcome);
            findViewById(R.id.homePage)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.app_background));
            Helpers.fixBackgroundRepeat(findViewById(R.id.homePage));

            userDB.child(sp.getString(getString(R.string.OTRK),"")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User regUser = dataSnapshot.getValue(User.class);
                    if(regUser != null ){
                        tv.setText("Registered as :\nName- " + regUser.name + "\nPhone- " + regUser.contact);
                    } else{
                        tv.setText("Welcome !. You are not registered in our DataBase ");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.v(Constants.TAG,"The read failed: " + databaseError.getCode());
                }
            });
        } else {
            setContentView(R.layout.key_spec_layout);
            findViewById(R.id.keySpecLayout)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.app_background));
            Helpers.fixBackgroundRepeat(findViewById(R.id.keySpecLayout));

            tv = (TextView) findViewById(R.id.otrMsg);
            OTRK = (EditText)findViewById(R.id.OTR);
            FloatingActionButton otrFAB = (FloatingActionButton)findViewById(R.id.otr_fab);

            showKeyboardByDefault(OTRK);
            OTRK.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        Helpers.hideKeyboard(v,getApplicationContext());
                    }
                }
            });

            otrFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String code = OTRK.getText().toString();
                    if( code.length() > 0 ){

                        userDB.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if( dataSnapshot.getValue() != null ){
                                    sp.edit().putString(getString(R.string.OTRK),dataSnapshot.getKey()).apply();
                                    Helpers.restartApplication(MainActivity.this);
                                } else {
                                    Log.v(Constants.TAG,"OTR does not exist !!");
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.v(Constants.TAG,"The read failed: " + databaseError.getCode());
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(),"OTR required",Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    private void showKeyboardByDefault(EditText pin) {
        pin.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
