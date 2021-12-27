package com.techtown.bbfindingfriends;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    private static final String TAG = "FriendsAdapter";

    ArrayList<Friend> friendArrayList = new ArrayList<Friend>();

    private OnFriendsClickListener friendsClickListener;

    public void setOnItemClickListener(OnFriendsClickListener listener) {
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

    public void checkFriends() {
        Log.d(TAG, "checkFriends()");

        ArrayList<Friend> tempList = new ArrayList<Friend>();

        for (int i = 0; i < friendArrayList.size(); i++) {
            Friend friend = friendArrayList.get(i);
            Log.d(TAG, "checkFriends() - Index: " + i + "(" + friendArrayList.size() + ")");

            if (!friend.getCheckFriend()) {
                tempList.add(friendArrayList.get(i));
            } else {
                Log.d(TAG, "checkFriends() - Saved: " + friend.getUid());
            }
        }

        for (Friend friend : tempList) {
            Log.d(TAG, "checkFriends() - Deleted: " + friend.getUid());
            friendArrayList.remove(friend);
        }
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View friendView = inflater.inflate(R.layout.friend_item, parent, false);

        return new ViewHolder(friendView, friendsClickListener);
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

        View view;

        public ViewHolder(@NonNull View itemView, OnFriendsClickListener listener) {
            super(itemView);

            image = itemView.findViewById(R.id.iv_image);
            nick = itemView.findViewById(R.id.tv_name);

            view = itemView.findViewById(R.id.view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        friendsClickListener.onItemClickListener(FriendsAdapter.ViewHolder.this, view, position);
                    }
                }
            });
        }

        public void setFriend(Friend friend) {
            nick.setText(friend.getName());
        }
    }
}
