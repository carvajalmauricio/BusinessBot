package com.innovaweb.businessbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class businessRegister extends AppCompatActivity {


    private static final int INVENTORY_ACTIVITY_REQUEST_CODE = 1;
    String userId, userName, userPhoneNamber, selectedValue, userEmail;
    EditText editTextBusinessName;
    DatabaseReference userRef;
    Spinner spinnerOptions;
    ArrayAdapter<String> optionsAdapter;
    SharedPreferences prefs;
    String[] options = {"Servicio", "Producto"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_register);

        Button btnaccept = findViewById(R.id.btnaccept);
        editTextBusinessName = findViewById(R.id.editTextBusinessName);
        spinnerOptions = findViewById(R.id.spinnerOptions);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userPhoneNamber = intent.getStringExtra("userPhoneNamber");
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);



        optionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        optionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(optionsAdapter);
        spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedValue = options[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnaccept.setOnClickListener(view -> selectedRol());
    }
    private void selectedRol() {
        Intent intent;
        final String businessName = editTextBusinessName.getText().toString();
        String offer = selectedValue;

        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("businessName", businessName);
        updateValues.put("offer", offer);
        updateValues.put("userName", userName);
        updateValues.put("userID", userId);
        updateValues.put("userPhoneNamber", userPhoneNamber);
        updateValues.put("userEmail", userEmail);

        userRef.updateChildren(updateValues);

        intent = new Intent(businessRegister.this, principalMenu.class);
        startActivityForResult(intent, INVENTORY_ACTIVITY_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INVENTORY_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String someData = data.getStringExtra("some_key");
            }
        }
    }
}
