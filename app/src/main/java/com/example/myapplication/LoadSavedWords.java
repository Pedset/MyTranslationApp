package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class LoadSavedWords extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_saved_words);
        setTitle("Saved Words");
        TextView textView1 = findViewById(R.id.textViewLangFrom);
        TextView textView2 = findViewById(R.id.textViewIn);
        TextView textView3 = findViewById(R.id.textViewLangTo);
        TextView textView4 = findViewById(R.id.textViewOut);

        Intent intent = getIntent();

        textView1.setText("From " + intent.getStringExtra("langFrom").substring(0,intent.getStringExtra("langFrom").length()-2 ));
        textView2.setText(intent.getStringExtra("wordSearched"));
        textView3.setText("To " + intent.getStringExtra("langTo").substring(0,intent.getStringExtra("langTo").length()-2 ));
        textView4.setText(intent.getStringExtra("wordOut"));

    }
}
