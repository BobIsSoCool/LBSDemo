package com.example.wub.lbsdemo;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {
    /**
     * LocationClient
     */
    public LocationClient mLocationClient;
    /**
     * 位置信息
     */
    TextView tvPosition;
    /**
     * 显示第几次定位
     */
    private int count = 0;
    /**
     * 地图控件
     */
    MapView mapView;
    /**
     * BaiduMap
     */
    private BaiduMap baiduMap;
    /**
     * 标识是否第一次定位
     */
    private boolean isFirstLocate = true;
    /**
     * 点击回到当前位置
     */
    BDLocation mBdLocation = null;
    Button bt_ShowMe;

    StringBuilder currentPosition = new StringBuilder();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvPosition.setText(currentPosition);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        //布局
        setContentView(R.layout.activity_main);
        //控件
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        mapView = (MapView) findViewById(R.id.mapView);
        bt_ShowMe = (Button) findViewById(R.id.bt_MoveToMeOnMap);
        //点击显示当前位置
        bt_ShowMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBdLocation != null) {
                    navigateTo(mBdLocation);
                }
            }
        });
        //地图控制器
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        //运行时权限处理
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            //定位
            requestLocation();
        }

    }

    /**
     * 定位
     */
    public void requestLocation() {
        //定位的一些设置
        initLocation();
        //开始定位
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //权限满足，开始定位
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知的错误", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    /**
     * 定位成功回调
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            //把位置信息赋值给全局BDLocation变量，用于点击按钮回到当前位置的参数
            mBdLocation = bdLocation;

            //第一次定位自动显示当前位置
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                if (isFirstLocate) {
                    navigateTo(bdLocation);
                    isFirstLocate = false;
                }
            }

            //清空位置信息
            currentPosition.delete(0, currentPosition.length());

            currentPosition.append("第").append(++count).append("次请求").append("\n");
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
            currentPosition.append("定位方式：");
            //判断定位方式
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }
            currentPosition.append("\n\n");
            //显示位置信息
            //handler.sendEmptyMessage(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvPosition.setText(currentPosition);
                }
            });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    /**
     * 定位设置
     */
    public void initLocation() {
        LocationClientOption option = new LocationClientOption();

        //每5秒获取一次位置信息
        option.setScanSpan(5000);
        //定位模式
        //1、Device_Sensors：传感器，只会使用GPS定位
        //2、Hight_Accuracy:高精度（默认的模式），GPS信号正常优先使用GPS定位，无法接受GPS信号会使用网络定位
        //3、Battery_Saving：节电模式：只会使用网络定位
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //表示需要获取详细地址信息
        option.setIsNeedAddress(true);

        mLocationClient.setLocOption(option);
    }

    /**
     * 将地图移动到当前位置
     *
     * @param location
     */
    private void navigateTo(BDLocation location) {

        //移动到当前位置
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(16f);
        baiduMap.animateMapStatus(update);

        //在地图上显示光标
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData locationData = builder.build();
        baiduMap.setMyLocationData(locationData);
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
}
