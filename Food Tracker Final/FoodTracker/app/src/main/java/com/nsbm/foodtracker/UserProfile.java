package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.android.material.button.MaterialButton;
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
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    GoogleSignInClient mGoogleSignInClient;
    EditText userName1, userEmail1, userPhone1;
    MaterialButton btnUpdate, btnPassword;
    ImageView profileImage1, userIMG;
    FirebaseAuth mAuth;
    TextView profileName, profileEmail, profileName1,profilePic;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Users users;
    Uri imageUri;
    StorageReference storageReference;
    String cameraPermission[];
    String storagePermission[];
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.toolBar4);
        setSupportActionBar(toolbar);

        userName1 = (EditText) findViewById(R.id.UPName);
        userEmail1 = (EditText) findViewById(R.id.UPEmail);
        userPhone1 = findViewById(R.id.UPPhoneNo);
        btnUpdate = findViewById(R.id.btn_UPUpdate);
        profileImage1 = findViewById(R.id.profileImage);
        profilePic = findViewById(R.id.PicChange);
        profileName1 = findViewById(R.id.profileUserName);
        btnPassword = findViewById(R.id.NewPassword);


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
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateDetails();
            }
        });

        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfile.this,SignUpActivity.class));
            }
        });
    }

    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromGallery();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        CropImage.activity().start(UserProfile.this);
    }

    // checking storage permissions
    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    // Requesting  gallery permission
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST);
    }

    // checking camera permissions
    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    // Requesting camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST);
    }

    private void UpdateDetails() {
        String _userName = userName1.getText().toString();
        String _telephone = userPhone1.getText().toString();

        if(_userName.equals(userName1.getText().toString()))
        {
            Toast.makeText(UserProfile.this, "Please update Name", Toast.LENGTH_SHORT).show();

        }
        else if(_telephone.equals(userPhone1.getText().toString()))
        {
            Toast.makeText(UserProfile.this, "Please update Telephone", Toast.LENGTH_SHORT).show();
        }

        else if(!_userName.equals(userName1.getText().toString()) && !_telephone.equals(userPhone1.getText().toString()))
        {
            String UID = mAuth.getUid();
            users.setUserPhone(userPhone1.getText().toString());
            databaseReference.child(UID).setValue(users);
            databaseReference.child(UID).child("userName").setValue(userName1.getText().toString());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Glide.with(UserProfile.this).load(imageUri).into(profileImage1);
                uploadImage();
            }
        }
    }

    private void uploadImage() {
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUri = uri;
                        users.setUserProfile(downloadUri.toString());
                        Toast.makeText(UserProfile.this, "uploaded", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfile.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    @Override
    protected void onStart() {
        super.onStart();
        viewProfile();
    }

    private void viewProfile() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
        if (acct != null) {
            btnPassword.setVisibility(View.INVISIBLE);
            profileName.setText(acct.getDisplayName());
            profileEmail.setText(acct.getEmail());
            Picasso.get().load(acct.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).into(userIMG);
            profileImage1.setImageURI(acct.getPhotoUrl());
            profileName.setText(acct.getDisplayName());
            userName1.setText(acct.getDisplayName());
            userEmail1.setText(acct.getEmail());
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            btnPassword.setVisibility(View.INVISIBLE);
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                    try {
                        String first_name = jsonObject.getString("first_name");
                        String last_name = jsonObject.getString("last_name");
                        String userEmails = jsonObject.getString("email");
                        String id = jsonObject.getString("id");
                        String image_url = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");

                        profileName.setText(first_name + " " + last_name);
                        userName1.setText(first_name + " " + last_name);
                        profileEmail.setText(userEmails);
                        Picasso.get().load(image_url).placeholder(R.mipmap.ic_launcher).into(profileImage1);

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
        else
        {
            databaseReference.child(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        btnPassword.setVisibility(View.INVISIBLE);
                        //Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        DataSnapshot dataSnapshot = task.getResult();
                        String _userEmail = String.valueOf(dataSnapshot.child("userEmail").getValue());
                        String _userName = String.valueOf(dataSnapshot.child("userName").getValue());
                        Uri _userImage = Uri.parse(String.valueOf(dataSnapshot.child("userProfile").getValue()));
                        Glide.with(UserProfile.this).load(_userImage).into(userIMG);
                        profileName1.setText(_userName);
                        profileEmail.setText(_userEmail);
                        Glide.with(UserProfile.this).load(_userImage).into(profileImage1);
                        profileName.setText(_userName);
                        userName1.setText(_userName);
                        userEmail1.setText(_userEmail);
                        //Toast.makeText(ViewRecords.this, "email: "+_userEmail, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_view1:
                startActivity(new Intent(UserProfile.this, ViewRecords.class));
                break;
            case R.id.nav_add:
                String options[] = {"Manually", "Scan"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add record by");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            startActivity(new Intent(UserProfile.this, AddRecords.class));
                        } else if (i == 1) {
                            startActivity(new Intent(UserProfile.this, OCR_Activity2.class));
                        }
                    }
                });
                builder.create().show();
                break;
            case R.id.nav_edit:
                startActivity(new Intent(UserProfile.this, EditData.class));
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(UserProfile.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserProfile.this, Login.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(UserProfile.this, UserProfile.class));
                break;
            default:
                startActivity(new Intent(UserProfile.this, MainActivity.class));
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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