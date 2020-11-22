package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.lang.Double.*;

public class RecordWeight extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    Button confirmBtn;
    EditText weightET;
    ImageView logoImg;
    User u;
    Boolean useMetric, needToUpdate;
    DailyWeightInfo d;
    DailyWeightInfo checkIfExists;
    String key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_weight);

        confirmBtn = findViewById(R.id.confirm_button_cap);
        weightET = findViewById(R.id.recordWeightET_cap);
        logoImg = findViewById(R.id.LogoImgView_cap);


        needToUpdate = false;

        mAuth = FirebaseAuth.getInstance();
        SetMeasurement(); // Calls the method to update the UI based on measurement system.

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                final String recordDate = df.format(date);

                double weight = parseDouble(weightET.getText().toString());

                if (weight <= 0)
                {
                    Toast.makeText(RecordWeight.this, "Please enter valid information!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (useMetric == false){
                        weight = weight / 2.205; // Converting the value back to a metric system value.
                    }

                    //final double userWeight = weight;
                    try {
                        final DatabaseReference captureUserInfo = db.getReference(mAuth.getCurrentUser().getUid()); // Getting the current user's UID
                        d = new DailyWeightInfo(recordDate,weight);


                        //Boolean needToUpdate = false;
                        //String key ="";

                        captureUserInfo.child("Daily Weight").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {



                                for (DataSnapshot daily : snapshot.getChildren()){

                                    checkIfExists = daily.getValue(DailyWeightInfo.class);

                                    if (checkIfExists.getCaptureDate().equals(recordDate))
                                    {
                                        needToUpdate = true;
                                        key = daily.getKey();
                                        break;
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(RecordWeight.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }) ;

                        if (needToUpdate == true)
                        {

                            //captureUserInfo.child("Daily Weight").child(key).child("weight").setValue(userWeight); // doesn't work - creates new object
                            captureUserInfo.child("Daily Weight").child(key).setValue(d); // doesn't work - creates new object
                            //Toast.makeText(RecordWeight.this, key, Toast.LENGTH_LONG).show();

                            //Log.d("tag",key);
                        }
                        else{
                            captureUserInfo.child("Daily Weight").push().setValue(d);
                        }

                         Toast.makeText(RecordWeight.this, "Daily weight successfully captured!", Toast.LENGTH_SHORT).show();

                    } catch (Exception e){
                        Toast.makeText(RecordWeight.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    // Updates the UI based on the user's selected measurement system.
    public void SetMeasurement(){
        try {
            DatabaseReference ref = db.getReference(mAuth.getCurrentUser().getUid());
            ref.child("User Details").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    u = snapshot.getValue(User.class);

                    if (u.getUseMetric() == false){
                        useMetric = false;
                        weightET.setHint(R.string.record_weight_imp);
                    }
                    else
                    {
                        useMetric = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RecordWeight.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void Resume() {
        SetMeasurement();
    }

}