package com.linkloving.dyh08.logic.UI.setting;

import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.BLEProvider;
import com.example.android.bluetoothlegatt.proltrol.dto.LPDeviceInfo;
import com.linkloving.dyh08.BleService;
import com.linkloving.dyh08.MyApplication;
import com.linkloving.dyh08.R;
import com.linkloving.dyh08.ViewUtils.Wheelview.WheelView;
import com.linkloving.dyh08.basic.toolbar.ToolBarActivity;
import com.linkloving.dyh08.logic.dto.UserEntity;
import com.linkloving.dyh08.prefrences.LocalUserSettingsToolkits;
import com.linkloving.dyh08.prefrences.devicebean.DeviceSetting;
import com.linkloving.dyh08.utils.DeviceInfoHelper;
import com.linkloving.dyh08.utils.logUtils.MyLog;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Daniel.Xu on 2016/10/16.
 */

public class NotificationSettingActivity extends ToolBarActivity {
    private final static String TAG = NotificationSettingActivity.class.getSimpleName() ;
    @InjectView(R.id.listview)
    ListView listview;
    private LayoutInflater layoutInflater;
    private View totalView;
    private ArrayList<String> hrStrings;
    private ArrayList<String> minStrings;
    private int ischecked ;
    private DeviceSetting deviceSetting;
    private final static int STARTSEDENTARY = 1 ;
    private final static  int ENDSEDENTARY = 2;
    private final static  int CLOCKONE = 3;
    private final static  int CLOCKTWO = 4;
    private final static  int CLOCKTHREE = 5;
    private final static  int CLOCKFOUR = 6;
    private final static  int TIMESETTINGONE = 7;
    private final static  int TIMESETTINGTWO = 8;
    private final static  int TIMESETTINGTHREE = 9;
    private final static  int TIMESETTINGFOUR = 10;
    /**闹钟*/
    private String clockoneHr ="00" ;
    private String clockoneMin ="00" ;
    private String clocktwoHr  ="00";
    private String clocktwoMin ="00" ;
    private String clockthreeHr ="00";
    private String clockthreeMin ="00" ;
    private String clockfourHr ="00";
    private String clockfourMin  ="00";
    /**定时器*/
    private String timeoneHr ="00";
    private String timeoneMin ="00" ;
    private String timetwoHr ;
    private String timetwoMin ;
    private String timethreeHr;
    private String timethreeMin ;
    private String timefourHr ;
    private String timefourMin ;

