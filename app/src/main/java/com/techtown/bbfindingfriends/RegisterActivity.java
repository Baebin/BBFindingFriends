package com.techtown.bbfindingfriends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase fdb;
    private DatabaseReference myRef;

    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d(TAG, "onCreate()");

        view = findViewById(R.id.view);

        // Toolbar - Back Button
        Toolbar mToolbar = findViewById(R.id.toolbar_back);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("회원가입");

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        fdb = FirebaseDatabase.getInstance();
        myRef = fdb.getReference("Users");

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        EditText etv_email = findViewById(R.id.etv_email);
        EditText etv_nickname = findViewById(R.id.etv_nickname);
        EditText etv_pw = findViewById(R.id.etv_pw);
        EditText etv_pw2 = findViewById(R.id.etv_pw2);

        Button button_register = findViewById(R.id.button_register);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setOnClickListener()");
                vibrator.vibrate(300);

                String email = etv_email.getText().toString();
                String nickname = etv_nickname.getText().toString();
                String pw = etv_pw.getText().toString();
                String pw2 = etv_pw2.getText().toString();

                if (email.equals("") || email == null) {
                    showSnackbar("이메일을 입력해주세요.");
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        showSnackbar("이메일 형식이 올바르지 않습니다.");
                    } else if (nickname.equals("") || nickname == null) {
                        showSnackbar("닉네임을 입력해주세요.");
                    } else if (pw.equals("") || pw == null) {
                        showSnackbar("비밀번호를 입력해주세요.");
                    } else if (pw.length() < 5 || pw.length() > 15) {
                        showSnackbar("비밀번호를 최소 6글자 이상 15글자 이하로 설정해주세요.");
                    } else if (pw2.equals("") || pw2 == null) {
                        showSnackbar("비밀번호 재확인을 입력해주세요.");
                    } else {
                        if (pw.equals(pw2)) {
                            checkID(email, nickname, pw);
                        } else {
                            showSnackbar("비밀번호 재확인이 올바르지 않습니다.");
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "Toolbar back");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkID(String email, String nickname, String pw) {
        Log.d(TAG, "checkID(" + email + ", " + nickname + ", " + pw + ")");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "myRef.addValueEventListener");
                int i = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    i++;
                    String nick = child.child("Nickname").getValue().toString();
                    Log.d(TAG, i + ". " + nick + ": " + child);

                    if (nickname.toLowerCase().equals(nick.toLowerCase())) {
                        showSnackbar("이미 존재하는 닉네임입니다.");
                        myRef.removeEventListener(this);
                        return;
                    }
                }
                myRef.removeEventListener(this);
                register(email, nickname, pw);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Register - The read failed: " + error.getCode());
            }
        });
    }

    private void register(String email, String nickname, String pw) {
        Log.d(TAG, "register(" + email + ", " + nickname + ", " + pw + ")");

        firebaseAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    showToast("성공적으로 회원가입 되었습니다.");

                    FirebaseUser user = task.getResult().getUser();
                    String UID = user.getUid();
                    myRef.child(UID).child("Nickname").setValue(nickname);

                    Log.d(TAG, "UID: " + UID);

                    finish();
                } else {
                    showSnackbar("이미 존재하는 아이디입니다.");
                }
            }
        });
    }

    public void showToast(String data) {
        Log.d(TAG, "showToast(" + data + ")");
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
    }

    public void showSnackbar(String data) {
        Log.d(TAG, "showSnackbar(" + data + ")");
        final Snackbar snackbar = Snackbar.make(view, data, Snackbar.LENGTH_SHORT);
        snackbar.setAction("확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        }).show();
    }
}