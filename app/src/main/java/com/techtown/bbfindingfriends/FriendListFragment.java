package com.techtown.bbfindingfriends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendListFragment extends Fragment {
    private static final String TAG = "FriendListFragment";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase fdb;
    private DatabaseReference myRef;

    private FirebaseUser user;

    private FriendActivity friendActivity;

    private FriendsListAdapter friendsListAdapter;
    private RecyclerView friendView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        friendActivity = (FriendActivity) getActivity();
        Log.d(TAG, "onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        friendActivity = null;
        Log.d(TAG, "onDetach()");
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        fdb = FirebaseDatabase.getInstance();
        myRef = fdb.getReference(getString(R.string.db_users));

        user = firebaseAuth.getCurrentUser();

        // Root View
        View rootView = inflater.inflate(R.layout.fragment_friend_list, container, false);

        friendView = rootView.findViewById(R.id.friend_layout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        friendView.setLayoutManager(linearLayoutManager);

        friendsListAdapter = new FriendsListAdapter();
        friendsListAdapter.setOnFriendsClickListener(new OnFriendsClickListener() {
            @Override
            public void onItemClickListener(FriendsAdapter.ViewHolder holder, View view, int position) {
            }

            @Override
            public void onItemClickListener(FriendsListAdapter.ViewHolder holder, View view, int position) {
            }

            @Override
            public void onItemLongClickListener(FriendsAdapter.ViewHolder holder, View view, int position) {
            }

            @Override
            public void onItemLongClickListener(FriendsListAdapter.ViewHolder holder, View view, int position) {
                Log.d(TAG, "friendsListAdapter.onItemLongClickListener() : " + position);

                AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog);
                builder.setTitle(friendsListAdapter.getFriend(position).getName());
                //builder.setMessage("별명: 없음");
                builder.setPositiveButton("친구 삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "AlertDialog.setPositiveButton(친구 삭제)");

                        delFriend(friendsListAdapter.getFriend(position), position);
                    }
                });
                builder.setNegativeButton("별명 초기화", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "AlertDialog.setNegativeButton(별명 초기화)");

                        resetAlias(friendsListAdapter.getFriend(position), position);
                    }
                });
                builder.setNeutralButton("별명 설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "AlertDialog.setNeutralButton(별명 설정)");
                    }
                });
                builder.show();
            }
        });

        setAdapter(0);
        
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
    }

    public void delFriend(Friend friend, int position) {
        String uid = friend.getUid();
        Log.d(TAG, "delFriend(" + uid + ")");

        myRef.child(user.getUid()).child(getString(R.string.db_child_friends)).child(uid).removeValue();
        friendActivity.showSnackbar(friend.getName() + "님께서 친구 목록에서 삭제되었습니다.");

        setAdapter(position);
    }

    public void resetAlias(Friend friend, int position) {
        String uid = friend.getUid();
        Log.d(TAG, "resetAlias(" + uid + ")");

        String value = getString(R.string.db_value_null);
        myRef.child(user.getUid()).child(getString(R.string.db_child_friends)).child(uid).setValue(value);

        friendActivity.showSnackbar(friend.getName() + "님의 별명이 초기화되었습니다.");

        setAdapter(position);
    }

    public void setAdapter(int position) {
        Log.d(TAG, "setAdapter()");
        friendsListAdapter.resetFriends();

        myRef.child(user.getUid()).child(getString(R.string.db_child_friends)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "setAdapter() - onDataChange()");

                ArrayList<Friend> friendsList = new ArrayList<Friend>();

                int count = (int) snapshot.getChildrenCount();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Uid
                    String uid = dataSnapshot.getKey();
                    Log.d(TAG, "setAdapter() - Loading " + uid);

                    count--;

                    String nickname = dataSnapshot.getValue().toString();

                    Friend friend = new Friend(uid);
                    friend.setName(nickname);
                    friend.setUid(uid);

                    int finalCount = count;
                    myRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                            if (nickname.equals(getString(R.string.db_value_null))) {
                                String friendName = friendSnapshot.child(getString(R.string.db_child_nick)).getValue().toString();
                                friend.setName(friendName);

                                Log.d(TAG, "getFilter() - Loading Nickname: " + friendName);
                            }

                            friendsList.add(friend);
                            Log.d(TAG, "getFilter() - Loaded " + uid + ": " + nickname);

                            if (finalCount == 0) {
                                init(friendsList, position);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError friendError) {
                            Log.d(TAG, "getFilter() - Loading Nickname - The read failed: " + friendError.getCode());

                            if (finalCount == 0) {
                                init(friendsList, position);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "getFilter() - The read failed: " + error.getCode());
            }
        });
    }

    public void init(ArrayList<Friend> friends, int position) {
        friendsListAdapter.resetFriends();

        if (friends.isEmpty()) {
            // 갱신 오류 방지
            friendView.setVisibility(View.INVISIBLE);
        } else {
            for (Friend friend : friends) {
                friendsListAdapter.addFriend(friend);
            }

            friendView.setAdapter(friendsListAdapter);

            // 화면 세부 조정
            friendView.setVisibility(View.VISIBLE);
            friendView.scrollToPosition(position);
        }
    }
}