    private int clock1Switch ;
    private int clock2Switch ;
    private int clock3Switch ;
    private int clock4Switch ;
    private int timeType = 0 ;
    private String starttimeHr  ;
    private String starttimeMin ;
    private String endTimeHr ;
    private String endTimeMin ;
    private BLEProvider provider;
    private UserEntity userEntity;
    private int phonecall ;
    private int SMScall ;
    private int Emailcall ;
    private int appscall ;
    private TextView clock1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tw_setting_activity);
        ButterKnife.inject(this);
        userEntity = MyApplication.getInstance(getApplicationContext()).getLocalUserInfoProvider();
        deviceSetting = LocalUserSettingsToolkits.getLocalSetting(NotificationSettingActivity.this, userEntity.getUser_id() + "");
        provider = BleService.getInstance(NotificationSettingActivity.this).getCurrentHandlerProvider();
        hrStrings = new ArrayList<>();
        for (int i = 0 ; i<=23;i++){
            String s ;
            if (i<10){
                s="0"+i;
            }else{
                s = Integer.toString(i);
            }
            hrStrings.add(s);
        }
        minStrings = new ArrayList<>();
        for (int i=0 ;i<=59;i++){
            String s ;
            if (i<10){
                s="0"+i;
            }else{
                s = Integer.toString(i);
            }
            minStrings.add(s);
        }
        String[] strings = {getString(R.string.messagenoti),
                getString(  R.string.sendentarynotif),
                getString(R.string.alarmclock),
                getString(R.string.timersetting),
        };
        layoutInflater = LayoutInflater.from(NotificationSettingActivity.this);
        totalView = layoutInflater.inflate(R.layout.tw_setting_activity, null);
        Myadapter myadapter = new Myadapter(NotificationSettingActivity.this,strings);
        listview.setAdapter(myadapter);
        View heardview = LayoutInflater.from(NotificationSettingActivity.this).inflate(R.layout.tw_setting_heardview, null);
        TextView textHeartView = (TextView) heardview.findViewById(R.id.textHeartView);
        textHeartView.setText(R.string.Notificationsetting);
        listview.addHeaderView(heardview);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /**因为有头所以从1开始*/
                switch (position){
                    case 1:
                        initMessagePopupWindow();
                        break;
                    case 2:
                        initSedentaryPopupWindow();
                        break;
                    case 3:
                        initClockPopupWindow();
                        break;
                    case 4:
                        initTimeSettingPopupWindow();
                        break;

                }
            }
        });
    }

    private void initTimeSettingPopupWindow() {
        View view = layoutInflater.inflate(R.layout.timesettingpopupwindow, null);
        ImageView dismiss = (ImageView) view.findViewById(R.id.dismiss);
        clock1 = (TextView) view.findViewById(R.id.clock_1);
//        TextView clock2 = (TextView) view.findViewById(R.id.clock2);
//        TextView clock3 = (TextView) view.findViewById(R.id.clock_3);
//        TextView clock4 = (TextView) view.findViewById(R.id.clock_4);
        Switch switch1  = (Switch) view.findViewById(R.id.timesetting1);
//        Switch switch2  = (Switch) view.findViewById(R.id.timesetting2);
//        Switch switch3  = (Switch) view.findViewById(R.id.timesetting3);
//        Switch switch4  = (Switch) view.findViewById(R.id.timesetting4);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
//        popupWindow.setClippingEnabled(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffD3D3D3));
        popupWindow.showAtLocation(totalView, Gravity.BOTTOM,0,0);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        clock1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType=TIMESETTINGONE ;
                showTimePopupWindow();

            }
        });
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    MyLog.e(TAG,"clockone"+clock1.getText().toString());
                    if (clock1.getText().toString().equals("00:00")){
                        //都为0就不发送命令
                    }else {
                        if (provider.isConnectedAndDiscovered()){
                            MyLog.e(TAG,"发送定时器的指令了");
                            LPDeviceInfo lpDeviceInfo1 = new LPDeviceInfo();
                            lpDeviceInfo1.millions = Integer.parseInt(timeoneHr)*3600+Integer.parseInt(timeoneMin)*60;
                            provider.SetTimeSetting(NotificationSettingActivity.this,lpDeviceInfo1);
                        }else {
                            Toast.makeText(NotificationSettingActivity.this,"请保持连接设备",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
//        clock2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                timeType = TIMESETTINGTWO ;
//                showTimePopupWindow();
//            }
//        });
//        clock3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                timeType = TIMESETTINGTHREE ;
//                showTimePopupWindow();
//            }
//        });
//        clock4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                timeType = TIMESETTINGFOUR ;
//                showTimePopupWindow();
//            }
//        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });

    }

    private void initClockPopupWindow() {
        View view = layoutInflater.inflate(R.layout.alarmclockpopupwindow, null);
        ImageView dismiss = (ImageView) view.findViewById(R.id.dismiss);
        TextView clock1 = (TextView) view.findViewById(R.id.clock_1);
        TextView clock2 = (TextView) view.findViewById(R.id.clock2);
        TextView clock3 = (TextView) view.findViewById(R.id.clock_3);
        TextView clock4 = (TextView) view.findViewById(R.id.clock_4);
        final Switch switch1 = (Switch) view.findViewById(R.id.clock1);
        Switch switch2 = (Switch) view.findViewById(R.id.clock22);
        Switch switch3 = (Switch) view.findViewById(R.id.clock3);
        Switch switch4 = (Switch) view.findViewById(R.id.clock4);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
//        popupWindow.setClippingEnabled(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffD3D3D3));
        popupWindow.showAtLocation(totalView, Gravity.BOTTOM,0,0);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        clock1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType=CLOCKONE ;
                showTimePopupWindow();
            }
        });
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clock1Switch = isChecked?1:0 ;
            }
        });
        clock2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType=CLOCKTWO ;
                showTimePopupWindow();
            }
        });
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clock2Switch = isChecked?1:0 ;
            }
        });
        clock3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType=CLOCKTHREE ;
                showTimePopupWindow();
            }
        });
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clock3Switch = isChecked?1:0 ;
            }
        });
        clock4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType =CLOCKFOUR ;
                showTimePopupWindow();
            }
        });
        switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clock4Switch = isChecked?1:0 ;
            }
        });
        /**结束时把几个闹钟保存设置*/
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
//                1+2+4+8+16+32+64= 127 意思是一周每天都有
                String clock1 = clockoneHr+":"+clockoneMin+"-"+"127"+"-"+clock1Switch;
                String clock2 = clocktwoHr+":"+clocktwoMin+"-"+"127"+"-"+clock2Switch;
                String clock3 = clockthreeHr +":"+clockthreeMin+"-"+"127"+"-"+clock3Switch;
                String clock4 = clockfourHr+":"+clockfourMin+"-"+"127"+"-"+clock4Switch;
                deviceSetting.setAlarm_one(clock1);
                deviceSetting.setAlarm_two(clock2);
                deviceSetting.setAlarm_three(clock3);
                provider.SetClock(NotificationSettingActivity.this,DeviceInfoHelper.fromUserEntity(NotificationSettingActivity.this,userEntity));
            }
        });
    }

    private void initSedentaryPopupWindow() {
        View view = layoutInflater.inflate(R.layout.sedentarypopupwindow, null);
        ImageView dismiss = (ImageView) view.findViewById(R.id.dismiss);
        ImageView startTimeNext = (ImageView) view.findViewById(R.id.startTimeNext);
        final ImageView endTimeNext = (ImageView) view.findViewById(R.id.endTimeNext);
        Switch switchInterval = (Switch) view.findViewById(R.id.switchinterval);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
//        popupWindow.setClippingEnabled(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffD3D3D3));
        popupWindow.showAtLocation(totalView, Gravity.BOTTOM,0,0);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        startTimeNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType = STARTSEDENTARY ;
                showTimePopupWindow();
            }
        });
        endTimeNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType = ENDSEDENTARY ;
                showTimePopupWindow();
            }
        });
        switchInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ischecked = 1;
                }else{
                    ischecked = 0 ;
                }
            }
        });
        /**在结束时把时间保存*/
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                String time = starttimeHr+":"+starttimeMin+"-"+endTimeHr+":"+endTimeMin;
                deviceSetting.setLongsit_time(time);
                deviceSetting.setLongsit_vaild(ischecked+"");
                LocalUserSettingsToolkits.updateLocalSetting(NotificationSettingActivity.this,
                        deviceSetting);
                /**判断蓝牙是否连接*/
                provider.SetLongSit(NotificationSettingActivity.this, DeviceInfoHelper.fromUserEntity(NotificationSettingActivity.this,
                        userEntity));
            /*    //判断蓝牙是否连接
                if (provider.isConnectedAndDiscovered()) {
                    //同步到设备
                    provider.SetLongSit(LongSitActivity.this, DeviceInfoHelper.fromUserEntity(LongSitActivity.this, userEntity));
                    Synchronize_device.setVisibility(View.VISIBLE);
                    BLE_Button.setText(getString(R.string.saved_to_local));
                    toast.show();
                }else{
                    android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(LongSitActivity.this)
                            .setTitle(ToolKits.getStringbyId(LongSitActivity.this, R.string.portal_main_unbound))
                            .setMessage(ToolKits.getStringbyId(LongSitActivity.this, R.string.portal_main_unbound_msg))
                            .setPositiveButton(ToolKits.getStringbyId(LongSitActivity.this, R.string.general_ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).create();
                    dialog.show();
                }*/
            }
        });
    }

    public PopupWindow showTimePopupWindow(){
        View view = View.inflate(NotificationSettingActivity.this, R.layout.starttimepopupwindow, null);
        ImageView dismiss = (ImageView) view.findViewById(R.id.dismiss);
        WheelView hrWheelView = (WheelView) view.findViewById(R.id.hr);
        hrWheelView.setItems(hrStrings);
        WheelView minWheelView = (WheelView) view.findViewById(R.id.min);
        minWheelView.setItems(minStrings);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
//        popupWindow.setClippingEnabled(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffD3D3D3));
        popupWindow.showAtLocation(totalView, Gravity.BOTTOM,0,0);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
       hrWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
           @Override
           public void onSelected(int selectedIndex, String item) {
               switch (timeType){
                   case STARTSEDENTARY:
                       starttimeHr = item ;
                       break;
                   case ENDSEDENTARY :
                       endTimeHr = item ;
                       break;
                   case CLOCKONE:
                       clockoneHr = item ;
                       break;
                   case CLOCKTWO:
                       clocktwoHr = item ;
                       break;
                   case CLOCKTHREE:
                       clockthreeHr = item ;
                       break;
                   case CLOCKFOUR:
                       clockfourHr = item ;
                       break;
                   case TIMESETTINGONE:
                       timeoneHr = item ;
                       break;
                   case TIMESETTINGTWO:
                       timetwoHr = item ;
                       break;
                   case TIMESETTINGTHREE:
                       timethreeHr = item ;
                       break;
                   case TIMESETTINGFOUR:
                       timefourHr = item ;
                       break;

               }
           }
       });
        minWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
            @Override
            public void onSelected(int selectedIndex, String item) {
                switch (timeType){
                    case STARTSEDENTARY:
                        starttimeMin = item ;
                        break;
                    case ENDSEDENTARY :
                        endTimeMin = item ;
                        break;
                    case CLOCKONE:
                        clockoneMin = item ;
                        break;
                    case CLOCKTWO:
                        clocktwoMin = item ;
                        break;
                    case CLOCKTHREE:
                        clockthreeMin = item ;
                        break;
                    case CLOCKFOUR:
                        clockfourMin = item ;
                        break;
                    case TIMESETTINGONE:
                        timeoneMin = item ;
                        break;
                }

            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                switch (timeType){
                    case CLOCKONE:
                        break;
                    case CLOCKTWO:
                        break;
                    case CLOCKTHREE:
                        break;
                    case CLOCKFOUR:
                        break;
                    case TIMESETTINGONE:
                        clock1.setText(timeoneHr+":"+timeoneMin);
                        break;

                }
            }
        });
        return  popupWindow;
    }

    private void initMessagePopupWindow() {
        View view = layoutInflater.inflate(R.layout.messagepopupwindow, null);
        ImageView dismiss = (ImageView) view.findViewById(R.id.dismiss);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
//        popupWindow.setClippingEnabled(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffD3D3D3));
        popupWindow.showAtLocation(totalView, Gravity.BOTTOM,0,0);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        Switch switchPhonecall = (Switch) view.findViewById(R.id.switchPhoneCall);
        switchPhonecall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                     phonecall = 1;
                }else {
                    phonecall = 0 ;
                }
            }
        });
        Switch switchTextmessage = (Switch) view.findViewById(R.id.switchTextmessage);
        switchTextmessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                        SMScall = 1 ;
                }else {
                    SMScall = 0 ;
                }
            }
        });
        Switch switchEmail = (Switch) view.findViewById(R.id.switchEmail);
        switchEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Emailcall =1 ;
                }else {
                    Emailcall = 0 ;
                }
            }
        });
        Switch switchapps = (Switch) view.findViewById(R.id.switchMessagingapps);
        switchapps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    appscall = 1 ;
                }else {
                    appscall = 0 ;
                }
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                String notif_ = ""+phonecall+SMScall+Emailcall+appscall ;
                int notif_data = Integer.parseInt(notif_, 2);
                byte[] send_data = intto2byte(notif_data);
                deviceSetting.setANCS_value(notif_data);
                LocalUserSettingsToolkits.updateLocalSetting(NotificationSettingActivity.this
                ,deviceSetting);
                if (provider.isConnectedAndDiscovered()){
                    provider.setNotification(NotificationSettingActivity.this,send_data);
                }else{
                    MyLog.e(TAG,"消息提醒的蓝牙没连接");
                }
            }
        });
    }
    /**int转byte*/
    public static byte[] intto2byte(int a)
    {
        byte[] m = new byte[2];
        m[0] = (byte) ((0xff & a));
        m[1] = (byte) (0xff & (a >> 8));
        return m;
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
