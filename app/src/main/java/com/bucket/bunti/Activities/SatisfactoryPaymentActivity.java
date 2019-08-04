package com.bucket.bunti.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.bucket.bunti.R;

public class SatisfactoryPaymentActivity extends AppCompatActivity {

    private TextView textViewMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satisfactory_payment);
        Intent intent = getIntent();
        String minutes = intent.getStringExtra("minutes");
        textViewMinutes = findViewById(R.id.textViewMinutes);
        textViewMinutes.setText(String.format("%s", minutes));
    }
}
