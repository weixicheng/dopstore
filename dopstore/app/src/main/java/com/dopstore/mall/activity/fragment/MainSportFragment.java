package com.dopstore.mall.activity.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.dopstore.mall.R;
import com.dopstore.mall.activity.WebActivity;
import com.dopstore.mall.activity.adapter.ActivityAdapter;
import com.dopstore.mall.activity.adapter.HomeAdImageAdapter;
import com.dopstore.mall.activity.adapter.TabAdapter;
import com.dopstore.mall.activity.bean.ActivityData;
import com.dopstore.mall.activity.bean.CarouselData;
import com.dopstore.mall.activity.bean.MainTabData;
import com.dopstore.mall.base.BaseFragment;
import com.dopstore.mall.shop.activity.ActivityDetailActivity;
import com.dopstore.mall.shop.activity.ActivityListActivity;
import com.dopstore.mall.util.Constant;
import com.dopstore.mall.util.HttpHelper;
import com.dopstore.mall.util.ProUtils;
import com.dopstore.mall.util.SkipUtils;
import com.dopstore.mall.util.T;
import com.dopstore.mall.util.URL;
import com.dopstore.mall.view.EScrollView;
import com.dopstore.mall.view.MyListView;
import com.dopstore.mall.view.PullToRefreshView;
import com.dopstore.mall.view.PullToRefreshView.OnFooterRefreshListener;
import com.dopstore.mall.view.PullToRefreshView.OnHeaderRefreshListener;
import com.dopstore.mall.view.rollviewpager.OnItemClickListener;
import com.dopstore.mall.view.rollviewpager.RollPagerView;
import com.dopstore.mall.view.rollviewpager.hintview.IconHintView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 喜成 on 16/9/5
 * name 活动
 */
@SuppressLint("ValidFragment")
public class MainSportFragment extends BaseFragment implements OnHeaderRefreshListener, OnFooterRefreshListener {
    private PullToRefreshView pullToRefreshView;
    private ScrollView scrollView;
    private MyListView listView;
    private TextView leftTv;
    private ImageButton rightBt;
    private LinearLayout searchLayout;
    private EditText seartchEt;
    private TextView seartchBt;
    private EScrollView eScrollView;
    private List<MainTabData> tabList = new ArrayList<MainTabData>();
    private TabAdapter tabAdapter;
    private LinearLayout headLayout;
    private RelativeLayout firstLy, secondLy;
    private TextView firstTv, secondTv;
    private View firstV, secondV;
    private RollPagerView rollPagerView;
    private List<CarouselData> titleAdvertList = new ArrayList<CarouselData>();
    private List<ActivityData> aList = new ArrayList<ActivityData>();
    private String latitude = "";
    private String longitude = "";
    private int page = 1;
    private boolean isRefresh = false;
    private boolean isUpRefresh = false;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private View v;
    private int viewType=0;
    private String typeId="";
    private Context context;

