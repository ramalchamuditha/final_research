package com.nsbm.foodtracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class OCR_Test1 extends AppCompatActivity implements View.OnClickListener {
    ImageView imageView;
    Button btnCapture,btnText;
    TextView viewText;
    Intent CameraIntent,CropIntent;
    public static final int RequestPermissionCode = 1;
    DisplayMetrics displayMetrics;
    File file;
    Uri uri;
    int width,height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_test1);

        imageView = findViewById(R.id.imageView26);
        btnCapture = findViewById(R.id.captureImage1);
        btnText = findViewById(R.id.btnIdentify1);
        viewText = findViewById(R.id.txtIdentified1);
        btnCapture.setOnClickListener(this);

        EnableRunTimePermission();

    }

    private void EnableRunTimePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(OCR_Test1.this,
                Manifest.permission.CAMERA))
        {
            Toast.makeText(OCR_Test1.this, "Camera permission allows", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(OCR_Test1.this,new String[]{Manifest.permission.CAMERA},RequestPermissionCode);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.captureImage1:
                ClickImageFromCamera();
                break;

            case R.id.btnIdentify1:
                Toast.makeText(OCR_Test1.this, "Not completed", Toast.LENGTH_SHORT).show();
        }

    }

    private void ClickImageFromCamera()
    {
        CameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                "file"+String.valueOf(System.currentTimeMillis())+".jpg");
        uri = Uri.fromFile(file);
        CameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        CameraIntent.putExtra("return-data",true);
        startActivityForResult(CameraIntent,0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK)
        {
            ImageCropFunction();
        }
        else if(requestCode == 2)
        {
            if(data != null)
            {
                uri = data.getData();
                ImageCropFunction();
            }
        }
        else if(requestCode == 1)
        {
            if(data != null)
            {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void ImageCropFunction() {
        try {
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri, "image/*");
            CropIntent.putExtra("crop", true);
            CropIntent.putExtra("outputX", 180);
            CropIntent.putExtra("outputY", 180);
            CropIntent.putExtra("aspectX", 3);
            CropIntent.putExtra("aspectY", 3);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);
            startActivityForResult(CropIntent,2);

        }catch (ActivityNotFoundException e)
        {

        }
    }


}