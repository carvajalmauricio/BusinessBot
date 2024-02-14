package com.innovaweb.businessbot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    Button btnGoogle;
    FirebaseAuth auth;
    FirebaseDatabase database;
    private FirebaseUser user;
    GoogleSignInClient mGoogleSignInclient;
    ProgressDialog progressDialog;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGoogle = findViewById(R.id.btnGoogle);
        prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Creando la cuenta");
        progressDialog.setMessage("Espere unos segundos");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail().build();

        mGoogleSignInclient = GoogleSignIn.getClient(this, gso);
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {sigIn();}
        });
    }
    int RC_SIGN_IN = 40;
    private void sigIn() {
        Intent intent = mGoogleSignInclient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());}
            catch (ApiException e) {throw new RuntimeException(e);}
        }
    }

    private void firebaseAuth(String idToken) {
        SharedPreferences.Editor editor = prefs.edit();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = auth.getCurrentUser();
                        checkUserExists(user.getUid());

                        editor.putString("userId", user.getUid());
                        editor.putString("userName", user.getDisplayName());
                        editor.putString("userEmail", user.getEmail());
                        editor.apply();

                    } else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserExists(String userId) {
        DatabaseReference userRef = database.getReference().child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Intent intent = new Intent(MainActivity.this, principalMenu.class);
                    startActivity(intent);
                    finish();
                } else {
                    Users users = new Users();
                    users.setUserId(user.getUid());
                    users.setName(user.getDisplayName());
                    users.setProfile(user.getPhotoUrl().toString());

                    Toast.makeText(MainActivity.this, "Bienvenido: "+ user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, businessRegister.class);
                    intent.putExtra("userId", user.getUid());
                    intent.putExtra("userName", user.getDisplayName());
                    intent.putExtra("userPhoneNamber", user.getPhoneNumber());
                    intent.putExtra("userEmail", user.getEmail());

                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}