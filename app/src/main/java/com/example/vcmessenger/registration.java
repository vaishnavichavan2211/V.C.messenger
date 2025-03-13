package com.example.vcmessenger;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {
    TextView loginbut;
    EditText rg_username, rg_email , rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageURI;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing The Account");
        progressDialog.setCancelable(false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        loginbut = findViewById(R.id.loginbut);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_repassword = findViewById(R.id.rgrepassword);
        rg_profileImg = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.signupbutton);


        loginbut.setOnClickListener(v -> {
            Intent intent = new Intent(registration.this,login.class);
            startActivity(intent);
            finish();
        });

        rg_signup.setOnClickListener(v -> {
            String namee = rg_username.getText().toString();
            String emaill = rg_email.getText().toString();
            String Password = rg_password.getText().toString();
            String cPassword = rg_repassword.getText().toString();
            String status = "Hey I'm Using This Application";
            String imageuri = "";

            if (TextUtils.isEmpty(namee) || TextUtils.isEmpty(emaill) ||
                    TextUtils.isEmpty(Password) || TextUtils.isEmpty(cPassword)){
                progressDialog.dismiss();
                Toast.makeText(registration.this, "Please Enter Valid Information", Toast.LENGTH_SHORT).show();
            }else  if (!emaill.matches(emailPattern)){
                progressDialog.dismiss();
                rg_email.setError("Type A Valid Email Here");
            }else if (Password.length()<6){
                progressDialog.dismiss();
                rg_password.setError("Password Must Be 6 Characters Or More");
            }else if (!Password.equals(cPassword)){
                progressDialog.dismiss();
                rg_password.setError("The Password Doesn't Match");
            }else {
                auth.createUserWithEmailAndPassword(emaill,Password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        String id = task.getResult().getUser().getUid();

                        auth = FirebaseAuth.getInstance();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(namee)
                                .build();
                        FirebaseUser user = auth.getCurrentUser();

                        user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                if (imageURI == null) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Users users = new Users(id,namee,emaill,Password,imageuri,status);

                                    db.collection("users").document(id).set(users).addOnSuccessListener(aVoid -> {
                                        Intent intent = new Intent(registration.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                }else{
                                    StorageHelper.uploadImage(getApplicationContext(),StorageHelper.PROFILE,imageURI, id, new StorageHelper.ImageUploadListener() {
                                        @Override
                                        public void onUploadSuccess(String imageUrl) {
                                            runOnUiThread(() -> {
                                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                                Users users = new Users(id,namee,emaill,Password,imageUrl,status);

                                                UserProfileChangeRequest profileUpdates1 = new UserProfileChangeRequest.Builder()
                                                        .setPhotoUri(Uri.parse(imageUrl))
                                                        .build();

                                                user.updateProfile(profileUpdates1).addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {
                                                        db.collection("users").document(id).set(users).addOnSuccessListener(aVoid -> {
                                                            Intent intent = new Intent(registration.this,MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        });
                                                    }
                                                });
                                            });
                                            Log.e("TAG", "onUploadSuccess: " + imageUrl);
                                        }

                                        @Override
                                        public void onUploadError(String message) {
                                            runOnUiThread(() -> {
                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                Users users = new Users(id,namee,emaill,Password,imageuri,status);

                                                db.collection("users").document(id).set(users).addOnSuccessListener(aVoid -> {
                                                    Intent intent = new Intent(registration.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                            });
                                            Log.e("TAG", "onUploadError: " + message);
                                        }
                                    });
                                }

                            }
                        });
                    }else {
                        Toast.makeText(registration.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });


        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),10);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                imageURI = ImageUtils.getPickImageResultUri(data, this);
                if (imageURI!=null){
                    rg_profileImg.setImageURI(imageURI);
                }
            }
        }
    }
}