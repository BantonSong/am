package com.origintech.alarmx.alarm;

import android.content.Context;

import com.origintech.alarmx.alarm.property.PropertyCollection;
import com.origintech.alarmx.alarm.property.SimpleProperty;
import com.origintech.alarmx.global.Global;

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

    //Alarm Reminder property
    public final static String PROPERTY_REMINDER_ENABLE = "reminder_enable";
    public final static String PROPERTY_REMINDER_TEXT = "reminder_text";

    //parent mode
    public final static String PROPERTY_PARENT_MODE_ENABLE = "parent_mode_enable";


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

        //alarm reminder
        mProperty.addProperty(new SimpleProperty(PROPERTY_REMINDER_ENABLE,0));
        mProperty.addProperty(new SimpleProperty(PROPERTY_REMINDER_TEXT,""));

        //parent mode
        mProperty.addProperty(new SimpleProperty(PROPERTY_PARENT_MODE_ENABLE,0));
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

        //alarm reminder
        names.add(PROPERTY_REMINDER_ENABLE);
        names.add(PROPERTY_REMINDER_TEXT);

        //parent mode
        names.add(PROPERTY_PARENT_MODE_ENABLE);

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

    public boolean isPM()
    {
        if(getInt(PROPERTY_HOUR) >= 12)
            return true;
        else
            return false;
    }
    public boolean isEnable()
    {
        return getInt(PROPERTY_ENABLED) == 1;
    }
    public String getTimeString(boolean mode_24hour)
    {
        return Global.Day.getTimeString(
                getInt(PROPERTY_HOUR),
                getInt(PROPERTY_MIN),
                mode_24hour);
    }
    public String getRepeatString(Context context)
    {
        return Global.Week.getRepeatModeString(context,getInt(PROPERTY_REPEAT));
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
