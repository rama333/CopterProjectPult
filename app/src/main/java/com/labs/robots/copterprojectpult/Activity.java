package com.labs.robots.copterprojectpult;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ramil on 15.11.2018.
 */

public class Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti);

        Button button = (Button) findViewById(R.id.button);
        final TextView textView = (TextView) findViewById(R.id.textView);

        final EditText editText = (EditText) findViewById(R.id.editText2);
        final EditText editText1 = (EditText) findViewById(R.id.editText3);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(String.valueOf(calc(Integer.valueOf(editText.getText().toString()),Integer.valueOf(editText1.getText().toString()), 0)));
                textView.setTextColor(Color.BLUE);
            }
        });






    }

    private static int calc(int a, int b, int c) {

        if (c == 0)
            return a + b;
        if (c == 1)
            return a - b;
        if (c == 2)
            return a * b;
        if (c == 3)
            return a / b;

        return 0;
    }
}