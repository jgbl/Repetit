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

    public TreeNodeHolder(){

    }
    public TreeNodeHolder(String Text, String path, MainActivity context)
    {
        this.Text = Text;
        this.path = path;
        this.context = context;
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
}
