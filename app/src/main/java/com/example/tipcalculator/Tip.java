package com.example.tipcalculator;

class Tip {
    private int id;
    private int bill_date;
    private float bill_amount;
    private float tip_percent;
//    private String date;

    public Tip() {
        //empty
    }

    public Tip(int id, int bill_date, float bill_amount, float tip_percent) {
        this.id = id;
        this.bill_date = bill_date;
        this.bill_amount = bill_amount;
        this.tip_percent = tip_percent;
//        this.date = "";
    }

    public int getID() {
        return id;
    }

    public int getBillDate() {
        return bill_date;
    }

    public float getBillAmount() {
        return bill_amount;
    }

    public float getTipPercent() {
        return tip_percent;
    }

//    public String getDate() {
//        return date;
//    }

    public void setID(int id) {
        this.id = id;
    }

    public void setDate(int bill_date) {
        this.bill_date = bill_date;
    }

    public void setAmount(float bill_amount) {
        this.bill_amount = bill_amount;
    }

    public void setPercent(float tip_percent) {
        this.tip_percent = tip_percent;
    }

//    public void setDate(String date) {
//        this.date = date;
//    }
}
