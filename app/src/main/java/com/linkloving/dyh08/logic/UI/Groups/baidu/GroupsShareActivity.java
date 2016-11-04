package com.linkloving.dyh08.logic.UI.Groups.baidu;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.example.android.bluetoothlegatt.utils.ToastUtil;
import com.linkloving.band.dto.SportRecord;
import com.linkloving.dyh08.MyApplication;
import com.linkloving.dyh08.R;
import com.linkloving.dyh08.basic.toolbar.ToolBarActivity;
import com.linkloving.dyh08.db.sport.UserDeviceRecord;
import com.linkloving.dyh08.logic.UI.workout.Greendao.TraceGreendao;
import com.linkloving.dyh08.utils.CommonUtils;
import com.linkloving.dyh08.utils.ToolKits;
import com.linkloving.dyh08.utils.logUtils.MyLog;
import com.linkloving.utils.TimeZoneHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import Trace.GreenDao.DaoMaster;
import Trace.GreenDao.Note;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.twitter.Twitter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Daniel.Xu on 2016/8/22.
 */
public class GroupsShareActivity extends ToolBarActivity {
    private static final String TAG = GroupsShareActivity.class.getSimpleName();
    @InjectView(R.id.gourps_topmap)
    MapView gourpsTopmap;
    @InjectView(R.id.groups_time)
    TextView groupsTime;
    @InjectView(R.id.groups_tv_Step)
    AppCompatTextView groupsTvStep;
    @InjectView(R.id.groups_tv_Calories)
    AppCompatTextView groupsTvCalories;
    @InjectView(R.id.groups_tv_Distance)
    AppCompatTextView groupsTvDistance;
    @InjectView(R.id.main_tv_Duration)
    AppCompatTextView groupsTvDuration;
    private static Overlay overlay = null;
    //    @InjectView(R.id.twitter_share)
//    ImageView twitterShare;
//    @InjectView(R.id.instagram_share)
//    ImageView instagramShare;
//    @InjectView(R.id.fb_share)
//    ImageView fbShare;
//    @InjectView(R.id.google_share)
//    ImageView googleShare;
    @InjectView(R.id.shareET)
    EditText shareText;
    @InjectView(R.id.shareWhere)
    EditText shareWhereText;
    @InjectView(R.id.sharebutton)
    Button shareButton;
    @InjectView(R.id.screenhot)
    LinearLayout screenhot;

