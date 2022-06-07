package com.nsbm.foodtracker_emp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    ImageView ProfileImg;
    Button btn_login;
    EditText userEmail,userPassword;
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Employee employee;

    SharedPreferences sharedPreferences;
    public static final String UPrefernce = "loginPreference";
    public static final String Uname = null ;
    public static final String UPass = "null" ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        ProfileImg = findViewById(R.id.img_profile);
        btn_login = findViewById(R.id.btn_manual_login);
        userEmail = findViewById(R.id.txt_username);
        userPassword = findViewById(R.id.txt_password);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Employee");
        employee = new Employee();


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                String emailID = userEmail.getText().toString();
                String passwd = userPassword.getText().toString();
                
                if(emailID.isEmpty())
                {
                    Toast.makeText(Login.this, "Please enter login user name", Toast.LENGTH_SHORT).show();
                }
                else if(passwd.isEmpty())
                {
                    Toast.makeText(Login.this, "Please enter login password", Toast.LENGTH_SHORT).show();
                }
                else if (emailID.isEmpty() && passwd.isEmpty())
                {
                    Toast.makeText(Login.this, "Please enter login credentials", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    databaseReference.child(emailID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            }
                            else {
                                DataSnapshot dataSnapshot = task.getResult();
                                String _userName = String.valueOf(dataSnapshot.child("userName").getValue());
                                String _password = String.valueOf(dataSnapshot.child("password").getValue());
                                
                                if(emailID.equals(_userName) && passwd.equals(_password))
                                {
                                    startActivity(new Intent(Login.this,MainActivity.class));
                                    Toast.makeText(Login.this, "Successfully", Toast.LENGTH_SHORT).show();
//                                    sharedPreferences = getSharedPreferences(UPrefernce,MODE_PRIVATE);
//                                    SharedPreferences.Editor ed = sharedPreferences.edit();
//                                    ed.putString(Uname,emailID);
//                                    ed.putString(UPass,passwd);
//                                    ed.apply();

                                }
                                else
                                {
                                    Toast.makeText(Login.this, "Wrong credentials", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
                }
            }
        });



    }


    @Override
    public void onStart() {
        super.onStart();
    }




}