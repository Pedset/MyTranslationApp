package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Spinner mySpinner = findViewById(R.id.spinner);
        Spinner mySpinner2= findViewById(R.id.spinner2);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(MainActivity.this,
               android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.languages));

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);
        mySpinner2.setAdapter(myAdapter);

        mQueue = Volley.newRequestQueue(this);






    }

    public void setSpeech(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        Spinner spinner= (Spinner) findViewById(R.id.spinner);
        String lang = spinner.getSelectedItem().toString().toUpperCase();

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (intent.resolveActivity(getPackageManager()) !=null){

            startActivityForResult(intent, 10);
        }
        else{
            Toast.makeText(this,"Speech to text is not supported", Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 10:{
                if(resultCode == RESULT_OK && data !=null){
                    ArrayList<String> result =data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    TextView textView = (TextView) findViewById(R.id.input);
                    textView.setText(result.get(0));



                }
            }
        }
    }

    public void translateText(View view) {
        final TextView textView = (TextView) findViewById(R.id.textView4);
        Spinner spinnerFrom = (Spinner) findViewById(R.id.spinner);
        Spinner spinnerTo = (Spinner) findViewById(R.id.spinner2);
        TextView ourText = (TextView) findViewById(R.id.input);

        // comma = %2c     space = %20   ///    ! = %21     //   ? = %3F   /// . = 	%2E
// ...

        String translateFrom;
        String translateTo;


        if (ourText!=null){

            traslateText =  ourText.getText().toString().replaceAll("\\s+", "%20");
            traslateText = traslateText.replaceAll("\\,", "%2c");
            traslateText = traslateText.replaceAll("\\!" , "%21");
            traslateText =  traslateText.replaceAll("\\.","%2E");
            traslateText = traslateText.replaceAll("\\?", "%3F");
        }
        translateFrom = spinnerFrom.getSelectedItem().toString().substring(spinnerFrom.getSelectedItem().toString().length() -2);
        translateTo = spinnerTo.getSelectedItem().toString().substring(spinnerTo.getSelectedItem().toString().length() -2);


        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20200427T192712Z.e87c72affbaf3316.f1c99ef3dd944b77af9e2125ae12a8d91a31d1d7&text="+ traslateText +"&lang="+ translateFrom +"-" + translateTo;
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
                textView.setText("failed to get data");
            }
        });

        mQueue.add(request);
    }
}
