package com.nsbm.foodtracker_emp;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

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

public class EditData extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DatePickerDialog.OnDateSetListener {
    EditText txtName,txtEXP,txtAmount;
    String itemID,itemName,itemEXP,itemAmount;
    FirebaseAuth mAuth;
    Button btnUpdate;
    DrawerLayout drawer;
    ImageView userIMG;
    TextView profileName,profileEmail;
    private DatabaseReference mDatabase,databaseReference,databaseReference1,databaseReference2;
    private FirebaseDatabase firebaseDatabase;
    Batch batch;
    Users users;
    AutoCompleteTextView autoCompleteTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);

        Toolbar toolbar = findViewById(R.id.toolBar3);
        setSupportActionBar(toolbar);

        txtName = findViewById(R.id.updateName);
        txtEXP = findViewById(R.id.updateEXp);
        btnUpdate = findViewById(R.id.btn_update);
        txtAmount = findViewById(R.id.txtAmount3);
        autoCompleteTextView = findViewById(R.id.txt_itemName58);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference("Items");
        databaseReference = firebaseDatabase.getReference("Users");
        databaseReference1 = firebaseDatabase.getReference("Products");
        databaseReference2 = firebaseDatabase.getReference("BatchItems");

        users = new Users();
        batch = new Batch();

        populateSearch();

        Intent intent = getIntent();
        itemID = intent.getStringExtra("Item_ID");
        itemName = intent.getStringExtra("Item_Name");
        itemEXP = intent.getStringExtra("Item_EXP");
        itemAmount = intent.getStringExtra("Item_Amount");

        txtName.setText(itemID);
        autoCompleteTextView.setText(itemName);
        txtEXP.setText(itemEXP);
        txtAmount.setText(itemAmount);


        drawer = findViewById(R.id.drawerLayoutEditData);
        NavigationView navigationView = findViewById(R.id.nav_view3);
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


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(txtName.getText().toString().isEmpty())
                {
                    Toast.makeText(EditData.this, "Please enter before updating !", Toast.LENGTH_SHORT).show();
                }
                else if(txtEXP.getText().toString().isEmpty())
                {
                    Toast.makeText(EditData.this, "Please enter valid expire date before updating", Toast.LENGTH_SHORT).show();
                }
                else if(autoCompleteTextView.getText().toString().isEmpty())
                {
                    Toast.makeText(EditData.this, "Please enter valid expire date before updating", Toast.LENGTH_SHORT).show();
                }
                else if(txtAmount.getText().toString().isEmpty())
                {
                    Toast.makeText(EditData.this, "Please enter valid expire date before updating", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    updateItem(itemID,itemName,itemEXP);
                }

            }
        });


    }
    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf3 = new SimpleDateFormat("MM/dd/yy");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, i);
        c.set(Calendar.MONTH, i1);
        c.set(Calendar.DAY_OF_MONTH, i2);

        Date parsedDate = null;                     // dd-MM-yy
        try {
            String currentDate = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
            //String newDate = currentDate.replace("/", "-");
            parsedDate = sdf3.parse(currentDate);
            String newPDate = sdf.format(parsedDate);
            txtEXP.setText(newPDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void openDate(View view) {
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(),"Date picker");
    }
    private void populateSearch()
    {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    ArrayList<String> productsName = new ArrayList<>();
                    for(DataSnapshot ds: snapshot.getChildren())
                    {
                        String n = ds.child("ProductName").getValue(String.class);
                        productsName.add(n);
                    }
                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1,productsName);
                    autoCompleteTextView.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference1.addValueEventListener(eventListener);
    }

    private void updateItem(String ItemID,String ItemName, String ItemEXP)
    {
        if (!ItemID.equals(txtName.getText().toString()))
        {
            databaseReference2.child(ItemID).child("batchID").setValue(txtName.getText().toString());
        }
        if (!itemAmount.equals(txtAmount.getText().toString()))
        {
            databaseReference2.child(ItemID).child("amount").setValue(txtName.getText().toString());
        }
        if (!ItemName.equals(autoCompleteTextView.getText().toString()))
        {
            databaseReference2.child(ItemID).child("productName").setValue(txtName.getText().toString());
        }
        if (!ItemEXP.equals(txtEXP.getText().toString()))
        {
            databaseReference2.child(ItemID).child("productExp").setValue(txtEXP.getText().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            String myDate = txtEXP.getText().toString();
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
        else
        {
            Toast.makeText(EditData.this, "Does not updated the Data", Toast.LENGTH_SHORT).show();
        }

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(EditData.this, "Changes has been added", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };

        databaseReference2.addChildEventListener(childEventListener);

    }

    private void startAlarm(Calendar cal) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,MyNotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pendingIntent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_view1:
                startActivity(new Intent(EditData.this, ViewRecords.class));
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
                            startActivity(new Intent(EditData.this, AddRecords.class));
                        }
                        else if(i==1)
                        {
                            startActivity(new Intent(EditData.this, OCR_Activity2.class));
                        }
                    }
                });
                builder.create().show();
                break;

            case R.id.nav_edit:
                startActivity(new Intent(EditData.this,EditData.class));
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(EditData.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditData.this,Login.class));
                break;
            default:
                startActivity(new Intent(EditData.this,MainActivity.class));
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
            startActivity(new Intent(EditData.this, ViewRecords.class));
        }
    }

}