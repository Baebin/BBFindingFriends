package com.techtown.bbfindingfriends;

import android.util.Log;

import androidx.annotation.NonNull;

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

    public Friend(String uid) {
        this.uid = uid;

        fdb = FirebaseDatabase.getInstance();
        myRef = fdb.getReference("Nickname");

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

    public void refreshName() {
        myRef.child(this.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Nickname").exists()) {
                    name = snapshot.child("Nickname").getValue().toString();
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
