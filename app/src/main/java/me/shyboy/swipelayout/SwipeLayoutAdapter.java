package me.shyboy.swipelayout;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by foul on 14/12/5.
 */
public abstract class SwipeLayoutAdapter<T> extends ArrayAdapter
{
    private static final int _resourceId = R.layout.item_swipe;
    int _contentViewResourceId;   //item的内容区 的id
    int _actionViewResourceId; //item操作区(滑动显示区域)的
    HorizontalScrollView _currentActiveHSV = null;//当前actionView处于显示状态的HorizontalScrollView
    private int _itemWidth = 0;

    /**
     * 构造函数
     * @param context
     * @param contentViewResourceId
     * @param actionViewResourceId
     * @param objects
     */
    public SwipeLayoutAdapter(Context context,int contentViewResourceId,int actionViewResourceId,T[] objects)
    {
        super(context,R.layout.item_swipe,objects);
        _contentViewResourceId = contentViewResourceId;
        _actionViewResourceId = actionViewResourceId;
    }

    /**
     * 构造函数
     * @param context
     * @param contentViewResourceId
     * @param actionViewResourceId
     * @param objects
     */
    public SwipeLayoutAdapter(Context context,int contentViewResourceId,int actionViewResourceId,List<T> objects)
    {
        super(context,R.layout.item_swipe,objects);
        _contentViewResourceId = contentViewResourceId;
        _actionViewResourceId = actionViewResourceId;
    }
    /**
     * 用户设置自定义的View(item展示的内容区)
     * @param contentView
     * @param position
     * @param parent
     */
    public abstract void setContentView(View contentView,int position,HorizontalScrollView parent);

    /**
     * 用户设置自定义的View(隐藏的操作区)
     * @param actionView
     * @param position
     * @param parent
     */
    public abstract void setActionView(View actionView,int position,HorizontalScrollView parent);

    @Override
    public View getView(int position,View convertView, ViewGroup parent)
    {
        final ViewHolder viewHolder;
        if(convertView == null)
        {
            //获取Item
            convertView = LayoutInflater.from(getContext()).inflate(_resourceId,parent,false);
            //获取内容显示区域的View
            View contentView = LayoutInflater.from(getContext()).inflate(_contentViewResourceId,parent,false);
            //获取操作区的View
            View actionView = LayoutInflater.from(getContext()).inflate(_actionViewResourceId,parent,false);
            viewHolder = new ViewHolder();
            //获取item组件
            viewHolder.hSView = (HorizontalScrollView)convertView.findViewById(R.id.hsv);
            viewHolder.viewContainer = (LinearLayout)convertView.findViewById(R.id.item_view_container);
            //把内容区和操作区添加到item
            viewHolder.viewContainer.addView(contentView);
            viewHolder.viewContainer.addView(actionView);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        //获取item的宽度
        ViewTreeObserver vto = viewHolder.hSView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(_itemWidth == 0)
                {
                    _itemWidth = viewHolder.hSView.getMeasuredWidth();
                    notifyDataSetChanged();
                }
            }
        });
        //设置默认宽度
        ViewGroup.LayoutParams lpContent =viewHolder.viewContainer.getChildAt(0).getLayoutParams();
        lpContent.width = _itemWidth;
        //设置默认宽度
        ViewGroup.LayoutParams lpAction = viewHolder.viewContainer.getChildAt(1).getLayoutParams();
        lpAction.width = _itemWidth / 3 * 2;
        //定义item显示的内容区
        setContentView(viewHolder.viewContainer.getChildAt(0),position,viewHolder.hSView);
        //定义item隐藏的操作区域
        setActionView(viewHolder.viewContainer.getChildAt(1),position,viewHolder.hSView);

        convertView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                //获得ViewHolder
                ViewHolder viewHolder = (ViewHolder) v.getTag();

                if(_currentActiveHSV != null && _currentActiveHSV != viewHolder.hSView)
                {
                    _currentActiveHSV.smoothScrollTo(0,0);
                    _currentActiveHSV = null;
                    return true;
                }
                switch (event.getAction())
                {

                    case MotionEvent.ACTION_UP:
                        //获得HorizontalScrollView滑动的水平方向值.
                        int scrollX = viewHolder.hSView.getScrollX();

                        //获得操作区域的长度
                        int actionW = viewHolder.viewContainer.getChildAt(1).getWidth();

                        //注意使用smoothScrollTo,这样效果看起来比较圆滑,不生硬
                        //如果水平方向的移动值<操作区域的长度的一半,就复原
                        if (scrollX < actionW / 2)
                        {
                            viewHolder.hSView.smoothScrollTo(0, 0);
                        }
                        else//否则的话显示操作区域
                        {
                            viewHolder.hSView.smoothScrollTo(actionW, 0);
                            _currentActiveHSV = viewHolder.hSView;
                        }
                        return true;
                }
                return false;
            }
        });
        return convertView;
    }
    /*
     *ViewHolder
     */
    class ViewHolder
    {
        HorizontalScrollView hSView;
        LinearLayout viewContainer;
    }
}
