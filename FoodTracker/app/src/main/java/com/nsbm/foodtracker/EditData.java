package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditData extends AppCompatActivity {
    EditText txtName,txtEXP;
    String itemID,itemName,itemEXP;
    Button btnUpdate;
    private DatabaseReference mDatabase;
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);

        txtName = findViewById(R.id.updateName);
        txtEXP = findViewById(R.id.updateEXp);
        btnUpdate = findViewById(R.id.btn_update);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference("Items");

        Intent intent = getIntent();
        itemID = intent.getStringExtra("Item_ID");
        itemName = intent.getStringExtra("Item_Name");
        itemEXP = intent.getStringExtra("Item_EXP");

        txtName.setText(itemName);
        txtEXP.setText(itemEXP);

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
                else
                {
                    updateItem(itemID,itemName,itemEXP);
                }

            }
        });


    }

    private void updateItem(String ItemID,String ItemName, String ItemEXP)
    {
        if (!ItemName.equals(txtName.getText().toString()))
        {
            mDatabase.child(ItemID).child("itemName").setValue(txtName.getText().toString());
        }
        if (!ItemEXP.equals(txtEXP.getText().toString()))
        {
            mDatabase.child(ItemID).child("expireDate").setValue(txtEXP.getText().toString());
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

        mDatabase.addChildEventListener(childEventListener);

    }


}