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


    protected void refreshAlarmView(View v,int position)
    {
        AlarmItem alarm = mAlarms.get(position);
        //load views
        TextView alarmTime = (TextView)v.findViewById(R.id.list_alarm_time);
        TextView alarmApm = (TextView)v.findViewById(R.id.list_alarm_time_apm);
        ImageView alarmSwitch = (ImageView)v.findViewById(R.id.list_alarm_switch);

        TextView alarmName = (TextView)v.findViewById(R.id.list_alarm_name);
        TextView alarmRepeat = (TextView)v.findViewById(R.id.list_alarm_repeat);
        ImageView alarmRingtone = (ImageView)v.findViewById(R.id.list_alarm_ringtone);
        ImageView alarmVibration = (ImageView)v.findViewById(R.id.list_alarm_vibration);
        ImageView alarmLock = (ImageView)v.findViewById(R.id.list_alarm_lock);
        ImageView alarmRemind = (ImageView)v.findViewById(R.id.list_alarm_remind);

        //set views
        alarmName.setText(alarm.getString(AlarmItem.PROPERTY_NAME));
        alarmTime.setText(Global.Day.getTimeString(mContext,
                alarm.getInt(AlarmItem.PROPERTY_HOUR),
                alarm.getInt(AlarmItem.PROPERTY_MIN)));
    }
}
