package com.origintech.alarmx.dao;

import com.origintech.alarmx.alarm.AlarmItem;

import java.util.List;

/**
 * Created by evilatom on 14-1-7.
 */
public interface IDao
{
    public List<AlarmItem> fetchAllAlarms();
    public void saveAllAlarms(List<AlarmItem> alarms);
    public void insertAlarm(AlarmItem alarm);
    public void delAlarm(AlarmItem alarm);
    public void updateAlarm(AlarmItem alarm);
}
