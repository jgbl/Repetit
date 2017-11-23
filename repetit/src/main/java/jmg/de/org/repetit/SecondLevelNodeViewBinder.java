package jmg.de.org.repetit;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.AppCompatImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;
import me.texy.treeview.base.CheckableNodeViewBinder;
import me.texy.treeview.base.SpinnerNodeViewBinder;

/**
 * Created by zxy on 17/4/23.
 */

public class SecondLevelNodeViewBinder extends SpinnerNodeViewBinder {

    TextView textView;
    AppCompatImageView imageView;
    View  itemView;
    LinearLayout linLayout;
    public SecondLevelNodeViewBinder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        textView = (TextView) itemView.findViewById(R.id.node_name_view);
        imageView = (AppCompatImageView) itemView.findViewById(R.id.arrow_img);
        linLayout = (LinearLayout) itemView.findViewById(R.id.node_container);
        itemView.setLongClickable(true);
    }

    @Override
    public int getSpinnerViewId() {
        return R.id.spinner;
    }


    @Override
    public int getLayoutId() {
        return R.layout.item_second_level;
    }

    @Override
    public void bindView(final TreeNode treeNode) {
        treeNode.holder = this;
        textView.setText(treeNode.getValue().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            imageView.setRotation(treeNode.isExpanded() ? 90 : 0);
        }
        int level = treeNode.getLevel();
        if (level > 5) level = level - 5;
        switch (level)
        {
            case 2:
                //relLayout.setBackground(null);
                itemView.setBackgroundColor(Color.BLUE);
                break;
            case 3:
                itemView.setBackgroundColor(Color.DKGRAY);
                break;
            case 4:
                itemView.setBackgroundColor(Color.rgb(104,69,69));
                break;
            case 5:
                itemView.setBackgroundColor(Color.BLACK);
        }
        if (treeNode.getLevel()>1)
        {
            RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) linLayout.getLayoutParams();
            l.setMargins(lib.dpToPx(40) + (treeNode.getLevel() - 1) * lib.dpToPx(20), 0, 0, 0);
            linLayout.setLayoutParams(l);
        }
        /*
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SecondLevelNodeViewBinder.this.treeView.collapseNode(treeNode);
                treeNode.getChildren().clear();
                onNodeToggled(treeNode,true);
                return false;
            }
        });
        */

    }

    @Override
    public void onNodeToggled(TreeNode treeNode, boolean expand) {
        onNodeToggled(treeNode,expand,null);
    }

    @Override
    public void onNodeToggled(TreeNode treeNode, boolean expand, ArrayList<TreeNode>children) {
        if (expand) {
            try {
                buildTree(treeView,treeNode,children);
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

    public static void buildTree(TreeView tv, TreeNode treeNodeParent, ArrayList<TreeNode> children) throws Throwable {
        if (treeNodeParent.getChildren().size() > 0) return;
        TreeNodeHolderSympt h = (TreeNodeHolderSympt) treeNodeParent.getValue();
        dbSqlite db = h.getContext().db;
        try {
            Cursor c;
            if (h.ParentMedID == -1)
            {
                c = db.query("Select Symptome.* FROM Symptome WHERE Symptome.ParentSymptomID = " + h.ID + " ORDER BY Symptome.Text COLLATE NOCASE");
            }
            else
            {
                c = db.query("Select Symptome.*, SymptomeOfMedikament.Grade FROM SymptomeOfMedikament, Symptome WHERE SymptomeOfMedikament.MedikamentID = " + h.ParentMedID +
                        " AND Symptome.ParentSymptomID = " + h.ID + " AND Symptome.ID = SymptomeOfMedikament.SymptomID ORDER BY Symptome.Text COLLATE NOCASE");
            }
            try {
                if (c.moveToFirst()) {
                    int ColumnTextId = c.getColumnIndex("Text");
                    int ColumnIDId = c.getColumnIndex("ID");
                    int ColumnShortTextId = c.getColumnIndex("ShortText");
                    int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
                    int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");
                    int ColumnGradeId = c.getColumnIndex("Grade");
                    do {
                        int ID = c.getInt(ColumnIDId);
                        String Text = c.getString(ColumnTextId);
                        boolean found = false;
                        if (children != null)
                        {
                            for (TreeNode T: children)
                            {
                                if (((TreeNodeHolderSympt) T.getValue()).SymptomText.equalsIgnoreCase(Text))
                                {
                                    treeNodeParent.addChild(T);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found) continue;
                        String ShortText = c.getString(ColumnShortTextId);
                        Integer KoerperTeilId = c.getInt(ColumnKoerperTeilId);
                        Integer ParentSymptomId = c.getInt(ColumnParentSymptomId);
                        Integer Grade = (ColumnGradeId>=0?c.getInt(ColumnGradeId):0);
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderSympt(h.getContext(), h.level + 1, ShortText + ((Grade > 0) ? "(" + Grade + ")" : "") , "Sympt" + ID, ID, Text, ShortText, KoerperTeilId, ParentSymptomId,h.ParentMedID,Grade));
                        treeNode.setLevel(h.level + 1);
                        treeNodeParent.addChild(treeNode);
                    } while (c.moveToNext());
                    tv.expandNode(treeNodeParent);
                }
            } finally {
                c.close();
            }
        } finally {
            db.close();
        }

    }
}