    public MainSportFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.layout_main_sport_fragment, null);
        initView(v);
        initData();
        return v;
    }

    private void initView(View v) {
        leftTv = (TextView) v.findViewById(R.id.sport_title_left_textView);
        rightBt = (ImageButton) v.findViewById(R.id.sport_title_right_imageButton);
        searchLayout = (LinearLayout) v.findViewById(R.id.sport_search_title_layout);
        seartchEt = (EditText) v.findViewById(R.id.sport_search_title_et);
        seartchBt = (TextView) v.findViewById(R.id.sport_search_title_tv);
        leftTv.setOnClickListener(listener);
        rightBt.setOnClickListener(listener);
        seartchBt.setOnClickListener(listener);
        eScrollView = (EScrollView) v.findViewById(R.id.main_sport_fragment_tab_escrollview);
        scrollView = (ScrollView) v.findViewById(R.id.main_sport_fragment_main_scrollview);
        pullToRefreshView = (PullToRefreshView) v.findViewById(R.id.main_sport_fragment_pulltorefreshview);
        listView = (MyListView) v.findViewById(R.id.main_sport_fragment_listview);
        initHeadView(v);
        pullToRefreshView.setOnHeaderRefreshListener(this);
        pullToRefreshView.setOnFooterRefreshListener(this);
    }

    private void initHeadView(View headView) {
        headLayout = (LinearLayout) headView.findViewById(R.id.main_sport_head_Layout);
        rollPagerView = (RollPagerView) headView.findViewById(R.id.roll_view_pager);
        firstLy = (RelativeLayout) headView.findViewById(R.id.main_sport_head_first_ly);
        secondLy = (RelativeLayout) headView.findViewById(R.id.main_sport_head_second_ly);
        firstTv = (TextView) headView.findViewById(R.id.main_sport_head_first_tv);
        secondTv = (TextView) headView.findViewById(R.id.main_sport_head_second_tv);
        firstV = headView.findViewById(R.id.main_sport_head_first_v);
        secondV = headView.findViewById(R.id.main_sport_head_second_v);
        firstLy.setOnClickListener(listener);
        secondLy.setOnClickListener(listener);
    }


    private void initData() {
        titleAdvertList.clear();
        aList.clear();
        tabList.clear();
        getTabData();
        getCarousel();
    }

    private void getTabData() {
        proUtils.show();
        httpHelper.getDataAsync(context, URL.ACT_CATEGORIES, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                T.checkNet(context);
                proUtils.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONObject jo = new JSONObject(body);
                    String code = jo.optString(Constant.ERROR_CODE);
                    if ("0".equals(code)) {
                        MainTabData data = new MainTabData();
                        data.setId("");
                        data.setName("推荐");
                        data.setIsSelect("1");
                        tabList.add(data);
                        JSONArray ja = jo.getJSONArray(Constant.CATEGORIES);
                        if (ja.length() > 0) {
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject tab = ja.getJSONObject(i);
                                MainTabData tabData = new MainTabData();
                                tabData.setId(tab.optString(Constant.ID));
                                tabData.setName(tab.optString(Constant.NAME));
                                tabData.setIsSelect("0");
                                tabList.add(tabData);
                            }
                        }
                    } else {
                        String msg = jo.optString(Constant.ERROR_MSG);
                        T.show(context, msg);
                    }
                    handler.sendEmptyMessage(UPDATA_TAB_CODE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                proUtils.dismiss();
            }
        }, null);
    }

    private void getCarousel() {
        proUtils.show();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("project_type", "2");
        httpHelper.postKeyValuePairAsync(context, URL.HOME_CAROUSEL, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                T.checkNet(context);
                proUtils.dismiss();
            }

            @Override
            public void onResponse(Call call,Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONObject jo = new JSONObject(body);
                    String code = jo.optString(Constant.ERROR_CODE);
                    if ("0".equals(code)) {
                        JSONArray ja = jo.getJSONArray(Constant.CAROUSEL);
                        if (ja.length() > 0) {
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject job = ja.getJSONObject(i);
                                CarouselData data = new CarouselData();
                                data.setId(job.optString(Constant.ID));
                                data.setUrl(job.optString(Constant.URL));
                                data.setTitle(job.optString(Constant.TITLE));
                                data.setPicture(job.optString(Constant.PICTURE));
                                titleAdvertList.add(data);
                            }
                        }
                    } else {
                        String msg = jo.optString(Constant.ERROR_MSG);
                        T.show(context, msg);
                    }
                    handler.sendEmptyMessage(UPDATA_HEAD_CODE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                proUtils.dismiss();
            }
        }, null);
        getTdata("");
    }

    private void getTdata(final String id) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constant.PAGESIZE, "10");
        map.put(Constant.PAGE, page + "");
        if (!TextUtils.isEmpty(id)) {
            map.put(Constant.CATEGORY_ID, id);
        }
        httpHelper.postKeyValuePairAsync(context, URL.RECOMMENDED_ACT, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                T.checkNet(context);
                dismissRefresh();
                proUtils.dismiss();
            }

            @Override
            public void onResponse(Call call,Response response) throws IOException {
                String body = response.body().string();
                analysisData(body);
                if (!TextUtils.isEmpty(id)) {
                    handler.sendEmptyMessage(UPDATA_OTHER_CODE);
                } else {
                    handler.sendEmptyMessage(UPDATA_DATA_CODE);
                }
                dismissRefresh();
                proUtils.dismiss();
            }
        }, null);
    }

    private void getNdata() {
        proUtils.show();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constant.PAGESIZE, "10");
        map.put(Constant.PAGE, "1");
        map.put(Constant.LAT, latitude);
        map.put(Constant.LNG, longitude);
        httpHelper.postKeyValuePairAsync(context, URL.RECOMMENDED_ACT, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                T.checkNet(context);
                dismissRefresh();
                proUtils.dismiss();
            }

            @Override
            public void onResponse(Call call,Response response) throws IOException {
                String body = response.body().string();
                analysisData(body);
                handler.sendEmptyMessage(UPDATA_NFC_CODE);
                dismissRefresh();
                proUtils.dismiss();
            }
        }, null);
    }


    /**
     * 设置轮播
     */
    private void setAdvertisementData() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        // 设置图片宽高
        int screenWidth = ((Activity)context).getWindowManager()
                .getDefaultDisplay().getWidth();
        int picSize = screenWidth / 2;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                screenWidth, picSize);
        rollPagerView.setLayoutParams(layoutParams);

        if (titleAdvertList != null) {
            //设置播放时间间隔
            rollPagerView.setPlayDelay(1000);
            //设置透明度
            rollPagerView.setAnimationDurtion(500);
            //设置适配器
            rollPagerView.setAdapter(new HomeAdImageAdapter(context, titleAdvertList));
            //设置指示器（顺序依次）
            rollPagerView.setHintView(new IconHintView(context, R.mipmap.dop_press, R.mipmap.dop_normal));

            if (titleAdvertList.size() == 1) {
                rollPagerView.pause();
                rollPagerView.setHintView(null);
            }
        }
        rollPagerView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                CarouselData data = titleAdvertList.get(position);
                String urlStr = data.getUrl();
