package com.nsbm.foodtracker_emp;

public class Product {
    private String ProductID;
    private String ProductName;
    private String ProductPrice;

    public Product() {
    }

    public Product(String productID, String productName, String productPrice) {
        ProductID = productID;
        ProductName = productName;
        ProductPrice = productPrice;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(String productPrice) {
        ProductPrice = productPrice;
    }
}
