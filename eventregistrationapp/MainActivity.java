package com.java.eventregistrationapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_USER_VERIFIED = "user_verified";
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        Button button = findViewById(R.id.button3);
        Button button2 = findViewById(R.id.button2);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        button2.setOnClickListener(v -> {
            scanCode();
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            checkDataInDatabase(result.getContents());
            builder.show();
        }
    });
    private void checkDataInDatabase(String scannedData) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.orderByChild("email").equalTo(scannedData).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DatabaseCheck", "DataSnapshot exists: " + snapshot.exists());
                if (snapshot.exists()) {
                    // User with the provided email exists
                    Log.d("DatabaseCheck", "User Verified");
                    Toast.makeText(MainActivity.this, "User Verified", Toast.LENGTH_SHORT).show();
                    // Optionally, you can perform additional actions here
                } else {
                    // User with the provided email does not exist
                    Log.d("DatabaseCheck", "User Not Verified");
                    Toast.makeText(MainActivity.this, "User Not Registered", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseCheck", "Database Error: " + databaseError.getMessage());
            }
        });
    }



}