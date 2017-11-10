package com.solutions.isecpowify.smarthome;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
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
    TextView tv,lightValue,humValue,tempValue,msg;
    DatabaseReference userDB,sensorDB;
    Switch inStatus,outStatus;
    CardView statusCard,roomCard;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = Helpers.getSharedPreferences(getApplicationContext());
        userDB = FirebaseDatabase.getInstance().getReference().child("users");
        sensorDB = FirebaseDatabase.getInstance().getReference().child("state");

        if( sp.contains(getString(R.string.OTRK)) ){

            setContentView(R.layout.activity_main);
            selectItem(Constants.SMART_HOME_OP);
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

    void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new OptionsFragment(MainActivity.this);
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_OPTION_NUMBER, position);

        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        setTitle(Constants.OptionTitles[position-1]);
    }

    private void showKeyboardByDefault(EditText pin) {
        pin.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static class OptionsFragment extends Fragment {

        private MainActivity current;

        public OptionsFragment() {
            // Empty constructor required for fragment subclasses
        }

        @SuppressLint("ValidFragment")
        public OptionsFragment(MainActivity current) {
            this.current = current;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int option = getArguments().getInt(Constants.ARG_OPTION_NUMBER);
            View rootView = inflateCorrectFragment(option, inflater, container);
            setView(rootView, option, current);
            return rootView;
        }

        private View inflateCorrectFragment(int option, LayoutInflater inflater, ViewGroup container) {
            switch (option) {
                case Constants.SENSOR_OP_CODE:
                    return inflater.inflate(R.layout.fragment_sensors, container, false);
                case Constants.SMART_HOME_OP:
                default:
                    return inflater.inflate(R.layout.fragment_smart_home, container, false);
            }
        }

        private void setView(View rootView, int option, MainActivity current) {
            String optionName = Constants.OptionTitles[option-1];
            switch (option) {
                case Constants.SENSOR_OP_CODE:
                    if (current != null && rootView != null)
                        Helpers.configSensorData(rootView, current);
                    break;
                case Constants.SMART_HOME_OP:
                    if (current != null && rootView != null)
                        Helpers.configSmartHome(rootView, current);
                default:
                    ;
            }
            getActivity().setTitle(optionName);
        }
    }
}
