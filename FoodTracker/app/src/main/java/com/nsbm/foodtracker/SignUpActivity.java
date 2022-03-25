package com.nsbm.foodtracker;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    EditText email, password, rePassword;
    Button signUp;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email_edt_text);
        password = findViewById(R.id.pass_edt_text);
        signUp = findViewById(R.id.signup_btn);
        rePassword = findViewById(R.id.pass_re_text);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = email.getText().toString();
                String userPWD = password.getText().toString();
                String PassValidation = rePassword.getText().toString();

                if (!userEmail.isEmpty() && !userPWD.isEmpty()) {
                    //Toast.makeText(SignUpActivity.this, "Please enter your email address and password above", Toast.LENGTH_SHORT).show();
                    if (userPWD.equals(PassValidation))
                    {
                        newAccount(userEmail, userPWD);
                    }
                    else
                    {
                        Toast.makeText(SignUpActivity.this, "Password is mismatch", Toast.LENGTH_SHORT).show();
                    }
                } else if (!userPWD.equals(PassValidation)) {
                    Toast.makeText(SignUpActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpActivity.this, "Error has been occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void newAccount(String userEmail, String userPWD) {
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPWD).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this.getApplicationContext(), "SignUp unsuccessful: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this,Login.class));

                    String UID = firebaseAuth.getUid();
                    users.setUserEmail(userEmail);
                    users.setLoginPassword(userPWD);
                    users.setUserID(UID);

                    databaseReference.child(UID).setValue(users);
                }
            }
        });
    }


}

