package com.stone.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView positionText;
    public LocationClient mLocationClient;
    private MapView mapView;

    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        positionText = findViewById(R.id.position_test_view);

        List permissionList = new ArrayList();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = (String[]) permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            //给地图设置经纬度
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

            System.out.println("输出一下经纬度==" + location.getLatitude() + "sssss=" + location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            //            //设置缩放级别
            //            update = MapStatusUpdateFactory.zoomTo(18.7f);
            //            baiduMap.animateMapStatus(update);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(19f);
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

            isFirstLocate = false;

        }

        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);

    }


    private void requestLocation() {

        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //设置3秒更新一下位置
        option.setScanSpan(3000);
        //设置为详细的地理位置
        option.setIsNeedAddress(true);
        //设置为GPS定位模式
        //                option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "必须同意所有授权才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(MainActivity.this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }

    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //            StringBuffer currentLocation = new StringBuffer();
            //            currentLocation.append("纬度").append(location.getLatitude()).append("\n");
            //            currentLocation.append("经度").append(location.getLongitude()).append("\n");
            //            currentLocation.append("国家:").append(location.getCountry()).append("\n");
            //            currentLocation.append("省:").append(location.getProvince()).append("\n");
            //            currentLocation.append("市:").append(location.getCity()).append("\n");
            //            currentLocation.append("区:").append(location.getDistrict()).append("\n");
            //            currentLocation.append("街道:").append(location.getStreet()).append("\n");
            //            currentLocation.append("定位方式:");
            //
            //            if (location.getLocType() == BDLocation.TypeGpsLocation) {
            //                currentLocation.append("GPS");
            //
            //            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
            //                currentLocation.append("网络");
            //            }
            //            positionText.setText(currentLocation);
            //
            if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }

        }
    }

}
