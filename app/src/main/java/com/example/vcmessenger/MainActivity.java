package com.example.vcmessenger;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

    @SuppressLint({"MissingInflatedId"})
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
        String myUid = auth.getCurrentUser().getUid();

        db.collection("users").whereEqualTo("userId",myUid).addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            ChatArrayList.clear();
            for (DocumentSnapshot document : value.getDocuments()) {
                Object chats = document.get("chats");
                if (chats == null) {
                    continue;
                }
                ArrayList<HashMap<String, Object>> chatList = (ArrayList<HashMap<String, Object>>) chats;
                for (HashMap<String, Object> chat : chatList) {
                    ArrayList<HashMap<String,Object>> members = (ArrayList<HashMap<String,Object>>) chat.get("members");
                    for (HashMap<String,Object> member : members) {
                        if (member.get("id").equals(myUid)) {
                            ChatRoom chatRoom = new ChatRoom();
                            chatRoom.setChatRoomId((String) chat.get("chatRoomId"));
                            chatRoom.members = new ArrayList<>();
                            boolean isGroup = false;
                            if (members.size() > 2){
                                isGroup = true;
                                chatRoom.setTitle(chat.get("title").toString());
                                chatRoom.setChatRoomImageUrl("");
                            }
                            for (HashMap<String,Object> user : (ArrayList<HashMap<String,Object>>) chat.get("members")) {
                                if (!user.get("id").equals(myUid) && !isGroup){
                                    chatRoom.setTitle((String) user.get("userName"));
                                    chatRoom.setChatRoomImageUrl((String) user.get("profilepic"));
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
            Dialog dialog = new Dialog(MainActivity.this, R.style.CustomDialog);
            dialog.setContentView(R.layout.group_or_single);
            Button group, single, cancel;
            group = dialog.findViewById(R.id.group);
            single = dialog.findViewById(R.id.single);
            cancel = dialog.findViewById(R.id.cancel);
            group.setOnClickListener(v1 -> {
                Intent intent = new Intent(MainActivity.this, AddNewGroupChat.class);
                startActivity(intent);
                dialog.dismiss();
            });
            single.setOnClickListener(v1 -> {
                Intent intent = new Intent(MainActivity.this, addNewChat.class);
                startActivity(intent);
                dialog.dismiss();
            });
            cancel.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });
    }
}