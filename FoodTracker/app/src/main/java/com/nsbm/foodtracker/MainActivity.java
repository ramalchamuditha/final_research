package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button AddRecord,viewRecords,Report,Edit;
    FirebaseAuth mAuth;
    DrawerLayout drawer;
    ImageView userIMG;
    TextView profileName,profileEmail;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        users = new Users();

        AddRecord = findViewById(R.id.btn_addRecord);
        viewRecords = findViewById(R.id.btn_viewData);
        Report = findViewById(R.id.btn_reports);
        Edit = findViewById(R.id.btn_editData);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
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

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        viewRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ViewRecords.class));
            }
        });

        AddRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        MainActivity.this,R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.layout_botton_sheet,
                                (LinearLayout)findViewById(R.id.bottomSheetContainer));
                bottomSheetView.findViewById(R.id.QuickScan).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(MainActivity.this, "OCR", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this,OCR_Activity2.class));
                        bottomSheetDialog.dismiss();;
                    }
                });

                bottomSheetView.findViewById(R.id.EnterData).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(MainActivity.this, "Enter data", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this,AddRecords.class));
                        bottomSheetDialog.dismiss();
                    }
                });


                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();

            }
        });

        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NotificationActivity.class));
            }
        });


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_view1:
                startActivity(new Intent(MainActivity.this,ViewRecords.class));
                break;
            case R.id.nav_add:
                String options[] = {"Manually","Scan"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add record by");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                        {
                            startActivity(new Intent(MainActivity.this,AddRecords.class));
                        }
                        else if(i==1)
                        {
                            startActivity(new Intent(MainActivity.this,OCR_Activity2.class));
                        }
                    }
                });
                builder.create().show();
                break;
            case R.id.nav_edit:
                startActivity(new Intent(MainActivity.this,EditData.class));
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(MainActivity.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,Login.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, UserProfile.class));
                break;
            default:
                startActivity(new Intent(MainActivity.this,MainActivity.class));
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

    @Override
    protected void onStart() {
        super.onStart();
        viewProfile();
        addFBAccount();
        AddGAccount();
    }

    private void AddGAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getBaseContext());
        if (account != null)
        {
            users.setUserEmail(account.getEmail());
            users.setUserID(account.getId());
            users.setUserName(account.getDisplayName());
            users.setUserProfile(account.getPhotoUrl().toString());

            databaseReference.child(mAuth.getUid()).setValue(users);
        }
    }

    private void addFBAccount() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken!=null)
        {
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                    try {
                        String first_name = jsonObject.getString("first_name");
                        String last_name = jsonObject.getString("last_name");
                        String userEmail = jsonObject.getString("email");
                        String id = jsonObject.getString("id");
                        String image_url = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");

                        String UID = mAuth.getUid();
                        users.setUserEmail(userEmail);
                        users.setUserID(id);
                        users.setUserName(first_name + " " + last_name);
                        users.setUserProfile(image_url);
                        databaseReference.child(UID).setValue(users);

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


    private void viewProfile(){
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
        if (acct != null) {
            profileName.setText(acct.getDisplayName());
            profileEmail.setText(acct.getEmail());
            Picasso.get().load(acct.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).into(userIMG);
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
                        String userEmail = jsonObject.getString("email");
                        String id = jsonObject.getString("id");
                        String image_url = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");

                        profileName.setText(first_name +" "+ last_name);
                        profileEmail.setText(userEmail);
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.dontAnimate();

                        Glide.with(MainActivity.this).load(image_url).into(userIMG);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            Bundle parameter = new Bundle();
            parameter.putString("fields","first_name,last_name,email,id,link,picture.type(large)");
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
                        //Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        DataSnapshot dataSnapshot = task.getResult();
                        String _userEmail = String.valueOf(dataSnapshot.child("userEmail").getValue());
                        String _userName = String.valueOf(dataSnapshot.child("userName").getValue());
                        Uri _userImage = Uri.parse(String.valueOf(dataSnapshot.child("userProfile").getValue()));
                        Glide.with(MainActivity.this).load(_userImage).into(userIMG);
                        profileName.setText(_userName);
                        profileEmail.setText(_userEmail);
                        //Toast.makeText(ViewRecords.this, "email: "+_userEmail, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }
}