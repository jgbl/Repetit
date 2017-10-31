package jmg.de.org.repetit;

import android.database.Cursor;
import android.os.Build;
import android.view.View;
import android.support.v7.widget.AppCompatImageView;
import android.widget.TextView;

import jmg.de.org.repetit.lib.dbSqlite;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;
import me.texy.treeview.base.CheckableNodeViewBinder;
import me.texy.treeview.base.SpinnerNodeViewBinder;
import android.support.v7.widget.AppCompatImageView;
/**
 * Created by zxy on 17/4/23.
 */

public class FirstLevelNodeViewBinder extends SpinnerNodeViewBinder {
    TextView textView;
    AppCompatImageView imageView;
    public FirstLevelNodeViewBinder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.node_name_view);
        imageView = (AppCompatImageView) itemView.findViewById(R.id.arrow_img);
    }


    @Override
    public int getSpinnerViewId() {
        return R.id.spinner;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_first_level;
    }

    @Override
    public void bindView(final TreeNode treeNode) {
        textView.setText(treeNode.getValue().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            imageView.setRotation(treeNode.isExpanded() ? 90 : 0);
        }
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                FirstLevelNodeViewBinder.this.treeView.collapseNode(treeNode);
                treeNode.getChildren().clear();
                onNodeToggled(treeNode,true);
                return false;
            }
        });
    }

    @Override
    public void onNodeToggled(TreeNode treeNode, boolean expand) {

        if (expand) {
            try {
                buildTree(treeView,treeNode);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                imageView.animate().rotation(90).setDuration(200);
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                imageView.animate().rotation(0).setDuration(200);
            }
        }
    }
    public static void buildTree(TreeView tv, TreeNode treeNodeParent) throws  Throwable {
        if (treeNodeParent.getChildren().size()>0) return;
        TreeNodeHolder h = (TreeNodeHolder) treeNodeParent.getValue();
        int ParentMedID = -1;
        dbSqlite db = h.getContext().db;
        try {
            Cursor c;
            if (h.getClass() == MedActivity.TreeNodeHolderMed.class) {
                throw new RuntimeException("Not a Sympt!");
                //ParentMedID = ((MedActivity.TreeNodeHolderMed)h).ID;
                //c = db.query("Select Symptome.* FROM SymptomeOfMedikament, Symptome WHERE SymptomeOfMedikament.MedikamentID = " + ParentMedID +
                //        " AND Symptome.ParentSymptomID IS Null AND Symptome.ID = SymptomeOfMedikament.SymptomID ORDER BY Symptome.Text");
            }
            else
            {
                c = db.query("Select Symptome.* FROM Symptome WHERE Symptome.ParentSymptomID = " + ((TreeNodeHolderSympt)h).ID + " ORDER BY Symptome.Text COLLATE NOCASE");
            }
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
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderSympt(h.getContext(), 1, ShortText, "Sympt" + ID, ID, Text, ShortText, KoerperTeilId, ParentSymptomId, ParentMedID,0));
                        treeNode.setLevel(1);
                        treeNodeParent.addChild(treeNode);
                    } while (c.moveToNext());
                    tv.expandNode(treeNodeParent);
                }
            } finally {
                c.close();
            }
        }
        finally
        {
            db.close();
        }
    }

}
