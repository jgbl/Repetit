package jmg.de.org.repetit;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;

import static jmg.de.org.repetit.lib.lib.libString.MakeFitForQuery;


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
    private ImageButton btnSearchAnd;
    private ImageButton btnSearchOr;

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
        if (!lib.libString.IsNullOrEmpty(_main.lastQuery)) {
            String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                    "WHERE Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + _main.lastQuery + ")";
            qryMedGrade += " ORDER BY Medikamente.Name, SymptomeOfMedikament.GRADE DESC";
            buildTreeRep(qryMedGrade, false, null);
        }
        else
        {
            buildTree("SELECT * FROM Medikamente ORDER BY Name", false);
        }
        treeView = new TreeView(root, _main, new MyNodeViewFactoryMed());
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
                    if (!lib.libString.IsNullOrEmpty(txt)) searchSymptoms(txt, true);
                    return true;
                }
            });
            btnSearchAnd = (ImageButton) v.findViewById(R.id.btnSearchAnd);
            btnSearchAnd.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String txt = txtSearch.getText().toString();
                    if (!lib.libString.IsNullOrEmpty(txt)) searchSymptoms(txt,true);
                }
            });
            btnSearchOr = (ImageButton) v.findViewById(R.id.btnSearchOr);
            btnSearchOr.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String txt = txtSearch.getText().toString();
                    if (!lib.libString.IsNullOrEmpty(txt)) searchSymptoms(txt,false);
                }
            });
            initTreeView(v);
            restoreTreeView(savedinstancestate);
            return v;
        }
        catch (Throwable ex)
        {
            return null;
        }
    }

    private void restoreTreeView(Bundle savedinstancestate) throws Throwable
    {
        if (treeView != null && savedinstancestate != null)
        {
            if (root != null)
            {
                ArrayList<Integer> expMed = savedinstancestate.getIntegerArrayList("expMed");
                ArrayList<Integer> expMedSymp = savedinstancestate.getIntegerArrayList("expMedSymp");
                ArrayList<Integer> Selected = savedinstancestate.getIntegerArrayList("Selected");
                if (expMed.size() == 0 ) return;
                for (TreeNode t : root.getChildren())
                {
                    if (t.hasChild() == false || true)
                    {
                        TreeNodeHolderMed h = (TreeNodeHolderMed) t.getValue();
                        if (expMed.contains(h.ID))
                        {
                            expMed.remove(new Integer(h.ID));
                            if (t.hasChild() == false) FirstLevelNodeViewBinderMed.buildTree(treeView,t);
                            else treeView.expandNode(t);
                            expSympMed(h.ID,t,expMedSymp,Selected);
                        }
                        if (expMed.size() == 0) break;

                    }
                }

            }

        }
    }

    private void expSympMed(int id, TreeNode t, ArrayList<Integer> expMedSymp, ArrayList<Integer> selected) throws Throwable
    {

        for (TreeNode tt : t.getChildren())
        {
            if (selected.size()<=0) break;
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
            for (int i = 0; i < selected.size(); i += 2)
            {
                if (selected.get(i) == id && selected.get(i + 1) == h.ID)
                {
                    treeView.selectNode(tt);
                    selected.remove(i);
                    selected.remove(i);
                    break;
                }
            }
        }
        if (expMedSymp.size()==0) return;
        if (-99 == expMedSymp.get(0) && -99 == expMedSymp.get(1))
        {
            expMedSymp.remove(0);
            expMedSymp.remove(0);
            return;
        }

        if (expMedSymp.size()==0) return;

        for (TreeNode tt : t.getChildren())
        {
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
            if (h.ParentMedID == expMedSymp.get(0) && h.ID == expMedSymp.get(1))
            {
                expMedSymp.remove(0);
                expMedSymp.remove(0);
                if(tt.hasChild()==false)SecondLevelNodeViewBinder.buildTree(treeView,tt);
                else treeView.expandNode(tt);
                expSympMed(id,tt,expMedSymp,selected);
                if (expMedSymp.size()<=0) break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("lastquery",_main.lastQuery);
        if (treeView != null)
        {
            if (root != null)
            {
                ArrayList<Integer> expMed = new ArrayList<>();
                ArrayList<Integer> expMedSymp = new ArrayList<>();
                ArrayList<Integer> Selected = new ArrayList<>();
                for (TreeNode t : root.getChildren())
                {
                    if (t.hasChild() && t.isExpanded())
                    {
                        TreeNodeHolderMed h = (TreeNodeHolderMed) t.getValue();
                        expMed.add(h.ID);
                        getSympMed(h.ID,t,expMedSymp);
                    }
                }
                for (TreeNode t : treeView.getSelectedNodes())
                {
                    if (t.getValue() instanceof  TreeNodeHolderSympt)
                    {
                        TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
                        Selected.add(h.ParentMedID);
                        Selected.add(h.ID);
                    }
                }
                outState.putIntegerArrayList("expMed", expMed);
                outState.putIntegerArrayList("expMedSymp", expMedSymp);
                outState.putIntegerArrayList("Selected",Selected);
            }

        }
    }

    private void getSympMed(int Medid, TreeNode t, ArrayList<Integer> sympMed)
    {
        boolean hasChild = false;
        for (TreeNode tt: t.getChildren())
        {
            if (tt.hasChild() && tt.isExpanded())
            {
                TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
                sympMed.add(Medid);
                sympMed.add(h.ID);
                getSympMed(Medid,tt,sympMed);
                sympMed.add (-99);
                sympMed.add (-99);
            }
            else
            {

            }
        }
    }


    private void searchSymptoms(String searchtxt, boolean AndFlag) {
        if (lib.libString.IsNullOrEmpty(searchtxt)) return;
        String[] txt = searchtxt.split(";");
        try {
            //String qry = "SELECT Medikamente.* FROM Symptome WHERE ";
            String where = "";
            for (String s : txt) {
                if (!lib.libString.IsNullOrEmpty(s)) {
                    if (AndFlag) {
                        if (!(where.equalsIgnoreCase(""))) where += " AND ";
                        if (txt.length > 1)
                            where += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome WHERE Text LIKE '%" + MakeFitForQuery(s, true) + "%')";
                        else
                            where += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome WHERE ShortText LIKE '%" + MakeFitForQuery(s, true) + "%')";
                    }
                    else
                    {
                        if (!(where.equalsIgnoreCase(""))) where += " OR ";
                        where += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome WHERE ShortText LIKE '%" + MakeFitForQuery(s, true) + "%')";
                    }
                }
            }
            if(!AndFlag||txt.length<2) txt=null;
            //AddSymptomeQueryRecursive(root,qry,-1,true);
            String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                    "WHERE Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + where + ")";
            qryMedGrade += " ORDER BY Medikamente.Name, SymptomeOfMedikament.GRADE DESC";
            buildTreeRep(qryMedGrade,true,txt);

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
                stringBuilder.append(selectedNodes.get(i).getValue().toString()).append(",");
            } else
            {
                stringBuilder.append("...and ").append(selectedNodes.size() - 5).append(" more.");
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
    public void buildTreeRep(String qry, boolean refresh, String[] txt)
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
                        int nexts = 0;
                        TreeNodeHolderMed hMed = new TreeNodeHolderMed((MainActivity)getActivity(),0, Name, "Med" + ID, ID, Name, Beschreibung);
                        TreeNode treeNode = new TreeNode(hMed);
                        treeNode.setLevel(0);
                        root.addChild(treeNode);
                        if (insertSymptom(c,treeNode,hMed,ID,txt)){
                            sum = c.getInt(ColumnGrade);
                            nexts += 1;
                        }
                        while (c.moveToNext() && c.getInt(ColumnIDId) == ID) {
                            if(insertSymptom(c,treeNode,hMed,ID, txt)) {
                                nexts += 1;
                                sum += c.getInt(ColumnGrade);
                            }
                        }
                        hMed.totalGrade = sum;
                        hMed.count = nexts;
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
                    //if (refresh) treeView.refreshTreeView();

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

    private boolean insertSymptom(Cursor c, TreeNode treeNode, TreeNodeHolderMed hMed, int ID, String[] txt)
    {
        final int ColumnTextId = c.getColumnIndex("Text");
        final int ColumnSymptomIDId = c.getColumnIndex("SymptomID");
        final int ColumnShortTextId = c.getColumnIndex("ShortText");
        final int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
        final int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");
        final int ColumnGradeId = c.getColumnIndex("Grade");
        int grade = -1;
        if (ColumnGradeId >= 0)
        {
            grade = c.getInt(ColumnGradeId);
        }
        int SympID = c.getInt(ColumnSymptomIDId);
        String Text = c.getString(ColumnTextId);
        String ShortText = c.getString(ColumnShortTextId);
        Integer KoerperTeilId = c.getInt(ColumnKoerperTeilId);
        Integer ParentSymptomId = c.getInt(ColumnParentSymptomId);
        boolean found = false;
        if (txt!=null && txt.length>0)
        {
            for (String t : txt)
            {
                if (ShortText.toLowerCase().contains(t.toLowerCase()))
                {
                    found = true;
                    break;
                }
            }
        }
        else
        {
            found = true;
        }
        if (!found) return false;
        ShortText+= (grade >=0 ? "("+ grade + ")":"");
        TreeNode treeNode2 = new TreeNode(new TreeNodeHolderSympt(hMed.getContext(), 1, ShortText, "Sympt" + SympID, SympID, Text, ShortText, KoerperTeilId, ParentSymptomId,hMed.ID,grade));
        try {
            SymptomsActivity.AddNodesRecursive(hMed.getContext(),0,treeNode2,treeNode,ParentSymptomId,hMed.ID);
            return  true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return  false;
        }
        //treeNode2.setLevel(1);
        //treeNode.addChild(treeNode2);

    }
    //private String lastQuery = "";
    public String[] getQueryMed(boolean OrFlag, boolean Wide, boolean blnAdd) {
        if (!blnAdd)_main.lastQuery = "";
        String qry = "";
        String qrySymptMed = _main.lastQuery;
        for (TreeNode t : treeView.getSelectedNodes()) {
            if (t.getValue() instanceof  TreeNodeHolderMed) continue;
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
            if (!lib.libString.IsNullOrEmpty(qrySymptMed)) {
                if (OrFlag)
                    qrySymptMed += " OR ";
                else
                    qrySymptMed += " AND ";
            }
            if (!lib.libString.IsNullOrEmpty(qry)) {
                if (OrFlag)
                    qry += " OR ";
                else
                    qry += " AND ";
            }

            if (!Wide) {
                qry +=
                        "Medikamente.ID in (Select MedikamentID from SymptomeOfMedikament where SymptomID = " + h.ID + ")";
                qrySymptMed += "SymptomeOfMedikament.SymptomID = " + h.ID;
            } else {
                qry +=
                        "Medikamente.ID in (Select MedikamentID from SymptomeOfMedikament where SymptomID IN (SELECT ID FROM Symptome WHERE Text LIKE '%" + MakeFitForQuery(h.SymptomText, true) + "%'))";
                qrySymptMed += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome WHERE Text LIKE '%" + MakeFitForQuery(h.SymptomText, true) + "%')";
            }
        }
        _main.lastQuery = qrySymptMed;
        return new String[]{qry, qrySymptMed};

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

