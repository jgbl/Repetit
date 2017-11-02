package me.texy.treeview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;

/**
 * Created by jhmgbl on 01.11.17.
 */

public class ContextMenuRecyclerView extends RecyclerView
{

    private RecyclerViewContextMenuInfo mContextMenuInfo;

    public ContextMenuRecyclerView(Context context)
    {
        super(context);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        int longPressPosition;
        try
        {
            longPressPosition = getChildPosition(originalView);
        }
        catch (Throwable ex)
        {
            longPressPosition = -1;
        }
        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            TreeNode n = ((TreeViewAdapter)getAdapter()).getNodeAt(longPressPosition);
            mContextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId, n);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public final TreeNode treeNode;

        public RecyclerViewContextMenuInfo(int position, long id, TreeNode n) {
            this.position = position;
            this.id = id;
            this.treeNode = n;
        }

        final public int position;
        final public long id;
    }
}