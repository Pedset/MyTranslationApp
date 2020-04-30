package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    String traslateText;
    private RequestQueue mQueue;
    String savedTranslated;
    String savedSearched;
    Boolean wordIsSaved = false;
    String savedLangFrom;
    String savedLangTo;
    String voiceLang = "";
    private TextToSpeech mTTs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Spinner mySpinner = findViewById(R.id.spinner);
        Spinner mySpinner2 = findViewById(R.id.spinner2);

        // adapter for dropdown menu
        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.languages));

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);
        mySpinner2.setAdapter(myAdapter);

        mQueue = Volley.newRequestQueue(this);

    }
    // text to speech method that's called from speak btn
    private void speak(){
        TextView txtV = findViewById(R.id.textView4);
        String text = txtV.getText().toString();
        float pitch = 1.0f;
        float speed = 1.0f;
        mTTs.setPitch(pitch);
        mTTs.setSpeechRate(speed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTTs.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            mTTs.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }



 // force stop text to speech when app is closed
    @Override
    protected void onDestroy() {
        if (mTTs!= null){
            mTTs.stop();
            mTTs.shutdown();
        }
        super.onDestroy();
    }



 // speech to text
    public void setSpeech(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        Spinner spinner = findViewById(R.id.spinner);
        String lang = spinner.getSelectedItem().toString().toUpperCase();

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (intent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Speech to text is not supported", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    TextView textView = findViewById(R.id.input);
                    textView.setText(result.get(0));
                }
            }
        }
    }



// gets the text / sends it with api call / gets json obj / writes the translated text /
    public void translateText(View view) {
        final TextView textView = findViewById(R.id.textView4);
        Spinner spinnerFrom = findViewById(R.id.spinner);
        Spinner spinnerTo = findViewById(R.id.spinner2);
        TextView ourText = findViewById(R.id.input);

        // comma = %2c     space = %20   ///    ! = %21     //   ? = %3F   /// . = 	%2E
// ...
        String translateFrom;
        String translateTo;

        if (ourText != null) {

            traslateText = ourText.getText().toString().replaceAll("\\s+", "%20");
            traslateText = traslateText.replaceAll("\\,", "%2c");
            traslateText = traslateText.replaceAll("\\!", "%21");
            traslateText = traslateText.replaceAll("\\.", "%2E");
            traslateText = traslateText.replaceAll("\\?", "%3F");
        }
        translateFrom = spinnerFrom.getSelectedItem().toString().substring(spinnerFrom.getSelectedItem().toString().length() - 2);
        translateTo = spinnerTo.getSelectedItem().toString().substring(spinnerTo.getSelectedItem().toString().length() - 2);
        voiceLang = spinnerTo.getSelectedItem().toString().toUpperCase().substring(0,spinnerTo.getSelectedItem().toString().length() - 3);

        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20200427T192712Z.e87c72affbaf3316.f1c99ef3dd944b77af9e2125ae12a8d91a31d1d7&text=" + traslateText + "&lang=" + translateFrom + "-" + translateTo;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                JSONArray jsonArray = null;
                try {
                    jsonArray = response.getJSONArray("text");


                    textView.setText(jsonArray.get(0).toString());
                    // mTextViewResult.append();


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Failed to get data", Toast.LENGTH_LONG).show();
            }
        });

        mQueue.add(request);


        // text to speech
        mTTs = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){

                    int result = mTTs.setLanguage(Locale.ENGLISH);
                    switch (voiceLang){

                        case "GERMAN" :{
                            result = mTTs.setLanguage(Locale.GERMAN);
                            break;
                        }
                        case "CHINESE" :{
                            result = mTTs.setLanguage(Locale.CHINESE);
                            break;
                        }
                        case "FRENCH" :{
                            result = mTTs.setLanguage(Locale.FRENCH);
                            break;
                        }
                    }
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS","Language not supported");
                    } else {

                    }

                } else{
                    Log.e("TTS","Initialization failed");
                }
            }
        });

        ImageButton imgbtn = findViewById(R.id.imageButton2);
        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = findViewById(R.id.textView4);
                if (textView.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Text field is empty", Toast.LENGTH_LONG).show();
                }
                else{
                    speak();
                }
            }
        });
    }
// saves the text, from which lang to which and the translated text
    public void saveText(View view) {
        TextView textViewIn = findViewById(R.id.input);
        TextView textViewOut = findViewById(R.id.textView4);
        Spinner spinnerFrom = findViewById(R.id.spinner);
        Spinner spinnerTo = findViewById(R.id.spinner2);

        if (textViewIn.getText().toString().isEmpty() || textViewOut.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "One or both text fields are empty", Toast.LENGTH_LONG).show();
        } else {
            savedSearched = textViewIn.getText().toString();
            savedTranslated = textViewOut.getText().toString();
            savedLangFrom = spinnerFrom.getSelectedItem().toString();
            savedLangTo = spinnerTo.getSelectedItem().toString();
            wordIsSaved = true;
            Toast.makeText(getApplicationContext(), "Words are successfully saved!", Toast.LENGTH_LONG).show();
        }

    }
// copies the translated text into clipboard
    public void copyWord(View view) {
        TextView textViewOut = findViewById(R.id.textView4);
        if (textViewOut.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "There is nothing to copy", Toast.LENGTH_LONG).show();
        } else {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("EditText", textViewOut.getText().toString());
            clipboardManager.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied!", Toast.LENGTH_LONG).show();
        }


    }

    // Save in shared preferences on pause if we have saved.
    @Override
    protected void onPause() {
        super.onPause();

        if (wordIsSaved) {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("wordSearched", savedSearched);
            editor.putString("wordOut", savedTranslated);
            editor.putString("langFrom", savedLangFrom);
            editor.putString("langTo", savedLangTo);
            editor.putBoolean("savedOrWhat", wordIsSaved);

            editor.apply();
        }

    }
// set the values back to variables on start
    @Override
    protected void onStart() {
        super.onStart();
        try {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            savedSearched = prefs.getString("wordSearched", "hi");
            savedLangTo = prefs.getString("langTo", "hi");
            savedLangFrom = prefs.getString("langFrom", "hi");
            savedTranslated = prefs.getString("wordOut", "hi");
            wordIsSaved = prefs.getBoolean("savedOrWhat", false);

        } catch (Exception e) {

        }
    }

    //new page -> send data with start activity for result to next page

    public void loadSavedWords(View view) {
    if (wordIsSaved){
        Intent intent = new Intent(MainActivity.this,LoadSavedWords.class);
        intent.putExtra("wordSearched", savedSearched);
        intent.putExtra("langTo", savedLangTo);
        intent.putExtra("langFrom", savedLangFrom);
        intent.putExtra("wordOut", savedTranslated);



        startActivityForResult(intent, 0);
    }
              else{
                  Toast.makeText(getApplicationContext(), "There are no words saved!", Toast.LENGTH_LONG).show();
    }








    }
}