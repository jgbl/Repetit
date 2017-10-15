package jmg.de.org.repetit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;

import static android.R.attr.prompt;
import static jmg.de.org.repetit.lib.lib.OpenDialogs;

/**
 * Created by hmnatalie on 29.08.17.
 */

public class SymptomsActivity extends Fragment {
public final  static int fragID = 1;

    public MainActivity _main;

    protected Toolbar toolbar;
    private ViewGroup viewGroup;
    private TreeNode root;
    public TreeView treeView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _main = ((MainActivity) getActivity());
    }
    @Override
    public void onViewCreated(View view, Bundle savedinstancestate) {
        //initTreeView(view);
    }

    public void initTreeView(View view) throws Throwable {
        initView(view);

        root = TreeNode.root();
        buildTree(root,"Select Symptome.* FROM Symptome WHERE Symptome.ParentSymptomID IS Null ORDER BY Text");
        treeView = new TreeView(root, _main, new MyNodeViewFactory());
        View view2 = treeView.getView();
        view2.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewGroup.addView(view2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstancestate) {
        try
        {
            View v = inflater.inflate(R.layout.activity_sympt, container, false);
            initTreeView(v);
            return v;
        }
        catch (Throwable ex)
        {
            lib.ShowException(_main,ex);
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {

        menuInflater.inflate(R.menu.symptoms_menu, menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_find_symptoms:
                AlertDialog.Builder A = new AlertDialog.Builder(getContext());
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText("");
                A.setView(input);

                A.setPositiveButton(getContext().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String txt = input.getText().toString();
                        if (!lib.libString.IsNullOrEmpty(txt))
                        {
                            try {
                                buildTree(root,"SELECT * FROM Symptoms WHERE ShortText LIKE '%" + lib.libString.MakeFitForQuery(txt,true) + "%'");
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    }
                });
                A.setNegativeButton(getContext().getString(R.string.cancel), null);
                A.setMessage(getContext().getString(R.string.msg_find_symptoms));
                A.setTitle(getContext().getString(R.string.msg_find_title));
                AlertDialog dlg = A.create();
                break;
            case R.id.mnu_qry_med:
                String qry = getQueryMed();
                ((MainActivity)getActivity()).fPA.fragMed.buildTree(qry);
                break;
            }
        return super.onOptionsItemSelected(item);
    }

    private String getQueryMed() {
        String qry;
        for (TreeNode t : treeView.getSelectedNodes())
        {
            If qrySymptMed <> "" Then
            If True OrElse OrFlag Then
            qrySymptMed &= " OR "
            Else
            qrySymptMed &= " AND "
            End If
            End If
            If Not Wide Then
            qry = qry &
                    "ID in (Select MedikamentID from SymptomeOfMedikament where SymptomID = " & Child2.Tag!ID & ")"
            Else
                    qry = qry &
                    "ID in (Select MedikamentID from SymptomeOfMedikament where SymptomID IN (SELECT ID FROM Symptome WHERE Text LIKE '%" & MakeFitForQuery(Child2.Tag!Text) & "%'))"
            End If
            qrySymptMed &= "SymptomeOfMedikament.SymptomID = " & Child2.Tag!ID

        }

    }

    public String getSelectedNodes() {
        StringBuilder stringBuilder = new StringBuilder("You have selected: ");
        List<TreeNode> selectedNodes = treeView.getSelectedNodes();
        for (int i = 0; i < selectedNodes.size(); i++) {
            if (i < 5) {
                stringBuilder.append(selectedNodes.get(i).getValue().toString() + ",");
            } else {
                stringBuilder.append("...and " + (selectedNodes.size() - 5) + " more.");
                break;
            }
        }
        return stringBuilder.toString();
    }

    private void buildTree(TreeNode treeNodeParent, String qry) throws  Throwable {
        if (treeNodeParent.getChildren().size()>0) return;
        //MedActivity.TreeNodeHolderMed h = (MedActivity.TreeNodeHolderMed) treeNodeParent.getValue();
        dbSqlite db = new dbSqlite(getContext(),false);
        try {
            Cursor c = db.query(qry);
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
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderSympt(getContext(), 1, ShortText, "Sympt" + ID, ID, Text, ShortText, KoerperTeilId, ParentSymptomId));
                        treeNode.setLevel(0);
                        treeNodeParent.addChild(treeNode);
                    } while (c.moveToNext());
                    //this.treeView.expandNode(treeNodeParent);
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

    private void setLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            _main.getWindow().setStatusBarColor(Color.WHITE);
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    private void initView(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        viewGroup = (RelativeLayout) view.findViewById(R.id.container);
        //_main.setSupportActionBar(toolbar);
        //setLightStatusBar(viewGroup);
    }
}

