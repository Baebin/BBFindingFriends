package com.techtown.bbfindingfriends;

import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Friend {
    private static final String TAG = "Friend";

    private FirebaseDatabase fdb;
    private DatabaseReference myRef;

    private String uid;
    private String name;

    private boolean friend_status = false;

    private String db_users = "Users";
    private String db_child_nick = "Nickname";
    private String db_child_friends = "Friends";

    public Friend(String uid) {
        this.uid = uid;

        fdb = FirebaseDatabase.getInstance();
        myRef = fdb.getReference();

        //this.refreshName();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void checkFriend(FirebaseUser currentUser) {
        Log.d(TAG, "checkFriend(): " + uid);
        if (uid == null) {
            return;
        }

        myRef.child(db_users).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(db_child_friends).child(currentUser.getUid()).exists()) {
                    friend_status = true;

                    Log.d(TAG, "checkFriend() - Complete");
                } else {
                    Log.d(TAG, "checkFriend() - Failed: No Exist Exception");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "checkFriend() - Loading Nickname - The read failed: " + error.getCode());
            }
        });
    }

    public boolean getCheckFriend() {
        return friend_status;
    }

    public void refreshName() {
        myRef.child(db_users).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(db_child_nick).exists()) {
                    name = snapshot.child(db_child_nick).getValue().toString();
                } else {
                    name = "Null";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "refreshName() - The read failed: " + error.getCode());
            }
        });
    }
}
