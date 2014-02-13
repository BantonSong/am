package com.origintech.alarmx.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.origintech.alarmx.alarm.AlarmItem;
import com.origintech.alarmx.dao.IDao;
import com.origintech.alarmx.dao.SQLiteDao;
import com.origintech.alarmx.global.Global;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by evilatom on 14-1-7.
 * 采用线性表对数据进行管理
 */
public class AlarmService extends Service
{
    public static String ACTION_START_SERVICE = "com.origintech.alarmx.alarm.service.start";
    public static String ACTION_LANUCH_ALARM = "com.origintech.alarmx.alarm.service.lanuchAlarm";

    private IDao mDao = null;
    private List<AlarmItem> mAlarms = null;

    private IBinder mBinder = new ServiceBinder();
    public class ServiceBinder extends Binder
    {
        public AlarmService getService()
        {
            return AlarmService.this;
        }
    }

    //数据改变时调用的接口及相关的操作
    public interface OnDataChange
    {
        public void onDataChange();
    }
    private List<OnDataChange> mOnDataChange = new ArrayList<OnDataChange>();
    public void registerOnDataChange(OnDataChange i)
    {
        if(i != null)
            mOnDataChange.add(i);
    }
    public void unregisterOnDataChange(OnDataChange i)
    {
        if(i != null)
            mOnDataChange.remove(i);
    }
    protected void invokeOnDataChange()
    {
        synchronized (mOnDataChange)
        {
            int size = mOnDataChange.size();
            for(int i = 0;i < size;i++)
                mOnDataChange.get(i).onDataChange();
        }
        //数据改变后重新设置闹钟
        registerAlarm();
    }


    //返回当前拥有的所有Alarm
    public List<AlarmItem> getAllAlarms()
    {
        synchronized (mAlarms)
        {
            return mAlarms;
        }
    }

    //保存当前所有的Alarm
    public void saveAllAlarms()
    {
        List<AlarmItem> alarms = this.getAllAlarms();

        mDao.saveAllAlarms(alarms);
    }

    //插入一个Alarm
    public void insertAlarm(AlarmItem alarm)
    {
        //数据存入数据库
        mDao.insertAlarm(alarm);
        //同步内存数据
        synchronized (mAlarms)
        {
            mAlarms.add(alarm);
        }
        //更新数据链表
        arrangeAlarms();
        //通知数据改变
        invokeOnDataChange();
    }

    //更新一个Alarm
    public void updateAlarm(AlarmItem alarm)
    {
        //将数据存入数据库
        mDao.updateAlarm(alarm);

        //对数据重新排序
        arrangeAlarms();
        //通知数据改变
        invokeOnDataChange();
    }

    //删除一个Alarm
    public void delAlarm(AlarmItem alarm)
    {
        //将数据库中的数据删除
        mDao.delAlarm(alarm);
        //更新内存数据
        synchronized (mAlarms)
        {
            mAlarms.remove(alarm);
        }
        //通知数据改变
        invokeOnDataChange();
    }

    //对整个Alarm按时间排序
    private void sortAlarms()
    {
        AlarmItem cur = null;
        AlarmItem temp = null;
        synchronized (mAlarms)
        {
            int size = mAlarms.size();

            int i,j;
            //直接插入排序
            for(i = 1;i < size;i++)
            {
                cur = mAlarms.get(i);
                for(j = i - 1;j >= 0;j--)
                {
                    temp = mAlarms.get(j);
                    if(cur.compareTo(temp) < 0)
                    {
                        mAlarms.set(j + 1,temp);
                    }
                    else
                    {
                        mAlarms.set(j + 1,cur);
                        break;
                    }
                }
                //忘了哨兵，找了好久了
                if(j < 0)
                    mAlarms.set(0,cur);
            }
        }
    }

    //调整所有Alarm
    private synchronized void arrangeAlarms()
    {
        AlarmItem cur = null;
        AlarmItem temp = null;

        sortAlarms();

        Calendar current = Calendar.getInstance();
        int hour = current.get(Calendar.HOUR_OF_DAY);
        int min = current.get(Calendar.MINUTE);

        synchronized (mAlarms)
        {
            //调整最近位置
            int size = mAlarms.size();

            for(int i = 0;i < size;i++)
            {
                temp = mAlarms.get(0);
                if(temp.compareTo(hour,min) >= 0)
                    break;
                temp = mAlarms.remove(0);
                mAlarms.add(temp);
            }
        }
    }

