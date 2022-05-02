package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddRecords extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReferenceItems, databaseReferenceUsers;
    Button btnInsert,btnEdit;
    EditText itemName,itemExp;
    DrawerLayout drawer;
    ImageView userIMG;
    TextView profileName,profileEmail;
    GoogleSignInClient mGoogleSignInClient;
    Item items;
    Users users;
    ListView listView;
    List<Item> itemList;
    final Calendar myCalender = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_records);

        Toolbar toolbar = findViewById(R.id.toolBar1);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        btnInsert = findViewById(R.id.btn_insert);
        itemName = findViewById(R.id.txt_itemName);
        btnEdit = findViewById(R.id.btn_edit);
        listView = findViewById(R.id.txt_list);
        itemExp = findViewById(R.id.editDate);

        drawer = findViewById(R.id.drawerLayoutAddRecord);
        NavigationView navigationView = findViewById(R.id.nav_view1);
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


        items = new Item();

        itemList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceItems = firebaseDatabase.getReference("Items");
        databaseReferenceUsers = firebaseDatabase.getReference("Users");

        String UserID = mAuth.getCurrentUser().getUid();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                myCalender.set(Calendar.YEAR,year);
                myCalender.set(Calendar.MONTH,month);
                myCalender.set(Calendar.DAY_OF_MONTH,day);
                updateCalender();
            }
        };

        databaseReferenceItems.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();

                for(DataSnapshot itemDataSnap : snapshot.getChildren())
                {
                    Item item = itemDataSnap.getValue(Item.class);
                    String CurrentUID = item.getUserID().toString();
                    if(CurrentUID.equals(UserID))
                    {
                        itemList.add(item);
                    }

                }
                ItemAdapter adapter = new ItemAdapter(AddRecords.this,itemList);
                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        itemExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddRecords.this,date,myCalender.get(Calendar.YEAR),myCalender.get(Calendar.MONTH),myCalender.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                String IName = itemName.getText().toString();
                String IExp = itemExp.getText().toString();
                String userID = mAuth.getCurrentUser().getUid();

                if (IName.isEmpty() && IExp.isEmpty())
                {
                    Toast.makeText(AddRecords.this, "Please Fill the above fields before insert !!", Toast.LENGTH_SHORT).show();
                }
                else if (IExp.isEmpty())
                {
                    Toast.makeText(AddRecords.this, "Please enter Expire Date for this product!", Toast.LENGTH_SHORT).show();
                }
                else if (IName.isEmpty())
                {
                    Toast.makeText(AddRecords.this, "Please enter Item name first !", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    itemDataAdd(userID,IExp,IName);
                    itemName.setText("");
                    itemExp.setText("");
                    btnInsert.setText("Add another");
                    Toast.makeText(AddRecords.this, "Record added Successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddRecords.this,ViewRecords.class));
            }
        });

    }

    private void itemDataAdd(String UserID, String itemExp, String itemName)
    {
        String KeyID = firebaseDatabase.getReference("Items").push().getKey();
        items.setUserID(UserID);
        items.setItemName(itemName);
        items.setExpireDate(itemExp);
        items.setItemID(KeyID);

        databaseReferenceItems.child(KeyID).setValue(items);

    }

    private void updateCalender()
    {
        String myFormat = "yy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.UK);
        btnEdit.setText(dateFormat.format(myCalender.getTime()));
    }


    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_view1:
                startActivity(new Intent(AddRecords.this,ViewRecords.class));
                break;
            case R.id.nav_add:
                startActivity(new Intent(AddRecords.this,AddRecords.class));
                break;
            case  R.id.nav_report:
                startActivity(new Intent(AddRecords.this,Reports.class));
                break;
            case R.id.nav_edit:
                startActivity(new Intent(AddRecords.this,EditData.class));
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(AddRecords.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddRecords.this,Login.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(AddRecords.this, UserProfile.class));
                break;
            default:
                startActivity(new Intent(AddRecords.this,MainActivity.class));
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

                        Glide.with(AddRecords.this).load(image_url).into(userIMG);
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
            databaseReferenceUsers.child(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        //Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        DataSnapshot dataSnapshot = task.getResult();
                        String _userEmail = String.valueOf(dataSnapshot.child("userEmail").getValue());
                        profileEmail.setText(_userEmail);
                        //Toast.makeText(ViewRecords.this, "email: "+_userEmail, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


    }
}