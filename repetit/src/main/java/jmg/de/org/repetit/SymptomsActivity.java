package jmg.de.org.repetit;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jmg.de.org.repetit.lib.ProgressClass;
import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;

import static jmg.de.org.repetit.lib.lib.libString.MakeFitForQuery;

/**
 * Created by hmnatalie on 29.08.17.
 */

public class SymptomsActivity extends Fragment {
    public final static int fragID = 1;
    private static final String TAG = "SymptomsActivity";


    public MainActivity _main;

    protected Toolbar toolbar;
    private ViewGroup viewGroup;
    private TreeNode root;
    public TreeView treeView;
    private AppCompatEditText txtSearch;
    private ImageButton btnSearchAnd;
    private ImageButton btnSearchOr;
    private String lastQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _main = ((MainActivity) getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedinstancestate) {
        //initTreeView(view);
    }

    public void initTreeView(View view, Bundle savedinstancestate) throws Throwable {
        initView(view);

        root = TreeNode.root();
        treeView = new TreeView(root, _main, new MyNodeViewFactory());
        View view2 = treeView.getView();
        view2.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewGroup.addView(view2);

        String qry = "Select Symptome.* FROM Symptome WHERE Symptome.ParentSymptomID IS Null ORDER BY Text COLLATE NOCASE";
        if (!lib.libString.IsNullOrEmpty(lastQuery)) {
            qry = lastQuery;
            buildTree(root, qry, true, true, savedinstancestate);
        } else {
            buildTree(root, qry, true, false, savedinstancestate);
        }
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstancestate) {
        View v = null;
        try {
            v = inflater.inflate(R.layout.activity_sympt, container, false);
            txtSearch = (AppCompatEditText) v.findViewById(R.id.txtSearch);
            txtSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
            txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String txt = txtSearch.getText().toString();
                        searchSymptoms(txt, true);
                    }
                    return true;
                }
            });
            btnSearchAnd = (ImageButton) v.findViewById(R.id.btnSearchAnd);
            btnSearchAnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String txt = txtSearch.getText().toString();
                    searchSymptoms(txt, true);
                }
            });
            btnSearchOr = (ImageButton) v.findViewById(R.id.btnSearchOr);
            btnSearchOr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String txt = txtSearch.getText().toString();
                    searchSymptoms(txt, false);
                }
            });
            if (savedinstancestate != null) lastQuery = savedinstancestate.getString("lastquery");
            initTreeView(v,savedinstancestate);

            return v;
        } catch (Throwable ex) {
            lib.ShowException(_main, ex);
            return v;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("lastquery", lastQuery);
        if (treeView != null) {
            if (root != null) {
                ArrayList<Integer> expSymp = new ArrayList<>();
                ArrayList<Integer> Selected = new ArrayList<>();
                getSymp(root, expSymp);
                for (TreeNode t : treeView.getSelectedNodes()) {
                    if (t.getValue() instanceof TreeNodeHolderSympt) {
                        TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
                        //Selected.add(h.ParentMedID);
                        Selected.add(h.ID);
                        Selected.add(t.getWeight());
                    }
                }
                outState.putIntegerArrayList("expSymp", expSymp);
                outState.putIntegerArrayList("Selected", Selected);
            }

        }
    }

    private void getSymp(TreeNode t, ArrayList<Integer> symp) {
        boolean hasChild = false;
        for (TreeNode tt : t.getChildren()) {
            if (tt.hasChild() && tt.isExpanded()) {
                TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
                symp.add(h.ID);
                getSymp(tt, symp);
                hasChild = true;
            } else {

            }
        }
        if (hasChild) symp.add(-99);
    }

    private void restoreTreeView(Bundle savedinstancestate) throws Throwable {
        if (treeView != null && savedinstancestate != null) {
            if (root != null) {
                ArrayList<Integer> expSymp = savedinstancestate.getIntegerArrayList("expSymp");
                ArrayList<Integer> Selected = savedinstancestate.getIntegerArrayList("Selected");
                lib.gStatus = "expSymp";
                expSymp(root, expSymp, Selected);
                lib.gStatus = "AddNodesRecursive";
                while (Selected.size()>0) {
                    AddNodesRecursive(_main, 0, null, root, Selected.get(0),Selected.get(1), -1);
                    Selected.remove(0);
                    Selected.remove(0);
                }
            }

        }
    }

    private void expSymp(TreeNode t, ArrayList<Integer> expSymp, ArrayList<Integer> selected) throws Throwable {

        checkSelected(t, selected);
        if (expSymp.size() == 0) return;
        if (-99 == expSymp.get(0))
        {
            expSymp.remove(0);
            return;
        }


        for (TreeNode tt : t.getChildren()) {
            if (expSymp.size() == 0) return;
            if (-99 == expSymp.get(0)) {
                expSymp.remove(0);
                break;
            }
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
            if (h.ID == expSymp.get(0)) {
                expSymp.remove(0);
                if (t == root) {
                    if (tt.hasChild() == false) FirstLevelNodeViewBinder.buildTree(treeView, tt);
                    else treeView.expandNode(tt);
                } else {
                    if (tt.hasChild() == false) SecondLevelNodeViewBinder.buildTree(treeView, tt);
                    else treeView.expandNode(tt);
                }
                if (expSymp.size() <= 0) {
                    checkSelected(tt, selected);
                    break;
                }
                if (-99 == expSymp.get(0))

                {
                    expSymp.remove(0);
                    checkSelected(tt, selected);
                } else {
                    expSymp(tt, expSymp, selected);
                }

            }
        }

    }

    private void checkSelected(TreeNode t, ArrayList<Integer> selected) {
        for (TreeNode tt : t.getChildren()) {
            if (selected.size() <= 0) break;
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
            int found = selected.indexOf(new Integer(h.ID));
            if (found > -1 && found % 2 != 0)
            {
                found = -1;
                for (int i = 0; i < selected.size(); i+=2)
                {
                    if (selected.get(i) == h.ID)
                    {
                        found = i;
                        break;
                    }
                }
            }
            if (found > -1) {
                treeView.selectNode(tt,1);
                tt.setWeight(selected.get(found + 1));
                selected.remove(found);
                selected.remove(found);
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.symptoms_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
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
                        searchSymptoms(txt, true);
                    }
                });
                A.setNegativeButton(getContext().getString(R.string.cancel), null);
                A.setMessage(getContext().getString(R.string.msg_find_symptoms));
                A.setTitle(getContext().getString(R.string.msg_find_title));
                AlertDialog dlg = A.create();
                dlg.show();
                break;

        }
        return super.

                onOptionsItemSelected(item);

    }

    public static String getWhereWhole(String column, String search)
    {
        return "(" + column + " like '% " + search + " %' OR " + column + " like '" + search + " %' OR " + column + " like '% " + search + "' OR " + column + " like '%, " + search + " %' OR " + column + " like '% " + search + ", %' OR " + column + " like '" + search + "')";
    }


    private void searchSymptoms(String searchtxt, boolean AndFlag) {
        if (lib.libString.IsNullOrEmpty(searchtxt)) return;
        String[] txt = searchtxt.split("\\.");
        try {
            String qry = "SELECT * FROM Symptome WHERE ";
            String where = "";
            for (String s : txt) {
                if (!lib.libString.IsNullOrEmpty(s)) {
                    s = MakeFitForQuery(s,true);
                    if (AndFlag) {
                        if (where != "") where += " AND ";
                        if (txt.length > 1)
                            where += (_main.blnSearchWholeWord?getWhereWhole("Text",s):"Text LIKE '%" + s + "%'");
                        else where += (_main.blnSearchWholeWord?getWhereWhole("ShortText",s):"ShortText LIKE '%" + s + "%'");
                    } else {
                        if (where != "") where += " OR ";
                        if (txt.length > 1)
                            where += (_main.blnSearchWholeWord?getWhereWhole("ShortText",s):"ShortText LIKE '%" + s + "%'");
                        else where += (_main.blnSearchWholeWord?getWhereWhole("ShortText",s):"ShortText LIKE '%" + s + "%'");
                    }
                }
            }
            //AddSymptomeQueryRecursive(root,qry,-1,true);
            if (where != "")
                buildTree(root, qry + where + " ORDER BY Text COLLATE NOCASE", true, true, null);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    //String lastQuery = "";
    public String[] getQueryMed(boolean OrFlag, boolean Wide, boolean blnAdd, ArrayList<Integer> selected) {
        if (!blnAdd) {
            _main.lastQuery = "";
            selected.clear();
        }
        String qry = "";
        String qrySymptMed = _main.lastQuery;
        for (TreeNode t : treeView.getSelectedNodes()) {
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
            selected.add(-1); selected.add(h.ID);selected.add(t.getWeight());
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

    private void buildTree(final TreeNode treeNodeParent, final String qry, final boolean refresh, final boolean getParents, final Bundle savedinstancestate) throws Throwable {
        final Context context = getContext();

        new AsyncTask<Void, ProgressClass, Integer>()
        {
            public Throwable ex;
            public int counter;
            public int oldmax;
            public String oldmsg;
            ProgressDialog pd;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                createProgress();
            }

            private void createProgress()
            {

                pd = new ProgressDialog(context);
                pd.setTitle(getString(R.string.Repertoriasing));
                pd.setMessage(getString(R.string.startingRep));
                pd.setIndeterminate(false);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();

            }

            @Override
            protected Integer doInBackground(Void... params)
            {
                ProgressClass pc = new ProgressClass(0,100,context.getString(R.string.startingquery),false);
                publishProgress(pc);
                if (treeNodeParent.getChildren().size() > 0) {
                    List<TreeNode> l = treeNodeParent.getChildren();
                    l.clear();
                    treeNodeParent.setChildren(l);
                    //treeView.collapseNode(treeNodeParent);
                }
                //MedActivity.TreeNodeHolderMed h = (MedActivity.TreeNodeHolderMed) treeNodeParent.getValue();
                dbSqlite db = ((MainActivity) getActivity()).db;
                try {
                    Cursor c = db.query(qry);
                    try {
                        if (c.moveToFirst()) {
                            int ColumnTextId = c.getColumnIndex("Text");
                            int ColumnIDId = c.getColumnIndex("ID");
                            int ColumnShortTextId = c.getColumnIndex("ShortText");
                            int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
                            int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");
                            if (getParents) lastQuery = qry;
                            int count = c.getCount();
                            do {
                                counter += 1;
                                if (pc.max<10||counter%(count/10)==0)
                                {
                                    pc.update(counter, count, context.getString(R.string.processingquery), false);
                                    publishProgress(pc);
                                }
                                int ID = c.getInt(ColumnIDId);
                                String Text = c.getString(ColumnTextId);
                                String ShortText = c.getString(ColumnShortTextId);
                                Integer KoerperTeilId = c.getInt(ColumnKoerperTeilId);
                                Integer ParentSymptomId = c.getInt(ColumnParentSymptomId);
                                int Level = treeNodeParent.getLevel();
                                if (treeNodeParent == root) Level = -1;
                                TreeNode treeNode = new TreeNode(new TreeNodeHolderSympt((MainActivity) getActivity(), Level + 1, ShortText, "Sympt" + ID, ID, Text, ShortText, KoerperTeilId, ParentSymptomId, -1, 0));
                                if (!getParents || ParentSymptomId == null) {
                                    treeNode.setLevel(Level + 1);
                                    treeNodeParent.addChild(treeNode);
                                } else {
                                    try
                                    {
                                        AddNodesRecursive((MainActivity) getActivity(), Level, treeNode, treeNodeParent, ParentSymptomId, 0, -1);
                                    }
                                    catch (Throwable throwable)
                                    {
                                        this.ex = throwable;
                                    }
                                }
                            } while (c.moveToNext());
                            //this.treeView.expandNode(treeNodeParent);
                        }
                    } finally {
                        c.close();
                    }
                }
                catch (Throwable ex)
                {
                    this.ex = ex;
                }
                finally {
                    db.close();
                }


                return  counter;

            }

            @Override
            protected void onProgressUpdate(ProgressClass... params)
            {
                try
                {
                    super.onProgressUpdate(params);
                    ProgressClass p = params[0];
                    if (pd != null)
                    {
                        if (p.blnRestart)
                        {
                            pd.dismiss();
                            createProgress();
                            pd.show();
                        }
                        pd.setProgress(p.counter);
                        if (p.msg != null && !p.msg.equalsIgnoreCase(oldmsg))
                        {
                            pd.setMessage(p.msg);
                            oldmsg = p.msg;
                        }
                        if (p.max > 0 && !(p.max==oldmax))
                        {
                            pd.setMax(p.max);
                            oldmax = p.max;
                        }
                    } else
                    {
                        Log.i("dbsqlite", "no progress");
                    }
                }
                catch (Throwable ex)
                {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void onPostExecute( final Integer result ) {
                // continue what you are doing...

                pd.dismiss();
                if (refresh && treeView != null) treeView.refreshTreeView();
                if (this.ex != null) lib.ShowException(context,ex);
                if (savedinstancestate!=null) try
                {
                    restoreTreeView(savedinstancestate);
                }
                catch (Throwable throwable)
                {
                    throwable.printStackTrace();
                }
            }



        }.execute();
    }

    public static void AddNodesRecursive(MainActivity activity, int Level, TreeNode treeNode, TreeNode treeNodeParent, Integer ParentSymptomId, Integer Weight, int ParentMedID) throws Throwable {
        ArrayList<TreeNode> list = new ArrayList<>();
        if (treeNode != null) list.add(treeNode);
        getParents(activity, ParentSymptomId, list, ParentMedID);
        TreeNode parent = treeNodeParent;
        if (treeNode == null && list.size()>0 && Weight >= 0) list.get(0).setWeight(Weight);
        for (int i = list.size() - 1; i >= 0; i--) {
            boolean blnDouble = false;
            boolean blnIsNewNode = false;
            if (i == 0) blnIsNewNode = true;
            TreeNode t = list.get(i);
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
            for (TreeNode tt : parent.getChildren()) {
                TreeNodeHolderSympt h2 = (TreeNodeHolderSympt) tt.getValue();
                if (h2.ID == h.ID) {
                    if (blnIsNewNode) {
                        h2.Text = h.Text;
                    }
                    parent = tt;
                    blnDouble = true;
                    break;
                }
            }
            Level += 1;
            if (!blnDouble) {
                t.setLevel(Level);
                h.level = Level;
                parent.addChild(t);
                parent = t;
            }
        }
    }

    private static void getParents(MainActivity activity, int ParentSymptomID, ArrayList<TreeNode> list, int ParentMedID) throws Throwable {
        dbSqlite db = activity.db;
        try {
            Cursor c = db.query("SELECT * FROM Symptome WHERE ID = " + ParentSymptomID);
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
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderSympt(activity, 0, ShortText, "Sympt" + ID, ID, Text, ShortText, KoerperTeilId, ParentSymptomId, ParentMedID, 0));
                        list.add(treeNode);
                        if (!(ParentSymptomId == null))
                            getParents(activity, ParentSymptomId, list, ParentMedID);
                    } while (c.moveToNext());
                    //this.treeView.expandNode(treeNodeParent);
                }
            } finally {
                c.close();
            }
        } finally {
            //db.close();
        }
        //if (refresh && treeView != null) treeView.refreshTreeView();
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void AddSymptomeQueryRecursive(TreeNode SNode, String qrySympt, int ParentSymptomID, boolean refresh) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            throw new RuntimeException("Wrong Android Version");
        if (SNode.getChildren().size() > 0) {
            SNode.getChildren().clear();
        }
        final String CodeLoc = TAG + "AddSymptomeUndefinedQuery";
        String qry = "";
        String sort = "";
        int found = 0;
        found = qrySympt.indexOf("Order By");
        if (found != -1) {
            qry = qrySympt.substring(0, found - 1);
            sort = " " + qry.substring(found);
        } else {
            qry = qrySympt;
        }
        if (ParentSymptomID >= 0) {
            qry = qry + " AND KoerperTeilID is NULL ";
        }

        if (qry.startsWith(" AND"))
            qry = qry.substring(4);
        String qry2 = qry;
        int foundsel = qry2.toLowerCase().indexOf(" from symptome");
        if (foundsel > 0) {
            qry2 = qry2.substring(0, foundsel) + ", 0 as level, Symptome.ShortText as path " + qry2.substring(foundsel);
        }

        qry2 = "WITH RECURSIVE " + "SympParents (ID,Text,ShortText,KoerperTeilID,ParentSymptomID, Level, path) " + "AS (" + qry2 + " UNION ALL " + " SELECT Symptome.*, SympParents.Level + 1, sympparents.path || '/' || symptome.shorttext FROM Symptome" + " JOIN SympParents ON Symptome.ID = SympParents.ParentSymptomID)" + " SELECT DISTINCT ID,Text,ShortText,KoerperTeilID,ParentSymptomID,Level FROM SympParents";
        if (ParentSymptomID >= 0) {
            qry2 += " WHERE ParentSymptomID = " + ParentSymptomID;
        } else {
            qry2 += " WHERE ParentSymptomID IS NULL";
        }

        qry2 += " ORDER BY Level DESC, ParentSymptomID ASC, Text ASC";
        Cursor c = null;
        try {
            lib.gStatus = CodeLoc + " qry: " + qry2;
            c = ((MainActivity) getActivity()).db.query(qry2);
            // & sort)
            if (c.getCount() > 0) {
                int LastID = -1;
                TreeNode symptNode = null;
                TreeNode rootNode = null;
                //Dim colLevels As New colSympsByID
                if (c.moveToFirst()) {
                    int ColumnText = c.getColumnIndex("Text");
                    int ColumnID = c.getColumnIndex("ID");
                    int ColumnShortText = c.getColumnIndex("ShortText");
                    int ColumnKoerperTeil = c.getColumnIndex("KoerperTeilID");
                    int ColumnParentSymptom = c.getColumnIndex("ParentSymptomID");
                    int ColumnLevel = c.getColumnIndex("Level");
                    int Level = -1;

                    do {
                        if (c.isNull(ColumnParentSymptom)) {
                            rootNode = SNode;
                        } else if (LastID == c.getInt(ColumnParentSymptom)) {
                            rootNode = symptNode;
                        } else {
                            rootNode = SNode;
                        }
                        Level = c.getInt(ColumnLevel);


                        TreeNodeHolderSympt ParentSympRow = (TreeNodeHolderSympt) rootNode.getValue();
                        TreeNodeHolderSympt Parent = new TreeNodeHolderSympt((MainActivity) getActivity(), Level + 1, c, -1, 0);
                        TreeNodeHolderSympt drow = Parent;
                        for (TreeNode t : rootNode.getChildren()) {
                            if (((TreeNodeHolderSympt) (t.getValue())).ID == drow.ID) {
                                symptNode = t;
                                break;
                            }
                        }
                        if (symptNode == null) {
                            symptNode = new TreeNode(new TreeNodeHolderSympt((MainActivity) getActivity(), rootNode.getLevel() + 1, c, -1, 0));
                        }
                        LastID = ((TreeNodeHolderSympt) (symptNode.getValue())).ID;
                        symptNode.setLevel(Level);
                        rootNode.addChild(symptNode);

                    } while (c.moveToNext());

                    List<TreeNode> l = SNode.getChildren();

                    Collections.sort(l, new Comparator<TreeNode>() {
                        @Override
                        public int compare(TreeNode lhs, TreeNode rhs) {
                            TreeNodeHolderSympt h1 = (TreeNodeHolderSympt) lhs.getValue();
                            TreeNodeHolderSympt h2 = (TreeNodeHolderSympt) rhs.getValue();
                            return h1.SymptomText.compareToIgnoreCase(h2.SymptomText);
                        }
                    });
                    SNode.setChildren(l);
                    if (refresh) treeView.refreshTreeView();
                } else {
                    qry = "Text = 'AKZB794'";
                }
            }
        } finally {
            if (c != null) c.close();
        }
    }
}

