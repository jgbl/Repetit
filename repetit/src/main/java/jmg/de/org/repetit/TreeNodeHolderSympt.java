package jmg.de.org.repetit;

import android.content.Context;

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
    public final int level;

    public TreeNodeHolderSympt(MainActivity context, int level, String Text, String path, int ID, String SymptomText, String ShortText, Integer KoerperTeilID, Integer ParentSymptomID)
    {
        super(Text,path,context);
        this.level = level;
        this.ID = ID;
        this.SymptomText = SymptomText;
        this.ShortText = ShortText;
        this.KoerperTeilID = KoerperTeilID;
        this.ParentSymptomID = ParentSymptomID;
    }
}