package com.origintech.alarmx.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.origintech.alarmx.R;
import com.origintech.alarmx.alarm.AlarmItem;
import com.origintech.alarmx.global.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evilatom on 14-1-8.
 */
public class AlarmAdapter extends BaseAdapter
{
    private Context mContext = null;
    private List<AlarmItem> mAlarms = null;
    private int mViewId;


    /////////////////////////////////////////////////////////////////////////
    //监听alarm删除事件接口
    private OnActionInterface mOnActionInterface = null;
    public void setOnActionInterface(OnActionInterface i)
    {
        mOnActionInterface = i;
    }
    public interface OnActionInterface
    {
        public void onDelete(List<AlarmItem> alarms);
        public void onSingleTap(int position);
        public void onLongPress(int position);
        public void onMultiSelectionChanged(int size);
    }

    //////////////////////////////////////////////////////////////////////

    public AlarmAdapter(Context context,List<AlarmItem> alarms,int viewId)
    {
        mContext = context;
        mAlarms = alarms;
        mViewId = viewId;
    }

    public void setAlarms(List<AlarmItem> alarms)
    {
        mAlarms = alarms;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return mAlarms.size();
    }

    @Override
    public AlarmItem getItem(int position)
    {
        return mAlarms.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return mAlarms.get(position).getInt(AlarmItem.PROPERTY_ID);
    }

    ////////////////////////////////////////////////////////////////////////
    //触碰事件

    private GestureDetector.OnGestureListener mGestureDetectorListener =
            new GestureDetector.SimpleOnGestureListener()
    {
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            if(mOnActionInterface != null && mSelectedPosition >= 0)
                mOnActionInterface.onSingleTap(mSelectedPosition);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            if(mOnActionInterface != null && mSelectedPosition >= 0)
                mOnActionInterface.onLongPress(mSelectedPosition);
        }

                @Override
        public void onShowPress(MotionEvent e)
        {
            startDelete();
        }

