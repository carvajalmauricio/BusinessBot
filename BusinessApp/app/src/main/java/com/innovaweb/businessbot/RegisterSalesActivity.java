package com.innovaweb.businessbot;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterSalesActivity extends AppCompatActivity {
    private ArrayList<ProductSelling> products;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_sales);
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        LinearLayout productList = findViewById(R.id.product_list);
        DatePicker datePicker = findViewById(R.id.date_picker);

        TimePicker timePicker = findViewById(R.id.time_picker);

        Calendar calendarMin = Calendar.getInstance();
        datePicker.setMinDate(calendarMin.getTimeInMillis());
        Calendar calendarMax = Calendar.getInstance();
        calendarMax.add(Calendar.DAY_OF_YEAR, 14);
        datePicker.setMaxDate(calendarMax.getTimeInMillis());

        Button generateButton = findViewById(R.id.generate_button);
        loadData();

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //agregamos nuestra verificación aquí
                if (isAnyProductSelected(productList)) {
                    // Sólo generar y enviar el Json si al menos un producto ha sido seleccionado
                    String json = generateJson(productList, datePicker, timePicker);
                    sendJson(json);

                    //Log.d("JSON: ", json);
                } else {
                    Toast.makeText(RegisterSalesActivity.this, "No se ha seleccionado ningún producto.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void loadData() {
        new LoadDataTask().execute();
    }
    private class LoadDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            String result = "";
            try {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("user_id", userId) // Agregar una parte al MultipartBody
                        .build();
                Request request = new Request.Builder()
                        .url("http://192.168.3.90:5000/products/" + userId)
                        .post(requestBody).build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Error: " + response);
                }

                result = response.body().string();


            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Error", "Error en doInBackground: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Type listType = new TypeToken<ArrayList<ProductSelling>>(){}.getType();
            products = new Gson().fromJson(result, listType);

            LinearLayout productList = findViewById(R.id.product_list);

            if (products != null) {
                // Genera los CheckBoxes para los productos
                for (ProductSelling product : products) {
                    LinearLayout productLayout = new LinearLayout(RegisterSalesActivity.this);
                    productLayout.setOrientation(LinearLayout.HORIZONTAL);

                    CheckBox checkBox = new CheckBox(RegisterSalesActivity.this);
                    checkBox.setText(product.getNombre()+ " : "+ product.getDescripcion());
                    productLayout.addView(checkBox); // Añade el CheckBox a productLayout

                    EditText quantityInput = new EditText(RegisterSalesActivity.this);
                    quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                    quantityInput.setHint("Cantidad");

                    quantityInput.setVisibility(View.GONE);

                    TextView quantityValidation = new TextView(RegisterSalesActivity.this);
                    quantityValidation.setText("   en stock: "+product.getCantidad());

                    final int maxQuantity = product.getCantidad(); // Obtén la cantidad máxima

                    final int originalQuantity = product.getCantidad();
                    if (originalQuantity == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterSalesActivity.this);
                        builder.setTitle("Oh Oh! Tienes uno o varios productos agotados")
                                .setMessage("Por favor, dirigete al inventario y grega más productos")
                                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(RegisterSalesActivity.this, InventoryActivity2.class);
                                        startActivity(intent);}
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }



                    quantityInput.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            // You can implement a toast message here before the number changes
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (!s.toString().equals("")) {
                                int inputQuantity = Integer.parseInt(s.toString());
                                int flag = originalQuantity - inputQuantity;
                                quantityValidation.setText("   en stock: " + flag);
                                if (flag == 0 || originalQuantity == 0) quantityValidation.setTextColor(Color.RED);
                                else quantityValidation.setTextColor(Color.BLACK);

                                if (inputQuantity > maxQuantity) {
                                    quantityInput.setText(String.valueOf(maxQuantity));
                                    // Positions the cursor at the end
                                    quantityInput.setSelection(quantityInput.getText().length());
                                }
                                if (inputQuantity < 1  ) {
                                    quantityInput.setText(String.valueOf(1));
                                    quantityInput.setSelection(quantityInput.getText().length());
                                }
                            } else {
                                quantityValidation.setText("   en stock: " + originalQuantity);

                                quantityValidation.setTextColor(Color.BLACK);
                                if (quantityInput.getText().toString().equals("")) {
                                    quantityInput.setSelection(quantityInput.getText().length());
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    productLayout.addView(quantityInput)
                    ; // Añade el EditText a productLayout
                    productLayout.addView(quantityValidation);
                    productList.addView(productLayout); // Añade productLayout a productList
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                if (product.getCantidad() == 0) {
                                    checkBox.setChecked(false); // Uncheck the checkbox
                                    Toast.makeText(RegisterSalesActivity.this, "Este producto está agotado", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Make the EditText visible
                                    quantityInput.setVisibility(View.VISIBLE);
                                }
                            } else {
                                // Hide the EditText
                                quantityInput.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            } else {
                new AlertDialog.Builder(RegisterSalesActivity.this)
                        .setTitle("Aviso")
                        .setMessage("Aún no has agregado productos")
                        .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(RegisterSalesActivity.this, InventoryActivity2.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
    // Generacion el JSON basado en las vistas
    private String generateJson(LinearLayout productList, DatePicker datePicker, TimePicker timePicker) {
        //si no se selecciona ningun producto, regresar null
        if (!isAnyProductSelected(productList)) return null;
        JsonObject jsonObject = new JsonObject();
        JsonObject metaDataObject = new JsonObject();
        String id = "";
        double totalPrice = 0.0;
        for (int i = 0; i < productList.getChildCount(); i++) {

            View child = productList.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout productLayout = (LinearLayout) child;
                CheckBox checkBox = (CheckBox) productLayout.getChildAt(0);
                if (checkBox.isChecked()) {
                    datePicker.setVisibility(View.VISIBLE);
                    id += products.get(i).getId()+"-";}}
        }
        id += datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth() + " " +
                timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute();

        JsonObject productsObject = new JsonObject();

        for (int i = 0; i < productList.getChildCount(); i++) {
            LinearLayout productLayout = (LinearLayout) productList.getChildAt(i);
            CheckBox checkBox = (CheckBox) productLayout.getChildAt(0);
            if (checkBox.isChecked()) {

                EditText quantityInput = (EditText) productLayout.getChildAt(1);

                // Validación: Si quantityInput está vacío, mostrar un mensaje y no generar el JSON
                if (quantityInput.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterSalesActivity.this, "Por favor, ingrese una cantidad.", Toast.LENGTH_SHORT).show();
                    return null;
                }
                JsonObject productObject = new JsonObject();

                productObject.addProperty("id", products.get(i).getId());
                productObject.addProperty("nombre", products.get(i).getNombre());
                productObject.addProperty("precio unitario", products.get(i).getPrecio());
                productObject.addProperty("cantidad", Integer.parseInt(quantityInput.getText().toString()));

                double precioTotal = products.get(i).getPrecio()*Integer.parseInt(quantityInput.getText().toString());
                productObject.addProperty("precio total", precioTotal);
                totalPrice += precioTotal;
                productsObject.add(products.get(i).getId(), productObject);
            }}

        String date = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
        String time =  timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute();
        metaDataObject.addProperty("fecha", date);
        metaDataObject.addProperty("hora", time);
        metaDataObject.addProperty("precio total", totalPrice);
        productsObject.add("metadata", metaDataObject);
        jsonObject.add(id, productsObject);
        return jsonObject.toString();
    }

    private void sendJson(String json) {
        if (json == null) return;
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("json", json)
                .addFormDataPart("user_id", userId)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.3.90:5000/sales/" + userId)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    throw new IOException("Error: " + response);
                } else {
                    Log.d("Response: ", response.body().string());
                }
            }
        });
        recreate();
    }
    private boolean isAnyProductSelected(LinearLayout productList) {
        for (int i = 0; i < productList.getChildCount(); i++) {
            View child = productList.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout productLayout = (LinearLayout) child;
                CheckBox checkBox = (CheckBox) productLayout.getChildAt(0);
                if (checkBox.isChecked()) {
                    return true;
                }
            }
        }
        return false;
    }
}