package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

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

    private FirebaseAuth mAuth = FirebaseAuth.getInstance(); // Initialising an instance of FirebaseAuth.
    FirebaseDatabase db = FirebaseDatabase.getInstance(); // Initialising an instance of FirebaseDatabase.

    TextView firstNameTV, surnameTV, currentHeightTV, goalWeightTV, dailyCalorieGoalTV, metricChoiceTV, emailTV;
    EditText firstNameET, surnameET, currentHeightET, goalWeightET, dailyCalorieGoalET, emailET;
    ImageView logoImg;
    Button confirmBtn;
    Switch metricSwitch;

    Boolean useMetric;
    User u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firstNameTV = findViewById(R.id.usersNameLabelTV_settings);
        surnameTV = findViewById(R.id.usersSurnameLabelTV_settings);
        currentHeightTV = findViewById(R.id.currentHeightTV_settings);
        goalWeightTV = findViewById(R.id.goalWeightTV_settings);
        dailyCalorieGoalTV = findViewById(R.id.calorieGoalTV_settings);
        metricChoiceTV = findViewById(R.id.useMetricTV_settings);
        emailTV = findViewById(R.id.usersEmailLabelTV_settings);
        firstNameET = findViewById(R.id.usersNameET_settings);
        surnameET = findViewById(R.id.usersSurnameET_settings);
        currentHeightET = findViewById(R.id.currentHeightNumET_settings);
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

                //Getting the values from the user input fields and updating the object in the realtime database.
                String firstName = firstNameET.getText().toString().trim();
                String surname = surnameET.getText().toString().trim();
                double goalCal = Double.parseDouble(dailyCalorieGoalET.getText().toString());
                double currentHeight = Double.parseDouble(currentHeightET.getText().toString());
                double goalWeight = Double.parseDouble(goalWeightET.getText().toString());


                // If Statements for data validation.
                if (firstName.equals("") || surname.equals("") || currentHeight <= 0 || goalWeight <= 0 || goalCal <= 0) {
                    Toast.makeText(SettingsActivity.this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
                } else if (useMetric && currentHeight > 220) {
                    Toast.makeText(SettingsActivity.this, "Height cannot exceed 220cm!", Toast.LENGTH_SHORT).show();
                } else if (!useMetric && currentHeight > 7.217847769) // 7.217847769ft is exactly equivalent to 220cm
                {
                    Toast.makeText(SettingsActivity.this, "Height cannot exceed 7.2ft!", Toast.LENGTH_SHORT).show();
                } else if (useMetric && goalWeight > 250) {
                    Toast.makeText(SettingsActivity.this, "Goal Weight cannot exceed 250kg!", Toast.LENGTH_SHORT).show();
                } else if (!useMetric && goalWeight > 551.25) {
                    Toast.makeText(SettingsActivity.this, "Goal Weight cannot exceed 551.25lb!", Toast.LENGTH_SHORT).show();
                } else if (goalCal > 10000) {
                    Toast.makeText(SettingsActivity.this, "Goal calories cannot exceed 10 000 calories!", Toast.LENGTH_SHORT).show();
                } else {

                    // If Statements to control the data being pushed to Firebase.
                    if (!useMetric) {
                        currentHeight = Double.parseDouble(currentHeightET.getText().toString()) * 30.48;
                        goalWeight = Double.parseDouble(goalWeightET.getText().toString()) / 2.205;
                    }
                    try {
                        DatabaseReference captureUserInfo = db.getReference(mAuth.getCurrentUser().getUid()); // Getting the current user's UID
                        User u = new User(firstName, surname, goalWeight, currentHeight, goalCal, useMetric);
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
                    goalWeightTV.setText(R.string.goal_weight_imp);
                    double impGoalWeight = Double.parseDouble(goalWeightET.getText().toString()) * 2.205;
                    goalWeightET.setText(impGoalWeight + "");

                    useMetric = false; // Setting the user's unit system choice to imperial.
                }
            }
        });
    }

    // Method to populate all the fields with the user's data.
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

                    if (!u.getUseMetric()) {
                        metricSwitch.setChecked(false);
                        useMetric = false; // Setting the user's unit system choice to imperial.
                        // Handling imperial height.
                        currentHeightTV.setText(R.string.height_imp);
                        double impHeight = u.getCurrentHeight() / 30.48;
                        currentHeightET.setText(impHeight + "");

                        // Handling imperial goal weight.
                        goalWeightTV.setText(R.string.goal_weight_imp);
                        double impWeightGoal = u.getGoalWeight() * 2.205;
                        goalWeightET.setText(impWeightGoal + "");
                    } else {
                        metricSwitch.setChecked(true);
                        currentHeightET.setText(u.getCurrentHeight() + "");
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void Resume() {
        PopulateUserData();
    }
}
