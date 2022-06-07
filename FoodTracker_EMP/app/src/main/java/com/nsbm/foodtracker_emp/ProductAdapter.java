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

public class ProductAdapter extends ArrayAdapter {
    private Activity mContext;
    List<Product> productList;

    public ProductAdapter(Activity mContext, List<Product> productList){
        super(mContext,R.layout.products_datas,productList);
        this.mContext = mContext;
        this.productList = productList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = mContext.getLayoutInflater();
        View listItemView = inflater.inflate(R.layout.products_datas,null,true);

        TextView txtProductID = listItemView.findViewById(R.id.productDataID);
        TextView txtProductName = listItemView.findViewById(R.id.productDataName);
        TextView txtProductEXP = listItemView.findViewById(R.id.productDataPrice);


        Product product = productList.get(position);

        txtProductID.setText(product.getProductID());
        txtProductName.setText(product.getProductName());
        txtProductEXP.setText(product.getProductPrice());


        return listItemView;
    }
}
