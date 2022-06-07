package com.nsbm.foodtracker_emp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewRecords extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReferenceItems,databaseReferenceUsers,databaseReference1,databaseReference2;
    DrawerLayout drawer;
    ImageView userIMG;
    TextView profileName,profileEmail;
    ListView myList;
    Button btnEdit;
    Item items;
    List<Item> itemList;
    List<Batch> batchList;
    Batch batch;
    int id = -1;
    Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_records);

        Toolbar toolbar = findViewById(R.id.toolBar2);
        setSupportActionBar(toolbar);

        myList = findViewById(R.id.viewList1);

        items = new Item();
        batch = new Batch();
        itemList = new ArrayList<>();
        batchList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceItems = firebaseDatabase.getReference("Items");
        databaseReferenceUsers = firebaseDatabase.getReference("Users");
        databaseReference1 = firebaseDatabase.getReference("Products");
        databaseReference2 = firebaseDatabase.getReference("BatchItems");
        users = new Users();

        drawer = findViewById(R.id.drawerLayoutViewRecord);
        NavigationView navigationView = findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);

        //View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header);
        View headerLayout = navigationView.getHeaderView(0);
        userIMG = (ImageView) headerLayout.findViewById(R.id.userImage);
        profileName = (TextView) headerLayout.findViewById(R.id.nav_userName);
        profileEmail = (TextView) headerLayout.findViewById(R.id.nav_Email);


        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                batchList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Batch batch1 = dataSnapshot.getValue(Batch.class);
                    batchList.add(batch1);
                }
                BatchAdapter batchAdapter = new BatchAdapter(ViewRecords.this,batchList);
                myList.setAdapter(batchAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String options[] = {"Edit Item","Delete Item"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewRecords.this);
                builder.setTitle("Choose options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int idk) {
                        if(idk==0)
                        {
                            //startActivity(new Intent(ViewRecords.this,AddRecords.class));
                            id = i;
                            String itemID = batchList.get(i).getBatchID();
                            String itemName = batchList.get(i).getProductName();
                            String itemExp = batchList.get(i).getProductExp();
                            String itemAmount = batchList.get(i).getAmount();

                            Intent update = new Intent(ViewRecords.this, EditData.class);
                            update.putExtra("Item_ID", itemID);
                            update.putExtra("Item_Name", itemName);
                            update.putExtra("Item_EXP", itemExp);
                            update.putExtra("Item_Amount", itemAmount);
                            startActivity(update);
                        }
                        else if(idk==1)
                        {
                            id = i;
                            String itemID = batchList.get(i).getBatchID();
                            
                            databaseReference2.child(itemID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    batchList.remove(i);
//                                    ItemAdapter adapter = new ItemAdapter(ViewRecords.this, itemList);
//                                    myList.setAdapter(adapter);
                                    Toast.makeText(ViewRecords.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                builder.create().show();
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_view1:
                startActivity(new Intent(ViewRecords.this,ViewRecords.class));
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
                            startActivity(new Intent(ViewRecords.this, AddRecords.class));
                        }
                        else if(i==1)
                        {
                            startActivity(new Intent(ViewRecords.this, OCR_Activity2.class));
                        }
                    }
                });
                builder.create().show();
                break;
            case R.id.nav_edit:
                startActivity(new Intent(ViewRecords.this,EditData.class));
                break;
            case R.id.nav_logout:
                Toast.makeText(ViewRecords.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ViewRecords.this,Login.class));
                break;
            default:
                startActivity(new Intent(ViewRecords.this,MainActivity.class));
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