package com.innovaweb.businessbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConfigchatActivity extends AppCompatActivity {
    private String userId, selectedItem1, selectedItem2, selectedItemTon, selectedItemSchedule;
    EditText localidad, ciudad, telf, inf;
    Spinner dropdownTons,dropdownSchedule,dropdownScheduleStart,dropdownScheduleFinish;
    Button uploadButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        setContentView(R.layout.activity_configchat);

        dropdownTons = findViewById(R.id.spinnerTons);
        dropdownSchedule = findViewById(R.id.spinnerSchedule);
        dropdownScheduleStart = findViewById(R.id.spinnerScheduleStart);
        dropdownScheduleFinish = findViewById(R.id.spinnerScheduleFinish);
        uploadButton = findViewById(R.id.uploadChat);
        localidad = findViewById(R.id.editLocalidad);
        ciudad = findViewById(R.id.editTextTextCiudad);
        telf = findViewById(R.id.editTextTelefonos);
        inf = findViewById(R.id.editTextTextAdditional);

        String[] itemsTons = new String[]{"Amable", "Formal", "Informativo", "Persuasivo", "Empático", "Seguro"};
        String[] itemsSchedule = new String[]{"De Lunes a Viernes", "Solo fines de semana", "De Lunes a Domingo"};
        String[] itemshourShedule = new String[24];
        for (int i = 0; i < 24; i++) {
            itemshourShedule[i] = String.valueOf(i) + ":00"; // Convert the integer to string and add to the array
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsTons);
        dropdownTons.setAdapter(adapter);
        dropdownTons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItemTon = parent.getItemAtPosition(position).toString();}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {parent.getItemAtPosition(1);}
        });
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsSchedule);
        dropdownSchedule.setAdapter(adapter1);
        dropdownSchedule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item
                selectedItemSchedule = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {parent.getItemAtPosition(1);}
        });

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemshourShedule);
        dropdownScheduleStart.setAdapter(adapter2);
        dropdownScheduleStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item
                selectedItem1 = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.getItemAtPosition(1);
            }
        });

        dropdownScheduleFinish.setAdapter(adapter2);
        dropdownScheduleFinish.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item
                selectedItem2 = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {parent.getItemAtPosition(1);}
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if fields are empty
                if (localidad.getText().toString().isEmpty() ||
                        ciudad.getText().toString().isEmpty() ||
                        telf.getText().toString().isEmpty() ||
                        inf.getText().toString().isEmpty()) {

                    Toast.makeText(ConfigchatActivity.this, "Todos los campos deben estar llenos.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Create JSON
                    JSONObject json = new JSONObject();
                    try {
                        json.put("horarioApertura", selectedItem1);
                        json.put("horarioCierre", selectedItem2);
                        json.put("tonoDerespuesta", selectedItemTon);
                        json.put("horario", selectedItemSchedule);
                        json.put("direccion", localidad.getText());
                        json.put("ciudad", ciudad.getText());
                        json.put("contacto", telf.getText());
                        json.put("informacion", inf.getText());
                        // Send JSON
                        sendJson(json, "http://192.168.3.90:5000/information/" + userId);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void sendJson(final JSONObject json, String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {e.printStackTrace();}

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        } else {int responseCode = response.code();
                            if (responseCode == 200) {
                                // La solicitud fue exitosa
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ConfigchatActivity.this, "Datos actualizados exitosamente.", Toast.LENGTH_SHORT).show();}
                                });
                            } else {runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {Toast.makeText(ConfigchatActivity.this, "Error al actualizar los datos.", Toast.LENGTH_SHORT).show();
                                    }});
                            }
                        }
                    }
                });
            }
        });
    }
}