package com.nsbm.foodtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCR_Activity extends AppCompatActivity {

    Button btnCapture,btncopy;
    TextView viewText;
    private static final int REQUESTS_CAMERA_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        btnCapture = findViewById(R.id.BtnCapture);
        btncopy = findViewById(R.id.btn_copy);
        viewText = findViewById(R.id.view_data);
        

        if(ContextCompat.checkSelfPermission(OCR_Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(OCR_Activity.this,new String[]{
                    Manifest.permission.CAMERA
            }, REQUESTS_CAMERA_CODE);
        }
        
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                
            }
        });


    }
}