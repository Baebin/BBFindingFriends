package com.techtown.bbfindingfriends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class FriendSearchFragment extends Fragment {
    private static final String TAG = "FriendSearchFragment";

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
        View rootView = inflater.inflate(R.layout.fragment_friend_search, container, false);

        EditText search = rootView.findViewById(R.id.etv_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                getFilter(search.getText().toString());
            }
        });

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
                builder.setMessage("친구 추가 하시겠습니까?");
                builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "AlertDialog.setPositiveButton(아니오)");
                    }
                });
                builder.setNeutralButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "AlertDialog.setNeutralButton(예)");

                        addFriend(friendsListAdapter.getFriend(position));
                    }
                });
                builder.show();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
    }

    public void addFriend(Friend friend) {
        String uid = friend.getUid();
        Log.d(TAG, "addFriend(" + uid + ")");

        if (user.getUid().equals(uid)) {
            friendActivity.showSnackbar("자기 자신은 친구로 추가할 수 없습니다.");
            return;
        }

        String value = getString(R.string.db_value_null);
        myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(getString(R.string.db_child_friends)).child(uid).exists()) {
                    friendActivity.showSnackbar("이미 친구 상태입니다.");
                } else {
                    myRef.child(user.getUid()).child(getString(R.string.db_child_friends)).child(uid).setValue(value);
                    friendActivity.showSnackbar("성공적으로 친구 추가가 되었습니다.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "addFriend() - The read failed: " + error.getCode());
            }
        });
    }

    public void getFilter(String search) {
        Log.d(TAG, "getFilter(" + search + ")");

        // 입력어 공백 방지
        if (search.equals("") || search == null) {
            friendView.setVisibility(View.INVISIBLE);
            return;
        } else {
            friendView.setVisibility(View.VISIBLE);
        }

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "getFilter() - onDataChange()");

                ArrayList<Friend> friendsList = new ArrayList<Friend>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Uid
                    String uid = dataSnapshot.getKey();
                    Log.d(TAG, "getFilter() - Checking " + uid);

                    String nickname = null;

                    if (dataSnapshot.hasChild("Nickname")) {
                        nickname = dataSnapshot.child(getString(R.string.db_child_nick)).getValue().toString();

                        if (!nickname.toLowerCase().contains(search.toLowerCase())) {
                            Log.d(TAG, "getFilter() - Failed " + uid + ": " + nickname);
                            continue;
                        }

                        Friend friend = new Friend(uid);
                        friend.setName(nickname);
                        friend.setUid(uid);

                        friendsList.add(friend);

                        Log.d(TAG, "getFilter() - Added " + uid + ": " + nickname);
                    }
                }

                init(friendsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "getFilter() - The read failed: " + error.getCode());
            }
        });
    }

    public void init(ArrayList<Friend> friends) {
        friendsListAdapter.resetFriends();

        if (!friends.isEmpty()) {
            for (Friend friend : friends) {
                friendsListAdapter.addFriend(friend);
            }
            friendView.setAdapter(friendsListAdapter);
        }
    }
}