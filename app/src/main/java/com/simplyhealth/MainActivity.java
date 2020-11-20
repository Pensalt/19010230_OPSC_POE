package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class  MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Declaring an instance of FirebaseAuth.

    EditText emailET, passwordET;
    Button loginBtn, signupBtn;
    ImageView logoImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // Initializing the FirebaseAuth instance.

        emailET = findViewById(R.id.emailET_login);
        passwordET = findViewById(R.id.passwordET_login);
        loginBtn = findViewById(R.id.loginButton_login);
        signupBtn = findViewById(R.id.signupButton_login);
        logoImg = findViewById(R.id.LogoImgView_login);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailET.getText().toString().trim();
                String password = passwordET.getText().toString();

                if (email.equals("") || password.equals(""))
                {
                    Toast.makeText(MainActivity.this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    try {
                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful())
                                {
                                    emailET.getText().clear();
                                    passwordET.getText().clear();
                                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    // wait like 1.5 seconds
                                    Intent goToLog = new Intent(MainActivity.this,Log.class);
                                    startActivity(goToLog);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(MainActivity.this,"Invalid username or password!",Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailET.getText().clear(); // Clearing the input field for the user's email.
                passwordET.getText().clear(); //
                Intent goToSignUp = new Intent(MainActivity.this,SignUp.class);
                startActivity(goToSignUp);
            }
        });

    }
}