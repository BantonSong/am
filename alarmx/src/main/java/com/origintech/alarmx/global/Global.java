package com.origintech.alarmx.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.origintech.alarmx.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by evilatom on 14-1-6.
 */
public class Global
{
    public static String APP_NAME = "super alarm";
    public static String APP_VERSION = "1.0";

    public static class StringUtils
    {
        /**
         *
         * @param ids: xxx,xxx,xxx,xx
         *
         * @return　若ids为空串,则返回null
         * 否则，返回{"xxx","xxx","xxx","xx"}
         */
        public static List<Long> getlongFromString(String ids)
        {
            if(ids.length() == 0 )
                return null;
            ArrayList<Long> list = new ArrayList<Long>();
            String[] idsStr = ids.split(",");
            int len = idsStr.length;
            for(int i = 0;i < len;i++)
            {
                list.add(Long.valueOf(idsStr[i]));
            }
            return list;
        }

        public static String toStringWithSeperator(List list,char seperator)
        {
            if(list == null || list.size() == 0)
                return "";
            StringBuilder sb = new StringBuilder();
            int size = list.size();
            for(int i = 0;i < size;i++)
            {
                if(sb.length() == 0)
                {
                    sb.append(list.get(i));
                }
                else
                {
                    sb.append(seperator);
                    sb.append(list.get(i));
                }
            }

            return sb.toString();
        }
    }
    public static class Week
    {
        public final static int MON = 0x1;
        public final static int TUE = 0x2;
        public final static int WED = 0x4;
        public final static int THU = 0x8;
        public final static int FRI = 0x10;
        public final static int SAT = 0x20;
        public final static int SUN = 0x40;
        public final static int NONE=0x00;

        public static boolean isDay(int mode,int d)
        {
             return (mode & d) != 0;
        }

        public static int getWeekFromCalendar(Calendar cal)
        {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int day = MON;
            switch (dayOfWeek)
            {
                case Calendar.MONDAY:
                    day = MON;
                    break;
                case Calendar.TUESDAY:
                    day = TUE;
                    break;
                case Calendar.WEDNESDAY:
                    day = WED;
                    break;
                case Calendar.THURSDAY:
                    day = THU;
                    break;
                case Calendar.FRIDAY:
                    day = FRI;
                    break;
                case Calendar.SATURDAY:
                    day = SAT;
                    break;
                case Calendar.SUNDAY:
                    day = SUN;
                    break;
            }
            return day;
        }
        public static String getRepeatModeString(Context context,int mode)
        {
            String modeStr = "";

            if(isDay(mode,MON))
            {
                if(modeStr.length() == 0)
                    modeStr = context.getText(R.string.week_mon).toString();
                else
                    modeStr = modeStr + "," + context.getText(R.string.week_mon);
            }
            if(isDay(mode,TUE))
            {
                if(modeStr.length() == 0)
                    modeStr = context.getText(R.string.week_tue).toString();
                else
                    modeStr = modeStr + "," + context.getText(R.string.week_tue);
            }
            if(isDay(mode,WED))
            {
                if(modeStr.length() == 0)
                    modeStr = context.getText(R.string.week_wed).toString();
                else
                    modeStr = modeStr + "," + context.getText(R.string.week_wed);
            }
            if(isDay(mode,THU))
            {
                if(modeStr.length() == 0)
                    modeStr = context.getText(R.string.week_thu).toString();
                else
                    modeStr = modeStr + "," + context.getText(R.string.week_thu);
            }
            if(isDay(mode,FRI))
            {
                if(modeStr.length() == 0)
                    modeStr = context.getText(R.string.week_fri).toString();
                else
                    modeStr = modeStr + "," + context.getText(R.string.week_fri);
            }
            if(isDay(mode,SAT))
            {
                if(modeStr.length() == 0)
                    modeStr = context.getText(R.string.week_sat).toString();
                else
                    modeStr = modeStr + "," + context.getText(R.string.week_sat);
            }
            if(isDay(mode,SUN))
            {
                if(modeStr.length() == 0)
                    modeStr = context.getText(R.string.week_sun).toString();
                else
                    modeStr = modeStr + "," + context.getText(R.string.week_sun);
            }
            if(modeStr.length() == 0)
                modeStr = context.getText(R.string.week_never).toString();
            return modeStr;
        }
    }

    public static class Day
    {
        public static String getTimeString(int hour, int min, boolean twenty_four)
        {
            String hourStr = "";
            String minStr = getTimeStr(min);
            if(twenty_four)
            {
                hourStr = getTimeStr(hour);
                return hourStr + ":" + minStr;
            }
            else
            {
                hourStr = getTimeStr(hour % 12);
                if(hour >= 12)
                    return hourStr + ":" + minStr;
                else
                    return hourStr + ":" + minStr;
            }
        }
        public static String getDayString(int year,int month,int day)
        {
            return year + "-" + (month+1) + "-" + day;
        }
        public static String getTimeStr(int hour)
        {
            if(hour >= 10)
                return String.valueOf(hour);
            else
                return "0" + hour;
        }
        public static CharSequence getTimeString(Context context,int hour,int min)
        {
            boolean twenty_four = false;
            PreferenceSet pef = new PreferenceSet(context);
            twenty_four = pef.getBoolean(PreferenceSet.PREF_24_HOUR,true);

            return Day.getTimeString(hour, min, twenty_four);
        }
    }

    public static class PreferenceSet
    {
        public static final String PREF_SERVICE_STARTED = "pref_service_started";
        public final static String PERF_ON_CALL_LAUNCH = "on_call_launch";
        public final static String PREF_24_HOUR = "24_hour";
        public final static String PREF_SLIENT_MODE = "slient_mode";

        private SharedPreferences mPref = null;
        private SharedPreferences.Editor mEditor = null;
        private Context mContext = null;

        public PreferenceSet(Context context)
        {
            mContext = context.getApplicationContext();
            mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        public void beginEdit()
        {
            mEditor = mPref.edit();
        }
        public int getInt(String key,int defValue)
        {
            return mPref.getInt(key, defValue);
        }
        public boolean getBoolean(String key,boolean defValue)
        {
            return mPref.getBoolean(key, defValue);
        }
        public String getString(String key,String defValue)
        {
            return mPref.getString(key, defValue);
        }
        public void putBoolean(String key,boolean value)
        {
            mEditor.putBoolean(key, value);
        }
        public void putInt(String key,int value)
        {
            mEditor.putInt(key, value);
        }
        public void putString(String key,String value)
        {
            mEditor.putString(key, value);
        }
        public void endEdit()
        {
            mEditor.commit();
            mEditor = null;
        }
    }
}
