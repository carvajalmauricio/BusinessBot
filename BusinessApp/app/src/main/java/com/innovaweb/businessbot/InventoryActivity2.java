package com.innovaweb.businessbot;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InventoryActivity2 extends AppCompatActivity {
    private String userId, url;
    private Uri uri = null;
    private static int REQUEST_CODE_PICK_FILE = 1;
    private TextView textView, text_inf;
    ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        productList = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory2);
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_product);
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
                text_inf.setVisibility(View.GONE);
            }
        });
        textView = findViewById(R.id.textView);
        text_inf = findViewById(R.id.text_inf);
        text_inf.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progress);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFile();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToFileAndLog();
            }
        });
        loadData();}

    private void addProduct(){
        productList.add(new Product("name", "description", 2, 4));
        productAdapter = new ProductAdapter(InventoryActivity2.this, productList);
        recyclerView.setAdapter(productAdapter);

    }
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }
    private void sendFile() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            InventoryActivity2.UploadTask uploadTask = new UploadTask();
            uploadTask.execute(inputStream);
        } catch (FileNotFoundException e) {e.printStackTrace();
            Toast.makeText(InventoryActivity2.this, "File not found", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            uri = data.getData();
            textView.setText(uri.getLastPathSegment());
            //Log.d("File: ", ""+uri.getLastPathSegment());
            sendFile();
        }}

    private class UploadTask extends AsyncTask<InputStream, Void, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(InputStream... inputStreams) {
            InputStream inputStream = inputStreams[0];
            OkHttpClient client = new OkHttpClient();
            String result = "";

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            try {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("excel", "filename.xlsx", RequestBody.create(MediaType.parse("*/*"), byteArrayOutputStream.toByteArray()))
                        .addFormDataPart("user_id", userId).build();
                Request request = new Request.Builder()
                        .url("http://192.168.3.90:5000/upload")
                        .post(requestBody).build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {throw new IOException("Error: " + response);}
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }return result;}
        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            if (result != null) {
                displayProducts(result);
            } else {
                text_inf.setVisibility(View.VISIBLE);
            }}}

    private void displayProducts(String jsonData) {
        try {
            Object json = new JSONTokener(jsonData).nextValue();
            if (json instanceof JSONObject) {
                JSONObject jsonObject = new JSONObject(jsonData);
                if(jsonObject.has("Products")){
                    JSONObject productsObject = jsonObject.getJSONObject("Products");
                    Iterator<String> keys = productsObject.keys();

                    while(keys.hasNext()) {
                        String key = keys.next();
                        if (productsObject.get(key) instanceof JSONObject) {
                            JSONObject productObject = productsObject.getJSONObject(key);
                            processJSONObject(productObject);
                        }
                    }
                }
            } else if (json instanceof JSONArray) {
                JSONArray jsonArray = new JSONArray(jsonData);
                for(int i=0; i < jsonArray.length(); i++) {
                    JSONObject productObject = jsonArray.getJSONObject(i);
                    processJSONObject(productObject);
                }
            }
            productAdapter = new ProductAdapter(InventoryActivity2.this, productList);
            recyclerView.setAdapter(productAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processJSONObject(JSONObject productObject) {
        try {
            String name = productObject.getString("nombre");
            String description = productObject.getString("descripcion");
            double price = productObject.getDouble("precio");
            int quantity = productObject.getInt("cantidad");

            productList.add(new Product(name, description, price, quantity));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void saveToFileAndLog(){
        JSONArray jsonArray = new JSONArray();
        for (Product product : productList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("nombre", product.getNombre());
                jsonObject.put("descripcion", product.getDescripcion());
                jsonObject.put("precio", product.getPrecio());
                jsonObject.put("cantidad", product.getCantidad());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {e.printStackTrace();}
        }

        String jsonString = jsonArray.toString();
        new SaveDataTask().execute(jsonString);
    }
    private class SaveDataTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String jsonString = strings[0];
            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("json", jsonString).addFormDataPart("user_id", userId)
                    .build();
            Request request = new Request.Builder().url("http://192.168.3.90:5000/save").post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Error: " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }return null;}
    }
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        if (sharedPreferences.getString("jsonData", null) == null) {
            new LoadDataTask().execute();
        } else {
            String result = sharedPreferences.getString("jsonData", null);
            displayProducts(result);
        }
    }
    private class LoadDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            String result = "";

            try {
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("user_id", userId)
                        .build();
                Request request = new Request.Builder()
                        .url("http://192.168.3.90:5000/products/" + userId)
                        .post(requestBody).build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {throw new IOException("Error: " + response);}
                result = response.body().string();
                //Log.d("lll: ", ""+result);

            } catch (IOException e) {e.printStackTrace();}
            return result;}
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                displayProducts(result);
            } else {
                Toast.makeText(InventoryActivity2.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }}}}
