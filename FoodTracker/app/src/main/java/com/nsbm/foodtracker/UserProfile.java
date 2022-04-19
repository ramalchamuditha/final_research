package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nsbm.foodtracker.databinding.ActivityUserProfileBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserProfile extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    EditText userName,userEmail,userPhone,userPassword;
    Button btnUpdate,profilePic,btnPassword;
    ImageView profile;
    FirebaseAuth mAuth;
    TextView profileName;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Users users;
    ActivityUserProfileBinding binding;
    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userName = findViewById(R.id.UPName);
        userEmail = findViewById(R.id.UPEmail);
        userPhone = findViewById(R.id.UPPhoneNo);
        btnUpdate = findViewById(R.id.btn_UPUpdate);
        profile = findViewById(R.id.profileImage);
        profilePic = findViewById(R.id.PicChange);
        profileName = findViewById(R.id.profileUserName);
        btnPassword = findViewById(R.id.NewPassword);

        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        users = new Users();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UserProfile.this, "Fuck you 2", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfile.this,SignUpActivity.class));
            }
        });
    }

    public void uploadImage(View view) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("uploading File");
        progressDialog.show();
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK);
        Date now = new Date();
        String fileName = formatter.format(now);

        storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                binding.profileImage.setImageURI(null);
                Toast.makeText(UserProfile.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(UserProfile.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(acct==null && accessToken == null)
        {
            Toast.makeText(UserProfile.this, "Logging from nowhere", Toast.LENGTH_SHORT).show();
        }
        else
        {
            viewProfile();
        }

    }

    private void viewProfile()
    {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
        if (acct != null) {
            profileName.setText(acct.getDisplayName());
            userName.setText(acct.getDisplayName());
            userEmail.setText(acct.getEmail());
            Picasso.get().load(acct.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).into(profile);
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken!=null)
        {
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                    try {
                        String first_name = jsonObject.getString("first_name");
                        String last_name = jsonObject.getString("last_name");
                        String userEmails = jsonObject.getString("email");
                        String id = jsonObject.getString("id");
                        String image_url = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");

                        profileName.setText(first_name+" "+last_name);
                        userName.setText(first_name+" "+last_name);
                        userEmail.setText(userEmails);
                        Picasso.get().load(image_url).placeholder(R.mipmap.ic_launcher).into(profile);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            Bundle parameter = new Bundle();
            parameter.putString("fields", "first_name,last_name,email,id,link,picture.type(large)");
            request.setParameters(parameter);
            request.executeAsync();
        }
        if(isVerified())
        {
            //profileName.setText();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && data != null && data.getData() != null)
        {
            imageUri = data.getData();
            binding.profileImage.setImageURI(imageUri);
        }
    }

    public void selectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    public void UpdateDetails()
    {
        if(isVerified())
        {
            //Here is false statement
            //Where the non-facebook/google users updates
            //Check the textInputs are filled
            //Password changes, add phone number, add profile picture
            // after the profile picture update, have to create method to load into navigation drawer
            if(!userName.getText().toString().isEmpty())
            {
                if(!userEmail.getText().toString().isEmpty())
                {
                    if(!userPhone.getText().toString().isEmpty())
                    {
                        if(!userPassword.getText().toString().isEmpty())
                        {
                            updateNonValidUser();
                        }
                        else
                        {
                            Toast.makeText(UserProfile.this, "Username is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(UserProfile.this, "Username is empty", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(UserProfile.this, "Username is empty", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(UserProfile.this, "Username is empty", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void updateNonValidUser() {

        String usersName = userName.getText().toString();
        String usersEmail = userEmail.getText().toString();
        String usersProfile = imageUri.toString();
        String usersPhone = userPhone.getText().toString();
        String usersPassword = userPassword.getText().toString();

        if(usersName.isEmpty() || usersEmail.isEmpty() ||usersProfile.isEmpty() || usersPhone.isEmpty() || usersPassword.isEmpty())
        {
            Toast.makeText(UserProfile.this, "Please check the data before update", Toast.LENGTH_SHORT).show();
        }
        else
        {
            

        }

    }

    private boolean isVerified() {
        boolean result;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        result = acct != null || accessToken != null;
        return !result;

    }
}