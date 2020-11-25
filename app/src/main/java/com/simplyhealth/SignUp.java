package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth; // Declaring an instance of FirebaseAuth
    FirebaseDatabase db; // Declaring an instance of FirebaseDatabase.

    EditText fNameET, sNameET, emailET, currentHeightET, goalWeightET, goalCalET, passwordET;
    Button btnConfirm;
    ImageView logoImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        fNameET = findViewById(R.id.usersNameET_signup);
        sNameET = findViewById(R.id.usersSurnameET_signup);
        emailET = findViewById(R.id.usersEmailET_signup);
        currentHeightET = findViewById(R.id.currentHeightET_signup);
        goalWeightET = findViewById(R.id.goalWeightET_signup);
        goalCalET = findViewById(R.id.calorieGoalNumET_signup);
        passwordET = findViewById(R.id.usersPasswordET_signup);
        btnConfirm = findViewById(R.id.btnSignUp_signup);
        logoImg = findViewById(R.id.LogoImgView_signup);

        mAuth = FirebaseAuth.getInstance(); // Initializing the FirebaseAuth instance.
        db = FirebaseDatabase.getInstance(); // Initializing the FirebaseDatabase instance.

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = emailET.getText().toString().trim();
                String password = passwordET.getText().toString();
                String firstName = fNameET.getText().toString().trim(); // trim() to remove whitespace from first name.
                String surname = sNameET.getText().toString().trim(); // trim() to remove whitespace from surname.
                double currentHeight = Double.parseDouble(currentHeightET.getText().toString());
                double goalWeight = Double.parseDouble(goalWeightET.getText().toString());
                double goalCal = Double.parseDouble(goalCalET.getText().toString());

                if (email.equals("") || password.equals("")) {
                    Toast.makeText(SignUp.this, "Please enter valid information!", Toast.LENGTH_SHORT).show();
                }

                else if (firstName.equals("") || surname.equals("") || currentHeight <= 0 || goalWeight <= 0 || goalCal <= 0) {
                    Toast.makeText(SignUp.this, "Please enter valid information!", Toast.LENGTH_SHORT).show();
                } else if (currentHeight > 220) {
                    Toast.makeText(SignUp.this, "Height cannot exceed 220cm!", Toast.LENGTH_SHORT).show();
                } else if (goalWeight > 250) {
                    Toast.makeText(SignUp.this, "Goal weight cannot exceed 250kg!", Toast.LENGTH_SHORT).show();
                } else if (goalCal > 10000) {
                    Toast.makeText(SignUp.this, "Goal calories cannot exceed 10 000 calories!", Toast.LENGTH_SHORT).show();
                }
                else {

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        String firstName = fNameET.getText().toString().trim(); // trim() to remove whitespace from first name.
                                        String surname = sNameET.getText().toString().trim(); // trim() to remove whitespace from surname.
                                        double currentHeight = Double.parseDouble(currentHeightET.getText().toString());
                                        double goalWeight = Double.parseDouble(goalWeightET.getText().toString());
                                        double goalCal = Double.parseDouble(goalCalET.getText().toString());
                                        Boolean metric = true; // Always set to true as metric is the default measurement system.

                                        try {
                                            DatabaseReference captureUserInfo = db.getReference(mAuth.getCurrentUser().getUid()); // Getting the current user's UID
                                            User u = new User(firstName, surname, goalWeight, currentHeight, goalCal, metric);
                                            captureUserInfo.child("User Details").setValue(u);

                                            Toast.makeText(SignUp.this, "Account successfully created", Toast.LENGTH_SHORT).show();

                                            Intent toLog = new Intent(SignUp.this, Log.class); // Navigating the user to the log view.
                                            // Clearing the input fields before navigating to the log screen.
                                            fNameET.setText("");
                                            sNameET.setText("");
                                            emailET.setText("");
                                            passwordET.setText("");
                                            currentHeightET.setText("");
                                            goalCalET.setText("");
                                            goalWeightET.setText("");
                                            startActivity(toLog); // Navigating to the log screen.

                                        } catch (Exception e) {
                                            Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}