package com.innovaweb.businessbot.ui.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.innovaweb.businessbot.R;
import com.innovaweb.businessbot.businessRegister;
import com.innovaweb.businessbot.databinding.FragmentProfileBinding;
import com.innovaweb.businessbot.principalMenu;

public class ProfileFragment extends Fragment {


    private String userName, userEmail;
    private TextView textName, textEmail;

    private Context context;



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences prefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        userName = prefs.getString("userName", "");
        userEmail = prefs.getString("userEmail", "");

        textName = root.findViewById(R.id.text_name);
        textEmail = root.findViewById(R.id.text_email);


        textName.setText(userName);
        textEmail.setText(userEmail);
        Log.d("", userName);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}