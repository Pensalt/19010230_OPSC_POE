package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
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
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    EditText fNameET, sNameET, emailET, currentWeightET, currentHeightET, goalWeightET, goalCalET, passwordET;
    Button btnConfirm;
    ImageView logoImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        fNameET = findViewById(R.id.usersNameET_signup);
        sNameET = findViewById(R.id.usersSurnameET_signup);
        emailET = findViewById(R.id.usersEmailET_signup);
        currentWeightET = findViewById(R.id.currentWeightET_signup);
        currentHeightET = findViewById(R.id.currentHeightET_signup);
        goalWeightET = findViewById(R.id.goalWeightET_signup);
        goalCalET =findViewById(R.id.calorieGoalNumET_signup);
        passwordET = findViewById(R.id.usersPasswordET_signup);
        btnConfirm = findViewById(R.id.btnSignUp_signup);
        logoImg = findViewById(R.id.LogoImgView_signup);

        mAuth = FirebaseAuth.getInstance(); // Initializing the FirebaseAuth instance.

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = emailET.getText().toString().trim();
                String password = passwordET.getText().toString();

                if (email.equals("") || password.equals(""))
                {
                    Toast.makeText(SignUp.this, "Please enter valid information!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.createUserWithEmailAndPassword(email,password)
                            .addOnCompleteListener(SignUp.this,new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful())
                                    {
                                        String firstName = fNameET.getText().toString().trim();
                                        String surname = sNameET.getText().toString().trim();
                                        double currentWeight = Double.parseDouble(currentWeightET.getText().toString());
                                        double currentHeight = Double.parseDouble(currentHeightET.getText().toString());
                                        double goalWeight = Double.parseDouble(goalWeightET.getText().toString());
                                        double goalCal = Double.parseDouble(goalCalET.getText().toString());
                                        Boolean metric = true; // Always set to true as metric is the default measurement system.

                                        if (firstName.equals("") || surname.equals("") || currentHeight <= 0 || currentWeight <= 0 || goalWeight <= 0 || goalCal <= 0)
                                        {
                                            Toast.makeText(SignUp.this, "Please enter valid information!", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            try {
                                                DatabaseReference captureUserInfo = db.getReference(mAuth.getCurrentUser().getUid()); // Getting the current user's UID
                                                User u = new User(firstName,surname,currentWeight,goalWeight,currentHeight,goalCal,metric);
                                                captureUserInfo.child("User Details").setValue(u);

                                                Toast.makeText(SignUp.this,"Account successfully created",Toast.LENGTH_SHORT).show();
                                                // Put something in to wait 1.5 seconds
                                                Intent toMenu = new Intent(SignUp.this, Log.class); // Navigating the user to the log view.
                                                startActivity(toMenu);
                                            } catch (Exception e){
                                                Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUp.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}