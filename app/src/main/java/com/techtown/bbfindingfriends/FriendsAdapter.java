package com.techtown.bbfindingfriends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    ArrayList<Friend> friendArrayList = new ArrayList<Friend>();

    private FriendsClickListener listener;

    public void setOnItemClickListener(FriendsClickListener listener) {
        this.listener = listener;
    }

    public void addFriend(Friend friend) {
        friendArrayList.add(friend);
    }

    public void setFriends(ArrayList<Friend> friendArrayList) {
        this.friendArrayList = friendArrayList;
    }

    public Friend getFriend(int position) {
        return friendArrayList.get(position);
    }

    public void setFriend(int position, Friend friend) {
        friendArrayList.set(position, friend);
    }

    public void reverseFriends() {
        Collections.reverse(friendArrayList);
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View friendView = inflater.inflate(R.layout.friend_item, parent, false);

        return new ViewHolder(friendView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.ViewHolder holder, int position) {
        Friend friend = friendArrayList.get(position);
        holder.setFriend(friend);
    }

    @Override
    public int getItemCount() {
        return friendArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nick;
        CircleImageView image;

        public ViewHolder(@NonNull View itemView, FriendsClickListener listener) {
            super(itemView);

            image = itemView.findViewById(R.id.iv_image);
            nick = itemView.findViewById(R.id.tv_name);
        }

        public void setFriend(Friend friend) {
            nick.setText(friend.getName());
        }
    }
}
