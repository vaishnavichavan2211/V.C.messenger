package com.example.vcmessenger;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    Button button;
    TextView logsignup;
    EditText email, password;
    FirebaseAuth auth;
    String emailPatttern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    android.app.ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logbutton);
        email = findViewById(R.id.editTexLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        logsignup = findViewById(R.id.logsignup);

        logsignup.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, registration.class);
            startActivity(intent);
            finish();
        });

        button.setOnClickListener(v -> {
            String Email = email.getText().toString();
            String pass = password.getText().toString();

            if ((TextUtils.isEmpty(Email))) {
                progressDialog.dismiss();
                Toast.makeText(login.this, "Enter The Email", Toast.LENGTH_SHORT).show();
            } else if ((TextUtils.isEmpty(pass))) {
                progressDialog.dismiss();
                Toast.makeText(login.this, "Enter The Password", Toast.LENGTH_SHORT).show();
            } else if (!Email.matches(emailPatttern)) {
                progressDialog.dismiss();
                email.setError("Give proper Email Address");
            } else if (password.length() < 6) {
                progressDialog.dismiss();
                password.setError("More Tha Six Characters");
                Toast.makeText(login.this, "password need to be longer then six characters", Toast.LENGTH_SHORT).show();
            } else {

                auth.signInWithEmailAndPassword(Email, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.show();
                        try {
                            Intent intent = new Intent(login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}