                @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            if(mStartDeleting)
            {
                deleting((int)e1.getRawX(),(int)e2.getRawX());
                return true;
            }
            else
            {
                return false;
            }
        }

    };
    private GestureDetector mGestureDetector = new GestureDetector(mContext,mGestureDetectorListener);
    public GestureDetector getGestureDetector()
    {
        return mGestureDetector;
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
            {
                mSelectedView = v;
            }
            return false;
        }
    };
    //////////////////////////////////////////////////////////////////////
    //滑动删除相关
    private int mSelectedPosition = -1;
    private View mSelectedView = null;

    private int mSelectedViewWidth = 0;
    private int mSelectedViewMoved = 0;
    private boolean mStartDeleting = false;

    //用于后续删除操作
    private View mSelectedBackupView = null;
    private int mSelectedBackupPosition = -1;

    private float mFromX = 0;
    private float mToX = 0;

    public boolean isDeleting()
    {
        return mStartDeleting;
    }
    private void startDelete()
    {
        //确实选中一个listItem
        if(mSelectedView != null)
        {
            mSelectedPosition = (Integer)mSelectedView.getTag();
            mSelectedViewWidth = mSelectedView.getWidth();
            mSelectedViewMoved = 0;
            mStartDeleting = true;
        }
    }
    private void deleting(int rawX1,int rawX2)
    {
        //移动view
        mSelectedViewMoved = rawX2 - rawX1;
        int left = mSelectedViewMoved;
        int right = left + mSelectedViewWidth;
        mSelectedView.layout(left, mSelectedView.getTop(),right, mSelectedView.getBottom());
    }
    public void endDelete()
    {
        if(!mStartDeleting)
            return;
        mStartDeleting = false;
        //用于后续的删除操作处理
        mSelectedBackupView = mSelectedView;
        mSelectedView = null;
        mSelectedBackupPosition = mSelectedPosition;
        mSelectedPosition = -1;

        int halfWidth = (mSelectedViewWidth >> 1);

        if(Math.abs(mSelectedViewMoved) > halfWidth)
        {
            mFromX = mSelectedViewMoved;
            if(mSelectedViewMoved > 0)
                mToX = mSelectedViewWidth + 50;
            else
                mToX = -mSelectedViewWidth - 50;

        }
        else
        {
            mFromX = mSelectedViewMoved;
            mToX = 0;
            mSelectedBackupPosition = -1;
        }
        mSelectedViewMoved = 0;
        scrollView(mFromX,mToX,500);
    }
    //删除中
    private void onDelete()
    {
        if(mOnActionInterface != null && mSelectedBackupPosition >= 0)
        {
            List<AlarmItem> alarms = new ArrayList<AlarmItem>();
            alarms.add(getItem(mSelectedBackupPosition));
            mOnActionInterface.onDelete(alarms);
        }
    }
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_SCROOL_VIEW:
                    mSelectedBackupView.layout((int)mCurrentX
                            ,mSelectedBackupView.getTop()
                            ,(int)mCurrentX + mSelectedViewWidth
                            ,mSelectedBackupView.getBottom());
                    break;
                case  MSG_SCROOL_OVER:
                    //删除对应的闹钟
                    onDelete();
                    break;
            }
        }
    };
    private float mCurrentX = 0;
    private float mCurrentRight = 0;
    private final int MSG_SCROOL_VIEW = 0x1;
    private final int MSG_SCROOL_OVER = 0x2;

    private void scrollView(final float from,final float to,final int time /*毫秒*/)
    {
        final int TIME_UNIT = 50; //毫秒
        new Thread()
        {
            @Override
            public void run()
            {
                mCurrentX = from;
                float distance = to - from;
                float speed = distance / time * TIME_UNIT;
                do
                {
                    mCurrentX += distance;
                    mHandler.sendEmptyMessage(MSG_SCROOL_VIEW);
                    try
                    {
                        sleep(TIME_UNIT);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }while (mCurrentX < to);

                mHandler.sendEmptyMessage(MSG_SCROOL_OVER);
            }
        }.start();
    }
    private int getAlphaFromDistance(int distance)
    {
        int alpha = 100;
        alpha =(int) (((float)distance / (float) mSelectedViewWidth) * (float)150);
        return alpha;
    }
    //////////////////////////////////////////////////////////////////////
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = null;
        if(convertView == null)
        {
            view = View.inflate(mContext,mViewId,null);
        }
        else
        {
            view = convertView;
        }
        view.setTag(position);
        view.setOnTouchListener(mOnTouchListener);
        refreshAlarmView(view, position);
        return view;
    }

    private View.OnClickListener mOnSwitchClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            ImageView img = (ImageView)v;
            int position = (Integer)img.getTag();
            AlarmItem alarm = mAlarms.get(position);
            if(alarm.isEnable())
            {
                alarm.set(AlarmItem.PROPERTY_ENABLED,0);
                img.setBackgroundResource(R.drawable.alarm_switch_off);
            }
            else
            {
                alarm.set(AlarmItem.PROPERTY_ENABLED,1);
                img.setBackgroundResource(R.drawable.alarm_switch_on);
            }
        }
    };
    protected void refreshAlarmView(View v,int position)
    {
        AlarmItem alarm = mAlarms.get(position);
        //load views
        boolean mode_24hours = false;
        Global.PreferenceSet pf = new Global.PreferenceSet(mContext);
        mode_24hours = pf.getBoolean(Global.PreferenceSet.PREF_24_HOUR,false);

        TextView alarmTime = (TextView)v.findViewById(R.id.list_alarm_time);
        TextView alarmApm = (TextView)v.findViewById(R.id.list_alarm_time_apm);
        if(mode_24hours)
        {
            alarmApm.setVisibility(View.GONE);
        }
        else
        {
            alarmApm.setVisibility(View.VISIBLE);
            if(alarm.isPM())
                alarmApm.setText(R.string.daytime_pm);
            else
                alarmApm.setText(R.string.daytime_am);
        }
        alarmTime.setText(alarm.getTimeString(mode_24hours));


        ImageView alarmSwitch = (ImageView)v.findViewById(R.id.list_alarm_switch);
        alarmSwitch.setTag(position);
        alarmSwitch.setOnClickListener(mOnSwitchClicked);
        if(alarm.isEnable())
            alarmSwitch.setBackgroundResource(R.drawable.alarm_switch_on);
        else
            alarmSwitch.setBackgroundResource(R.drawable.alarm_switch_off);

        TextView alarmName = (TextView)v.findViewById(R.id.list_alarm_name);
        String name = alarm.getString(AlarmItem.PROPERTY_NAME);
        if(name.length() == 0)
            alarmName.setVisibility(View.GONE);
        else
        {
            alarmName.setVisibility(View.VISIBLE);
            alarmName.setText(name);
        }

        TextView alarmRepeat = (TextView)v.findViewById(R.id.list_alarm_repeat);
        alarmRepeat.setText(alarm.getRepeatString(mContext));

        ImageView alarmRingtone = (ImageView)v.findViewById(R.id.list_alarm_ringtone);
        ImageView alarmVibration = (ImageView)v.findViewById(R.id.list_alarm_vibration);
        ImageView alarmLock = (ImageView)v.findViewById(R.id.list_alarm_lock);
        ImageView alarmRemind = (ImageView)v.findViewById(R.id.list_alarm_remind);
    }
}
