package com.techtown.bbfindingfriends;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, AutoPermissionsListener {
    private static final String TAG = "MainActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase fdb;
    private DatabaseReference myRef;

    private FirebaseUser user;

    private String nickname;

    private DrawerLayout drawerLayout;
    private View drawerView;

    private GoogleMap map;

    private ImageView iv_profile;
    private TextView tv_nick;
    private Button button_logout;
    private Button button_friend;

    private FriendLocation myLocation = new FriendLocation();

    FriendsAdapter friendsAdapter;
    RecyclerView friendView;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        fdb = FirebaseDatabase.getInstance();
        myRef = fdb.getReference(getString(R.string.db_users));

        refresh();

        // GoogleMap
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer_view);

        drawerLayout.addDrawerListener(drawerListener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        iv_profile = findViewById(R.id.iv_profile);
        iv_profile.setImageResource(R.drawable.ic_launcher_background);

        tv_nick = findViewById(R.id.tv_nick);
        button_logout = findViewById(R.id.button_logout);

        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        button_friend = findViewById(R.id.button_friend);
        button_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendIntent("Friend");
            }
        });

        friendView = findViewById(R.id.friend_layout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        friendView.setLayoutManager(linearLayoutManager);

        friendsAdapter = new FriendsAdapter();

        Log.d(TAG, "friendsAdapter Called");

        // 위험 권한 자동 부여 요청
        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    private void setAdapter() {
        Log.d(TAG, "setAdapter()");

        friendsAdapter.resetFriends();

        myRef.child(user.getUid()).child(getString(R.string.db_child_friends)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

                                Log.d(TAG, "setAdapter() - Loading Nickname: " + friendName);
                            }

                            friendsAdapter.addFriend(friend);
                            Log.d(TAG, "setAdapter() - Added " + uid + ": " + nickname);

                            if (finalCount == 0) {
                                // 역순 출력: stack 마지막이 처음 보여지는 위치
                                friendsAdapter.reverseFriends();
                                friendView.setAdapter(friendsAdapter);
                                friendView.smoothScrollToPosition(friendsAdapter.getItemCount()-1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError friendError) {
                            Log.d(TAG, "setAdapter() - Loading Nickname - The read failed: " + friendError.getCode());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "setAdapter() - The read failed: " + error.getCode());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart()");
        setAdapter();
        startService();
    }

    private void setUser() {
        user = firebaseAuth.getCurrentUser();

        Log.d(TAG, "setUser(): " + user);
    }

    private void setNickname() {
        Log.d(TAG, "setNickname()");
        nickname = null;
        if (user == null) {
            setNicknameView();
            return;
        }

        Log.d(TAG, "setNickname() - Test");

        myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nickname = snapshot.child(getString(R.string.db_child_nick)).getValue().toString();
                setNicknameView();

                Log.d(TAG, "setNickname(): " + nickname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setNicknameView();
                Log.d(TAG, "setNickname() - The read failed: " + error.getCode());
            }
        });
    }

    private void setNicknameView() {
        Log.d(TAG, "setNicknameView()");
        if (nickname == null) {
            tv_nick.setText("Null");
            Log.d(TAG, "setNicknameView() - Null");
        } else {
            tv_nick.setText(nickname);
            Log.d(TAG, "setNicknameView() - " + nickname);
        }
    }

    private void refresh() {
        setUser();
        setNickname();
    }

    private void logout() {
        Log.d(TAG, "logout()");
        if (firebaseAuth.getCurrentUser() == null) {
            Log.d(TAG, "logout() - Failed");
            return;
        }
        firebaseAuth.signOut();
        sendIntent("Login");
        Log.d(TAG, "logout() - Successful");
    }

    private void sendIntent(String data) {
        Log.d(TAG, "sendIntent(" + data + ")");
        if (data == "Login") {
            Intent intent_Login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent_Login);
        } else if (data == "Friend") {
            Intent intent_Friend = new Intent(getApplicationContext(), FriendActivity.class);
            startActivity(intent_Friend);
            return;
        }

        finish();
    }

    DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            refresh();
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            refresh();
            friendView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            Log.d(TAG, "onDrawerStateChanged(" + newState + ")");
            if (newState == 1) {
                friendView.setVisibility(View.INVISIBLE);
            }
        }
    };

    private void startService() {
        Log.d(TAG, "startService()");
        Intent intent_service = new Intent(this, ForegroundService.class);
        ContextCompat.startForegroundService(this, intent_service);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent()");
        setLocation(intent);
        super.onNewIntent(intent);
    }

    private void setLocation(Intent intent) {
        Log.d(TAG, "setLocation()");

        int exception = R.integer.code_location_null;

        double mLatitude = intent.getDoubleExtra("Latitude", exception);
        double mLongtitude = intent.getDoubleExtra("Longtitude", exception);
        double mAltitude = intent.getDoubleExtra("Altitude", exception);

        if (mLatitude != exception
            && mLongtitude != exception
            && mAltitude != exception) {

            myLocation.setLatitude(mLatitude);
            myLocation.setLongtitude(mLongtitude);
            myLocation.setAltitude(mAltitude);

            Log.d(TAG, "gpsListener() - \n"
                    + "Latitude: " + mLatitude + "\n"
                    + "Longtitude: " + mLongtitude + "\n"
                    + "Altitude: " + mAltitude);

            saveMyLocation();
        } else {
            String[] exceptionList = new String[] {"Failed", "Failed", "Failed"};

            if (mLatitude != exception) exceptionList[0] = "(Complete) " + mLatitude;
            if (mLongtitude != exception) exceptionList[1] = "(Complete) " + mLongtitude;
            if (mAltitude != exception) exceptionList[2] = "(Complete) " + mAltitude;

            Log.d(TAG, "setLocation() - Failed: \n"
                    + "Latitude: " + exceptionList[0] + "\n"
                    + "Longtitude: " + exceptionList[1] + "\n"
                    + "Altitude: " + exceptionList[2]);
        }
    }

    private void saveMyLocation() {
        Log.d(TAG, "saveLocation()");
        if (!myLocation.checkException()) {
            Log.d(TAG, "saveLocation() - Failed");
            return;
        }

        myRef.child(user.getUid()).child(getString(R.string.db_child_location)).setValue(myLocation);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady(" + googleMap + ")");
        map = googleMap;

        // 기본 맵 한국어 설정
        Locale locale_kr = Locale.KOREAN;
        Locale.setDefault(locale_kr);

        // 자신의 위치 조회
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);

        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("대한민국 수도");

        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(16));

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, "onMapReady() - " + location.getLatitude() + ":" + location.getLongitude());
                    LatLng curLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLng(curLoc));
                    map.animateCamera(CameraUpdateFactory.zoomIn());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Allow: onGranted() Method Call
        // Deny: onDenied() Method Call
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);

    }

    @Override
    public void onDenied(int i, String[] strings) {
        Log.d(TAG, "onDenied(" + i + ", " + strings + ")");
    }

    @Override
    public void onGranted(int i, String[] strings) {
        Log.d(TAG, "onGranted(" + i + ", " + strings + ")");
    }
}