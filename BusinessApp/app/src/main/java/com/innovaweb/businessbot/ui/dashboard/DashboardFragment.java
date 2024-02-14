package com.innovaweb.businessbot.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.innovaweb.businessbot.R;
import com.innovaweb.businessbot.databinding.FragmentDashboardBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardFragment extends Fragment {
    private Context context;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    private String userId;
    private FragmentDashboardBinding binding;
    private CalendarView calendarView;
    private TextView selectedDate;
    private JSONObject response;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SharedPreferences prefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        calendarView = root.findViewById(R.id.calendarView);
        selectedDate = root.findViewById(R.id.dDate);
        progressBar = root.findViewById(R.id.progressBar);
        calendarView.setEnabled(false);
        fetchProductsSold();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String selectedDateText = year + "-" + month + "-" + dayOfMonth;
                selectedDate.setText(selectedDateText);

                if (response != null) {
                    try {
                        processResponse(response, selectedDateText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchProductsSold() {
        progressBar.setVisibility(View.VISIBLE);
//        String url = "https://4ca4-186-69-143-243.ngrok-free.app/sales/purchased/"+userId;
        String url = " http://172.25.221.236/sales/purchased/"+userId;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    try {
                        String responseBody = response.body().string();
                        Log.d("Server Response", responseBody); // Mostrar la respuesta en la consola
                        DashboardFragment.this.response = new JSONObject(responseBody);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                calendarView.setEnabled(true);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void processResponse(JSONObject response, String selectedDateText) throws JSONException {
        Iterator<String> keys = response.keys();
        StringBuilder productsSoldText = new StringBuilder("Productos vendidos:\n");
        boolean productsSoldOnSelectedDate = false;

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject sale = response.getJSONObject(key);
            JSONObject metadata = sale.getJSONObject("metadata");
            String saleDate = metadata.getString("fecha");

            if (saleDate.equals(selectedDateText)) {
                Iterator<String> productKeys = sale.keys();

                while (productKeys.hasNext()) {
                    String productKey = productKeys.next();
                    double totalPrice = 0;
                    if (!productKey.equals("metadata")) {
                        JSONObject product = sale.getJSONObject(productKey);
                        String name = product.getString("nombre");
                        int quantity = product.getInt("cantidad");
                        totalPrice =+ product.getDouble("precio unitario");

                        productsSoldText.append(" Cantidad: ").append(quantity).append(" Nombre: ").append(name).append(" Precio por Unidad: ").append(totalPrice).append("\n");
                        productsSoldOnSelectedDate = true;
                    }
                }
            }
        }

        if (productsSoldOnSelectedDate) {
            selectedDate.setText(productsSoldText.toString());
        }
        else {
            selectedDate.setText("No se vendieron productos en la fecha seleccionada.");
        }
    }
}
