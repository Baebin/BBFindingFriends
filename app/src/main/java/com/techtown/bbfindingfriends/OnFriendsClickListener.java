package com.techtown.bbfindingfriends;

import android.view.View;

public interface OnFriendsClickListener {
    void onItemClickListener(FriendsAdapter.ViewHolder holder, View view, int position);
    void onItemClickListener(FriendsListAdapter.ViewHolder holder, View view, int position);
    void onItemLongClickListener(FriendsAdapter.ViewHolder holder, View view, int position);
    void onItemLongClickListener(FriendsListAdapter.ViewHolder holder, View view, int position);
}
