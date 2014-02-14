package com.origintech.alarmx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.origintech.alarmx.adapter.AlarmAdapter;
import com.origintech.alarmx.alarm.AlarmItem;
import com.origintech.alarmx.global.Global;
import com.origintech.alarmx.service.AlarmService;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends ActionBarActivity
{
    private static AlarmService mService = null;

    private List<AlarmItem> mAlarms = null;  //目前所有的闹钟
    private ServiceConnection mServiceConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mService = ((AlarmService.ServiceBinder)service).getService();
            if(mService != null)
            {
                serviceConnected();
                mService.registerOnDataChange(mOnServiceDataChange);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            serviceDisconnected();
            mService.unregisterOnDataChange(mOnServiceDataChange);
            mService = null;
        }
    };
    private AlarmService.OnDataChange mOnServiceDataChange = new AlarmService.OnDataChange()
    {
        @Override
        public void onDataChange()
        {
            mAlarms = mService.getAllAlarms();
            mMainContent.setAlarms(mAlarms);
        }
    };
    private void initService()
    {
        boolean started = false;
        //检测服务是否启动
        Global.PreferenceSet prefs = new Global.PreferenceSet(this);
        prefs.beginEdit();
        started = prefs.getBoolean(Global.PreferenceSet.PREF_SERVICE_STARTED, false);
        prefs.endEdit();
        //根据检测结果，启动或连接服务
        Intent i = new Intent(this,AlarmService.class);
        if(!started)
        {
            i.setAction(AlarmService.ACTION_START_SERVICE);
            startService(i);
        }
        bindService(i,mServiceConn, Context.BIND_AUTO_CREATE);
    }
    private void serviceConnected()
    {
        mAlarms = mService.getAllAlarms();
        if(mMainContent != null)
            mMainContent.setAlarms(mAlarms);
    }
    private void serviceDisconnected()
    {

    }

    private PlaceholderFragment mMainContent = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
        {
            mMainContent = new PlaceholderFragment(this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container,mMainContent)
                    .commit();

            //初始化服务
            initService();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(mServiceConn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_new)
        {
            //创建一个默认闹钟
            AlarmItem alarm = AlarmItem.defaultAlarm();
            Calendar current = Calendar.getInstance();
            alarm.set(AlarmItem.PROPERTY_NAME,"");
            alarm.set(AlarmItem.PROPERTY_HOUR,current.get(Calendar.HOUR_OF_DAY));
            alarm.set(AlarmItem.PROPERTY_MIN,current.get(Calendar.MINUTE));
            alarm.set(AlarmItem.PROPERTY_REPEAT, Global.Week.NONE);

            mService.insertAlarm(alarm);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        private ListView mMainList = null;
        private AlarmAdapter mMainListAdpater = null;
        private Context mContext = null;

        private AlarmAdapter.OnActionInterface mOnAdapterInterface = new AlarmAdapter.OnActionInterface()
        {
            @Override
            public void onDelete(List<AlarmItem> alarms)
            {
                int size = alarms.size();
                for(int i = 0;i < size;i++)
                {
                    if(mService != null)
                        mService.delAlarm(alarms.get(i));
                }
            }

            @Override
            public void onSingleTap(int position)
            {

            }

            @Override
            public void onLongPress(int position)
            {

            }

            @Override
            public void onMultiSelectionChanged(int size)
            {

            }
        };

        public PlaceholderFragment(Context context)
        {
            mContext = context;
        }

        private List<AlarmItem> mAlarms = null;
        public void setAlarms(List<AlarmItem> alarms)
        {
            mAlarms = alarms;
            onDataChange();
        }

        //数据改变时
        protected void onDataChange()
        {
            if(mMainListAdpater == null)
            {
                mMainListAdpater = new AlarmAdapter(mContext,mAlarms,
                        R.layout.main_alarm_list_item_view);
                mMainListAdpater.setOnActionInterface(mOnAdapterInterface);
                mMainList.setAdapter(mMainListAdpater);
            }
            else
            {
                mMainListAdpater.setAlarms(mAlarms);
            }
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mMainList = (ListView)rootView.findViewById(R.id.main_listview);
            mMainList.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    boolean needEndDelete = false; //是否需要调用AlarmAdapter.endDelete();
                    boolean ret = false;
                    if(event.getAction() == MotionEvent.ACTION_UP)
                    {
                        if(mMainListAdpater != null && mMainListAdpater.isDeleting())
                            needEndDelete = true;
                    }
                    if(mMainListAdpater != null && mMainListAdpater.getGestureDetector().onTouchEvent(event))
                    {
                        ret = true;
                    }
                    else
                    {
                        ret = false;
                    }
                    if(needEndDelete)
                        mMainListAdpater.endDelete();
                    return ret;
                }
            });
            return rootView;
        }
    }

}
