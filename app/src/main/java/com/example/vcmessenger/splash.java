package com.example.vcmessenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class splash extends AppCompatActivity {

    ImageView logo;
    TextView name,own1,own2;
    Animation topAnim,bottomAnim;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        getSupportActionBar();
        logo=findViewById(R.id.logoimg);
        name=findViewById(R.id.logonameimg);
        own1=findViewById(R.id.ownone);
        own2=findViewById(R.id.owntwo);

        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);//logo page
        bottomAnim=AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        logo.setAnimation(topAnim);
        name.setAnimation(bottomAnim);
        own1.setAnimation(bottomAnim);
        own2.setAnimation(bottomAnim);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            if (auth.getCurrentUser() == null){
                Intent intent= new Intent(getApplicationContext(),login.class);
                startActivity(intent);
            }else{
                Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
            finish();
        },2000);
    }
}