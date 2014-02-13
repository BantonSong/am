package com.origintech.alarmx.alarm;

import com.origintech.alarmx.alarm.property.PropertyCollection;
import com.origintech.alarmx.alarm.property.SimpleProperty;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by evilatom on 14-1-6.
 */
public class AlarmItem implements  Comparable<AlarmItem>
{
/*    *//**
     * Alarm type
     *//*
    public final static int ALARM_APP = 0;
    public final static int ALARM_SMS = 1;
    public final static int ALARM_CALL = 2;
    public final static int ALARM_REMINDER = 3;
    public final static int ALARM_REMOTE = 4;
    public final static int ALARM_DEFAULT = ALARM_REMINDER;*/

    //Alarm common property
    public final static String PROPERTY_ID = "id";
    public final static String PROPERTY_NAME = "name";
    public final static String PROPERTY_TYPE = "type";
    public final static String PROPERTY_ENABLED = "enable";

    public final static String PROPERTY_HOUR = "hour";
    public final static String PROPERTY_MIN = "min";
    public final static String PROPERTY_REPEAT = "repeat_mode";

    public final static String PROPERTY_SNOOZE = "snooze";
    public final static String PROPERTY_SNOOZE_DURATION = "snooze_duration";
    public final static String PROPERTY_SNOOZE_TIMES = "snooze_time";

    //Alarm voice
    public final static String PROPERTY_ALARM_VOLUMN = "alarm_volumn";
    public final static String PROPERTY_ALARM_RINGTONE_URI = "alarm_ringtone_uri";
    //public final static String PROPERTY_ALARM_RECORD_ENABLE = "alarm_record_enable";


    //Alarm SMS property
    public final static String PROPERTY_SMS_ENABLE = "sms_enable";
    public final static String PROPERTY_PHONE = "phone";
    public final static String PROPERTY_SMS_CONTENT = "sms_content";
    //Alarm Reminder property
    public final static String PROPERTY_REMINDER_ENABLE = "reminder_enable";
    public final static String PROPERTY_REMINDER_TEXT = "reminder_text";
    //Alarm App property
    public final static String PROPERTY_APP_ENABLE = "app_enable";
    public final static String PROPERTY_PACKAGE_NAME = "app_package_name";
    public final static String PROPERTY_STOP_APP = "app_stop";
    //Alarm network
    public final static String PROPERTY_NETWORK_RELATE = "network";
    public final static String PROPERTY_WIFI_RELATE = "wifi";


    //properties
    protected PropertyCollection mProperty = new PropertyCollection();

    protected AlarmItem()
    {
        mProperty.addProperty(new SimpleProperty(PROPERTY_ID,-1));
        mProperty.addProperty(new SimpleProperty(PROPERTY_NAME,"Default"));
        mProperty.addProperty(new SimpleProperty(PROPERTY_ENABLED,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_HOUR,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_MIN,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_REPEAT, 0));

        mProperty.addProperty(new SimpleProperty(PROPERTY_SNOOZE,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_SNOOZE_DURATION,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_SNOOZE_TIMES,0));
        //alarm voice
        mProperty.addProperty(new SimpleProperty(PROPERTY_ALARM_VOLUMN,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_ALARM_RINGTONE_URI,""));
        //alarm sms
        mProperty.addProperty(new SimpleProperty(PROPERTY_SMS_ENABLE,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_PHONE,""));
        mProperty.addProperty(new SimpleProperty(PROPERTY_SMS_CONTENT,""));
        //alarm reminder
        mProperty.addProperty(new SimpleProperty(PROPERTY_REMINDER_ENABLE,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_REMINDER_TEXT,""));
        //alarm app
        mProperty.addProperty(new SimpleProperty(PROPERTY_APP_ENABLE,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_PACKAGE_NAME,""));
        mProperty.addProperty(new SimpleProperty(PROPERTY_STOP_APP,0));
        //alarm network
        mProperty.addProperty(new SimpleProperty(PROPERTY_NETWORK_RELATE,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_WIFI_RELATE,0));
    }

