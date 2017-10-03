package jmg.de.org.repetit.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class HackyViewPager extends android.support.v4.view.ViewPager
{

    public HackyViewPager(Context context)
    {
        super(context);
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        try
        {
            return super.onInterceptTouchEvent(ev);
        }
        catch (IllegalArgumentException e)
        {
            //uncomment if you really want to see these errors
            //e.printStackTrace();
            return false;
        }
    }
}
