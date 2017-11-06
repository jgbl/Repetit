package jmg.de.org.repetit.lib;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jmg.de.org.repetit.SQLiteDataActivity;

public class ListAdapter extends BaseAdapter
{

    private Cursor cursor;
    Context context;
    ArrayList<String> ID_Array;
    ArrayList<String> date_Array;
    ArrayList<String> ex_Array;
    ArrayList<String> CodeLoc_Array;
    ArrayList<String> comment_Array;
    ArrayList<String> exported_Array;


    public ListAdapter(
            Context context2,
            ArrayList<String> ID_Array,
            ArrayList<String> date_Array,
            ArrayList<String> ex_Array,
            ArrayList<String> CodeLoc_Array,
            ArrayList<String> comment_Array,
            ArrayList<String> exported_Array
    )
    {

        this.context = context2;
        this.ID_Array = ID_Array;
        this.date_Array = date_Array;
        this.ex_Array = ex_Array;
        this.CodeLoc_Array = CodeLoc_Array;
        this.comment_Array = comment_Array;
        this.exported_Array = exported_Array;
    }

    public ListAdapter(SQLiteDataActivity sqLiteDataActivity, Cursor cursor)
    {
        this.context = sqLiteDataActivity.getContext();
        this.cursor = cursor;
    }

    public int getCount()
    {
        // TODO Auto-generated method stub
        return cursor.getCount();
    }

    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View child, ViewGroup parent)
    {

        Holder holder;

        LayoutInflater layoutInflater;

        if (child == null)
        {
            RelativeLayout layout = new RelativeLayout(context);
            holder = new Holder();
            TextView t = null;
            for (int i = 0; i < cursor.getColumnCount(); i++)
            {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                if (t != null)
                {
                    params.addRule(RelativeLayout.BELOW, t.getId());
                }
                t = new TextView(context);
                t.setId(i+1);
                layout.addView(t, params);
                holder.views.add(t);
            }

            child = layout;

            child.setTag(holder);

        } else
        {

            holder = (Holder) child.getTag();
        }
        cursor.moveToPosition(position);
        for (int i = 0; i < cursor.getColumnCount(); i++)
        {
            TextView t = holder.views.get(i);
            t.setText(cursor.getString(i));
        }


        return child;
    }

    public class Holder
    {

        ArrayList<TextView> views = new ArrayList<>();
    }

}
