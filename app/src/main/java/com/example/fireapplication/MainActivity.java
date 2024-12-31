package com.example.fireapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView name_user,email_user;
    private EditText etId, etName, etEmail, etGender;
    private ListView listView;
    private DatabaseReference database;
    private List<Student> studentList;
    private StudentAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private Button btnLogout,btnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etGender = findViewById(R.id.etGender);
        listView = findViewById(R.id.listView);
        name_user=findViewById(R.id.name_user);
        email_user=findViewById(R.id.email_user);
        btnChat=findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra("RECIPIENT_ID", "gMYiKsBnCrS7q5St8WPOCm0aknp1"); // ID người nhận
            startActivity(intent);
        });
        String name=getIntent().getStringExtra("name");
        String email=getIntent().getStringExtra("email");
        name_user.setText(name);
        email_user.setText(email);
        database = FirebaseDatabase.getInstance().getReference("students");
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(this, studentList);

        listView.setAdapter(adapter);

        fetchStudents();


        firebaseAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.btnLogout);  // Nút đăng xuất

        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Nếu chưa đăng nhập, chuyển về màn hình đăng nhập
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Đăng xuất khi nhấn nút "Đăng xuất"
        btnLogout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            Toast.makeText(MainActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            if (AccessToken.getCurrentAccessToken() != null) {
                // Đăng xuất khỏi Facebook
                LoginManager.getInstance().logOut();
            }
            // Sau khi đăng xuất, chuyển về màn hình đăng nhập
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void onAddClicked(View view) {
        String id = etId.getText().toString();
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String gender = etGender.getText().toString();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Student student = new Student(id, name, email, gender);
        database.child(id).setValue(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add student: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void onUpdateClicked(View view) {
        String id = etId.getText().toString();
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String gender = etGender.getText().toString();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Student student = new Student(id, name, email, gender);
        database.child(id).setValue(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student updated successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update student: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void onDeleteClicked(View view) {
        String id = etId.getText().toString();

        if (id.isEmpty()) {
            Toast.makeText(this, "ID is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        database.child(id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student deleted successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete student: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchStudents() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Student student = data.getValue(Student.class);
                    studentList.add(student);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to fetch students: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        etId.setText("");
        etName.setText("");
        etEmail.setText("");
        etGender.setText("");
    }
}
