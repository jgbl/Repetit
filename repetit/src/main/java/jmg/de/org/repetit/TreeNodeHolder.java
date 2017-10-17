package jmg.de.org.repetit;

import android.content.Context;

/**
 * Created by hmnatalie on 14.10.17.
 */

public class TreeNodeHolder
{
    private MainActivity context;
    public String Text;
    public String path;
    public int level;

    public TreeNodeHolder(){

    }
    public TreeNodeHolder(int level, String Text, String path, MainActivity context)
    {
        this.Text = Text;
        this.path = path;
        this.context = context;
        this.level = level;
    }

    public MainActivity getContext()
    {
        return  context;
    }

    @Override
    public String toString()
    {
        return Text;
    }

    public void setContext(MainActivity context) {
        this.context = context;
    }
}

