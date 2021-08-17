package com.techtown.bbfindingfriends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {
    private String TAG = "StartActivity";

    private FirebaseAuth firebaseAuth;

    String file = "data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Log.d(TAG, "onCreate()");

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();

        int result = intent.getIntExtra("Result", R.integer.code_fistLogin);
        boolean firstLogin = (result == R.integer.code_fistLogin);
        boolean autoLogin = getCheck("Login");

        Log.d(TAG, "result: " + result);
        Log.d(TAG, "firstLogin: " + firstLogin);
        Log.d(TAG, "autoLogin: " + autoLogin);

        switch (result) {
            case R.integer.code_fistLogin:
            case R.integer.code_login:
                String email, pw;
                if (firstLogin) {
                    if (!autoLogin) {
                        sendIntent("Login");
                        return;
                    }

                    email = getData("Email");
                    pw = getData("pw");
                } else {
                    email = intent.getStringExtra("Email");
                    pw = intent.getStringExtra("pw");
                }

                Log.d(TAG, "Email: " + email + "\n"
                        + "pw: " + pw);

                if (email != null && !email.equals("")
                    && pw != null && !pw.equals("")) {

                    if (email != null && pw != null) {
                        firebaseAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "task: " + task.isSuccessful());
                                if (autoLogin) {
                                    if (task.isSuccessful()) {
                                        sendIntent("Main");
                                    } else {
                                        sendIntent("Login");
                                    }
                                } else {
                                    if (task.isSuccessful()) {
                                        setResult(RESULT_OK);
                                    } else {
                                        setResult(R.integer.code_loginFailed);
                                    }
                                    finish();
                                }
                                return;
                            }
                        });
                    }
                } else if (firstLogin) {
                    sendIntent("Login");
                }
                break;
            default:
                sendIntent("Login");
                break;
        }
    }

    private void sendIntent(String data) {
        Log.d(TAG, "sendIntent(" + data + ")");
        if (data == "Main") {
            Intent intent_Main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent_Main);
        } else if (data == "Login") {
            Intent intent_Login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent_Login);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "oActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
    }

    private boolean getCheck(String position) {
        Log.d(TAG, "getCheck(" + position + ")");
        SharedPreferences sf = getSharedPreferences(file, MODE_PRIVATE);
        return sf.getBoolean(position, false);
    }

    private String getData(String position) {
        Log.d(TAG, "getData(" + position + ")");
        SharedPreferences sf = getSharedPreferences(file, MODE_PRIVATE);
        return sf.getString(position, "");
    }
}