package com.nsbm.foodtracker_emp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
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
import com.theartofdev.edmodo.cropper.CropImage;


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
    DatabaseReference databaseReference,databaseReferenceUsers,databaseReference1,databaseReference2;
    FirebaseDatabase firebaseDatabase;
    DrawerLayout drawer;
    ImageView userIMG;
    TextView profileName,profileEmail;
    MaterialButton click, clickDate, btnEdit, btnUpdate;
    TextView detectedText, detectedTextBlocks;
    EditText txtAmount;
    Item items;
    Product product;
    Batch batch;
    ListView listView;
    List<Product> productList;
    List<Batch> batchList;
    AutoCompleteTextView autoCompleteTextView;

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
        autoCompleteTextView = findViewById(R.id.txt_itemName56);
        txtAmount = findViewById(R.id.txtAmount);

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


        items = new Item();
        product = new Product();
        batch = new Batch();

        productList = new ArrayList<>();
        batchList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Items");
        databaseReference1 = firebaseDatabase.getReference("Products");
        databaseReference2 = firebaseDatabase.getReference("BatchItems");
        databaseReferenceUsers = firebaseDatabase.getReference("Users");

        populateSearch();

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                batchList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Batch batch1 = dataSnapshot.getValue(Batch.class);
                    batchList.add(batch1);
                }
                BatchAdapter batchAdapter = new BatchAdapter(OCR_Activity2.this,batchList);
                listView.setAdapter(batchAdapter);

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

    private void updateListed()
    {
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                batchList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Batch batch1 = dataSnapshot.getValue(Batch.class);
                    batchList.add(batch1);
                }
                BatchAdapter batchAdapter = new BatchAdapter(OCR_Activity2.this,batchList);
                listView.setAdapter(batchAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateData() {

        if (detectedText.getText().toString().isEmpty() && detectedTextBlocks.getText().toString().isEmpty()) {
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        } else if (detectedText.getText().toString().isEmpty()) {
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        } else if (txtAmount.getText().toString().isEmpty()){
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        }
        else if (autoCompleteTextView.getText().toString().isEmpty())
        {
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        }
        else if (detectedTextBlocks.getText().toString().isEmpty()) {
            Toast.makeText(OCR_Activity2.this, "Please add data to the above fields first", Toast.LENGTH_SHORT).show();
        } else {

            //String KeyID = firebaseDatabase.getReference("Items").push().getKey();
            //items.setUserID(mAuth.getUid());
//            items.setItemName(detectedText.getText().toString());
//            items.setExpireDate(checkDate(detectedTextBlocks.getText().toString()));
//            items.setItemID(KeyID);

            batch.setBatchID(detectedText.getText().toString());
            batch.setProductExp(checkDate(detectedTextBlocks.getText().toString()));
            batch.setAmount(txtAmount.getText().toString());
            batch.setProductName(autoCompleteTextView.getText().toString());

            databaseReference2.child(detectedText.getText().toString()).setValue(batch);
            updateListed();
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
                Toast.makeText(OCR_Activity2.this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OCR_Activity2.this,Login.class));
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

}