package com.linkloving.dyh08.logic.UI.main;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.android.bluetoothlegatt.BLEHandler;
import com.example.android.bluetoothlegatt.BLEProvider;
import com.example.android.bluetoothlegatt.proltrol.dto.LPDeviceInfo;
import com.jaeger.library.StatusBarUtil;
import com.linkloving.band.dto.DaySynopic;
import com.linkloving.band.dto.SportRecord;
import com.linkloving.dyh08.BleService;
import com.linkloving.dyh08.CommParams;
import com.linkloving.dyh08.IntentFactory;
import com.linkloving.dyh08.MyApplication;
import com.linkloving.dyh08.R;
import com.linkloving.dyh08.basic.AppManager;
import com.linkloving.dyh08.db.sport.UserDeviceRecord;
import com.linkloving.dyh08.db.summary.DaySynopicTable;
import com.linkloving.dyh08.http.basic.CallServer;
import com.linkloving.dyh08.http.basic.HttpCallback;
import com.linkloving.dyh08.http.basic.NoHttpRuquestFactory;
import com.linkloving.dyh08.http.data.DataFromServer;
import com.linkloving.dyh08.logic.UI.device.DeviceActivity;
import com.linkloving.dyh08.logic.UI.device.FirmwareDTO;
import com.linkloving.dyh08.logic.UI.main.boundwatch.BoundActivity;
import com.linkloving.dyh08.logic.UI.main.materialmenu.Left_viewVO1;
import com.linkloving.dyh08.logic.UI.main.materialmenu.MenuNewAdapter;
import com.linkloving.dyh08.logic.UI.main.materialmenu.MenuVO;
import com.linkloving.dyh08.logic.UI.more.MoreActivity;
import com.linkloving.dyh08.logic.dto.SportRecordUploadDTO;
import com.linkloving.dyh08.logic.dto.UserEntity;
import com.linkloving.dyh08.prefrences.PreferencesToolkits;
import com.linkloving.dyh08.prefrences.devicebean.LocalInfoVO;
import com.linkloving.dyh08.utils.CommonUtils;
import com.linkloving.dyh08.utils.DateSwitcher;
import com.linkloving.dyh08.utils.ScreenUtils;
import com.linkloving.dyh08.utils.ToolKits;
import com.linkloving.dyh08.utils.logUtils.MyLog;
import com.linkloving.dyh08.utils.manager.AsyncTaskManger;
import com.linkloving.dyh08.utils.sportUtils.SportDataHelper;
import com.linkloving.utils.TimeZoneHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yolanda.nohttp.Response;
import com.zhy.autolayout.AutoLayoutActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
public class PortalActivity extends AutoLayoutActivity implements View.OnClickListener {
    private SimpleDateFormat sdf = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD);
    private static final String TAG = PortalActivity.class.getSimpleName();
    private static final int REQUSET_FOR_PERSONAL = 1;
    private static final int LOW_BATTERY = 3;
    private static final int JUMP_FRIEND_TAG_TWO = 2;
    ViewGroup contentLayout;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawer;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recycler_view)
    RecyclerView menu_RecyclerView;
    @InjectView(R.id.user_head)
    ImageView user_head;//头像
    @InjectView(R.id.user_name)
    TextView user_name;//昵称
    /*---------------------------------------------------------*/
    @InjectView(R.id.Rl_step)
    RelativeLayout stepLayout;
    @InjectView(R.id.Rl_calories)
    RelativeLayout caloriesLayout;
    @InjectView(R.id.Rl_sleep)
    RelativeLayout sleepLayout;
    @InjectView(R.id.Rl_distance)
    RelativeLayout distanceLayout;
    @InjectView(R.id.main_tv_hour)
    AppCompatTextView main_tv_hour;
    @InjectView(R.id.main_tv_day)
    AppCompatTextView main_tv_day;

    @InjectView(R.id.main_tv_Step)
    AppCompatTextView stepView;
    @InjectView(R.id.main_tv_Calories)
    AppCompatTextView calView;
    @InjectView(R.id.main_tv_Distance)
    AppCompatTextView distanceView;
    @InjectView(R.id.main_tv_Sleep)
    AppCompatTextView sleepView;


    /*--------------------------------------------------------*/
    /** 侧滑栏适配器*/
    private MenuNewAdapter menuAdapter;
    /** 计算运动数据的进度条*/
    private ProgressDialog progressDialog;
    /** 地理位置*/
    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;

    private String User_avatar_file_name;
    /** 用户封装类*/
    private UserEntity userEntity;
    /** 蓝牙以及蓝牙回调事件*/
    private BLEProvider provider;
    private BLEHandler.BLEProviderObserverAdapter bleProviderObserver;

    private Handler timeHandler = new Handler();

    private  FragmentTransaction transaction ;

    /**
     * 当前正在运行中的数据加载异步线程(放全局的目的是尽量控制当前只有一个在运行，防止用户恶意切换导致OOM)
     */
    private AsyncTask<Object, Object, DaySynopic> currentDataAsync = null;

    @Override
    protected void onPause() {
        super.onPause();
        if (provider.getBleProviderObserver() != null) {
            provider.setBleProviderObserver(null);
        }
    }

    @Override
    protected void onPostResume() {
        MyLog.e(TAG, "onPostResume()了");
        super.onPostResume();

        provider = BleService.getInstance(PortalActivity.this).getCurrentHandlerProvider();
        provider.setBleProviderObserver(bleProviderObserver);

        UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();

//        if (userEntity == null || userEntity.getDeviceEntity() == null || userEntity.getUserBase() == null)
//            return;
        MyLog.e(TAG, "u.getDeviceEntity().getDevice_type():" + userEntity.getDeviceEntity().getDevice_type());
        //去数据库查询所要显示的数据
        getInfoFromDB();
        //绑定了蓝牙就去同步蓝牙数据
        if (!CommonUtils.isStringEmpty(userEntity.getDeviceEntity().getLast_sync_device_id())) {
//            if (provider.isConnectedAndDiscovered())
                BleService.getInstance(PortalActivity.this).syncAllDeviceInfoAuto(PortalActivity.this, false, null);
        }

        if (userEntity.getUserBase().getUser_avatar_file_name() == null) {
            MyLog.e(TAG, "u.getUserBase().getUser_avatar_file_name()是空的........");
            return;
        }

        if (!userEntity.getUserBase().getUser_avatar_file_name().equals(User_avatar_file_name) || !userEntity.getUserBase().getNickname().equals(userEntity.getUserBase().getNickname())) {
            refreshHeadView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        provider.setBleProviderObserver(null);
        AppManager.getAppManager().removeActivity(this);
        // 如果有未执行完成的AsyncTask则强制退出之，否则线程执行时会空指针异常哦！！！
        AsyncTaskManger.getAsyncTaskManger().finishAllAsyncTask();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_activity_portal_main);
        AppManager.getAppManager().addActivity(this);
        ButterKnife.inject(this);
        contentLayout = (ViewGroup) findViewById(R.id.main);
        //获取本地用户类
        userEntity = MyApplication.getInstance(this).getLocalUserInfoProvider();

        //绑定蓝牙事件START
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        bleProviderObserver = new BLEProviderObserverAdapterImpl();
        provider.setBleProviderObserver(bleProviderObserver);
        //绑定蓝牙事件OVER

        // 系统到本界面中，应该已经完成准备好了，开启在网络连上事件时自动启动同步线程的开关吧
        MyApplication.getInstance(this).setPreparedForOfflineSyncToServer(true);
        //初始化百度地图
        initLocation();
        //初始化UI
        initView();
        initListener();
        //开始定位
        mLocationClient.start();
        /*--------------------------------*/


        /*-------------------日历----------------*/

}

    /*-----------------------------------------------------------------------*/

    @OnClick(R.id.Rl_step)
    void stepLayout(View view) {
        IntentFactory.start_step(PortalActivity.this);
    }

    @OnClick(R.id.Rl_calories)
    void caloriesLayout(View view) {
       IntentFactory.start_calories(PortalActivity.this);
    }

    @OnClick(R.id.Rl_distance)
    void distanceLayout(View view){
        IntentFactory.start_distance(PortalActivity.this);
    }
    @OnClick(R.id.Rl_sleep)
    void sleepLayout(View view ){
        IntentFactory.start_sleep(PortalActivity.this);
    }


    @Override
    public void onClick(View v) {


       /* switch (v.getId()){
            case R.id.iv_left:

                monthDateView.onLeftClick();
            case R.id.iv_right:

                monthDateView.onRightClick();
        }*/
    }





    private void initLocation() {
        mLocationClient = new LocationClient(PortalActivity.this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02 bd09ll bd09
        option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1ms
        option.setIsNeedAddress(true); // 返回地址
        mLocationClient.setLocOption(option);
    }

    private void initView() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.general_loading));
        toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        Rect outRect = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        setSupportActionBar(toolbar);
        //隐藏title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        menu_RecyclerView.setLayoutManager(layoutManager);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.END);
        //无阴影
        drawer.setScrimColor(Color.TRANSPARENT);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        contentLayout.setBackgroundDrawable(getResources().getDrawable(R.mipmap.header));
        StatusBarUtil.setTranslucentForDrawerLayout(this, drawer, 0);
        ScreenUtils.setMargins(toolbar, 0, ScreenUtils.getStatusHeight(this), 0, 0);

        //侧边栏适配器
        setAdapter();
        refreshHeadView();
        //更新日历 1s一次
        updateTimeThread();

    }


