package jmg.de.org.repetit;

import android.content.Context;
import android.database.Cursor;

import me.texy.treeview.TreeNode;

/**
 * Created by hmnatalie on 14.10.17.
 */

public class TreeNodeHolderSympt extends TreeNodeHolder
{
    public final int ID;
    public final String SymptomText;
    public final String ShortText;
    public final Integer KoerperTeilID;
    public final Integer ParentSymptomID;

    public TreeNodeHolderSympt(MainActivity context, int level, String Text, String path, int ID, String SymptomText, String ShortText, Integer KoerperTeilID, Integer ParentSymptomID)
    {
        super(level,Text,path,context);
        this.level = level;
        this.ID = ID;
        this.SymptomText = SymptomText;
        this.ShortText = ShortText;
        this.KoerperTeilID = KoerperTeilID;
        this.ParentSymptomID = ParentSymptomID;
    }
    public  TreeNodeHolderSympt(MainActivity context, int level, Cursor c)
    {
        int ColumnTextId = c.getColumnIndex("Text");
        int ColumnIDId = c.getColumnIndex("ID");
        int ColumnShortTextId = c.getColumnIndex("ShortText");
        int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
        int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");

        ID = c.getInt(ColumnIDId);
        SymptomText = c.getString(ColumnTextId);
        ShortText = c.getString(ColumnShortTextId);
        KoerperTeilID = c.getInt(ColumnKoerperTeilId);
        ParentSymptomID = c.getInt(ColumnParentSymptomId);
        super.Text = ShortText;
        super.path = ("Sympt"+ID);
        super.level = level;
        super.setContext(context);

    }
}