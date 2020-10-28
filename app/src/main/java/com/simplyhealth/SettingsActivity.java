package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Declaring an instance of FirebaseAuth
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    TextView firstNameTV, surnameTV, currentHeightTV, currentWeightTV, goalWeightTV, dailyCalorieGoalTV, metricChoiceTV, emailTV;
    EditText firstNameET, surnameET, currentHeightET, currentWeightET, goalWeightET, dailyCalorieGoalET, emailET;
    ImageView logoImg;
    Button confirmBtn;
    Switch metricSwitch;

    Boolean useMetric;
    User u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance(); // Initializing the FirebaseAuth instance.

        firstNameTV = findViewById(R.id.usersNameLabelTV_settings);
        surnameTV = findViewById(R.id.usersSurnameLabelTV_settings);
        currentHeightTV = findViewById(R.id.currentHeightTV_settings);
        currentWeightTV = findViewById(R.id.currentWeightTV_settings);
        goalWeightTV = findViewById(R.id.goalWeightTV_settings);
        dailyCalorieGoalTV = findViewById(R.id.calorieGoalTV_settings);
        metricChoiceTV = findViewById(R.id.useMetricTV_settings);
        emailTV = findViewById(R.id.usersEmailLabelTV_settings);

        firstNameET = findViewById(R.id.usersNameET_settings);
        surnameET = findViewById(R.id.usersSurnameET_settings);
        currentHeightET = findViewById(R.id.currentHeightNumET_settings);
        currentWeightET = findViewById(R.id.currentWeightNumET_settings);
        goalWeightET = findViewById(R.id.goalWeightNumET_settings);
        dailyCalorieGoalET = findViewById(R.id.calorieGoalNumET_settings);
        emailET = findViewById(R.id.userEmailET_settings);

        logoImg = findViewById(R.id.LogoImgView_settings);

        confirmBtn = findViewById(R.id.confirmBtn_settings);

        metricSwitch = findViewById(R.id.metricSwitch_settings);


        PopulateUserData();

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // update firebase info

                //Getting the values from the user input fields and updating the object in the realtime database.
                String firstName = firstNameET.getText().toString().trim();
                String surname = surnameET.getText().toString().trim();
                double goalCal = Double.parseDouble(dailyCalorieGoalET.getText().toString());
                double currentHeight;
                double currentWeight;
                double goalWeight;



                if (useMetric == false) {
                    currentHeight = Double.parseDouble(currentHeightET.getText().toString()) * 30.48;
                    currentWeight = Double.parseDouble(currentWeightET.getText().toString()) / 2.205;
                    goalWeight = Double.parseDouble(goalWeightET.getText().toString()) / 2.205;
                    metricSwitch.setChecked(false);
                } else {
                    currentHeight = Double.parseDouble(currentHeightET.getText().toString());
                    currentWeight = Double.parseDouble(currentWeightET.getText().toString());
                    goalWeight = Double.parseDouble(goalWeightET.getText().toString());
                    metricSwitch.setChecked(true);
                }

                // Data validation
                if (firstName.equals("") || surname.equals("") || currentHeight <= 0 || currentWeight <= 0 || goalWeight <= 0 || goalCal <= 0)
                {
                    Toast.makeText(SettingsActivity.this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    try {
                        DatabaseReference captureUserInfo = db.getReference(mAuth.getCurrentUser().getUid()); // Getting the current user's UID
                        User u = new User(firstName, surname, currentWeight, goalWeight, currentHeight, goalCal, useMetric);
                        captureUserInfo.child("User Details").setValue(u);
                        Toast.makeText(SettingsActivity.this, "Settings Successfully Updated", Toast.LENGTH_SHORT).show(); // User validation message.
                    } catch (Exception e) {
                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                
            }
        });

        metricSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (metricSwitch.isChecked()) {
                    // Handling conversion to metric height during runtime (users can enter imperial values and have them converted to metric)
                    currentHeightTV.setText(R.string.height_met);
                    double metHeight = Double.parseDouble(currentHeightET.getText().toString()) * 30.48;
                    currentHeightET.setText(metHeight + "");

                    // Handling conversion to metric weight during runtime (users can enter imperial values and have them converted to metric)
                    currentWeightTV.setText(R.string.weight_met);
                    double metWeight = Double.parseDouble(currentWeightET.getText().toString()) / 2.205;
                    currentWeightET.setText(metWeight + "");

                    // Handling conversion to metric weight during runtime (users can enter imperial values and have them converted to metric)
                    goalWeightTV.setText(R.string.goal_weight_met);
                    double metGoalWeight = Double.parseDouble(goalWeightET.getText().toString()) / 2.205;
                    goalWeightET.setText(metGoalWeight + "");

                    useMetric = true; // Setting the user's unit system choice to metric.
                } else {
                    // Handling conversion to imperial height during runtime (users can enter metric values and have them converted to imperial)
                    currentHeightTV.setText(R.string.height_imp);
                    double impHeight = Double.parseDouble(currentHeightET.getText().toString()) / 30.48;
                    currentHeightET.setText(impHeight + "");

                    // Handling conversion to imperial weight during runtime (users can enter metric values and have them converted to imperial)
                    currentWeightTV.setText(R.string.weight_imp);
                    double impWeight = Double.parseDouble(currentWeightET.getText().toString()) * 2.205;
                    currentWeightET.setText(impWeight + "");

                    // Handling conversion to imperial weight during runtime (users can enter metric values and have them converted to imperial)
                    goalWeightTV.setText(R.string.goal_weight_imp);
                    double impGoalWeight = Double.parseDouble(goalWeightET.getText().toString()) * 2.205;
                    goalWeightET.setText(impGoalWeight + "");

                    useMetric = false; // Setting the user's unit system choice to imperial.
                }
            }
        });
    }

    public void PopulateUserData() {
        try {
            DatabaseReference ref = db.getReference(mAuth.getCurrentUser().getUid());
            ref.child("User Details").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    u = snapshot.getValue(User.class);

                    firstNameET.setText(u.getFirstName());
                    surnameET.setText(u.getSurname());
                    emailET.setText(mAuth.getCurrentUser().getEmail());
                    dailyCalorieGoalET.setText(u.getGoalCalories() + "");

                    if (u.getUseMetric() == false) {
                        metricSwitch.setChecked(false);
                        useMetric = false; // Setting the user's unit system choice to imperial.
                        // Handling imperial height.
                        currentHeightTV.setText(R.string.height_imp);
                        double impHeight = Math.round(u.getCurrentHeight() / 30.48);
                        currentHeightET.setText(impHeight + "");

                        // Handling imperial weight.
                        currentWeightTV.setText(R.string.weight_imp);
                        double impWeight = u.getCurrentWeight() * 2.205;
                        currentWeightET.setText(impWeight + "");

                        // Handling imperial goal weight.
                        goalWeightTV.setText(R.string.goal_weight_imp);
                        double impWeightGoal = u.getGoalWeight() * 2.205;
                        goalWeightET.setText(impWeightGoal + "");
                    } else {
                        metricSwitch.setChecked(true);
                        currentHeightET.setText(u.getCurrentHeight() + "");
                        currentWeightET.setText(u.getCurrentWeight() + "");
                        goalWeightET.setText(u.getGoalWeight() + "");
                        useMetric = true; // Setting the user's unit system choice to metric.
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SettingsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}