/*--------------------------------Daniel-----------------------------------*/





    /*--------------------------------------*/


    private void initListener() {
        user_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入个人信息.详细页面
//                Intent intent = IntentFactory.create_PersonalInfo_Activity_Intent(PortalActivity.this);
//                startActivityForResult(intent, REQUSET_FOR_PERSONAL);
            }
        });


    }

    /**
     * 实现GPS定位回调监听。
     */
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (PortalActivity.this != null) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                String city = location.getCity();
                MyLog.e("city",city);
                UserEntity userEntity = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
                if (userEntity.getUserBase() == null) {
                    return;
                }
                userEntity.getUserBase().setLongitude(longitude + "");
                userEntity.getUserBase().setLatitude(latitude + "");
                mLocationClient.stop();
            } else {
                mLocationClient.stop();
            }
        }
    }

    /**
     * 刷新用户头像和昵称
     */
    private void refreshHeadView() {
        MyLog.i(TAG, "刷新头像和昵称,等数据");
        //图像以后设置
        UserEntity u = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider();
        if (u == null) {
            MyLog.i(TAG, "获得的UserEntity是空的");
            return;
        }
        MyLog.i(TAG, "获得的UserEntity的名字=" + u.getUserBase().getNickname());
        user_name.setText(u.getUserBase().getNickname());
        User_avatar_file_name = u.getUserBase().getUser_avatar_file_name();
        if (User_avatar_file_name != null) {
            String url = NoHttpRuquestFactory.getUserAvatarDownloadURL(PortalActivity.this, u.getUser_id() + "", u.getUserBase().getUser_avatar_file_name(), true);
            User_avatar_file_name = u.getUserBase().getUser_avatar_file_name();
            DisplayImageOptions options;
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                    .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                    .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                    .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                    .showImageOnFail(R.mipmap.default_avatar)//加载失败显示图片
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                    .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                    .build();//构建完成

            ImageLoader.getInstance().displayImage(url, user_head, options, new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ImageView mhead = (ImageView) view;
                    mhead.setImageBitmap(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }

    }


    /**
     * 侧滑栏适配器
     */
    private void setAdapter() {
        List<MenuVO> list = new ArrayList<MenuVO>();
        for (int i = 0; i < Left_viewVO1.menuIcon.length; i++) {
            MenuVO vo = new MenuVO();
            vo.setImgID(Left_viewVO1.menuIcon[i]);
            vo.setTextID(Left_viewVO1.menuText[i]);
            list.add(vo);
        }
        menuAdapter = new MenuNewAdapter(this, list);
        menuAdapter.setOnRecyclerViewListener(new MenuNewAdapter.OnRecyclerViewListener() {
            //侧边栏点击事件 可以在这里复写 暂时没用到
            @Override
            public void onItemClick(int position) {

            }
        });
        menu_RecyclerView.setAdapter(menuAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            MoreActivity.ExitApp(this);
        }
    }


    //刷新手表电量  + 获取存在数据库的数据
    private void getInfoFromDB() {
        //子线程去计算汇总数据
        MyLog.e(TAG, "====================开始执行异步任务====================");
        AsyncTask<Object, Object, DaySynopic> dataAsyncTask = new AsyncTask<Object, Object, DaySynopic>() {
            String todyaStr;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                if (progressDialog != null && !progressDialog.isShowing())
//                    progressDialog.show();
            }

            @Override
            protected DaySynopic doInBackground(Object... params) {
                    DaySynopic mDaySynopic = null;
                    todyaStr = new SimpleDateFormat(ToolKits.DATE_FORMAT_YYYY_MM_DD).format(new Date());
                    ArrayList<DaySynopic> mDaySynopicArrayList = new ArrayList<DaySynopic>();
                    MyLog.e(TAG, "endDateString:" + todyaStr);
                    //今天的话 无条件去汇总查询
                    mDaySynopic = SportDataHelper.offlineReadMultiDaySleepDataToServer(PortalActivity.this, todyaStr, todyaStr);
                    if (mDaySynopic.getTime_zone() == null) {
                        return null;
                    }
                    MyLog.e(TAG, "daySynopic:" + mDaySynopic.toString());
                    mDaySynopicArrayList.add(mDaySynopic);
                    DaySynopicTable.saveToSqliteAsync(PortalActivity.this, mDaySynopicArrayList, userEntity.getUser_id() + "");
                    return mDaySynopic;
            }

            @Override
            protected void onPostExecute(DaySynopic mDaySynopic) {
                super.onPostExecute(mDaySynopic);
                //=============计算基础卡路里=====START========//
//                if (timeNow.equals(sdf.format(new Date()))) {
//                    int hour = Integer.parseInt(new SimpleDateFormat("HH").format(new Date()));
//                    int minute = Integer.parseInt(new SimpleDateFormat("mm").format(new Date()));
//                    cal_base = (int) ((hour * 60 + minute) * 1.15);//当前时间今天的卡路里
//                } else {
//                    cal_base = 1656;
//                }
//                //=============计算基础卡路里=====OVER========//
                if (mDaySynopic == null) {
                    MyLog.e(TAG, "mDaySynopic空的");
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    AsyncTaskManger.getAsyncTaskManger().removeAsyncTask(this);
                    return;
                }
                //daySynopic:[data_date=2016-04-14,data_date2=null,time_zone=480,record_id=null,user_id=null,run_duration=1.0,run_step=68.0,run_distance=98.0
                // ,create_time=null,work_duration=178.0,work_step=6965.0,work_distance=5074.0,sleepMinute=2.0916666984558105,deepSleepMiute=1.25 gotoBedTime=1460645100 getupTime=1460657160]
                //走路 步数
                int walkStep = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getWork_step()), 0));
                //跑步 步数
                int runStep = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getRun_step()), 0));
                int step = walkStep + runStep;
                /****************今天的步数给到 方便OAD完成后回填步数 的变量里面去****************/
                MyApplication.getInstance(PortalActivity.this).setOld_step(step);
                stepView.setText(step+"");

                //走路 里程
                int walkDistance = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getWork_distance()), 0));
                //跑步 里程
                int runDistance = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getRun_distance()), 0));
                int distance = walkDistance + runDistance;
              float  distanceKm = (float)distance/1000 ;
                String distancekm = DateSwitcher.oneFloat(distanceKm);
                distanceView.setText(distancekm+"");

                //浅睡 小时
                double lightSleepHour = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getSleepMinute()), 1);
                //深睡 小时
                double deepSleepHour = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getDeepSleepMiute()), 1);
                double sleepTime = CommonUtils.getScaledDoubleValue(lightSleepHour + deepSleepHour, 1);
                sleepView.setText(sleepTime+"");

                //走路 分钟
                double walktime = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getWork_duration()), 1);
                //跑步 分钟
                double runtime = CommonUtils.getScaledDoubleValue(Double.valueOf(mDaySynopic.getRun_duration()), 1);

                double worktime = CommonUtils.getScaledDoubleValue(walktime + runtime, 1);

                int walkCal = ToolKits.calculateCalories(Float.parseFloat(mDaySynopic.getWork_distance()), (int) walktime * 60, userEntity.getUserBase().getUser_weight());
                int runCal = ToolKits.calculateCalories(Float.parseFloat(mDaySynopic.getRun_distance()), (int) runtime * 60, userEntity.getUserBase().getUser_weight());
                int calValue = walkCal + runCal;
                calView.setText(calValue+"");

                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                AsyncTaskManger.getAsyncTaskManger().removeAsyncTask(this);
            }
        };
        // 确保当前只有一个AsyncTask在运行，否则用户恶心切换会OOM
        if (currentDataAsync != null){
            AsyncTaskManger.getAsyncTaskManger().removeAsyncTask(currentDataAsync, true);
        }
        AsyncTaskManger.getAsyncTaskManger().addAsyncTask(currentDataAsync = dataAsyncTask);
        dataAsyncTask.execute();
    }


    /**
     * 更新时钟上的UI显示
     */
    private void updateTimeThread() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        timeHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String timeWithStr = CommonUtils.getTimeWithStrNoS(":");
                                String dateWithStr = CommonUtils.getDateWithStr();
                                main_tv_hour.setText(timeWithStr);
                                main_tv_day.setText(dateWithStr);
                            }
                        });
                        sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BleService.REQUEST_ENABLE_BT) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED: //用户取消打开蓝牙
                    BleService.setNEED_SCAN(false);
                    break;

                case Activity.RESULT_OK:       //用户打开蓝牙
                    Log.e(TAG, "//用户打开蓝牙");
                    BleService.setNEED_SCAN(true);
                    provider.scanForConnnecteAndDiscovery();
                    break;

                default:
                    break;
            }
            return;
        } else if (requestCode == CommParams.REQUEST_CODE_BOUND && resultCode == Activity.RESULT_OK) {
            String type = data.getStringExtra(BundTypeActivity.KEY_TYPE);
            if (type.equals(BundTypeActivity.KEY_TYPE_WATCH)) {
                startActivityForResult(new Intent(PortalActivity.this, BoundActivity.class), CommParams.REQUEST_CODE_BOUND_WATCH);
            } else if (type.equals(BundTypeActivity.KEY_TYPE_BAND)) {
                startActivityForResult(IntentFactory.startActivityBundBand(PortalActivity.this), CommParams.REQUEST_CODE_BOUND_BAND);
            }
        } else if (requestCode == CommParams.REQUEST_CODE_BOUND_BAND && resultCode == Activity.RESULT_OK) {
            MyLog.e(TAG, "手环绑定成功");
        } else if (requestCode == CommParams.REQUEST_CODE_BOUND_WATCH && resultCode == Activity.RESULT_OK) {
        }
    }

    /**
     * 提示绑定的弹出框
     */
    private void showBundDialog() {
        // 您还未绑定 请您绑定一个设备
        AlertDialog dialog = new AlertDialog.Builder(PortalActivity.this)
                .setTitle(ToolKits.getStringbyId(PortalActivity.this, R.string.portal_main_unbound))
                .setMessage(ToolKits.getStringbyId(PortalActivity.this, R.string.portal_main_unbound_msg))
                .setPositiveButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                IntentFactory.startBundTypeActivity(PortalActivity.this);
                            }
                        })
                .setNegativeButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_cancel), null)
                .create();
        dialog.show();
    }

    /**
     * 蓝牙观察者实现类.
     */
    private class BLEProviderObserverAdapterImpl extends BLEHandler.BLEProviderObserverAdapter {

        @Override
        protected Activity getActivity() {
            return PortalActivity.this;
        }

        /**********
         * 用户没打开蓝牙
         *********/
        @Override
        public void updateFor_handleNotEnableMsg() {
            //用户未打开蓝牙
            Log.i(TAG, "updateFor_handleNotEnableMsg");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getActivity().startActivityForResult(enableBtIntent, BleService.REQUEST_ENABLE_BT);
        }

        @Override
        public void updateFor_handleUserErrorMsg(int id) {
            MyLog.e(TAG, "updateFor_handleConnectSuccessMsg");
        }

        /**********
         * BLE连接中
         *********/
        @Override
        public void updateFor_handleConnecting() {
            //正在连接
            MyLog.e(TAG, "updateFor_handleConnecting");
        }

        /**********
         * 扫描BLE设备TimeOut
         *********/
        @Override
        public void updateFor_handleScanTimeOutMsg() {
            MyLog.e(TAG, "updateFor_handleScanTimeOutMsg");
        }

        /**********
         * BLE连接失败
         *********/
        @Override
        public void updateFor_handleConnectFailedMsg() {
            //连接失败
            MyLog.e(TAG, "updateFor_handleConnectFailedMsg");
        }

        /**********
         * BLE连接成功
         *********/
        @Override
        public void updateFor_handleConnectSuccessMsg() {
            //连接成功
            MyLog.e(TAG, "updateFor_handleConnectSuccessMsg");
        }

        /**********
         * BLE断开连接
         *********/
        @Override
        public void updateFor_handleConnectLostMsg() {
            MyLog.e(TAG, "updateFor_handleConnectLostMsg");
            //蓝牙断开的显示
        }

        /**********
         * 0X13命令返回
         *********/
        @Override
        public void updateFor_notifyFor0x13ExecSucess_D(LPDeviceInfo latestDeviceInfo) {
            MyLog.e(TAG, "updateFor_notifyFor0x13ExecSucess_D");
        }

        @Override
        public void updateFor_notifyForDeviceUnboundSucess_D() {
            MyLog.e(TAG, "updateFor_notifyForDeviceUnboundSucess_D");
        }

        /**********
         * 剩余同步运动条目
         *********/
        @Override
        public void updateFor_SportDataProcess(Integer obj) {
            super.updateFor_SportDataProcess(obj);
            MyLog.e(TAG, "updateFor_SportDataProcess");
        }

        /**********
         * 运动记录读取完成
         *********/
        @Override
        public void updateFor_handleDataEnd() {
            MyLog.e(TAG, " updateFor_handleDataEnd ");
            //把数据库未同步到server的数据提交上去
            if (ToolKits.isNetworkConnected(PortalActivity.this)) {
                new AsyncTask<Object, Object, SportRecordUploadDTO>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected SportRecordUploadDTO doInBackground(Object... params) {
                        // 看看数据库中有多少未同步（到服务端的数据）
                        final ArrayList<SportRecord> up_List = UserDeviceRecord.findHistoryWitchNoSync(PortalActivity.this, MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id() + "");
                        MyLog.e(TAG, "【NEW离线数据同步】一共查询出" + up_List.size() + "条数据");
                        //有数据才去算
                        if (up_List.size() > 0) {
                            SportRecordUploadDTO sportRecordUploadDTO = new SportRecordUploadDTO();
                            final String startTime = up_List.get(0).getStart_time();
                            final String endTime = up_List.get(up_List.size() - 1).getStart_time();
                            sportRecordUploadDTO.setDevice_id("1");
                            sportRecordUploadDTO.setUtc_time("1");
                            sportRecordUploadDTO.setOffset(TimeZoneHelper.getTimeZoneOffsetMinute() + "");
                            sportRecordUploadDTO.setUser_id(MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id());
                            sportRecordUploadDTO.setList(up_List);
//                            HttpUtils.doPostAsyn(CommParams.SERVER_CONTROLLER_URL_NEW, HttpHelper.sport2Server(sportRecordUploadDTO), new HttpUtils.CallBack() {
//                                @Override
//                                public void onRequestComplete(String result) {
//                                    MyLog.e(TAG, "【NEW离线数据同步】服务端返回" + result);
//                                    long sychedNum = UserDeviceRecord.updateForSynced(PortalActivity.this, MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getUser_id() + "", startTime, endTime);
//                                    MyLog.d(TAG, "【NEW离线数据同步】本次共有" + sychedNum + "条运动数据已被标识为\"已同步\"！[" + startTime + "~" + endTime + "]");
//                                }
//                            });
                            return sportRecordUploadDTO;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(SportRecordUploadDTO sportRecordUploadDTO) {
                        super.onPostExecute(sportRecordUploadDTO);
                    }
                }.execute();
            }
            //运动数据同步完毕 去UI上显示同步完毕的结果
            getInfoFromDB();
        }

        /**********
         * 消息提醒设置成功
         *********/
        @Override
        public void updateFor_notify() {
            super.updateFor_notify();
            MyLog.e(TAG, "消息提醒设置成功！");
        }

        @Override
        public void updateFor_notifyForModelName(LPDeviceInfo latestDeviceInfo) {
            super.updateFor_notifyForModelName(latestDeviceInfo);
            if (latestDeviceInfo.modelName == null)
                latestDeviceInfo.modelName = "LW100";
            MyLog.e(TAG, "modelName:" + latestDeviceInfo.modelName);
            MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().setModel_name(latestDeviceInfo.modelName);
//            if((System.currentTimeMillis()/1000)-PreferencesToolkits.getOADUpdateTime(getActivity())>86400)
            {
                // 查询是否要更新固件
                final LocalInfoVO vo = PreferencesToolkits.getLocalDeviceInfo(PortalActivity.this);
                FirmwareDTO firmwareDTO = new FirmwareDTO();
                int deviceType = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type();
                if (deviceType == MyApplication.DEVICE_BAND || deviceType == MyApplication.DEVICE_BAND - 1) {
                    deviceType = 1;
                } else {
                    deviceType = MyApplication.getInstance(PortalActivity.this).getLocalUserInfoProvider().getDeviceEntity().getDevice_type();
                }
                firmwareDTO.setDevice_type(deviceType);
                firmwareDTO.setFirmware_type(DeviceActivity.DEVICE_VERSION_TYPE);
                int version_int = ToolKits.makeShort(vo.version_byte[1], vo.version_byte[0]);
                firmwareDTO.setVersion_int(version_int + "");
                firmwareDTO.setModel_name(latestDeviceInfo.modelName);
                if (MyApplication.getInstance(PortalActivity.this).isLocalDeviceNetworkOk()) {
                    //请求网络
                    CallServer.getRequestInstance().add(PortalActivity.this, false, CommParams.HTTP_OAD, NoHttpRuquestFactory.create_OAD_Request(firmwareDTO), new HttpCallback<String>() {
                        @Override
                        public void onSucceed(int what, Response<String> response) {
                            DataFromServer dataFromServer = JSON.parseObject(response.get(), DataFromServer.class);
                            String value = dataFromServer.getReturnValue().toString();
                            if (!CommonUtils.isStringEmpty(response.get())) {
                                if (dataFromServer.getErrorCode() != 10020) {
                                    JSONObject object = JSON.parseObject(value);
                                    String version_code = object.getString("version_code");
                                    if (Integer.parseInt(version_code, 16) > Integer.parseInt(vo.version, 16)) {
                                        PreferencesToolkits.setOADUpdateTime(PortalActivity.this);
                                        AlertDialog dialog = new AlertDialog.Builder(PortalActivity.this)
                                                .setTitle(ToolKits.getStringbyId(PortalActivity.this, R.string.general_tip))
                                                .setMessage(ToolKits.getStringbyId(PortalActivity.this, R.string.bracelet_oad_Portal))
                                                .setPositiveButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_ok),
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                startActivity(IntentFactory.star_DeviceActivityIntent(PortalActivity.this, DeviceActivity.DEVICE_UPDATE));
                                                            }
                                                        })
                                                .setNegativeButton(ToolKits.getStringbyId(PortalActivity.this, R.string.general_cancel), null)
                                                .create();
                                        dialog.show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailed(int what, String url, Object tag, CharSequence message, int responseCode, long networkMillis) {

                        }
                    });
                }
            }
        }

        /**********
         * 闹钟提醒设置成功
         *********/
        @Override
        public void updateFor_notifyForSetClockSucess() {
            super.updateFor_notifyForSetClockSucess();
            MyLog.e(TAG, "updateFor_notifyForSetClockSucess！");
        }

        /**********
         * 久坐提醒设置成功
         *********/
        @Override
        public void updateFor_notifyForLongSitSucess() {
            super.updateFor_notifyForLongSitSucess();
            MyLog.e(TAG, "updateFor_notifyForLongSitSucess！");
        }

        /**********
         * 身体信息(激活设备)设置成功
         *********/
        @Override
        public void updateFor_notifyForSetBodySucess() {
            MyLog.e(TAG, "updateFor_notifyForSetBodySucess");
        }

        /**********
         * 设置时间失败
         *********/
        @Override
        public void updateFor_handleSetTimeFail() {
            MyLog.e(TAG, "updateFor_handleSetTimeFail");
            super.updateFor_handleSetTimeFail();
        }

        /**********
         * 设置时间成功
         *********/
        @Override
        public void updateFor_handleSetTime() {
            MyLog.e(TAG, "updateFor_handleSetTime");
        }

        /***********
         * 获取设备ID=
         *********/
        @Override
        public void updateFor_getDeviceId(String obj) {
            super.updateFor_getDeviceId(obj);
            MyLog.e(TAG, "读到的deviceid:" + obj);
        }

        /**********
         * 卡号读取成功
         *********/
        @Override
        public void updateFor_CardNumber(String cardId) {
            MyLog.e(TAG, "updateFor_CardNumber：" + cardId);
            super.updateFor_CardNumber(cardId);
        }
    }

}
