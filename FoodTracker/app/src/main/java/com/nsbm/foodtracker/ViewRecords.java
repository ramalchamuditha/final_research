package com.nsbm.foodtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewRecords extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ListView myList;
    Button btnEdit;
    Item items;
    List<Item> itemList;
    int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_records);

        firebaseAuth = FirebaseAuth.getInstance();
        myList = findViewById(R.id.viewList1);

        items = new Item();
        itemList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Items");

        String UserID = firebaseAuth.getCurrentUser().getUid();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();

                for (DataSnapshot itemDataSnap : snapshot.getChildren()) {
                    Item item = itemDataSnap.getValue(Item.class);
                    String CurrentUID = item.getUserID().toString();
                    if (CurrentUID.equals(UserID)) {
                        itemList.add(item);
                    }

                }
                ItemAdapter adapter = new ItemAdapter(ViewRecords.this, itemList);
                myList.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                id = i;
                String itemID = itemList.get(i).getItemID();
                String itemName = itemList.get(i).getItemName();
                String itemExp = itemList.get(i).getExpireDate();


                Intent update = new Intent(ViewRecords.this, EditData.class);
                update.putExtra("Item_ID", itemID);
                update.putExtra("Item_Name", itemName);
                update.putExtra("Item_EXP", itemExp);
                startActivity(update);


            }
        });

    }

}