    //注册Alarm
    private synchronized void registerAlarm()
    {
        AlarmItem alarm = null;
        int enable;

        synchronized (mAlarms)
        {
            for(AlarmItem temp : mAlarms)
            {
                enable = temp.getInt(AlarmItem.PROPERTY_ENABLED);
                if(enable == 1)
                {
                    alarm = temp;
                    break;
                }
            }
        }

        Calendar now = Calendar.getInstance();

        if(alarm != null &&
                alarm.compareTo(now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE)) > 0)
        {
            Intent i = new Intent(this.getApplicationContext(),ServiceBroadcastReceiver.class);
            i.setAction(ACTION_LANUCH_ALARM);
            i.putExtra(AlarmItem.PROPERTY_ID,alarm.getInt(AlarmItem.PROPERTY_ID));
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, i,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            ((AlarmManager)this.getSystemService(Context.ALARM_SERVICE))
            .set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), sender);
        }

    }

    //取消Alarm注册,当不需要Alarm服务时完成这一动作
    private void unregisterAlarm()
    {
        Intent intent = new Intent(this.getApplicationContext(),ServiceBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    //按AlarmID返回指定的Alarm
    public AlarmItem getAlarmById(int id)
    {
        int alarmId = -1;
        AlarmItem alarm = null;
        for(int i = 0; i < mAlarms.size();i++)
        {
            alarm = mAlarms.get(i);
            alarmId = alarm.getInt(AlarmItem.PROPERTY_ID);
            if(alarmId == id)
                break;
        }
        return alarm;
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mDao = new SQLiteDao(this.getApplicationContext());
        mAlarms = mDao.fetchAllAlarms();
        Global.PreferenceSet prefs = new Global.PreferenceSet(this);
        prefs.beginEdit();
        prefs.putBoolean(Global.PreferenceSet.PREF_SERVICE_STARTED,true);
        prefs.endEdit();
    }

    //与获取当前Alarm有关的函数与变量
    private List<AlarmItem> mValidAlarms = new ArrayList<AlarmItem>();
    public List<AlarmItem> getValidAlarms()
    {
        return mValidAlarms;
    }
    protected void collectValidAlarms()
    {
        Calendar current = Calendar.getInstance();
        mValidAlarms.clear();
        synchronized (mAlarms)
        {
            //收集当前有效的闹钟
            AlarmItem ai = null;
            ArrayList<AlarmItem> tempAlarms = new ArrayList<AlarmItem>();

            while(mAlarms.size() != 0 && mAlarms.get(0).compareTo(
                    current.get(Calendar.HOUR_OF_DAY),
                    current.get(Calendar.MINUTE)) == 0
                    )
            {
                ai = mAlarms.remove(0);
                int repeatMode = ai.getInt(AlarmItem.PROPERTY_REPEAT);
                if(Global.Week.isDay(repeatMode, Global.Week.getWeekFromCalendar(current)))
                {
                    mValidAlarms.add(ai);
                    tempAlarms.add(ai);
                }
                else if(repeatMode == Global.Week.NONE)
                {
                    //若永不重复，闹钟激活后直接删除
                    mValidAlarms.add(ai);
                    mDao.delAlarm(ai);
                }
                else
                {
                    tempAlarms.add(ai);
                }

            }

            while (tempAlarms.size() != 0)
            {
                mAlarms.add(tempAlarms.remove(0));
            }
            tempAlarms = null;
        }
    }

    protected void recyleAlarms()
    {

    }

    //是否包含有效的Alarm
    public boolean hasActiveAlarm(List<AlarmItem> alarms)
    {
        return (mValidAlarms.size() != 0);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = intent.getAction();
        Log.d("Alarm Service","onStartCommand");

        if(ACTION_LANUCH_ALARM.equals(action))
        {
            //收集闹钟
            collectValidAlarms();
            //检测启动闹钟条件
            launchAlarm(getValidAlarms());
            //回收闹钟
            recyleAlarms();
            //重新设置闹钟
            registerAlarm();

        }
        else if(ACTION_START_SERVICE.equals(action))
        {
            arrangeAlarms();
            registerAlarm();
            Log.d("alarm service","start service");
        }

        return START_STICKY;
    }

    //启动Alarm
    private void launchAlarm(List<AlarmItem> alarms)
    {
        if(!hasActiveAlarm(alarms))
            return;
        //闹钟界面
        //Intent i = new Intent(this.getApplicationContext(), AlarmActivity.class);
        //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(i);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
