package jmg.de.org.repetit.lib;

/**
 * Created by jhmgbl on 28.10.17.
 */

public class ProgressClass
{
    public boolean blnRestart;
    public int counter;
    public int max;
    public String msg;

    public ProgressClass(int counter, int max, String msg, boolean blnRestart)
    {
        this.counter = counter;
        this.msg = msg;
        this.max = max;
        this.blnRestart = blnRestart;
    }

    public void update(int counter, int max, String msg, boolean blnRestart)
    {
        this.counter = counter;
        this.msg = msg;
        this.max = max;
        this.blnRestart = blnRestart;
    }
}