    public int getPropertyValueType(String property)
    {
        return mProperty.get(property).getValueType();
    }

    public static List<String> getSupportedPropertyNames(){
        ArrayList<String> names = new ArrayList<String>();
        names.add(PROPERTY_ID);
        names.add(PROPERTY_NAME);
        names.add(PROPERTY_ENABLED);

        names.add(PROPERTY_HOUR);
        names.add(PROPERTY_MIN);
        names.add(PROPERTY_REPEAT);

        names.add(PROPERTY_SNOOZE);
        names.add(PROPERTY_SNOOZE_DURATION);
        names.add(PROPERTY_SNOOZE_TIMES);

        //alarm voice
        names.add(PROPERTY_ALARM_VOLUMN);
        names.add(PROPERTY_ALARM_RINGTONE_URI);
        //alarm sms
        names.add(PROPERTY_SMS_ENABLE);
        names.add(PROPERTY_PHONE);
        names.add(PROPERTY_SMS_CONTENT);
        //alarm reminder
        names.add(PROPERTY_REMINDER_ENABLE);
        names.add(PROPERTY_REMINDER_TEXT);
        //alarm app
        names.add(PROPERTY_APP_ENABLE);
        names.add(PROPERTY_PACKAGE_NAME);
        names.add(PROPERTY_STOP_APP);
        //network
        names.add(PROPERTY_NETWORK_RELATE);
        names.add(PROPERTY_WIFI_RELATE);

        return names;
    }

    public static AlarmItem defaultAlarm()
    {
       return new AlarmItem();
    }
    public boolean set(String property,int value)
    {
        return mProperty.set(property,value);
    }

    public boolean set(String property,String value)
    {
        return mProperty.set(property,value);
    }
    public int getInt(String property)
    {
        return mProperty.getInt(property);
    }
    public String getString(String property)
    {
        return mProperty.getString(property);
    }
    public List<SimpleProperty> getAllProperties()
    {
        return mProperty.getAllProperties();
    }
    public long getTimeInMillis()
    {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, this.getInt(AlarmItem.PROPERTY_HOUR));
        today.set(Calendar.MINUTE, this.getInt(AlarmItem.PROPERTY_MIN));
        today.set(Calendar.SECOND, 0);
        return today.getTimeInMillis();
    }

    @Override
    public int compareTo(AlarmItem o)
    {
        int thisHour = getInt(AlarmItem.PROPERTY_HOUR);
        int thisMin = getInt(AlarmItem.PROPERTY_MIN);

        int oHour = o.getInt(AlarmItem.PROPERTY_HOUR);
        int oMin = o.getInt(AlarmItem.PROPERTY_MIN);

        int tt = (thisHour << 16) | thisMin;
        int ot = (oHour << 16) | oMin;
        if(tt > ot)
            return 1;
        else if(tt == ot)
            return 0;
        else
            return -1;
    }
    public int compareTo(int hour,int min)
    {
        int thisHour = getInt(AlarmItem.PROPERTY_HOUR);
        int thisMin = getInt(AlarmItem.PROPERTY_MIN);
        int tt = (thisHour << 16) | thisMin;

        int ot = (hour << 16) | min;

        if(tt > ot)
            return 1;
        else if(tt == ot)
            return 0;
        else
            return -1;
    }

    //json数据格式的Alarm信息
    public String toJsonString()
    {
        JSONObject jo = new JSONObject();
        try
        {
            jo.put(PROPERTY_HOUR,Integer.valueOf(getInt(PROPERTY_HOUR)));
            jo.put(PROPERTY_MIN,Integer.valueOf(getInt(PROPERTY_MIN)));
            jo.put(PROPERTY_REMINDER_TEXT,getString(PROPERTY_REMINDER_TEXT));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(jo.toString());
        return sb.toString();
    }
}
