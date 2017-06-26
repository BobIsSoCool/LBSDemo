package com.example.wub.lbsdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.poi.BaiduMapPoiSearch;

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
    /**
     * 查询内容
     */
    EditText etSearchConten;
    /**
     * 点击查询
     */
    Button btSearch;
    /**
     * 所在城市
     */
    private String city = "北京";
    /**
     * 显示查询结果的列表
     */
    ListView lvSearchResult;
    /**
     * 适配器
     */
    SearchResultAdapter adapter;
    /**
     * 数据
     */
    List<SuggestionResult.SuggestionInfo> mList = new ArrayList<>();
    /**
     * 点击显示或隐藏列表
     */
    CheckBox cbHideOrShowList;
    /**
     * 点击查看全景图
     */
    ImageView ivQuanJing;

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
        etSearchConten = (EditText) findViewById(R.id.etSearchContent);
        btSearch = (Button) findViewById(R.id.btSearch);
        lvSearchResult = (ListView) findViewById(R.id.lvSearchResult);
        cbHideOrShowList = (CheckBox) findViewById(R.id.cbHideOrShowList);
        ivQuanJing = (ImageView) findViewById(R.id.ivQuanJing);
        //点击显示当前位置
        bt_ShowMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBdLocation != null) {
                    navigateTo(mBdLocation.getLatitude(), mBdLocation.getLongitude());
                }
            }
        });
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //如果列表可见，点击地图时隐藏列表
                if (lvSearchResult.getVisibility() == View.VISIBLE) {
                    lvSearchResult.setVisibility(View.GONE);
                }
            }
        });
        //地图控制器
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        //室内图
        baiduMap.setIndoorEnable(true);
        baiduMap.setOnBaseIndoorMapListener(new BaiduMap.OnBaseIndoorMapListener() {
            @Override
            public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
                if (b) {
                    // 进入室内图
                    // 通过获取回调参数 mapBaseIndoorMapInfo 来获取室内图信息，包含楼层信息，室内ID等
                } else {
                    // 移除室内图
                }
            }
        });
        //卫星地图
        //baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        //开启交通图
        // baiduMap.setTrafficEnabled(true);

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

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击查询
                search();
            }
        });

        //监听输入框
        etSearchConten.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(etSearchConten.getText())) {
                    //hideKeyboard();
                    if (lvSearchResult.getVisibility() == View.VISIBLE) {
                        lvSearchResult.setVisibility(View.GONE);
                    }
                }
            }
        });

        lvSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {    //第一项无数据，不然点击会报空
                    //点击项的数据实例
                    SuggestionResult.SuggestionInfo currentInfo = mList.get(i);
                    //在地图上定位到该位置
                    navigateTo(currentInfo.pt.latitude, currentInfo.pt.longitude);
                }
            }
        });

        cbHideOrShowList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbHideOrShowList.isChecked()){
                    if (mList.size()>0){
                        lvSearchResult.setVisibility(View.VISIBLE);
                    }
                }else {
                    lvSearchResult.setVisibility(View.GONE);
                }
            }
        });

        ivQuanJing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开全景图
                BaiduMapPoiSearch.openBaiduMapPanoShow("65e1ee886c885190f60e77ff",MainActivity.this);
            }
        });
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
            //取出城市，用于查询
            city = bdLocation.getCity();

            //第一次定位自动显示当前位置
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                if (isFirstLocate) {
                    navigateTo(bdLocation.getLatitude(), bdLocation.getLongitude());
                    isFirstLocate = false;
                }
            }

            //清空位置信息
            currentPosition.delete(0, currentPosition.length());
            currentPosition.append("当前：");
            currentPosition.append(bdLocation.getProvince()).append("\n");
            currentPosition.append(bdLocation.getCity()).append("\n");
            currentPosition.append(bdLocation.getDistrict()).append("\n");
            currentPosition.append(bdLocation.getStreet()).append("\n");
            currentPosition.append("定位方式：");

            //判断定位方式
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }
            currentPosition.append("\n");
            currentPosition.append(bdLocation.getLocationDescribe());
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
        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(true);

        //定位模式
        //1、Device_Sensors：传感器，只会使用GPS定位
        //2、Hight_Accuracy:高精度（默认的模式），GPS信号正常优先使用GPS定位，无法接受GPS信号会使用网络定位
        //3、Battery_Saving：节电模式：只会使用网络定位
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //表示需要获取详细地址信息
        option.setIsNeedAddress(true);
        //设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);

        mLocationClient.setLocOption(option);
    }

    /**
     * 将地图移动到当前位置
     *
     * @param
     */
    private void navigateTo(double lat, double lon) {

        //移动到当前位置
        LatLng ll = new LatLng(lat, lon);
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(18f);
        baiduMap.animateMapStatus(update);

        //在地图上显示光标
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(lat);
        builder.longitude(lon);
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

    /**
     * 执行在线建议查询
     */
    public void search() {
        //创建在线建议查询
        SuggestionSearch search = SuggestionSearch.newInstance();
        //设置监听
        search.setOnGetSuggestionResultListener(listener);
        //获取查询内容
        String searchContent = etSearchConten.getText().toString();
        //发起在线建议查询
        search.requestSuggestion(new SuggestionSearchOption().keyword(searchContent).city(city));
    }

    //创建在线建议查询监听者
    OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
        public void onGetSuggestionResult(SuggestionResult res) {
            if (res == null || res.getAllSuggestions() == null) {
                Toast.makeText(MainActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                return;
            }
            //获取在线建议检索结果
            mList = res.getAllSuggestions();
            for (SuggestionResult.SuggestionInfo info : mList
                    ) {
                //Log.d("查询结果：",info.key);
            }
            //显示结果到列表
            showInList(mList);
        }
    };

    /**
     * 将查询结果显示在列表
     *
     * @param list
     */
    public void showInList(List<SuggestionResult.SuggestionInfo> list) {
        //构建适配器
        adapter = new SearchResultAdapter(this, list);
        //将当前位置传过去（用于计算距离）
        LatLng latLng = new LatLng(mBdLocation.getLatitude(),mBdLocation.getLongitude());
        adapter.setCurrentLatl(latLng);
        //关联适配器
        lvSearchResult.setAdapter(adapter);
        //显示列表
        lvSearchResult.setVisibility(View.VISIBLE);
        cbHideOrShowList.setChecked(true);
    }

    /**
     * 关闭软键盘
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && this.getCurrentFocus() != null) {
            if (this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
