package com.example.vcmessenger;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {
    TextView loginbut;
    EditText rg_username, rg_email , rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageURI;
    String imageuri;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseDatabase database;
    FirebaseStorage storage;
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
        storage = FirebaseStorage.getInstance();
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

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Users users = new Users(id,namee,emaill,Password,imageuri,status);

                        db.collection("users").document(id).set(users).addOnSuccessListener(aVoid -> {
                            progressDialog.show();
                            Intent intent = new Intent(registration.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        });

//                        DatabaseReference reference = database.getReference().child("user").child(id);
//                        StorageReference storageReference = storage.getReference().child("Upload").child(id);//                           storage part

//                        if (imageURI!=null){
//                            storageReference.putFile(imageURI).addOnCompleteListener(task1 -> {
//                                if (task1.isSuccessful()){
//                                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
//                                        imageuri = uri.toString();
//                                        Users users = new Users(id,namee,emaill,Password,imageuri,status);
//                                        reference.setValue(users).addOnCompleteListener(task2 -> {
//                                            if (task2.isSuccessful()){
//                                                progressDialog.show();
//                                                Intent intent = new Intent(registration.this,MainActivity.class);
//                                                startActivity(intent);
//                                                finish();
//                                            }else {
//                                                Toast.makeText(registration.this, "Error in creating the user", Toast.LENGTH_SHORT).show();//                  storage part
//                                            }
//                                        });
//                                    });
//                                }
//                            });
//                        }else {
//                            String status1 = "Hey I'm Using This Application";
//                            imageuri = "https://firebasestorage.googleapis.com/v0/b/av-messenger-dc8f3.appspot.com/o/man.png?alt=media&token=880f431d-9344-45e7-afe4-c2cafe8a5257";
//                            Users users = new Users(id,namee,emaill,Password,imageuri, status1);
//                            reference.setValue(users).addOnCompleteListener(task3 -> {
//                                if (task3.isSuccessful()){
//                                    progressDialog.show();
//                                    Intent intent = new Intent(registration.this,MainActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                }else {
//                                    Toast.makeText(registration.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
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
        if (requestCode==10){
            if (data!=null){
                imageURI = data.getData();
                rg_profileImg.setImageURI(imageURI);
            }
        }
    }
}