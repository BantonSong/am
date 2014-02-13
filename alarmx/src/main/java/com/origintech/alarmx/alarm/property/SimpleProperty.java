package com.origintech.alarmx.alarm.property;

/**
 * Created by evilatom on 14-1-7.
 */
public class SimpleProperty {
    public static final int TYPE_INT = 0x02;
    public static final int TYPE_TEXT = 0x03;

    public final int mType;
    public final String mName;
    public Object mValue;
    public SimpleProperty(String name,int type,int nullFlag)
    {
        mName = name;
        mType = type;
        mValue = null;
    }
    public SimpleProperty(String name,int value)
    {
        mType = TYPE_INT;
        mName = name;
        mValue = Integer.valueOf(value);
    }
    public SimpleProperty(String name,String value)
    {
        mType = TYPE_TEXT;
        mName = name;
        mValue = value;
    }
    public SimpleProperty(SimpleProperty pro)
    {
        mName = pro.mName;
        mType = pro.mType;
        mValue = pro.mValue;
    }

    public String getName()
    {
        return mName;
    }

    public int getInt()
    {
        if(mType == TYPE_INT)
        {
            return ((Integer)mValue).intValue();
        }
        else
            return 0;
    }
    public boolean set(int value)
    {
        if(mType == TYPE_INT)
        {
            mValue = Integer.valueOf(value);
            return true;
        }
        else
            return false;
    }
    public String getString()
    {
        if(mType == TYPE_TEXT)
        {
            return (String)mValue;
        }
        else
            return "";
    }
    public boolean set(String value)
    {
        if(mType == TYPE_TEXT)
        {
            mValue = value;
            return true;
        }
        else
            return false;
    }

    public int getValueType()
    {
        return mType;
    }
}
