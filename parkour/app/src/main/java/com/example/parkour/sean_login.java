package com.example.parkour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class sean_login extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    public String permit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        Spinner spinner = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.permit_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        permit = "None";
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        permit = parent.getItemAtPosition(position).toString();
        if(permit != "Select Your Permit"){
            permit = permit.substring(permit.length() - 1);

        }
        //Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void startButtonOnClick(View view) {
        Log.d("permit", permit);
        Intent getPermitIntent = new Intent(this, MapsActivity.class);
        getPermitIntent.putExtra("permit", permit);
        startActivityForResult(getPermitIntent, 1);
//        finish();
    }
}