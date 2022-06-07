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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OCR_Activity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String cameraPermission[];
    String storagePermission[];
    InputImage image, image1;
    DatabaseReference databaseReference,databaseReferenceUsers;
    FirebaseDatabase firebaseDatabase;
    DrawerLayout drawer;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    ImageView userIMG;
    TextView profileName,profileEmail;
    MaterialButton click, clickDate, btnEdit, btnUpdate;
    TextView detectedText, detectedTextBlocks;
    Item items;
    ListView listView;
    List<Item> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr2);

        Toolbar toolbar = findViewById(R.id.toolBar8);
        setSupportActionBar(toolbar);

        click = findViewById(R.id.click);
        clickDate = findViewById(R.id.click1);
        btnEdit = findViewById(R.id.click2);
        btnUpdate = findViewById(R.id.click3);

        detectedText = findViewById(R.id.detectedText);
        detectedTextBlocks = findViewById(R.id.detectedText3);

        listView = findViewById(R.id.txt_list1);

        drawer = findViewById(R.id.drawerLayoutOCR);
        NavigationView navigationView = findViewById(R.id.nav_view8);
        navigationView.setNavigationItemSelectedListener(this);

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

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Items");
        databaseReferenceUsers = firebaseDatabase.getReference("Users");

        mAuth = FirebaseAuth.getInstance();
        String UserID = mAuth.getCurrentUser().getUid();

        items = new Item();

        itemList = new ArrayList<>();


        databaseReference.addValueEventListener(new ValueEventListener() {
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
                ItemAdapter adapter = new ItemAdapter(OCR_Activity2.this,itemList);
                listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // allowing permissions of gallery and camera
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();
            }
        });
        clickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OCR_Activity2.this, CropperActivity2.class);
                startActivityForResult(intent, 101);
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OCR_Activity2.this,AddRecords.class));
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (detectedText.getText().toString().isEmpty() && detectedTextBlocks.getText().toString().isEmpty()) {
                    Toast.makeText(OCR_Activity2.this, "Please add the valid item name and item's expire date", Toast.LENGTH_SHORT).show();
                } else if (detectedText.getText().toString().isEmpty()) {
                    Toast.makeText(OCR_Activity2.this, "Please add the valid item name and item's expire date", Toast.LENGTH_SHORT).show();
                } else if (detectedTextBlocks.getText().toString().isEmpty()) {
                    Toast.makeText(OCR_Activity2.this, "Please add the valid item name and item's expire date", Toast.LENGTH_SHORT).show();
                } else {
                    updateData();
                }

            }
        });
    }

    private void updateData() {

        if (detectedText.getText().toString().isEmpty() && detectedTextBlocks.getText().toString().isEmpty()) {
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        } else if (detectedText.getText().toString().isEmpty()) {
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        } else if (detectedTextBlocks.getText().toString().isEmpty()) {
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        } else {

            String KeyID = firebaseDatabase.getReference("Items").push().getKey();
            items.setUserID(mAuth.getUid());
            items.setItemName(detectedText.getText().toString());
            items.setExpireDate(checkDate(detectedTextBlocks.getText().toString()));
            items.setItemID(KeyID);

            databaseReference.child(KeyID).setValue(items);
        }

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

    private void pickFromGallery() {
        CropImage.activity().start(OCR_Activity2.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    image = InputImage.fromFilePath(this, resultUri);
                    detectTextMethod();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (resultCode == -1 && requestCode == 101) {
            String result = data.getStringExtra("RESULT");
            Uri resultUri = null;
            if (result != null) {
                resultUri = Uri.parse(result);
            }
            try {
                image1 = InputImage.fromFilePath(this, resultUri);
                detectTextMethod1();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (resultCode == -88)
        {
            String NewDate = data.getStringExtra("NewEXP");
            String NewItem = data.getStringExtra("NewItem");
            detectedTextBlocks.setText(NewDate); // Date
            detectedText.setText(NewItem); //Name
        }
    }

    private void detectTextMethod() {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                String resultText = text.getText();
                for (Text.TextBlock block : text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoints = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoints = line.getCornerPoints();
                        Rect lineFrame = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            Rect elementFrame = element.getBoundingBox();
                            //detectedTextBlocks.setText(elementText);   // get the last line of the scanned document or subject
                        }
                    }
                    detectedText.setText(blockText);   // result of the function
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OCR_Activity2.this, "Fail to detect Text" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void detectTextMethod1() {
        TextRecognizer recognizer1 = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer1.process(image1).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                String resultText = text.getText();
                for (Text.TextBlock block : text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoints = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoints = line.getCornerPoints();
                        Rect lineFrame = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            Rect elementFrame = element.getBoundingBox();
                            detectedTextBlocks.setText(elementText);   // get the last line of the scanned document or subject
                        }
                    }
                    //detectedText.setText(blockText);   // result of the function
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OCR_Activity2.this, "Fail to detect Text" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }

    public String checkDate(String input) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yy-MM-dd");
        SimpleDateFormat sdf3 = new SimpleDateFormat("dd-MM-yy");
        sdf.setLenient(false);
        sdf1.setLenient(false);

        if (input.matches("([0-9]{2}).([0-9]{2}).([0-9]{4})")) {  // dd.MM.yyyy
            String newDate = input.replace(".", "-");
            try {
                Date parsedDate = sdf1.parse(newDate);  // dd-MM-yyyy
                String newPDate = sdf.format(parsedDate); // yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (input.matches("([0-9]{4})/([0-9]{2})/([0-9]{2})")) {  // yyyy/MM/dd
            String newDate = input.replace("/", "-");           //yyyy-MM-dd
            try {
                Date parsedDate = sdf.parse(newDate);  //  yyyy-MM-dd
                String newPDate = sdf.format(parsedDate); // yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (input.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})")) {    // dd/MM/yyyy
            String newDate = input.replace("/", "-");       //dd-MM-yyyy
            try {
                Date parsedDate = sdf1.parse(newDate);  // dd-MM-yyyy
                String newPDate = sdf.format(parsedDate); // yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else if (input.matches("([0-9]{2})/([0-9]{2})/([0-9]{2})")) {  // dd/MM/yy
            String newDate = input.replace("/", "-");       // dd-MM-yy
            try {
                Date parsedDate = sdf3.parse(newDate);                     // dd-MM-yy
                String newPDate = sdf.format(parsedDate);                   // yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else if (input.matches("([0-9]{2})/([0-9]{2})/([0-9]{2})")) {  // yy/MM/dd
            String newDate = input.replace("/", "-");           // yy-MM-dd
            try {
                Date parsedDate = sdf2.parse(newDate);                          //yy-MM-dd
                String newPDate = sdf.format(parsedDate);                       //yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else if (input.matches("([0-9]{4}).([0-9]{2}).([0-9]{2})")) {  // yyyy.MM.dd
            String newDate = input.replace("/", "-");           // yyyy-MM-dd
            try {
                Date parsedDate = sdf.parse(newDate);                          //yyyy-MM-dd
                String newPDate = sdf.format(parsedDate);                       //yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else if (input.matches("([0-9]{2}).([0-9]{2}).([0-9]{2})")) {  // dd.MM.yy
            String newDate = input.replace(".", "-");           // dd-MM-dd
            try {
                Date parsedDate = sdf3.parse(newDate);                          //dd-MM-yy
                String newPDate = sdf.format(parsedDate);                       //yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else if (input.matches("([0-9]{2}).([0-9]{2}).([0-9]{2})")) {  // yy.MM.dd
            String newDate = input.replace(".", "-");           // yy-MM-dd
            try {
                Date parsedDate = sdf2.parse(newDate);                          //yy-MM-dd
                String newPDate = sdf.format(parsedDate);                       //yyyy-MM-dd
                //Toast.makeText(OCR_Activity2.this, "Date " +newPDate, Toast.LENGTH_SHORT).show();
                return newPDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return "wrong value";
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_view1:
                startActivity(new Intent(OCR_Activity2.this,ViewRecords.class));
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
                            startActivity(new Intent(OCR_Activity2.this,AddRecords.class));
                        }
                        else if(i==1)
                        {
                            startActivity(new Intent(OCR_Activity2.this,OCR_Activity2.class));
                        }
                    }
                });
                builder.create().show();
                break;
            case R.id.nav_edit:
                startActivity(new Intent(OCR_Activity2.this,EditData.class));
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(OCR_Activity2.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OCR_Activity2.this,Login.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(OCR_Activity2.this, UserProfile.class));
                break;
            default:
                startActivity(new Intent(OCR_Activity2.this,MainActivity.class));
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

                        Glide.with(OCR_Activity2.this).load(image_url).into(userIMG);
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
                        Glide.with(OCR_Activity2.this).load(_userImage).into(userIMG);
                        profileName.setText(_userName);
                        profileEmail.setText(_userEmail);
                        //Toast.makeText(ViewRecords.this, "email: "+_userEmail, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


    }
}