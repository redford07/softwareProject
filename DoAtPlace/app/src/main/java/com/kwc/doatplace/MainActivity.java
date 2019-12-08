package com.kwc.doatplace;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.pwittchen.reactivebeacons.library.rx2.Beacon;
import com.github.pwittchen.reactivebeacons.library.rx2.Proximity;
import com.github.pwittchen.reactivebeacons.library.rx2.ReactiveBeacons;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends Activity {
    private static final boolean IS_AT_LEAST_ANDROID_M =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000;
    private static final String ITEM_FORMAT = "MAC: %s, RSSI: %d\n비콘과의거리: %.2fm, proximity: %s\n%s";
    private ReactiveBeacons reactiveBeacons;
    private Disposable subscription;
    private ListView lvBeacons;
    private Map<String, Beacon> beacons;

    Button loginbtn;
    SessionCallback callback;
    public static final String NICKNAME = "nick";
    public static final String USER_ID = "id";

    Button button;
    TextView textView;
    GridParser gridParser = new GridParser();
    Map<String, Object> grid;
    String urlStr;
    Button button1;
    private static int REQUEST_ACCESS_FINE_LOCATION = 1000;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvBeacons = (ListView) findViewById(R.id.lv_beacons);
        reactiveBeacons = new ReactiveBeacons(this);
        beacons = new HashMap<>();
        button1 = (Button) findViewById(R.id.button1);
        startLocationService();
        // 위치 가져오기 버튼
        // 좌표 텍스트뷰
        textView = (TextView) findViewById(R.id.textView);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                startActivity(intent);
            }
        });

        loginbtn = (Button)findViewById(R.id.login);



        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //로그아웃 성공 후 실행 동작
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_non = new Intent(getApplicationContext(),FirstActivity.class);
                startActivity(intent_non);
                finish();
            }
        });

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
    }

    //재 로그인 요청
    private void redirectLoginActivity() {
        final Intent intent = new Intent(this, FirstActivity.class);
        startActivity(intent);
        finish();
    }
    //간편로그인시 호출되는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //간편로그인시 호출 ,없으면 간편로그인시 로그인 성공화면으로 넘어가지 않음
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    //이 부분이 없는경우 누적 로그인(?)이 될 수 있음
    //맨 처음 테스트 시 안쓰니깐 로그인이 누적되서 intent 시 잘못된 값을 가져오더라구요..
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }
    //SessionCallback 클래스 구현
    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Logger.d(message);

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        finish();
                    } else {
                        //재 로그인
                        Toast.makeText(getApplicationContext(),"다시 로그인 해주세요.",Toast.LENGTH_SHORT).show();
                        redirectLoginActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                @Override
                public void onNotSignedUp() {
                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인 성공 시 로그인한 사용자의 일련번호, 닉네임, 이미지url 리턴
                    //사용자 캐시 정보 업데이트 - 별 필요 없는듯


                    final String nickName = userProfile.getNickname();//닉네임
                    final long userID = userProfile.getId();//사용자 고유번호

                    Intent intent = new Intent(MainActivity.this, FirstActivity.class);
                    intent.putExtra(NICKNAME,nickName);
                    intent.putExtra(USER_ID,String.valueOf(userID));
                    startActivity(intent);
                    finish();

                }
            });

        }


        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            // 세션 연결이 실패했을때
            if(exception != null) {
                Toast.makeText(getApplicationContext(),"카카오톡 로그인 DoAtPlace",Toast.LENGTH_SHORT).show();
                Logger.e(exception);
            }
        }
    }

    // 위치 가져오기 메소드
    public void startLocationService() {

        long minTime = 0;
        float minDistance = 0;
        // 단말에서 동작하고 있는 위치 시스템 관리자를 가져옴
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // manager를 통해 위치정보를 가져옴.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

                if(permissionCheck == PackageManager.PERMISSION_DENIED){

                    // 권한 없음
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_ACCESS_FINE_LOCATION);
//                             CDialog.onHide();
                }


            }
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener);
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);
        }catch (Exception e){
            Log.d("gpserror",e.toString());
        }

    } // end of startLocationService

    // 위치 리스너
    LocationListener listener  = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 위경도, gridxy 받아오기
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            grid = gridParser.getGridxy(latitude, longitude);
            int gridx = (int)(double) grid.get("x");
            int gridy = (int)(double) grid.get("y");

            // 텍스트뷰에 위경도, gridxy 보여주기
            textView.setText("내 위치 : " + latitude + ", " + longitude);
            textView.append("\ngridxy : " + grid.get("x") + ", " + grid.get("y"));

            // 동네예보 받아오기.
            urlStr = "x좌표:"+gridx+"  y좌표:"+gridy;
            textView.setText(urlStr);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };
    @Override protected void onResume() {
        super.onResume();

        if (!canObserveBeacons()) {
            return;
        }

        startSubscription();
    }

    private void startSubscription() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestCoarseLocationPermission();
            return;
        }

        subscription = reactiveBeacons.observe()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Beacon>() {
                    @Override public void accept(@NonNull Beacon beacon) throws Exception {
                        beacons.put(beacon.device.getAddress(), beacon);
                        refreshBeaconList();
                    }
                });
    }

    private boolean canObserveBeacons() {
        if (!reactiveBeacons.isBleSupported()) {
            Toast.makeText(this, "BLE is not supported on this device", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!reactiveBeacons.isBluetoothEnabled()) {
            reactiveBeacons.requestBluetoothAccess(this);
            return false;
        } else if (!reactiveBeacons.isLocationEnabled(this)) {
            reactiveBeacons.requestLocationAccess(this);
            return false;
        } else if (!isFineOrCoarseLocationPermissionGranted() && IS_AT_LEAST_ANDROID_M) {
            requestCoarseLocationPermission();
            return false;
        }

        return true;
    }

    private void refreshBeaconList() {
        List<String> list = new ArrayList<>();

        for (Beacon beacon : beacons.values()) {
            list.add(getBeaconItemString(beacon));
        }

        int itemLayoutId = android.R.layout.simple_list_item_1;
        lvBeacons.setAdapter(new ArrayAdapter<>(this, itemLayoutId, list));
    }

    private String getBeaconItemString(Beacon beacon) {
        String mac = beacon.device.getAddress();
        int rssi = beacon.rssi;
        double distance = beacon.getDistance();
        Proximity proximity = beacon.getProximity();
        String name = beacon.device.getName();
        if(distance<10) name="방도착";
        return String.format(ITEM_FORMAT, mac, rssi, distance, proximity, name);
    }

    @Override protected void onPause() {
        super.onPause();
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode,
                                                     @android.support.annotation.NonNull String[] permissions,
                                                     @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final boolean isCoarseLocation = requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION;
        final boolean permissionGranted = grantResults[0] == PERMISSION_GRANTED;

        if (isCoarseLocation && permissionGranted && subscription == null) {
            startSubscription();
        }
    }

    private void requestCoarseLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] { ACCESS_COARSE_LOCATION },
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    private boolean isFineOrCoarseLocationPermissionGranted() {
        boolean isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        boolean isFineLocationPermissionGranted = isGranted(ACCESS_FINE_LOCATION);
        boolean isCoarseLocationPermissionGranted = isGranted(ACCESS_COARSE_LOCATION);

        return isAndroidMOrHigher && (isFineLocationPermissionGranted
                || isCoarseLocationPermissionGranted);
    }

    private boolean isGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
    }
}
