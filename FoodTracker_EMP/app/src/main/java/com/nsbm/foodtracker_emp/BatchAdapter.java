package com.nsbm.foodtracker_emp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BatchAdapter extends ArrayAdapter {
    private Activity mContext;
    List<Batch> batchList;

    public BatchAdapter(Activity mContext,List<Batch> batchList)
    {
        super(mContext,R.layout.batch_data,batchList);
        this.mContext = mContext;
        this.batchList = batchList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater2 = mContext.getLayoutInflater();
        View listItemView = inflater2.inflate(R.layout.batch_data,null,true);

        TextView txtBatchID = listItemView.findViewById(R.id.BatchDataID);
        TextView txtBatchProduct = listItemView.findViewById(R.id.BatchProductName);
        TextView txtBatchEXP = listItemView.findViewById(R.id.BatchDataExp);

        Batch batch = batchList.get(position);

        txtBatchID.setText(batch.getBatchID());
        txtBatchProduct.setText(batch.getProductName());
        txtBatchEXP.setText(batch.getProductExp());

        return listItemView;

    }
}
