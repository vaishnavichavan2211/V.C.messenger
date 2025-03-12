package com.example.vcmessenger;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AddNewGroupChat extends AppCompatActivity{

    LinearLayout UsersList;
    ArrayList<Users> selectedUsers = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_group_chat);

        UsersList = findViewById(R.id.UsersList);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String my = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").whereNotEqualTo("userId", my).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("1", "onCreate: " + task.getResult().getDocuments().size());
                ArrayList<Users> usersArrayList = new ArrayList<>();
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                for (DocumentSnapshot document : documents) {
                    Users users = document.toObject(Users.class);
                    usersArrayList.add(users);
                }
                runOnUiThread(() -> {
                    Log.d("5", "onCreate: " + usersArrayList.size());
                    UsersList.removeAllViews();
                    for (Users users : usersArrayList) {
                        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.user_item, UsersList, false);

                        Log.d("4", "onCreate: " + users.getUserName());
                        // hold listner
                        view.setOnClickListener(v -> {
                            if (selectedUsers.contains(users.getUserId())) {
                                selectedUsers.remove(users);
                                view.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bordermain, null));
                            } else {
                                selectedUsers.add(users);
                                view.setBackgroundColor(getResources().getColor(R.color.light_grey));
                            }
                        });
                        ((TextView)view.findViewById(R.id.username)).setText(users.getUserName());
                        ((TextView)view.findViewById(R.id.userstatus)).setText(users.getStatus());
                        UsersList.addView(view);
                    }
                });
            }
        });
        FloatingActionButton fab = findViewById(R.id.add);
        fab.setOnClickListener(v -> {
            if (selectedUsers.size() <2) {
                Toast.makeText(this, "Select at least 2 users", Toast.LENGTH_SHORT).show();
                return;
            }
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.group_create_confirmation);
            dialog.findViewById(R.id.cancel).setOnClickListener(v1 -> dialog.dismiss());
            EditText groupName = dialog.findViewById(R.id.groupName);
            Button create = dialog.findViewById(R.id.donebutt);
            create.setOnClickListener(v1 -> {
                if (groupName.getText().toString().isEmpty()) {
                    groupName.setError("Group name is required");
                    return;
                }
                HashMap<String, Object> chat = new HashMap<>();
                String chatRoomId = db.collection("chats").document().getId();
                chat.put("chatRoomId", chatRoomId);
                chat.put("members", getMembers(selectedUsers, my));
                chat.put("title", groupName.getText().toString());

                db.collection("users").document(my).update("chats", FieldValue.arrayUnion(chat)).addOnCompleteListener(t->{
                    if (t.isSuccessful()){
                        TaskCompletionSource<Object> taskCompletionSource = new TaskCompletionSource<>();
                        AtomicInteger count = new AtomicInteger(selectedUsers.size());
                        for (Users user : selectedUsers) {
                            FirebaseFirestore.getInstance().collection("users").document(user.getUserId()).update("chats", FieldValue.arrayUnion(chat)).addOnCompleteListener(task -> {
                                if (count.decrementAndGet() == 0) {
                                    taskCompletionSource.setResult("done");
                                }
                            });
                        }
                        taskCompletionSource.getTask().addOnCompleteListener(task2 -> {
                            db.collection("chatRooms").document(chatRoomId).collection("messages").document().set(new HashMap<String, Object>() {
                                {
                                    put("message", "Welcome to the group chat");
                                    put("senderUid", "system");
                                    put("timeStamp", FieldValue.serverTimestamp());
                                }
                            }).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    finish();
                                }
                            });
                        });
                    }
                });
            });

            dialog.show();

        });
    }

    @NonNull
    private static ArrayList<HashMap<String, Object>> getMembers(ArrayList<Users> users, String my) {
        ArrayList<HashMap<String, Object>> members = new ArrayList<>();
        for (Users user : users) {
            HashMap<String, Object> member = new HashMap<>();
            member.put("id", user.getUserId());
            member.put("userName", user.getUserName());
            member.put("profilepic", user.getProfilepic());
            members.add(member);
        }
        HashMap<String, Object> myMap = new HashMap<>();
        myMap.put("id", my);
        myMap.put("userName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        Uri picUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        if (picUri != null) {
            myMap.put("profilepic", picUri.toString());
        }else{
            myMap.put("profilepic", "");
        }
        members.add(myMap);
        return members;
    }
}
