package com.origintech.alarmx.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.origintech.alarmx.alarm.AlarmItem;
import com.origintech.alarmx.global.Global;

import java.util.ArrayList;

/**
 * Created by evilatom on 14-1-14.
 */
public class ServiceBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if(AlarmService.ACTION_LANUCH_ALARM.equals(action))
        {
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE,
                    "alarmLock");
            wl.acquire(10000);
            Intent service = new Intent(context,AlarmService.class);
            service.putExtra(AlarmItem.PROPERTY_ID,intent.getIntExtra(AlarmItem.PROPERTY_ID,-1));
            service.setAction(AlarmService.ACTION_LANUCH_ALARM);
            context.startService(service);
        }
        else if(Intent.ACTION_BOOT_COMPLETED.equals(action))
        {
            Log.d("boot time","boot over");
            Intent service = new Intent(context,AlarmService.class);
            service.setAction(AlarmService.ACTION_START_SERVICE);
            context.startService(service);
        }
    }
}
