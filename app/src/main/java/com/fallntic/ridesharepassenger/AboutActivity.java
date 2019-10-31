package com.fallntic.ridesharepassenger;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.android.rideshareclient.R;

public class AboutActivity extends AppCompatActivity {

    private TextView mTitle;
    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mTitle = (TextView) findViewById(R.id.titleText);
        mText = (TextView) findViewById(R.id.aboutText);
    }
}
