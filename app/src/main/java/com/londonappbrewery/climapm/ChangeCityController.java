package com.londonappbrewery.climapm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeCityController extends AppCompatActivity {

    EditText editTextField;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.change_city_layout);

        editTextField = (EditText) findViewById(R.id.queryET);
        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChangeCityController.this,WeatherController.class));
                finish();
            }
        });

        editTextField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String newCity = editTextField.getText().toString();

                Intent newCityIntent = new Intent(ChangeCityController.this, WeatherController.class);
                newCityIntent.putExtra("city", newCity);
                startActivity(newCityIntent);
                finish();

             return true;
            }
        });

        super.onCreate(savedInstanceState);
    }
}
