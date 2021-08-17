package com.techtown.bbfindingfriends;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";

    private FirebaseAuth firebaseAuth;

    String file = "data";

    EditText etv_email;
    EditText etv_pw;
    CheckBox checkBox_ID;
    CheckBox checkBox_Login;
    Button button_login;

    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "onCreate()");

        view = (View) findViewById(R.id.view);

        // Toolbar - Back Button
        Toolbar mToolbar = findViewById(R.id.toolbar_back);
        setSupportActionBar(mToolbar);

        /*
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("계정");
         */

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        TextView tv_register = findViewById(R.id.tv_register);
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "button.setOnClickListener() Intent Show");
                sendIntent("Register");
            }
        });

        button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etv_email.getText().toString();
                String pw = etv_pw.getText().toString();
                if (email.equals("") || email == null) {
                    showSnackbar("이메일을 입력해주세요");
                } else if (pw.equals("") || pw == null) {
                    showSnackbar("비밀번호를 입력해주세요.");
                } else {
                    Intent intent_start = new Intent(getApplicationContext(), StartActivity.class);

                    intent_start.putExtra("Result", R.integer.code_login);
                    intent_start.putExtra("Email", email);
                    intent_start.putExtra("pw", pw);

                    startActivityResult.launch(intent_start);
                }
            }
        });

        etv_email = findViewById(R.id.etv_email);
        etv_pw = findViewById(R.id.etv_pw);

        checkBox_ID = findViewById(R.id.checkBox_Email);
        checkBox_Login = findViewById(R.id.checkBox_Login);

        if (getCheck("ID")
                && !getData("Email").equals("")
                && getData("Email") != null)
            etv_email.setText(getData("Email"));

        checkBox_ID.setChecked(getCheck("ID"));
        checkBox_Login.setChecked(getCheck("Login"));

        saveData("Email", "");

        Log.d(TAG, "ID: " + getCheck("ID") + "\n"
                + "Login: " + getCheck("Login") + "\n"
                + "Email: " + getData("Email"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");

        if (checkBox_ID.isChecked()) {
            saveCheck("ID", checkBox_ID.isChecked());
        }

        saveCheck("Login", checkBox_Login.isChecked());
        saveData("Email", etv_email.getText().toString());
        saveData("pw", etv_pw.getText().toString());
    }

    // New API
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult(" + result + ")");
                    Log.d(TAG, "result.getResultCode(): " + result.getResultCode());

                    switch (result.getResultCode()) {
                        case RESULT_OK:
                            sendIntent("Main");
                            break;
                        case R.integer.code_loginFailed:
                            showSnackbar("로그인에 실패하였습니다.");
                            break;
                        default:
                            break;
                    }
                }
            }
    );

    private void sendIntent(String data) {
        Log.d(TAG, "sendIntent(" + data + ")");
        if (data == "Register") {
            Intent intent_register = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent_register);
        } else if (data == "Main") {
            Intent intent_main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent_main);
            finish();
        }
    }

    /*
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
     */

    private void saveCheck(String position, boolean check) {
        Log.d(TAG, "saveCheck(" + position + ", " + check + ")");
        SharedPreferences sf = getSharedPreferences(file, MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();
        editor.putBoolean(position, check);
        editor.apply();
    }

    private boolean getCheck(String position) {
        Log.d(TAG, "getCheck(" + position + ")");
        SharedPreferences sf = getSharedPreferences(file, MODE_PRIVATE);
        return sf.getBoolean(position, false);
    }

    private void saveData(String position, String data) {
        Log.d(TAG, "saveData(" + position + ", " + data + ")");
        SharedPreferences sf = getSharedPreferences(file, MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();
        editor.putString(position, data);
        editor.apply();
    }

    private String getData(String position) {
        Log.d(TAG, "getData(" + position + ")");
        SharedPreferences sf = getSharedPreferences(file, MODE_PRIVATE);
        return sf.getString(position, "");
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