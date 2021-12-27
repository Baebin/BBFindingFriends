package com.techtown.bbfindingfriends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> {
    ArrayList<Friend> friendArrayList = new ArrayList<Friend>();

    private OnFriendsClickListener friendsClickListener = null;

    public void setOnFriendsClickListener(OnFriendsClickListener listener) {
        this.friendsClickListener = listener;
    }

    public void addFriend(Friend friend) {
        friendArrayList.add(friend);
    }

    public void setFriends(ArrayList<Friend> friendArrayList) {
        this.friendArrayList = friendArrayList;
    }

    public void resetFriends() {
        friendArrayList = new ArrayList<Friend>();
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
    public FriendsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View friendView = inflater.inflate(R.layout.friend_card_item, parent, false);

        return new FriendsListAdapter.ViewHolder(friendView, friendsClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = friendArrayList.get(position);
        holder.setFriend(friend);
    }

    @Override
    public int getItemCount() {
        return friendArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nick, uid;
        CircleImageView image;

        CardView cardView;

        public ViewHolder(@NonNull View itemView, OnFriendsClickListener listener) {
            super(itemView);

            image = itemView.findViewById(R.id.iv_image);
            nick = itemView.findViewById(R.id.tv_name);
            uid = itemView.findViewById(R.id.tv_uid);

            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        friendsClickListener.onItemLongClickListener(ViewHolder.this, view, position);
                        return true;
                    }
                    return false;
                }
            });
        }

        public void setFriend(Friend friend) {
            nick.setText(friend.getName());
            uid.setText(friend.getUid());
        }
    }
}
