package com.example.vcmessenger;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class addNewChat extends AppCompatActivity {

    LinearLayout UsersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_chat);

        UsersList = findViewById(R.id.UsersList);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String my = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").whereNotEqualTo("userId", my).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("1", "onCreate: " + task.getResult().getDocuments().size());
                ArrayList<Users> usersArrayList = new ArrayList<>();
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                for (DocumentSnapshot document : documents) {
                    Object chats = document.get("chats");
                    if (chats == null) {
                        Users users = document.toObject(Users.class);
                        usersArrayList.add(users);
                        Log.d("2", "onCreate: " + users.getUserName());
                        continue;
                    }
                    Log.d("3", "onCreate: " + chats);
                    ArrayList<HashMap<String, Object>> chatList = (ArrayList<HashMap<String, Object>>) chats;
                    boolean flag = false;
                    for (HashMap<String, Object> chat : chatList) {
                        if (chat.get("members") instanceof ArrayList) {
                            ArrayList<HashMap<String, Object>> members = (ArrayList<HashMap<String, Object>>) chat.get("members");
                            if (members.size() >2){
                                continue;
                            }
                            for (HashMap<String, Object> member : members) {
                                if (member.get("id").equals(my)) {
                                    flag = true;
                                }
                            }
                        }
                    }

                    if (!flag){
                        Users users = document.toObject(Users.class);
                        usersArrayList.add(users);
                    }
                }

                Log.d("5", "onCreate: " + usersArrayList.size());
                runOnUiThread(() -> {
                    Log.d("5", "onCreate: " + usersArrayList.size());
                    UsersList.removeAllViews();
                    for (Users users : usersArrayList) {
                        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.user_item, UsersList, false);
                        Log.d("4", "onCreate: " + users.getUserName());
                        ((TextView)view.findViewById(R.id.username)).setText(users.getUserName());
                        ((TextView)view.findViewById(R.id.userstatus)).setText(users.getStatus());

                        view.setOnClickListener(v -> {
                            HashMap<String, Object> chat = new HashMap<>();
                            String chatId = db.collection("chats").document().getId();
                            chat.put("chatRoomId", chatId);
                            ArrayList<HashMap<String, Object>> members = getMembers(users, my);
                            chat.put("members", members);

                            db.collection("users").document(my).update("chats", FieldValue.arrayUnion(chat)).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    db.collection("users").document(users.getUserId()).update("chats", FieldValue.arrayUnion(chat)).addOnCompleteListener(task3 -> {
                                        db.collection("chatRooms").document(chatId).collection("messages").document().set(new HashMap<String, Object>() {
                                            {
                                                put("message", "Hello");
                                                put("senderUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                put("timeStamp", FieldValue.serverTimestamp());
                                            }
                                        }).addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                finish();
                                            }
                                        });
                                    });
                                }
                            });
                        });
                        UsersList.addView(view);
                    }
                });
            }
        });
    }

    @NonNull
    private static ArrayList<HashMap<String, Object>> getMembers(Users users, String my) {
        HashMap<String, Object> member1 = new HashMap<>();
        member1.put("id", users.getUserId());
        member1.put("userName", users.getUserName());
        member1.put("profilepic", users.getProfilepic());
        HashMap<String, Object> member2 = new HashMap<>();
        member2.put("id", my);
        member2.put("userName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        Uri profileUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        if (profileUri != null) {
            member2.put("profilepic", profileUri.toString());
        }else {
            member2.put("profilepic", "");
        }
        ArrayList<HashMap<String, Object>> members = new ArrayList<>();
        members.add(member1);
        members.add(member2);
        return members;
    }
}