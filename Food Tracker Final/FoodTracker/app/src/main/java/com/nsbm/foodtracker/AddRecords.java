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
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddRecords extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DatePickerDialog.OnDateSetListener {

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

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf3 = new SimpleDateFormat("MM/dd/yy");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,i);
        c.set(Calendar.MONTH,i1);
        c.set(Calendar.DAY_OF_MONTH,i2);

        Date parsedDate = null;                     // dd-MM-yy
        try {
            String currentDate = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
            //String newDate = currentDate.replace("/", "-");
            parsedDate = sdf3.parse(currentDate);
            String newPDate = sdf.format(parsedDate);
            itemExp.setText(newPDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

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

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                    Calendar calendar = Calendar.getInstance();
                    String myDate = IExp;
                    try {
                        date = sdf.parse(myDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_MONTH,-14);
                    Date modified = calendar.getTime();

                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(System.currentTimeMillis());
                    c.setTime(modified);
                    c.set(Calendar.HOUR_OF_DAY,12);
                    c.set(Calendar.MINUTE,10);
                    c.set(Calendar.SECOND,0);

                    startAlarm(c);
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

    private void startAlarm(Calendar cal) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,MyNotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pendingIntent);
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


    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_view1:
                startActivity(new Intent(AddRecords.this,ViewRecords.class));
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
                            startActivity(new Intent(AddRecords.this,AddRecords.class));
                        }
                        else if(i==1)
                        {
                            startActivity(new Intent(AddRecords.this,OCR_Activity2.class));
                        }
                    }
                });
                builder.create().show();
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
                        String _userName = String.valueOf(dataSnapshot.child("userName").getValue());
                        Uri _userImage = Uri.parse(String.valueOf(dataSnapshot.child("userProfile").getValue()));
                        Glide.with(AddRecords.this).load(_userImage).into(userIMG);
                        profileName.setText(_userName);
                        profileEmail.setText(_userEmail);
                        //Toast.makeText(ViewRecords.this, "email: "+_userEmail, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


    }

    public void openDate(View view) {
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(),"Date picker");
    }
}