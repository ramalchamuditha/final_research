package com.nsbm.foodtracker;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;


public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    TextView NameText,signUp,forgot;
    ImageView ProfileImg;
    LoginButton loginButton;
    Button btn_login;
    EditText userEmail,userPassword;
    CallbackManager callbackManager;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        signInButton = findViewById(R.id.sign_in_button);
        NameText = findViewById(R.id.txt_name);
        ProfileImg = findViewById(R.id.img_profile);
        loginButton = findViewById(R.id.login_button);
        btn_login = findViewById(R.id.btn_manual_login);
        signUp = findViewById(R.id.textView2);
        userEmail = findViewById(R.id.txt_username);
        userPassword = findViewById(R.id.txt_password);
        forgot = findViewById(R.id.txtForgot);

        callbackManager = CallbackManager.Factory.create();
        loginButton.setPermissions(Arrays.asList("email","public_profile"));


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mAuth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newUser = new Intent(Login.this,SignUpActivity.class);
                startActivity(newUser);
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailID = userEmail.getText().toString();
                
                if(emailID.isEmpty())
                {
                    Toast.makeText(Login.this, "Please enter your E-mail first", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(emailID).addOnCompleteListener(Login.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Login.this, "Password rest link sent to your email", Toast.LENGTH_SHORT).show();
                            }
                            else 
                            {
                                Toast.makeText(Login.this, "Unable to send rest link to provided E-mail", Toast.LENGTH_SHORT).show();
                            }
                            
                        }
                    });
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                String emailID = userEmail.getText().toString();
                String passwd = userPassword.getText().toString();
                
                if(emailID.isEmpty())
                {
                    Toast.makeText(Login.this, "Please enter login credentials1", Toast.LENGTH_SHORT).show();
                }
                else if(passwd.isEmpty())
                {
                    Toast.makeText(Login.this, "Please enter login credentials2", Toast.LENGTH_SHORT).show();
                }
                else if (emailID.isEmpty() && passwd.isEmpty())
                {
                    Toast.makeText(Login.this, "Please enter login credentials", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.signInWithEmailAndPassword(emailID,passwd).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this,MainActivity.class));
                            }

                        }
                    });
                }



            }
        });


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                NameText.setText("User Id : "+loginResult.getAccessToken().getUserId());
                String image_url = "https://graph.facebook.com/"+loginResult.getAccessToken().getUserId()+"/picture?type=large";
                Glide.with(Login.this).load(image_url).into(ProfileImg);

                //String imageURL = "https://graph.facebook.com/"+loginResult.getAccessToken().getUserId()+"/picture?return_ssl_resources=1";
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(@NonNull FacebookException e) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                String userID = account.getId();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            startActivity(new Intent(Login.this,MainActivity.class));
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        Toast.makeText(Login.this, "credentials :"+credential, Toast.LENGTH_SHORT).show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }
    
    private void updateUI(FirebaseUser code)
    {
        if(code == null)
        {
            //Toast.makeText(Login.this, "Fail to Login", Toast.LENGTH_SHORT).show();
            //signIn();
        }
        else 
        {
            startActivity(new Intent(Login.this,MainActivity.class));
            Toast.makeText(Login.this, "Successfully login", Toast.LENGTH_SHORT).show();
        }
    }

    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(@Nullable AccessToken accessToken, @Nullable AccessToken accessToken1) {
            if(accessToken ==null)
            {
                Toast.makeText(Login.this, "Logout", Toast.LENGTH_SHORT).show();
            }
            else
            {
                loadUserProfile(accessToken);
            }

        }
    };

    private void loadUserProfile(AccessToken accessToken)
    {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                try {
                    String first_name = jsonObject.getString("first_name");
                    String last_name = jsonObject.getString("last_name");
                    String email = jsonObject.getString("email");
                    String id = jsonObject.getString("id");
                    String image_url = "https://graph.facebook.com/"+id+"/picture?type=normal";

                    NameText.setText(first_name +" "+ last_name);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.dontAnimate();

                    Glide.with(Login.this).load(image_url).into(ProfileImg);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        Bundle parameter = new Bundle();
        parameter.putString("fields","first_name,last_name,id");
        request.setParameters(parameter);
        request.executeAsync();
    }

}