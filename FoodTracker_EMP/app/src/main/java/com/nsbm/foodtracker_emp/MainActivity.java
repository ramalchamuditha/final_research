package com.nsbm.foodtracker_emp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button AddRecord,viewRecords,Edit;
    FirebaseAuth mAuth;
    DrawerLayout drawer;
    ImageView userIMG;
    TextView profileName,profileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        AddRecord = findViewById(R.id.btn_addRecord);
        viewRecords = findViewById(R.id.btn_viewData);
        Edit = findViewById(R.id.btn_editData);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        userIMG = (ImageView) headerLayout.findViewById(R.id.userImage);
        profileName = (TextView) headerLayout.findViewById(R.id.nav_userName);
        profileEmail = (TextView) headerLayout.findViewById(R.id.nav_Email);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


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
            default:
                startActivity(new Intent(MainActivity.this,MainActivity.class));
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}