package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddRecords extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    Button btnInsert,btnEdit;
    EditText itemName,itemExp;
    Item items;
    ListView listView;
    List<Item> itemList;
    final Calendar myCalender = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_records);

        mAuth = FirebaseAuth.getInstance();
        btnInsert = findViewById(R.id.btn_insert);
        itemName = findViewById(R.id.txt_itemName);
        btnEdit = findViewById(R.id.btn_edit);
        listView = findViewById(R.id.txt_list);
        itemExp = findViewById(R.id.editDate);

        items = new Item();

        itemList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Items");

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

        databaseReference.child(KeyID).setValue(items);

    }

    private void updateCalender()
    {
        String myFormat = "yy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.UK);
        btnEdit.setText(dateFormat.format(myCalender.getTime()));
    }
}