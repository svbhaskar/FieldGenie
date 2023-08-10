package com.example.fieldgenie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class FetchDataActivity extends AppCompatActivity {

    String atmTemp, atmHum, soilTemp;

    DatabaseReference db;

    TextView values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_data);

        values = findViewById(R.id.value);


        db = FirebaseDatabase.getInstance().getReference().child("test");
        try{
            db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    atmTemp = Objects.requireNonNull(snapshot.child("AtmTemp").getValue()).toString();
                    atmHum = Objects.requireNonNull(snapshot.child("AtmHum").getValue()).toString();
                    soilTemp = Objects.requireNonNull(snapshot.child("SoilTemp").getValue()).toString();

                    values.setText("Atmospheric Temperature: "+atmTemp+"℃"+"\n\nAtm  ospheric Humidity: "+atmHum+"%"+"\n\nSoil Temperature: "+soilTemp+"℃");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FetchDataActivity.this, "Failed to get Value.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ignored){}
    }
}