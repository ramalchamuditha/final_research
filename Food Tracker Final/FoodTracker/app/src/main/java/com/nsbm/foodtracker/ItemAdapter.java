package com.nsbm.foodtracker;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ItemAdapter extends ArrayAdapter {

    private Activity mContext;
    List<Item> itemsList;

    public ItemAdapter(Activity mContext, List<Item> itemsList){
        super(mContext,R.layout.item_data,itemsList);
        this.mContext = mContext;
        this.itemsList = itemsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = mContext.getLayoutInflater();
        View listItemView = inflater.inflate(R.layout.item_data,null,true);

        TextView txtItemName = listItemView.findViewById(R.id.itemDataName);
        TextView txtItemEXP = listItemView.findViewById(R.id.itemDataEXP);

        Item item = itemsList.get(position);

        txtItemName.setText(item.getItemName());
        txtItemEXP.setText(item.getExpireDate());

        return listItemView;
    }
}
