package com.example.tipcalculator;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.content.SharedPreferences.Editor;

public class TipCalculatorActivity extends AppCompatActivity
        implements OnEditorActionListener, OnClickListener {

    // helper object and variable
    StringBuilder sb;
    NumberFormat currency;
    NumberFormat percent;
    private int count = 0;

    // constant
    private static final float DEFAULT_TIP = 0.15f;
    private static final String TAG = "Bill List";
    // database object
    private TipCalculatorDB db;

    // define variables for the widgets
    private EditText billAmountEditText;
    private TextView percentTextView;
    private Button   percentUpButton;
    private Button   percentDownButton;
    private Button   saveButton;
    private TextView tipTextView;
    private TextView totalTextView;
    private TextView stringBuilderView;

    // define the SharedPreferences object
    private SharedPreferences savedValues;

    // define instance variables that should be saved
    private String billAmountString = "";
    private float tipPercent = DEFAULT_TIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_calculator_activity);

        // instantiate db object and helper object
        db = new TipCalculatorDB(this);
        sb = new StringBuilder();

        // get references to the widgets
        billAmountEditText  = findViewById(R.id.billAmount_editText);
        percentTextView     = findViewById(R.id.percent_textView);
        percentUpButton     = findViewById(R.id.percentUp);
        percentDownButton   = findViewById(R.id.percentDown);
        saveButton          = findViewById(R.id.tipSave);
        tipTextView         = findViewById(R.id.tip_textView);
        totalTextView       = findViewById(R.id.total_textView);
        stringBuilderView   = findViewById(R.id.stringBuilder_textView);

        // set the listeners
        billAmountEditText.setOnEditorActionListener(this);
        percentUpButton.setOnClickListener(this);
        percentDownButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        // Percentage and Currency Format Instance
        currency = NumberFormat.getCurrencyInstance();
        percent = NumberFormat.getPercentInstance();

        // get SharedPreferences object
        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.percentDown:
                tipPercent = tipPercent - .01f;
                calculateAndDisplay();
                break;
            case R.id.percentUp:
                tipPercent = tipPercent + .01f;
                calculateAndDisplay();
                break;

            case R.id.tipSave:
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                float billAmount = Float.parseFloat(billAmountEditText.getText().toString());

                Tip tip = new Tip(1, currentYear, billAmount, tipPercent);
                long insertID = db.saveTip(tip);
                if (insertID > 0) {
                    count++;
                    Log.d(TAG, "Inserted, the current ID is: " + insertID);
                    sb.append(count +". New item has been inserted. Current ID is "+ insertID +"\n");
                }

                // clear display and set new tip percent
                tipPercent = db.getAverageTipPercent();
                clearDisplay();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }
        return false;
    }

    @Override
    public void onPause() {
        // save the instance variables
        Editor editor = savedValues.edit();
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        tipPercent = db.getAverageTipPercent();
        Log.d(TAG, "avg_tip: " + tipPercent);
        count++;
        sb.append(count +". Average Tip Percent from Database: " + percent.format(tipPercent) + "\n");

        // get last saved item
        Log.d(TAG, "last_saved: " + db.getLastSaveDate());
        count++;
        sb.append(count +". Last Save Date is " + db.getLastSaveDate() + "\n");

        // call getTips method and loops through all saved tips
        count++;
        sb.append(count + ". Bill List from Database: " + "\n");
        ArrayList<Tip> tips = db.getTips();
        for (Tip t: tips) {
            Log.d(TAG,
                    "Bill_ID: " + t.getID() +
                            " Bill_Year: " + t.getBillDate() +
                            " Bill_Amount: " + t.getBillAmount() +
                            " Tip_Percent: " + t.getTipPercent());

            sb.append(
                    "       Bill ID: " + t.getID() + "\n" +
                    "       Bill Year: " + t.getBillDate() + "\n" +
                    "       Bill Amount: " + currency.format(t.getBillAmount()) + "\n" +
                    "       Tip Percent: " + percent.format(t.getTipPercent()) + "\n\n");
        }

        // get the instance variables
        billAmountString = savedValues.getString("billAmountString", "");
        tipPercent = savedValues.getFloat("tipPercent", DEFAULT_TIP);

        // set the bill amount and tip percent on its widget
        billAmountEditText.setText(billAmountString);

        // calculate and display tip result
        calculateAndDisplay();

        // display string builder
        displayStringBuilder();
    }

    public void calculateAndDisplay() {

        // get the bill amount
        billAmountString = billAmountEditText.getText().toString();
        float billAmount;
        if (billAmountString.equals("")) {
            billAmount = 0;
        }
        else {
            billAmount = Float.parseFloat(billAmountString);
        }

        // calculate tip and total
        float tipAmount = billAmount * tipPercent;
        float totalAmount = billAmount + tipAmount;

        // display the other results with formatting
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));
        percentTextView.setText(percent.format(tipPercent));
    }

    public void clearDisplay(){
        billAmountEditText.setText("");
        percentTextView.setText(percent.format(tipPercent));
        tipTextView.setText(currency.format(0));
        totalTextView.setText(currency.format(0));
    }

    private void displayStringBuilder() {
        stringBuilderView.setText(sb.toString());
    }
}
