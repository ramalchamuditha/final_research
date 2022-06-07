package com.nsbm.foodtracker_emp;

public class Employee {
    private String employeeID;
    private String password;
    private String role;
    private String shopID;
    private String userName;

    public Employee() {
    }

    public Employee(String employeeID, String password, String role, String shopID, String userName) {
        this.employeeID = employeeID;
        this.password = password;
        this.role = role;
        this.shopID = shopID;
        this.userName = userName;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
