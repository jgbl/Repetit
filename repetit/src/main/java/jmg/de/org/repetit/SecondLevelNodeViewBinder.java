package jmg.de.org.repetit;

import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import jmg.de.org.repetit.lib.dbSqlite;
import me.texy.treeview.TreeNode;
import me.texy.treeview.base.CheckableNodeViewBinder;

/**
 * Created by zxy on 17/4/23.
 */

public class SecondLevelNodeViewBinder extends CheckableNodeViewBinder {

    TextView textView;
    ImageView imageView;

    public SecondLevelNodeViewBinder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.node_name_view);
        imageView = (ImageView) itemView.findViewById(R.id.arrow_img);
    }

    @Override
    public int getCheckableViewId() {
        return R.id.checkBox;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_second_level;
    }

    @Override
    public void bindView(final TreeNode treeNode) {
        textView.setText(treeNode.getValue().toString());
        imageView.setRotation(treeNode.isExpanded() ? 90 : 0);
    }

    @Override
    public void onNodeToggled(TreeNode treeNode, boolean expand) {
        if (expand) {
            try {
                buildTree(treeNode);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            imageView.animate().rotation(90).setDuration(200).start();
        } else {
            imageView.animate().rotation(0).setDuration(200).start();
        }
    }

    private void buildTree(TreeNode treeNodeParent) throws Throwable {
        if (treeNodeParent.getChildren().size() > 0) return;
        TreeNodeHolderSympt h = (TreeNodeHolderSympt) treeNodeParent.getValue();
        dbSqlite db = new dbSqlite(h.getContext(), false);
        try {
            Cursor c = db.query("Select Symptome.* FROM Symptome WHERE Symptome.ParentSymptomID = " + h.ID + " ORDER BY Symptome.Text");
            try {
                if (c.moveToFirst()) {
                    int ColumnTextId = c.getColumnIndex("Text");
                    int ColumnIDId = c.getColumnIndex("ID");
                    int ColumnShortTextId = c.getColumnIndex("ShortText");
                    int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
                    int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");
                    do {
                        int ID = c.getInt(ColumnIDId);
                        String Text = c.getString(ColumnTextId);
                        String ShortText = c.getString(ColumnShortTextId);
                        Integer KoerperTeilId = c.getInt(ColumnKoerperTeilId);
                        Integer ParentSymptomId = c.getInt(ColumnParentSymptomId);
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderSympt(h.getContext(), h.level + 1, ShortText, "Sympt" + ID, ID, Text, ShortText, KoerperTeilId, ParentSymptomId));
                        treeNode.setLevel(h.level + 1);
                        treeNodeParent.addChild(treeNode);
                    } while (c.moveToNext());
                    this.treeView.expandNode(treeNodeParent);
                }
            } finally {
                c.close();
            }
        } finally {
            db.close();
        }

    }
}