    private int user_id;
    private static BaiduMap map = null;
    private SQLiteDatabase db;
    private DaoMaster.DevOpenHelper devOpenHelper;
    private List<Note> startTimeList;
    private List<Note> endTimeList;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private TraceGreendao traGreendao;
    private Integer position;
    private ArrayList<SportRecord> sportRecordArrayList;
    private Date startDate;
    private Date endDate;
    private float distance = 0;
    private int step = 0;
    private int calories = 0;
    private long itemDuration;
    private String duration;
    protected static MapStatusUpdate msUpdate = null;
    private static PolylineOptions polyline = null;
    protected static OverlayOptions overlayOptions;
    private static BitmapDescriptor realtimeBitmap;
    private static List<LatLng> pointList = new ArrayList<LatLng>();
    private String filePathCache = "/sdcard/ranking_v111.png";
    private String filePathCacheUnder = "/sdcard/ranking_v222.png";
    private String filePathCacheTotal = "/sdcard/ranking_v333.png";
    private LocationClient mlocationClient;
    private MyLocationListener mMyLocationListener;
    private ImageView fb;
    private ImageView twitter;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tw_groups_share);
        view = LayoutInflater.from(GroupsShareActivity.this).inflate(R.layout.tw_share_day, null);

        ButterKnife.inject(this);

        ShareSDK.initSDK(GroupsShareActivity.this);
        user_id = MyApplication.getInstance(GroupsShareActivity.this).getLocalUserInfoProvider().getUser_id();
        map = gourpsTopmap.getMap();
        Intent intent = getIntent();
        String postionStr = intent.getStringExtra("postion");
        position = Integer.valueOf(postionStr);
        devOpenHelper = new DaoMaster.DevOpenHelper(GroupsShareActivity.this, "Note", null);
        db = devOpenHelper.getReadableDatabase();
        traGreendao = new TraceGreendao(GroupsShareActivity.this, db);
        startTimeList = traGreendao.searchAllStarttime();
        endTimeList = traGreendao.searchAllEndTime();
        startDate = startTimeList.get(position).getStartDate();
        endDate = endTimeList.get(position).getStartDate();
        List<Note> lists = traGreendao.searchLocation(startDate, endDate);
        String durtion = getDurtion(position);
        groupsTvDuration.setText(durtion);
        groupsTvDistance.setText(getDistanceKM() + "");

        for (int i = 0; i < lists.size(); i++) {
            Date runDate = lists.get(i).getRunDate();
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String format1 = simpleDateFormat1.format(runDate);
            showRealtimeTrack(lists.get(i).getLatitude(), lists.get(i).getLongitude());
        }
        groupsTvStep.setText(getStep() + "");
        groupsTime.setText(getMiddleTime());
        initPopupWindow();


    }

    @OnClick(R.id.earthImageview)
    void setLocation(View view) {
        MyLog.e("方法执行了", "执行了");
        mlocationClient = new LocationClient(GroupsShareActivity.this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mlocationClient.registerLocationListener(mMyLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02 bd09ll bd09
        option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1ms
        option.setIsNeedAddress(true); // 返回地址
        mlocationClient.setLocOption(option);
        mlocationClient.start();


    }


    /**
     * 保存百度mapview的图片
     *
     * @param mBaiduMap
     */
    public void getScreenHot(BaiduMap mBaiduMap) {
        // 截图，在SnapshotReadyCallback中保存图片到 sd 卡
        mBaiduMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
            public void onSnapshotReady(Bitmap snapshot) {
                File file = new File(filePathCache);
                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    if (snapshot.compress(
                            Bitmap.CompressFormat.PNG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                    Toast.makeText(GroupsShareActivity.this,
                            "屏幕截图成功，图片存在: " + file.toString(),
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Toast.makeText(GroupsShareActivity.this, "正在截取屏幕图片...",
                Toast.LENGTH_SHORT).show();
    }


    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            String city = location.getCity();
            shareWhereText.setText(city);
            MyLog.e("city", city);
        }
    }


    private void initPopupWindow() {
        final View popupView = LayoutInflater.from(GroupsShareActivity.this).inflate(R.layout.tw_share_popuwindow, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(popupView);
        fb = (ImageView) popupView.findViewById(R.id.fb);
        twitter = (ImageView) popupView.findViewById(R.id.twitter);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffffffff));
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
//                ToolKits.getScreenHot(MapActivity.this.getWindow().getDecorView(), filePathCache);
//               截图分两步
                /**把下面的部分截图*/
                ToolKits.saveFile(ToolKits.getViewBitmap(screenhot), filePathCacheUnder);
                /**百度截图*/
                ToolKits.getScreenHot(map,filePathCache);
                final Bitmap bitmap = ToolKits.mergeBitmap_TB(BitmapFactory.decodeFile(filePathCache),
                        BitmapFactory.decodeFile(filePathCacheUnder), true);
                ToolKits.saveBitmap2file(bitmap, filePathCacheTotal);

            }
        });
        share();
    }


    private boolean saveBitmap2file(Bitmap bmp, String filename) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bmp.compress(format, quality, stream);
    }

    private void share() {
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Facebook.ShareParams shareParams = new Facebook.ShareParams();
//                shareParams.setFilePath(filePathCache);
                shareParams.setImagePath(filePathCacheTotal);
                Platform facebook = ShareSDK.getPlatform(Facebook.NAME);
                facebook.share(shareParams);
                boolean b = facebook.hasShareCallback();
                facebook.setPlatformActionListener(new PlatformActionListener() {
                    @Override
                    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {

                    }

                    @Override
                    public void onError(Platform platform, int i, Throwable throwable) {
                        MyLog.e(TAG,throwable.toString()+"-----------Error");
                    }

                    @Override
                    public void onCancel(Platform platform, int i) {

                    }
                });
                showToast(b);
            }
        });
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Twitter.ShareParams shareParams = new Twitter.ShareParams();
//                shareParams.setFilePath(filePathCache);
                shareParams.setImagePath(filePathCache);
                Platform platform = ShareSDK.getPlatform(Twitter.NAME);
                platform.share(shareParams);
                boolean b = platform.hasShareCallback();
                showToast(b);
            }
        });
    }

    private void showToast(boolean b) {
        if (b) {
            ToastUtil.showMyToast(GroupsShareActivity.this, "success");
        } else {
            ToastUtil.showMyToast(GroupsShareActivity.this, "failed");
        }

    }


