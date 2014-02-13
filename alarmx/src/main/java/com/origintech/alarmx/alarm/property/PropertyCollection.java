package com.origintech.alarmx.alarm.property;

/**
 * Created by evilatom on 14-1-7.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PropertyCollection
{
    private HashMap<String,SimpleProperty> mPropertys = null;

    public PropertyCollection()
    {
        mPropertys = new HashMap<String,SimpleProperty>();
    }

    public synchronized void addProperty(SimpleProperty pro)
    {
        mPropertys.put(pro.mName, pro);
    }

    public  boolean delProperty(SimpleProperty pro)
    {
        return delProperty(pro.mName);
    }

    public synchronized boolean delProperty(String key)
    {
        if(hasProperty(key))
        {
            mPropertys.remove(key);
            return true;
        }
        else
            return false;
    }

    public synchronized boolean set(String key,SimpleProperty value)
    {
        if(hasProperty(key))
        {
            mPropertys.put(key, value);
            return true;
        }
        else
            return false;
    }

    public synchronized boolean set(String key,int value)
    {
        return mPropertys.get(key).set(value);
    }
    public synchronized boolean set(String key,String value)
    {
        return mPropertys.get(key).set(value);
    }
    public synchronized int getInt(String key)
    {
        if(this.hasProperty(key))
            return mPropertys.get(key).getInt();
        else
            return -1;
    }

    public synchronized String getString(String key)
    {
        if(this.hasProperty(key))
            return mPropertys.get(key).getString();
        else
            return "";
    }

    public synchronized SimpleProperty get(String key)
    {
        return new SimpleProperty(mPropertys.get(key));
    }

    public synchronized boolean hasProperty(String key)
    {
        return mPropertys.containsKey(key);
    }

    public synchronized List<SimpleProperty> getAllProperties()
    {
        Collection<SimpleProperty> values = mPropertys.values();
        ArrayList<SimpleProperty> list = new ArrayList<SimpleProperty>();

        Iterator<SimpleProperty> it = mPropertys.values().iterator();
        while (it.hasNext())
        {
            list.add(it.next());
        }
        //return mPropertys.values();
        return list;
    }

    public synchronized int size()
    {
        return mPropertys.size();
    }
}
