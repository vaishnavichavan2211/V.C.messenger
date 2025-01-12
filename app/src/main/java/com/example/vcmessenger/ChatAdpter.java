package com.example.vcmessenger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdpter extends RecyclerView.Adapter<ChatAdpter.viewholder> {
    Context mainActivity;
    ArrayList<ChatRoom> usersArrayList;
    public ChatAdpter(MainActivity mainActivity, ArrayList<ChatRoom> usersArrayList) {
        this.mainActivity=mainActivity;
        this.usersArrayList=usersArrayList;
    }


    @NonNull
    @Override
    public ChatAdpter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.user_item,parent,false);
        return new viewholder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ChatAdpter.viewholder holder, int position) {

        ChatRoom chat = usersArrayList.get(position);
        holder.username.setText(chat.getTitle());
        holder.userstatus.setText("");

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("TAG", "onBindViewHolder: "+chat.members);

        new Thread(()->{
            for(String member:chat.members){
                if(!member.equals(myUid)){
                    Log.d("TAG", "name: "+chat.getTitle());
                    String path = chat.getChatRoomImageUrl();
                    Log.d("TAG", "image path: "+path);
                    if (path != null) {
                        if (!path.isEmpty()) {
                            Log.d("123", "onBindViewHolder: "+path);
                            File localFile = new File(mainActivity.getApplicationContext().getFilesDir(), member+".jpg");
                            StorageHelper.downloadFile(path, localFile, new StorageHelper.TaskListener() {
                                @Override
                                public void onSuccess(Object object) {
                                    File file = (File) object;
                                    Uri uri = Uri.fromFile(file);
                                    new Handler(Looper.getMainLooper()).post(() -> holder.userimg.setImageURI(uri));
                                }

                                @Override
                                public void onError(String message) {
                                    Log.d("TAG", "onError: "+message);
                                }
                            });
                        }
                    }
                }
            }
        }).start();

        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(mainActivity,chatWin.class);
            intent.putExtra("nameeee",chat.getTitle());
            intent.putExtra("RoomId",chat.getChatRoomId());
            intent.putExtra("image",chat.getChatRoomImageUrl());
            for(String member: chat.members){
                if(!member.equals(myUid)){
                    intent.putExtra("reciverId",member);
                }
            }
            mainActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username;
        TextView userstatus;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}