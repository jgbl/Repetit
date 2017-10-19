package jmg.de.org.repetit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;

import static jmg.de.org.repetit.lib.lib.libString.MakeFitForQuery;

/**
 * Created by hmnatalie on 29.08.17.
 */

public class MedActivity extends Fragment
{
    public final static int fragID = 0;
    private static final String TAG = "MedActivity";

    public MainActivity _main;

    protected Toolbar toolbar;
    private ViewGroup viewGroup;
    private TreeNode root;
    public TreeView treeView;
    private AppCompatEditText txtSearch;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _main = ((MainActivity) getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedinstancestate)
    {

        //initTreeView(view);
    }

    public void initTreeView(View view)
    {
        initView(view);

        root = TreeNode.root();
        buildTree("SELECT * FROM Medikamente", false);
        treeView = new TreeView(root, _main, new MyNodeViewFactory());
        View view2 = treeView.getView();
        view2.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewGroup.addView(view2);
        if (_main.treeView == null) _main.treeView = treeView;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstancestate)
    {
        try
        {
            View v = inflater.inflate(R.layout.activity_med, container, false);
            txtSearch = (AppCompatEditText)v.findViewById(R.id.txtSearch);
            txtSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
            txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    String txt = txtSearch.getText().toString();
                    if (!lib.libString.IsNullOrEmpty(txt)) searchSymptoms(txt);
                    return true;
                }
            });
            initTreeView(v);
            return v;
        }
        catch (Throwable ex)
        {
            return null;
        }
    }

    private void searchSymptoms(String searchtxt) {
        if (lib.libString.IsNullOrEmpty(searchtxt)) return;
        String[] txt = searchtxt.split(";");
        try {
            //String qry = "SELECT Medikamente.* FROM Symptome WHERE ";
            String where = "";
            for (String s : txt) {
                if (!lib.libString.IsNullOrEmpty(s)) {
                    if (where != "") where += " OR ";
                    where += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome WHERE ShortText LIKE '%" + MakeFitForQuery(s, true) + "%')";
                    ;
                }
            }
            //AddSymptomeQueryRecursive(root,qry,-1,true);
            String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                    "WHERE Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + where + ")";
            qryMedGrade += " ORDER BY Medikamente.Name, SymptomeOfMedikament.GRADE DESC";
            buildTreeRep(qryMedGrade,true);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    public String getSelectedNodes()
    {
        StringBuilder stringBuilder = new StringBuilder("You have selected: ");
        List<TreeNode> selectedNodes = treeView.getSelectedNodes();
        for (int i = 0; i < selectedNodes.size(); i++)
        {
            if (i < 5)
            {
                stringBuilder.append(selectedNodes.get(i).getValue().toString() + ",");
            } else
            {
                stringBuilder.append("...and " + (selectedNodes.size() - 5) + " more.");
                break;
            }
        }
        return stringBuilder.toString();
    }


    public class TreeNodeHolderMed extends TreeNodeHolder
    {
        public final int ID;
        public final String Name;
        public final String Beschreibung;
        public int totalGrade;
        public int count;

        public TreeNodeHolderMed(MainActivity context, int level, String Text, String path, int ID, String Name, String Beschreibung)
        {
            super(level, Text, path, context);
            this.ID = ID;
            this.Name = Name;
            this.Beschreibung = Beschreibung;
        }
    }


    public void buildTree(String qry, boolean refresh)
    {
        if (root.getChildren().size() > 0)
        {
            List<TreeNode> l = root.getChildren();
            l.clear();
            root.setChildren(l);
        }
        dbSqlite db = ((MainActivity)getActivity()).db;
        try
        {

            Cursor c = db.query(qry);
            try
            {
                if (c.moveToFirst())
                {
                    int ColumnNameId = c.getColumnIndex("Name");
                    int ColumnIDId = c.getColumnIndex("ID");
                    int ColumnBeschreibungId = c.getColumnIndex("Beschreibung");
                    do
                    {
                        int ID = c.getInt(ColumnIDId);
                        String Name = c.getString(ColumnNameId);
                        String Beschreibung = c.getString(ColumnBeschreibungId);
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderMed((MainActivity)getActivity(),0, Name, "Med" + ID, ID, Name, Beschreibung));
                        treeNode.setLevel(0);
                        root.addChild(treeNode);
                    } while (c.moveToNext());
                }
            }
            finally
            {
                c.close();
            }

        }
        finally
        {
            db.close();
        }

        if (refresh && treeView != null) treeView.refreshTreeView();

    }
    public void buildTreeRep(String qry, boolean refresh)
    {
        boolean Initialized = true;
        final String CodeLoc = TAG + ".initTreeview";
        lib.gStatus = CodeLoc + " Start";
        int MedID;
        //ArrayList<TreeNodeHolderMed> arrMed = new ArrayList<>();
        if (root.getChildren().size() > 0)
        {
            List<TreeNode> l = root.getChildren();
            l.clear();
            root.setChildren(l);
        }
        dbSqlite db = ((MainActivity)getActivity()).db;
        try
        {
            Cursor c = db.query(qry);
            try
            {
                if (c.moveToFirst())
                {
                    int ColumnNameId = c.getColumnIndex("Name");
                    int ColumnIDId = c.getColumnIndex("ID");
                    int ColumnBeschreibungId = c.getColumnIndex("Beschreibung");
                    int ColumnGrade = c.getColumnIndex("Grade");

                    do
                    {
                        //if(!c.moveToNext()) break;
                        int ID = c.getInt(ColumnIDId);
                        String Name = c.getString(ColumnNameId);
                        String Beschreibung = c.getString(ColumnBeschreibungId);
                        int sum = 0;
                        sum = c.getInt(ColumnGrade);
                        int nexts = 0;
                        TreeNodeHolderMed hMed = new TreeNodeHolderMed((MainActivity)getActivity(),0, Name, "Med" + ID, ID, Name, Beschreibung);
                        TreeNode treeNode = new TreeNode(hMed);
                        treeNode.setLevel(0);
                        root.addChild(treeNode);
                        insertSymptom(c,treeNode,hMed,ID);
                        while (c.moveToNext() && c.getInt(ColumnIDId) == ID) {
                            insertSymptom(c,treeNode,hMed,ID);
                            nexts += 1;
                            sum += c.getInt(ColumnGrade);
                        }
                        hMed.totalGrade = sum;
                        hMed.count = nexts + 1;
                        hMed.Text += "(" + hMed.totalGrade + "/" + hMed.count + ")";
                    } while (!c.isAfterLast());
                    List<TreeNode> l = root.getChildren();

                    Collections.sort(l, new Comparator<TreeNode>()
                    {
                        @Override
                        public int compare(TreeNode lhs, TreeNode rhs)
                        {
                            TreeNodeHolderMed h1 = (TreeNodeHolderMed) lhs.getValue();
                            TreeNodeHolderMed h2 = (TreeNodeHolderMed) rhs.getValue();
                            if (h1.totalGrade>h2.totalGrade) return -1;
                            if (h1.totalGrade==h2.totalGrade && h1.count>h2.count) return -1;
                            if (h1.totalGrade==h2.totalGrade && h1.count == h2.count) return 0;
                            return 1;
                        }
                    });
                    root.setChildren(l);
                    treeView.refreshTreeView();

                }
            }
            finally
            {
                c.close();
            }

        }
        finally
        {
            db.close();
        }

        if (refresh && treeView != null) treeView.refreshTreeView();

    }

    private void insertSymptom(Cursor c, TreeNode treeNode, TreeNodeHolderMed hMed, int ID)
    {
        final int ColumnTextId = c.getColumnIndex("Text");
        final int ColumnSymptomIDId = c.getColumnIndex("SymptomID");
        final int ColumnShortTextId = c.getColumnIndex("ShortText");
        final int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
        final int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");

        int SympID = c.getInt(ColumnSymptomIDId);
        String Text = c.getString(ColumnTextId);
        String ShortText = c.getString(ColumnShortTextId);
        Integer KoerperTeilId = c.getInt(ColumnKoerperTeilId);
        Integer ParentSymptomId = c.getInt(ColumnParentSymptomId);
        TreeNode treeNode2 = new TreeNode(new TreeNodeHolderSympt(hMed.getContext(), 1, ShortText, "Sympt" + SympID, SympID, Text, ShortText, KoerperTeilId, ParentSymptomId));
        try {
            SymptomsActivity.AddNodesRecursive(hMed.getContext(),0,treeNode2,treeNode,ParentSymptomId);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //treeNode2.setLevel(1);
        //treeNode.addChild(treeNode2);

    }

    private void setLightStatusBar (@NonNull View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int flags = view.getSystemUiVisibility();
            _main.getWindow().setStatusBarColor(Color.WHITE);
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    private void initView(View view)
    {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        viewGroup = (RelativeLayout) view.findViewById(R.id.container);
        //_main.setSupportActionBar(toolbar);
        //setLightStatusBar(viewGroup);
    }
}

