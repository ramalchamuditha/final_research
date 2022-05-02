package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
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

public class UserProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    GoogleSignInClient mGoogleSignInClient;
    TextInputEditText userName1,userEmail1,userPhone1;
    Button btnUpdate,profilePic,btnPassword;
    ImageView profile,userIMG;
    FirebaseAuth mAuth;
    TextView profileName,profileEmail;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Users users;
    ActivityUserProfileBinding binding;
    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.toolBar4);
        setSupportActionBar(toolbar);

        userName1 = findViewById(R.id.UPName);
        userEmail1 = (TextInputEditText) findViewById(R.id.UPEmail);
        userPhone1 = findViewById(R.id.UPPhoneNo);
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

        drawer = findViewById(R.id.drawerLayoutUserProfile);
        NavigationView navigationView = findViewById(R.id.nav_view4);
        navigationView.setNavigationItemSelectedListener(this);

        //View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header);
        View headerLayout = navigationView.getHeaderView(0);
        userIMG = (ImageView) headerLayout.findViewById(R.id.userImage);
        profileName = (TextView) headerLayout.findViewById(R.id.nav_userName);
        profileEmail = (TextView) headerLayout.findViewById(R.id.nav_Email);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

//        profilePic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(UserProfile.this, "Fuck you 2", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        btnUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                UpdateDetails();
//            }
//        });
//
//        btnPassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(UserProfile.this,SignUpActivity.class));
//            }
//        });
    }

    public void uploadImage(View view) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("uploading File");
        progressDialog.show();
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK);
        Date now = new Date();
        //String fileName = formatter.format(now);

        storageReference = FirebaseStorage.getInstance().getReference("images/"+mAuth.getUid());

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
            databaseReference.child(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        //Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        DataSnapshot dataSnapshot = task.getResult();
                        String _userEmail = String.valueOf(dataSnapshot.child("userEmail").getValue());
                        userEmail1.setText(_userEmail);
                        //Toast.makeText(UserProfile.this, "email: "+_userEmail, Toast.LENGTH_SHORT).show();
                    }

                }
            });
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
            profileName.setText(acct.getDisplayName());
            profileEmail.setText(acct.getEmail());
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
                        userName1.setText(first_name+" "+last_name);
                        profileEmail.setText(userEmails);
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
            if(!userName1.getText().toString().isEmpty())
            {
                if(!userEmail1.getText().toString().isEmpty())
                {
                    if(!userPhone1.getText().toString().isEmpty())
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
    }

    private void updateNonValidUser() {

        String usersName = userName1.getText().toString();
        String usersEmail = userEmail1.getText().toString();
        String usersProfile = imageUri.toString();
        String usersPhone = userPhone1.getText().toString();

        if(usersName.isEmpty() || usersEmail.isEmpty() ||usersProfile.isEmpty() || usersPhone.isEmpty() )
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_view1:
                startActivity(new Intent(UserProfile.this,ViewRecords.class));
                break;
            case R.id.nav_add:
                startActivity(new Intent(UserProfile.this,AddRecords.class));
                break;
            case  R.id.nav_report:
                startActivity(new Intent(UserProfile.this,Reports.class));
                break;
            case R.id.nav_edit:
                startActivity(new Intent(UserProfile.this,EditData.class));
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(UserProfile.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserProfile.this,Login.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(UserProfile.this, UserProfile.class));
                break;
            default:
                startActivity(new Intent(UserProfile.this,MainActivity.class));
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Exit Application?");
            alertDialogBuilder
                    .setMessage("Click yes to exit!")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    moveTaskToBack(true);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                }
                            })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}