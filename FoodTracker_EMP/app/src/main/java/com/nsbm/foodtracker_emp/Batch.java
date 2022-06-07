package com.nsbm.foodtracker_emp;

public class Batch {
    private String amount;
    private String batchID;
    private String productExp;
    private String productName;

    public Batch() {
    }

    public Batch(String amount, String batchID, String productExp, String productName) {
        this.amount = amount;
        this.batchID = batchID;
        this.productExp = productExp;
        this.productName = productName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public String getProductExp() {
        return productExp;
    }

    public void setProductExp(String productExp) {
        this.productExp = productExp;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
