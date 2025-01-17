package com.example.vcmessenger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    RecyclerView mainUserRecyclerView;
    ChatAdpter adapter;
    ArrayList<ChatRoom> ChatArrayList;
    ImageView imglogout;
    FloatingActionButton newChat;

    ImageView cumbut, setbut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        cumbut = findViewById(R.id.camBut);
        setbut = findViewById(R.id.settingBut);
        newChat = findViewById(R.id.addNewChat);

        ChatArrayList = new ArrayList<>();

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdpter(MainActivity.this, ChatArrayList);
        mainUserRecyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").whereNotEqualTo("userId", uid).addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            for (DocumentSnapshot document : value.getDocuments()) {
                Object chats = document.get("chats");
                if (chats == null) {
                    return;
                }
                ChatArrayList.clear();
                ArrayList<HashMap<String, Object>> chatList = (ArrayList<HashMap<String, Object>>) chats;
                for (HashMap<String, Object> chat : chatList) {
                    ArrayList<HashMap<String,Object>> members = (ArrayList<HashMap<String,Object>>) chat.get("members");
                    for (HashMap<String,Object> member : members) {
                        if (member.get("id").equals(uid)) {
                            ChatRoom chatRoom = new ChatRoom();
                            chatRoom.setChatRoomId((String) chat.get("chatRoomId"));
                            chatRoom.members = new ArrayList<>();
                            for (HashMap<String,Object> user : (ArrayList<HashMap<String,Object>>) chat.get("members")) {
                                if (!user.get("id").equals(uid)) {
                                    chatRoom.setTitle((String) user.get("userName"));
                                }
                                chatRoom.members.add((String) user.get("id"));
                            }
                            ChatArrayList.add(chatRoom);
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });


        imglogout = findViewById(R.id.logoutimg);
        imglogout.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this, R.style.dialoge);
            dialog.setContentView(R.layout.dialog_layout);
            Button no, yes;
            yes = dialog.findViewById(R.id.yesbnt);
            no = dialog.findViewById(R.id.nobnt);
            yes.setOnClickListener(v2 -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            });
            no.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });

        setbut.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, setting.class);
            startActivity(intent);
        });

        cumbut.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 10);
        });


        newChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, addNewChat.class);
            startActivity(intent);
        });
    }
}