/*
// 四个分享按钮的点击事件
   @OnClick(R.id.fb_share)
   void fbShare(View view){
       ToolKits.getScreenHot(GroupsShareActivity.this.getWindow().getDecorView(), filePathCache);
       Facebook.ShareParams shareParams = new Facebook.ShareParams();
       String  shareWord = shareText.getText().toString();
       shareParams.setText(shareWord);
       shareParams.setFilePath(filePathCache);
       shareParams.setImagePath(filePathCache);
       Platform facebook = ShareSDK.getPlatform(Facebook.NAME);
        facebook.share(shareParams);
   }

    @OnClick(R.id.twitter_share)
    void setTwitterShare(View view)
    {
        ToolKits.getScreenHot(GroupsShareActivity.this.getWindow().getDecorView(), filePathCache);
        Twitter.ShareParams shareParams = new Twitter.ShareParams();
        String  shareWord = shareText.getText().toString();
        shareParams.setText(shareWord);
        shareParams.setFilePath(filePathCache);
        shareParams.setImagePath(filePathCache);
        Platform platform = ShareSDK.getPlatform(Twitter.NAME);
        platform.share(shareParams);
    }

    @OnClick(R.id.instagram_share)
    void setInstagramShare(View view)
    {
        ToolKits.getScreenHot(GroupsShareActivity.this.getWindow().getDecorView(), filePathCache);
        Instagram.ShareParams shareParams = new Instagram.ShareParams();
        final String  shareWord = shareText.getText().toString();
        shareParams.setText(shareWord);
        shareParams.setFilePath(filePathCache);
        shareParams.setImagePath(filePathCache);
        Platform instagram = ShareSDK.getPlatform(Instagram.NAME);
        instagram.share(shareParams);

        instagram.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                if (throwable.toString().contains("ClientNotExistException")) {
                    MyLog.e("throwable", "执行了");
                    Looper.prepare();
                    Toast.makeText(GroupsShareActivity.this, "请安装Instagram客户端或更新到最新版本", Toast.LENGTH_SHORT).show();
                    Looper.loop();

                }
                throwable.printStackTrace();
            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        });

    }
    @OnClick(R.id.google_share)
    void setGoogleShare(View view)
    {
        ToolKits.getScreenHot(GroupsShareActivity.this.getWindow().getDecorView(), filePathCache);
        GooglePlus.ShareParams shareParams = new GooglePlus.ShareParams();
        String  shareWord = shareText.getText().toString();
        if ("".equals(shareWord)){
            shareWord=".";
        }
        shareParams.setText(shareWord);
        shareParams.setFilePath(filePathCache);
        shareParams.setImagePath(filePathCache);
        Platform google = ShareSDK.getPlatform(GooglePlus.NAME);
        google.share(shareParams);
        google.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {

            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                    throwable.printStackTrace();
            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        });
    }
*/


    private String getMiddleTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd a.HH:MM", Locale.US);
        String middleTime = simpleDateFormat.format(startDate);
        return middleTime;
    }

    private int getStep() {
        if (sportRecordArrayList.size() == 0) {
            step = 0;
        } else {
            for (SportRecord sportRecordArray : sportRecordArrayList) {
                MyLog.e(TAG, "step" + sportRecordArray.getStep());
                step = Integer.parseInt(sportRecordArray.getStep()) + step;
            }
        }
        return step;
    }

    private int getCalories() {
        if (sportRecordArrayList.size() == 0) {
            calories = 0;
        } else {
            for (SportRecord sportRecordArray : sportRecordArrayList) {
                MyLog.e(TAG, "calories" + sportRecordArray.getDistance());

            }
        }
        return step;
    }

    public String getDurtion(int position) {
        long endTime = endDate.getTime();
        long startTime = startDate.getTime();
//        时间间隔转UTC
        itemDuration = Math.abs(endTime - startTime);
        MyLog.e(TAG, itemDuration + "itemDuration");
        Date date = new Date(itemDuration);
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.MINUTE, -TimeZoneHelper.getTimeZoneOffsetMinute());
        Date time = instance.getTime();
        duration = simpleDateFormat.format(time);
        return duration;
    }


    private String getDistanceKM() {

        sportRecordArrayList = UserDeviceRecord.findHistoryChartwithHMS
                (GroupsShareActivity.this, String.valueOf(user_id), startDate, endDate);
//        MyLog.e(TAG,"sportRecordArrayList"+sportRecordArrayList.get(0).toString());
        if (sportRecordArrayList.size() == 0) {
            distance = 0;
        } else {
            for (SportRecord sportRecordArray : sportRecordArrayList) {
                MyLog.e(TAG, "distance" + sportRecordArray.getDistance());
                distance = Integer.parseInt(sportRecordArray.getDistance()) + distance;
            }
        }
//        这一步吧distance转换成如5.3 的用来加km
        MyLog.e("distance", distance + "");
        double distanceKM = CommonUtils.getDoubleValue(distance / 1000, 1);
        return new Formatter().format("%.1f", distanceKM).toString();
    }


    protected void showRealtimeTrack(double latitude, double longitude) {
        MyLog.e(TAG, latitude + "==========" + longitude);
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            Toast.makeText(GroupsShareActivity.this, "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(latitude, longitude);
            LatLng sourceLatLng = latLng;
            CoordinateConverter converter = new
                    CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(sourceLatLng);
            latLng = converter.convert();
            pointList.add(latLng);
            // 绘制实时点
            drawRealtimePoint(latLng);
        }
    }

    /**
     * 绘制实时点
     *
     * @param point
     */
    private void drawRealtimePoint(LatLng point) {
        if (null != overlay) {
            overlay.remove();
        }
        MapStatus mMapStatus = new MapStatus.Builder().target(point).zoom(19).build();
        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        if (null == realtimeBitmap) {
            realtimeBitmap = BitmapDescriptorFactory
                    .fromResource(R.mipmap.icon_geo);
        }
        overlayOptions = new MarkerOptions().position(point)
                .icon(realtimeBitmap).zIndex(9).draggable(true);
        if (pointList.size() >= 2 && pointList.size() <= 10000) {
            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(pointList);
        }
        addMarker();
    }

    /**
     * 添加地图覆盖物
     */
    protected static void addMarker() {

        if (null != msUpdate) {
            map.setMapStatus(msUpdate);
        }

        // 路线覆盖物
        if (null != polyline) {
            map.addOverlay(polyline);
        }

        // 实时点覆盖物
        if (null != overlayOptions) {
            overlay = map.addOverlay(overlayOptions);
        }

    }


    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
//关闭sso授权
        oks.disableSSOWhenAuthorize();

// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        oks.setTitle("标题");
// titleUrl是标题的网络链接，QQ和QQ空间等使用
        oks.setTitleUrl("http://sharesdk.cn");
// text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
// url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
// comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
// site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
// siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

// 启动分享GUI
        oks.show(this);
    }


    @Override
    protected void getIntentforActivity() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListeners() {

    }


}
