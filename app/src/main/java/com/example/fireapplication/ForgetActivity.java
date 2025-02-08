package com.example.fireapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgetActivity extends AppCompatActivity {
    private EditText email;
    private Button bt_send;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        email=findViewById(R.id.email);
        bt_send=findViewById(R.id.bt_send);
        firebaseAuth = FirebaseAuth.getInstance();
        bt_send.setOnClickListener(v -> {
            firebaseAuth.sendPasswordResetEmail(email.getText().toString())
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Emai đã được gửi",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(getApplicationContext(),"Emai gửi thất bại",Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}