//                if (!TextUtils.isEmpty(urlStr)) {
//                    Map<String, Object> map = new HashMap<String, Object>();
//                    map.put("title", titleAdvertList.get(position).getTitle());
//                    map.put("url", titleAdvertList.get(position).getUrl());
//                    SkipUtils.jumpForMap(context, WebActivity.class, map, false);
//                }else {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(Constant.ID, titleAdvertList.get(position).getId());
                    SkipUtils.jumpForMap(context, ActivityDetailActivity.class, map, false);
//                }
            }
        });
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.sport_title_left_textView: {
                }
                break;
                case R.id.main_sport_head_first_ly: {
                    firstTv.setTextColor(getResources().getColor(R.color.red_color_f93448));
                    firstV.setBackgroundColor(getResources().getColor(R.color.red_color_f93448));
                    secondTv.setTextColor(getResources().getColor(R.color.gray_color_33));
                    secondV.setBackgroundColor(getResources().getColor(R.color.white_color));
                    latitude = "";
                    longitude = "";
                    aList.clear();
                    getTdata("");
                }
                break;
                case R.id.main_sport_head_second_ly: {
                    firstTv.setTextColor(getResources().getColor(R.color.gray_color_33));
                    firstV.setBackgroundColor(getResources().getColor(R.color.white_color));
                    secondTv.setTextColor(getResources().getColor(R.color.red_color_f93448));
                    secondV.setBackgroundColor(getResources().getColor(R.color.red_color_f93448));
                    openGPSSettings();
                }
                break;
                case R.id.sport_title_right_imageButton: {
                    if (searchLayout.getVisibility()==View.VISIBLE){
                        searchLayout.setVisibility(View.GONE);
                        leftTv.setVisibility(View.VISIBLE);
                        rightBt.setVisibility(View.VISIBLE);

                    }else {
                        searchLayout.setVisibility(View.VISIBLE);
                        leftTv.setVisibility(View.GONE);
                        rightBt.setVisibility(View.GONE);
                    }
                }
                break;
                case R.id.sport_search_title_tv: {
                    String searchStr=seartchEt.getText().toString();
                    if (TextUtils.isEmpty(searchStr)){
                        searchLayout.setVisibility(View.GONE);
                        leftTv.setVisibility(View.VISIBLE);
                        rightBt.setVisibility(View.VISIBLE);
                    }else {
                        seartchEt.setText("");
                        searchLayout.setVisibility(View.GONE);
                        leftTv.setVisibility(View.VISIBLE);
                        rightBt.setVisibility(View.VISIBLE);
                        Map<String,Object> map=new HashMap<String,Object>();
                        map.put(Constant.ID,searchStr);
                        SkipUtils.jumpForMap(context, ActivityListActivity.class, map,false);
                    }

                }
                break;
            }
        }
    };


    private final static int UPDATA_TAB_CODE = 0;
    private final static int UPDATA_HEAD_CODE = 1;
    private final static int UPDATA_DATA_CODE = 2;
    private final static int UPDATA_NFC_CODE = 3;
    private final static int UPDATA_OTHER_CODE = 4;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_TAB_CODE: {
                    refreshTabAdapter();
                }
                break;
                case UPDATA_HEAD_CODE: {
                    setAdvertisementData();
                }
                break;
                case UPDATA_DATA_CODE: {
                    refreshAdapter();
                }
                break;
                case UPDATA_NFC_CODE: {
                    refreshNAdapter();
                }
                break;
                case UPDATA_OTHER_CODE: {
                    refreshOtherAdapter();
                }
                break;
            }
        }
    };

    private void refreshTabAdapter() {
        if (tabList.size() > 0) {
            if (tabAdapter == null) {
                tabAdapter = new TabAdapter(context, tabList);
                eScrollView.setAdapter(tabAdapter);
            } else {
                tabAdapter.notifyDataSetChanged();
            }
        }
        eScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (tabList.size() > 0) {
                    for (int i = 0; i < tabList.size(); i++) {
                        if (i == position) {
                            tabList.get(i).setIsSelect("1");
                        } else {
                            tabList.get(i).setIsSelect("0");
                        }
                    }
                    tabAdapter.notifyDataSetChanged();
                    if (position == 0) {
                        viewType=0;
                        headLayout.setVisibility(View.VISIBLE);
                        aList.clear();
                        listView.setAdapter(new ActivityAdapter(context, aList, 0));
                        isRefresh = true;
                        if (isRefresh) {
                            aList.clear();
                            page = 1;
                            initData();
                        }
                    } else {
                        viewType=1;
                        headLayout.setVisibility(View.GONE);
                        typeId= tabList.get(position).getId();
                        aList.clear();
                        listView.setAdapter(new ActivityAdapter(context, aList, 0));
                        getTdata(typeId);
                    }
                }

            }
        });
    }

    private void analysisData(String body) {
        try {
            JSONObject jo = new JSONObject(body);
            String code = jo.optString(Constant.ERROR_CODE);
            if ("0".equals(code)) {
                JSONArray ja = jo.getJSONArray(Constant.ACTIVITYS);
                if (ja.length() > 0) {
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject middle = ja.getJSONObject(i);
                        ActivityData middleData = new ActivityData();
                        middleData.setId(middle.optString(Constant.ID));
                        middleData.setName(middle.optString(Constant.NAME));
                        middleData.setPicture(middle.optString(Constant.PICTURE));
                        middleData.setAge(middle.optString(Constant.AGE));
                        middleData.setMerchant(middle.optString(Constant.MERCHANT));
                        middleData.setCity(middle.optString(Constant.CITY));
                        middleData.setLat(middle.optString(Constant.LAT));
                        middleData.setLng(middle.optString(Constant.LNG));
                        middleData.setStart_time(middle.optString(Constant.START_TIME));
                        middleData.setEnd_time(middle.optString(Constant.END_TIME));
                        middleData.setLimit(middle.optString(Constant.LIMIT));
                        middleData.setPrice(middle.optString(Constant.PRICE));
                        middleData.setAddress(middle.optString(Constant.ADDRESS));
                        middleData.setContent(middle.optString(Constant.CONTENT));
                        aList.add(middleData);
                    }
                }
            } else {
                String msg = jo.optString(Constant.ERROR_MSG);
                T.show(context, msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refreshAdapter() {
        if (aList.size() > 0) {

            listView.setAdapter(new ActivityAdapter(context, aList, 0));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(Constant.ID, aList.get(i).getId());
                    SkipUtils.jumpForMap(context, ActivityDetailActivity.class, map, false);
                }
            });
            scrollView.smoothScrollTo(0, 0);
        }
    }

    private void refreshNAdapter() {
        if (aList.size() > 0) {
            listView.setAdapter(new ActivityAdapter(context, aList, 1));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(Constant.ID, aList.get(i).getId());
                    SkipUtils.jumpForMap(context, ActivityDetailActivity.class, map, false);
                }
            });
        } else {
            listView.setAdapter(new ActivityAdapter(context, aList, 1));
        }
        scrollView.smoothScrollTo(0, 0);
    }

    private void refreshOtherAdapter() {

        listView.setAdapter(new ActivityAdapter(context, aList, 0));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Constant.ID, aList.get(i).getId());
                SkipUtils.jumpForMap(context, ActivityDetailActivity.class, map, false);
            }
        });
    }


    @Override
    public void onFooterRefresh(PullToRefreshView view) {
//        isUpRefresh = true;
//        if (isUpRefresh) {
//            page = page + 1;
//            initData();
//        }
        pullToRefreshView.onFooterRefreshComplete();
    }

    @Override
    public void onHeaderRefresh(PullToRefreshView view) {
        isRefresh = true;
        if (isRefresh) {
            if (viewType==0){
                aList.clear();
                page = 1;
                initData();
            }else {
                headLayout.setVisibility(View.GONE);
                aList.clear();
                listView.setAdapter(new ActivityAdapter(context, aList, 0));
                getTdata(typeId);
            }
        }
    }

    private void dismissRefresh() {
        if (isRefresh) {
            pullToRefreshView.onHeaderRefreshComplete();
            isRefresh = false;
        } else if (isUpRefresh) {
            pullToRefreshView.onFooterRefreshComplete();
            isUpRefresh = false;
        }

    }

    private void openGPSSettings() {
        LocationManager alm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        } else {
            aList.clear();
            listView.setAdapter(new ActivityAdapter(context, aList, 1));
            T.show(context, "请开启GPS");
        }
    }

    private void getLocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(context);
        }
        mLocationClient.setLocationListener(mLocationListener);
        if (mLocationOption == null) {
            mLocationOption = new AMapLocationClientOption();
        }
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setWifiActiveScan(false);
        mLocationOption.setMockEnable(false);
        mLocationOption.setLocationProtocol(AMapLocationProtocol.HTTP);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setOnceLocationLatest(true);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    latitude = aMapLocation.getLatitude() + "";//获取纬度
                    longitude = aMapLocation.getLongitude() + "";//获取经度
                    String city = aMapLocation.getCity();//城市信息
                    leftTv.setText(city);
                    getNdata();
                } else {
                    T.show(context, "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
