package com.example.vcmessenger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
//        Picasso.get().load(chat.profilepic).into(holder.userimg);



        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(mainActivity,chatWin.class);
            intent.putExtra("nameeee",chat.getTitle());
            intent.putExtra("RoomId",chat.getChatRoomId());
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