package com.linkloving.dyh08.logic.UI.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.BLEHandler;
import com.example.android.bluetoothlegatt.BLEListHandler;
import com.example.android.bluetoothlegatt.BLEListProvider;
import com.example.android.bluetoothlegatt.BLEProvider;
import com.linkloving.dyh08.BleService;
import com.linkloving.dyh08.IntentFactory;
import com.linkloving.dyh08.R;
import com.linkloving.dyh08.basic.toolbar.ToolBarActivity;
import com.linkloving.dyh08.logic.UI.main.PortalActivity;
import com.linkloving.dyh08.logic.UI.main.materialmenu.CommonAdapter;
import com.linkloving.dyh08.logic.dto.UserEntity;
import com.linkloving.dyh08.prefrences.PreferencesToolkits;
import com.linkloving.dyh08.utils.logUtils.MyLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by leo.wang on 2016/7/25.
 */
public class BluetoothActivity extends ToolBarActivity {

    @InjectView(R.id.blue_middle_changeIV)
    ImageView middleChangeIV;
    @InjectView(R.id.bluetooth_list)
    ListView mlistview;
    @InjectView(R.id.bluetooth_next)
    ImageView btn_Next;
    private BLEProvider provider;
    public static final int RESULT_BACK = 999;
    public static final int RESULT_FAIL = 998;
    public static final int RESULT_NOCHARGE = 997;
    public static final int RESULT_DISCONNECT = 996;
    private BLEProviderObserver observerAdapter;
    private BLEListHandler handler;
    private BLEListProvider listProvider;
    private List<DeviceVO>macList = new ArrayList();
    private macListAdapter mAdapter;
    private int selectionPostion ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tw_bluetooth_activity);
        ButterKnife.inject(this);
        middleChangeIV.setVisibility(View.INVISIBLE);
        btn_Next.setVisibility(View.GONE);
        observerAdapter = new BLEProviderObserver();
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        provider.setBleProviderObserver(observerAdapter);
        handler = new BLEListHandler(BluetoothActivity.this) {
            @Override
            protected void handleData(BluetoothDevice bluetoothDevice) {
                    for (DeviceVO v :macList){
                        if (v.mac.equals(bluetoothDevice.getAddress()))
                            return;
                    }
                DeviceVO vo = new DeviceVO();
                vo.mac= bluetoothDevice.getAddress();
                vo.name =bluetoothDevice.getName();
                vo.bledevice=bluetoothDevice ;
                macList.add(vo);
                mAdapter.notifyDataSetChanged();
            }
        };
        listProvider = new BLEListProvider(this, handler);
        mAdapter = new macListAdapter(this, macList);
        listProvider.scanDeviceList();
        mlistview.setDivider(null);
        mlistview.setDividerHeight(10);
        mlistview.setAdapter(mAdapter);
        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                provider.setCurrentDeviceMac(macList.get(position).mac);
                provider.setmBluetoothDevice(macList.get(position).bledevice);
                provider.connect_mac(macList.get(position).mac);
                ImageView stateIV = (ImageView) view.findViewById(R.id.list_item_imageview);
                stateIV.setVisibility(View.VISIBLE);

            }
        });

        btn_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothActivity.this, PortalActivity.class);
                startActivity(intent);
            }
        });
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

    class macListAdapter extends CommonAdapter<DeviceVO>{
        public class ViewHolder{
            public TextView equipment_name ;
            public TextView equipment_adress ;
            public ImageView item_state ;

        }
        ViewHolder holder ;

        public macListAdapter(Context context, List<DeviceVO> list) {
            super(context, list);
        }

        @Override
        protected View noConvertView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.bluetooth_listviewitem, parent, false);

            holder = new ViewHolder();
            holder.equipment_name = (TextView) convertView.findViewById(R.id.equipment_name);
            holder.equipment_adress = (TextView) convertView.findViewById(R.id.equipment_adress);
            holder.item_state = (ImageView) convertView.findViewById(R.id.list_item_imageview);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View hasConvertView(int position, View convertView, ViewGroup parent) {
            holder = (ViewHolder) convertView.getTag();
            return convertView;
        }

        @Override
        protected View initConvertView(int position, View convertView, ViewGroup parent) {
            String name = list.get(position).name;
            MyLog.e("name是",name);
            holder.equipment_name.setText(name);
//            String mac = list.get(position).mac.substring(list.get(position).mac.length() - 5, list.get(position).mac.length());
//            holder.equipment_adress.setText("ID:   " + removeCharAt(mac, 2));
            String mac = list.get(position).mac;
            holder.equipment_adress.setText(mac);
            return convertView;
        }
    }
    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (provider != null) {
            if (provider.getBleProviderObserver() == null) {
                provider.setBleProviderObserver(observerAdapter);
            }
        }
    }




    private class BLEProviderObserver extends BLEHandler.BLEProviderObserverAdapter{

        @Override
        protected Activity getActivity() {
            return BluetoothActivity.this ;
        }

        @Override
        public void updateFor_handleConnectSuccessMsg() {
            MyLog.e("BandListActivity", "连接成功");
//            provider.requestbound_fit(BluetoothActivity.this);
            UserEntity userAuthedInfo = PreferencesToolkits.getLocalUserInfoForLaunch(BluetoothActivity.this);
            userAuthedInfo.getDeviceEntity().setLast_sync_device_id(macList.get(selectionPostion).mac);
            middleChangeIV.setImageResource(R.mipmap.link);
            middleChangeIV.setVisibility(View.VISIBLE);
            btn_Next.setVisibility(View.VISIBLE);
            Toast.makeText(BluetoothActivity.this,"绑定成功",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void updateFor_handleConnectLostMsg() {
            MyLog.e("BandListActivity", "断开连接");
            middleChangeIV.setImageResource(R.mipmap.nofind);
            middleChangeIV.setVisibility(View.VISIBLE);

            provider.clearProess();
            provider.setCurrentDeviceMac(null);
            provider.setmBluetoothDevice(null);
            provider.resetDefaultState();
            setResult(RESULT_DISCONNECT);
            Toast.makeText(BluetoothActivity.this, "绑定失败", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    public void finish(){
        super.finish();
        listProvider.stopScan();
        provider = BleService.getInstance(this).getCurrentHandlerProvider();
        if (provider!=null)
            provider.setBleProviderObserver(null);

    }

    class DeviceVO {
        public String mac;
        public String name;
        public BluetoothDevice bledevice;

    }


}
