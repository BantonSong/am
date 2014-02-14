package com.origintech.alarmx.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.origintech.alarmx.alarm.AlarmItem;
import com.origintech.alarmx.alarm.property.SimpleProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evilatom on 14-1-7.
 */
public class SQLiteDao extends SQLiteOpenHelper
        implements IDao {

    public SQLiteDao(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private void getAlarmsFromDB(SQLiteDatabase db,List<AlarmItem> alarms,String table)
    {
        AlarmItem alarm = null;
        List<String> names = AlarmItem.getSupportedPropertyNames();

        int columns = names.size();
        Cursor c = db.query(table,null,null,null,null,null, AlarmItem.PROPERTY_ID);
        int dataType;

        int i,index;
        String property = null;
        if(c.moveToFirst())
        {
            do
            {
                alarm = AlarmItem.defaultAlarm();
                for(i = 0;i < columns;i++)
                {
                    property = names.get(i);
                    index = c.getColumnIndexOrThrow(property);
                    dataType = alarm.getPropertyValueType(property);
                    switch(dataType)
                    {
                        case SimpleProperty.TYPE_INT:
                            alarm.set(property,c.getInt(index));
                            break;
                        case SimpleProperty.TYPE_TEXT:
                            alarm.set(property,c.getString(index));
                            break;
                    }
                }
                alarms.add(alarm);

            }while(c.moveToNext());
        }
        c.close();
    }

    public synchronized List<AlarmItem> fetchAllAlarms()
    {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<AlarmItem> alarms = new ArrayList<AlarmItem>();

        getAlarmsFromDB(db,alarms,TABLE_ALARM);

        db.close();
        return alarms;
    }

    public synchronized void saveAllAlarms(List<AlarmItem> alarms)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        int size = alarms.size();

        AlarmItem alarm = null;


        for(int i = 0;i < size;i++)
        {
            alarm = alarms.get(i);
            insert(db,alarm,true);
        }

        db.close();
    }

    private void insert(SQLiteDatabase db,AlarmItem alarm,boolean update)
    {
        List<SimpleProperty> propertys = null;
        SimpleProperty property = null;

        propertys = alarm.getAllProperties();
        ContentValues values = new ContentValues();

        for(int j = 0;j < propertys.size();j++)
        {
            property = propertys.get(j);
            if(property.getName().equals(AlarmItem.PROPERTY_ID))
                continue;
            switch(property.getValueType())
            {
                case SimpleProperty.TYPE_INT:
                    values.put(property.getName(),property.getInt());
                    break;
                case SimpleProperty.TYPE_TEXT:
                    values.put(property.getName(),property.getString());
                    break;
            }
        }

        int id;
        String tableName = TABLE_ALARM;

        if(update)
        {
            id = alarm.getInt(AlarmItem.PROPERTY_ID);
            db.update(tableName,values, AlarmItem.PROPERTY_ID + "=" + id,null);
        }
        else
        {
            id = (int)db.insert(tableName,null,values);
            alarm.set(AlarmItem.PROPERTY_ID,id);
        }
        values.clear();
        values = null;
    }

    public synchronized void insertAlarm(AlarmItem alarm)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        insert(db,alarm,false);
        db.close();
    }

    public synchronized void delAlarm(AlarmItem alarm)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        int id = alarm.getInt(AlarmItem.PROPERTY_ID);
        int type = alarm.getInt(AlarmItem.PROPERTY_TYPE);

        db.delete(TABLE_ALARM, AlarmItem.PROPERTY_ID + "=" + id,null);
    }

    public synchronized void updateAlarm(AlarmItem alarm)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        int id = alarm.getInt(AlarmItem.PROPERTY_ID);

        insert(db,alarm,true);
    }

    private static final String DB_NAME = "db_super_alarm";
    private static final int DB_VERSION = 100;

    private static final String TABLE_ALARM = "table_alarm";

    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuilder sb = new StringBuilder();
        sb.append(" CREATE TABLE ");
        sb.append(TABLE_ALARM);
        sb.append(" ( ");

        createCommonColumn(sb);
        createReminderField(sb);

        sb.append(" ) ");

        db.execSQL(sb.toString());
    }

    private void createCommonColumn(StringBuilder sb)
    {
        sb.append(" " + AlarmItem.PROPERTY_ID + " ");
        sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");

        sb.append(" " + AlarmItem.PROPERTY_NAME + " ");
        sb.append(" TEXT NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_HOUR + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_MIN + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_REPEAT + " ");
        sb.append(" INTEGER NOT NULL, ");

/*        sb.append(" " + AlarmItem.PROPERTY_TYPE + " ");
        sb.append(" INTEGER NOT NULL, ");*/

        sb.append(" " + AlarmItem.PROPERTY_ENABLED + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_SNOOZE + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_SNOOZE_DURATION + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_SNOOZE_TIMES + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_ALARM_VOLUMN + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_ALARM_RINGTONE_URI + " ");
        sb.append(" TEXT NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_PARENT_MODE_ENABLE + " ");
        sb.append(" INTEGER NOT NULL, ");

    }

    private void createReminderField(StringBuilder sb)
    {
        sb.append(" " + AlarmItem.PROPERTY_REMINDER_ENABLE + " ");
        sb.append(" INTEGER NOT NULL, ");

        sb.append(" " + AlarmItem.PROPERTY_REMINDER_TEXT + " ");
        sb.append(" TEXT NOT NULL ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
