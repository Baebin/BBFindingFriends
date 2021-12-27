package com.techtown.bbfindingfriends;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

public class FriendActivity extends AppCompatActivity {
    private static final String TAG = "FriendActivity";

    private final int listView = 1;
    private final int searchView = 2;

    FriendListFragment listFragment = new FriendListFragment();
    FriendSearchFragment searchFragment = new FriendSearchFragment();

    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitiy_friend);

        // Toolbar - Back Button
        Toolbar mToolbar = findViewById(R.id.toolbar_back);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applySetting(listView);

        view = findViewById(R.id.view);
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

    public void setTitle(int position) {
        Log.d(TAG, "setTitle(" + position + ")");

        switch (position) {
            case listView:
                getSupportActionBar().setTitle("친구 목록");
                return;
            case searchView:
                getSupportActionBar().setTitle("친구 찾기");
                return;
        }
    }

    public void moveFragment(int position) {
        Log.d(TAG, "moveFragment(" + position + ")");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (position) {
            case listView:
                transaction.replace(R.id.frame_layout, listFragment);
                transaction.commit();
                return;
            case searchView:
                transaction.replace(R.id.frame_layout, searchFragment);
                transaction.commit();
                return;
        }
    }

    public void applySetting(int position) {
        setTitle(position);
        moveFragment(position);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "Toolbar back");
                finish();
                return true;
            case R.id.item_list:
                applySetting(listView);
                return true;
            case R.id.item_search:
                applySetting(searchView);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.friend, menu);
        return true;
    }
}
