package com.example.vcmessenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatWin extends AppCompatActivity {
    String reciverimg, RoomId, reciverName, SenderUID, reciverId;
    CircleImageView profile;
    TextView reciverNName;
    FirebaseAuth firebaseAuth;
    public static String senderImg;
    public static String reciverIImg;
    CardView sendbtn;
    EditText textmsg;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;
    Timestamp lastTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);
        firebaseAuth = FirebaseAuth.getInstance();

        reciverName = getIntent().getStringExtra("nameeee");
        RoomId = getIntent().getStringExtra("RoomId");
        reciverimg = getIntent().getStringExtra("image");
        reciverId = getIntent().getStringExtra("reciverId");
        messagesArrayList = new ArrayList<>();

        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(chatWin.this, messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        reciverNName.setText("" + reciverName);

        lastTimestamp = Timestamp.now();

        SenderUID = firebaseAuth.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("chatRooms")
                .document(RoomId)
                .collection("messages")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(chatWin.this, "No Messages", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        messagesArrayList.clear();

                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        for (int i = documents.size() - 1; i >= 0; i--) {
                            DocumentSnapshot snapshot = documents.get(i);
                            msgModelclass msg = snapshot.toObject(msgModelclass.class);
                            if (i == 0) {
                                lastTimestamp = snapshot.getTimestamp("timeStamp");
                            }
                            if (msg != null) {
                                messagesArrayList.add(msg);
                            }
                        }

                        // Update UI
                        mmessagesAdpter.notifyDataSetChanged();
                        scrollToBottom();

                        listenForNewMessages(db);
                    } else {
                        Log.e("Firestore", "Error fetching messages: ", task.getException());
                    }
                });

        new Thread(() -> {
            if (reciverimg != null) {
                if (!reciverimg.isEmpty()) {
                    File localFile = new File(getApplicationContext().getFilesDir(), reciverId + ".jpg");
                    StorageHelper.downloadFile(reciverimg, localFile, new StorageHelper.TaskListener() {
                        @Override
                        public void onSuccess(Object object) {
                            File file = (File) object;
                            Uri uri = Uri.fromFile(file);
                            new Handler(Looper.getMainLooper()).post(() -> profile.setImageURI(uri));
                            reciverIImg = uri.toString();
                        }

                        @Override
                        public void onError(String message) {
                            Log.d("TAG", "onError: " + message);
                        }
                    });
                }
            }
            Uri profileUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

            if(profileUri != null){
                String myProfilePicture = profileUri.toString();
                if(myProfilePicture != null){
                    if(!myProfilePicture.isEmpty()){
                        File localFile = new File(getApplicationContext().getFilesDir(), SenderUID + ".jpg");
                        StorageHelper.downloadFile(myProfilePicture, localFile, new StorageHelper.TaskListener() {
                            @Override
                            public void onSuccess(Object object) {
                                File file = (File) object;
                                Uri uri = Uri.fromFile(file);
                                new Handler(Looper.getMainLooper()).post(() -> senderImg = uri.toString());
                            }

                            @Override
                            public void onError(String message) {
                                Log.d("TAG", "onError: " + message);
                            }
                        });
                    }
                }
            }
        }).start();

        sendbtn.setOnClickListener(view -> {
            String message = textmsg.getText().toString();
            if (message.isEmpty()) {
                Toast.makeText(chatWin.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                return;
            }
            textmsg.setText("");
            HashMap<String, Object> msg = new HashMap<>();
            msg.put("message", message);
            msg.put("senderUid", SenderUID);
            msg.put("timeStamp", FieldValue.serverTimestamp());
            msg.put("userName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            msgModelclass msgModelclass = new msgModelclass(message, SenderUID, null, "");
            messagesArrayList.add(msgModelclass);
            mmessagesAdpter.notifyDataSetChanged();
            scrollToBottom();

            db.collection("chatRooms").document(RoomId).collection("messages").add(msg).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(chatWin.this, "Message Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(chatWin.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void listenForNewMessages(FirebaseFirestore db) {
        final boolean[] isFirstLoad = {true};
        db.collection("chatRooms")
                .document(RoomId)
                .collection("messages")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(chatWin.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Snapshot listener error: ", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        if (isFirstLoad[0]) {
                            isFirstLoad[0] = false;
                            return;
                        }

                        DocumentSnapshot snapshot = value.getDocuments().get(0);
                        msgModelclass msg = snapshot.toObject(msgModelclass.class);
                        if (msg != null && !Objects.equals(msg.senderUid, SenderUID)) {
                            messagesArrayList.add(msg);
                            mmessagesAdpter.notifyDataSetChanged();
                            scrollToBottom();
                        }
                    }
                });
    }

    private void scrollToBottom() {
        messageAdpter.scrollToPosition(messagesArrayList.size() - 1);
    }
}