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
import static android.os.Build.ID;
import static jmg.de.org.repetit.lib.lib.OpenDialogs;
import static jmg.de.org.repetit.lib.lib.libString.MakeFitForQuery;

/**
 * Created by hmnatalie on 29.08.17.
 */

public class SymptomsActivity extends Fragment
{
    public final static int fragID = 1;

    public MainActivity _main;

    protected Toolbar toolbar;
    private ViewGroup viewGroup;
    private TreeNode root;
    public TreeView treeView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _main = ((MainActivity) getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedinstancestate)
    {
        //initTreeView(view);
    }

    public void initTreeView(View view) throws Throwable
    {
        initView(view);

        root = TreeNode.root();
        buildTree(root, "Select Symptome.* FROM Symptome WHERE Symptome.ParentSymptomID IS Null ORDER BY Text", false);
        treeView = new TreeView(root, _main, new MyNodeViewFactory());
        View view2 = treeView.getView();
        view2.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewGroup.addView(view2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstancestate)
    {
        try
        {
            View v = inflater.inflate(R.layout.activity_sympt, container, false);
            initTreeView(v);
            return v;
        }
        catch (Throwable ex)
        {
            lib.ShowException(_main, ex);
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        menuInflater.inflate(R.menu.symptoms_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.mnu_find_symptoms:
                AlertDialog.Builder A = new AlertDialog.Builder(getContext());
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText("");
                A.setView(input);

                A.setPositiveButton(getContext().getString(R.string.ok), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String txt = input.getText().toString();
                        if (!lib.libString.IsNullOrEmpty(txt))
                        {
                            try
                            {
                                buildTree(root, "SELECT * FROM Symptome WHERE ShortText LIKE '%" + MakeFitForQuery(txt, true) + "%'", true);
                            }
                            catch (Throwable throwable)
                            {
                                throwable.printStackTrace();
                            }
                        }
                    }
                });
                A.setNegativeButton(getContext().getString(R.string.cancel), null);
                A.setMessage(getContext().getString(R.string.msg_find_symptoms));
                A.setTitle(getContext().getString(R.string.msg_find_title));
                AlertDialog dlg = A.create();
                dlg.show();
                break;
            case R.id.mnu_qry_med:
                String[] qry = getQueryMed(true, false);
                ((MainActivity) getActivity()).mPager.setCurrentItem(MedActivity.fragID);
                //String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                //        "WHERE " + qry[0] + " AND Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + qry[1] + ")";
                String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                        "WHERE Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + qry[1] + ")";
                qryMedGrade += " ORDER BY Medikamente.Name, SymptomeOfMedikament.GRADE DESC";
                ((MainActivity) getActivity()).fPA.fragMed.buildTreeRep(qryMedGrade, true);
                //((MainActivity)getActivity()).fPA.fragMed.buildTree("SELECT * FROM Medikamente WHERE " + qry, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] getQueryMed(boolean OrFlag, boolean Wide)
    {
        String qry = "";
        String qrySymptMed = "";
        for (TreeNode t : treeView.getSelectedNodes())
        {
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
            if (!lib.libString.IsNullOrEmpty(qrySymptMed))
            {
                if (OrFlag)
                    qrySymptMed += " OR ";
                else
                    qrySymptMed += " AND ";
            }
            if (!lib.libString.IsNullOrEmpty(qry))
            {
                if (OrFlag)
                    qry += " OR ";
                else
                    qry += " AND ";
            }

            if (!Wide)
            {
                qry +=
                        "Medikamente.ID in (Select MedikamentID from SymptomeOfMedikament where SymptomID = " + h.ID + ")";
                qrySymptMed += "SymptomeOfMedikament.SymptomID = " + h.ID;
            }
            else
            {
                qry +=
                        "Medikamente.ID in (Select MedikamentID from SymptomeOfMedikament where SymptomID IN (SELECT ID FROM Symptome WHERE Text LIKE '%" + MakeFitForQuery(h.SymptomText, true) + "%'))";
                qrySymptMed += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome WHERE Text LIKE '%" + MakeFitForQuery(h.SymptomText, true) + "%')";
            }
            }
        return new String[]{qry,qrySymptMed};

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

    private void buildTree(TreeNode treeNodeParent, String qry, boolean refresh) throws Throwable
    {
        if (treeNodeParent.getChildren().size() > 0)
        {
            List<TreeNode> l = treeNodeParent.getChildren();
            l.clear();
            treeNodeParent.setChildren(l);
            //treeView.collapseNode(treeNodeParent);
        }
        //MedActivity.TreeNodeHolderMed h = (MedActivity.TreeNodeHolderMed) treeNodeParent.getValue();
        dbSqlite db = ((MainActivity)getActivity()).db;
        try
        {
            Cursor c = db.query(qry);
            try
            {
                if (c.moveToFirst())
                {
                    int ColumnTextId = c.getColumnIndex("Text");
                    int ColumnIDId = c.getColumnIndex("ID");
                    int ColumnShortTextId = c.getColumnIndex("ShortText");
                    int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
                    int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");
                    do
                    {
                        int ID = c.getInt(ColumnIDId);
                        String Text = c.getString(ColumnTextId);
                        String ShortText = c.getString(ColumnShortTextId);
                        Integer KoerperTeilId = c.getInt(ColumnKoerperTeilId);
                        Integer ParentSymptomId = c.getInt(ColumnParentSymptomId);
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderSympt((MainActivity)getActivity(), 1, ShortText, "Sympt" + ID, ID, Text, ShortText, KoerperTeilId, ParentSymptomId));
                        treeNode.setLevel(0);
                        treeNodeParent.addChild(treeNode);
                    } while (c.moveToNext());
                    //this.treeView.expandNode(treeNodeParent);
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

    private void setLightStatusBar(@NonNull View view)
    {
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

    private void AddSymptomeUndefinedQuery(ref TreeNode SNode, string qrySympt, int ParentSymptomID)
    {
		const string CodeLoc = ClassName + "AddSymptomeUndefinedQuery";
        string qry = "";
        string sort = "";
        System.Windows.Forms.TreeNode rootNode = null;
        System.Windows.Forms.TreeNode symptNode = null;

        //Dim dr As SQLiteDataReader = Nothing
        System.Data.DataTable dt = null;
        System.Data.DataRow drowSymptome = null;
        // dsRep.SymptomeRow
        int found = 0;
        bool blnFirst = true;

        _blnSkipAfterCheck = true;

        found = Strings.InStr(1, qrySympt, "Order By", CompareMethod.Text);
        if (found != 0) {
            qry = VB.Left(qrySympt, found - 1);
            sort = " " + VB.Right(qrySympt, Strings.Len(qrySympt) - found + 1);
        }
        if (ParentSymptomID >= 0) {
            qry = qry + " AND KoerperTeilID is NULL ";
        }

        if (qry.StartsWith(" AND"))
            qry = qry.Substring(4);
        string qry2 = qry;
        int foundsel = qry2.ToLower().IndexOf(" from symptome");
        if (foundsel > 0) {
            qry2 = qry2.Substring(0, foundsel) + ",0 as level, symptome.shorttext as path " + qry2.Substring(foundsel);
        }
        //qry = qry.Replace(" FROM Symptome ", ",0 FROM Symptome ").Replace(" from Symptome ", ",0 FROM Symptome ").Replace(" From Symptome ", ",0 FROM Symptome ")
        //qry2 = "WITH RECURSIVE " &
        //  "SympParents (ID,Text,ShortText,KoerperTeilID,ParentSymptomID, Level) " _
        //  & "AS (" & qry2 & " UNION ALL " &
        //  " SELECT Symptome.*, SympParents.Level + 1 FROM Symptome" &
        //  " JOIN SympParents ON Symptome.ID = SympParents.ParentSymptomID)" &
        //  " SELECT DISTINCT ID,Text,ShortText,KoerperTeilID,ParentSymptomID,Level FROM SympParents ORDER BY Level DESC, ParentSymptomID ASC, Text ASC"

        qry2 = "WITH RECURSIVE " + "SympParents (ID,Text,ShortText,KoerperTeilID,ParentSymptomID, Level, path) " + "AS (" + qry2 + " UNION ALL " + " SELECT Symptome.*, SympParents.Level + 1, sympparents.path || '/' || symptome.shorttext FROM Symptome" + " JOIN SympParents ON Symptome.ID = SympParents.ParentSymptomID)" + " SELECT DISTINCT ID,Text,ShortText,KoerperTeilID,ParentSymptomID,Level FROM SympParents";
        if (ParentSymptomID >= 0) {
            qry2 += " WHERE ParentSymptomID = " + ParentSymptomID;
        } else {
            qry2 += " WHERE ParentSymptomID IS NULL";
        }

        qry2 += " ORDER BY Level DESC, ParentSymptomID ASC, Text ASC";

        try {
            gStatus = CodeLoc + " qry: " + qry2;
            //dr = _clsdbRep.GetReader(qry)
            dt = _clsdbRep.GetDataTable(qry2);
            // & sort)
            rootNode = SNode;
            // SNode.Nodes.Add("undefMed", GetLang("undef", "undefiniert"))
            if (_colCheckedNodes.ContainsKey(rootNode.Name))
                rootNode.Checked = true;
            //rootNode.Tag = Nothing
            //rootNode.ImageIndex = (modGlobal.NodeTypes.KoerperTeil)
            // dr.HasRows Then
            if (dt.Rows.Count > 0) {
                int LastID = -1;
                symptNode = null;
                //Dim colLevels As New colSympsByID
                foreach (System.Data.DataRow drowSymptome_loopVariable in dt.Rows) {
                    drowSymptome = drowSymptome_loopVariable;
                    int Level = -1;
                    if (Information.IsDBNull(drowSymptome["ParentSymptomID"])) {
                        rootNode = SNode;
                    } else if (LastID == drowSymptome["ParentSymptomID"]) {
                        rootNode = symptNode;
                    } else if (rootNode != null) {
                        try {
                            rootNode = SNode;
                            //colLevels.Item(drowSymptome!ParentSymptomID)
                        } catch {
                            continue;
                        }
                    }
                    Level = drowSymptome["Level"];

                    //Do While dr.Read
                    //drowSymptome = dr. '_clsdbRep.Symptome.FindByID(CInt(dr!ID))
                    //If Not (lAutoSelect AndAlso _colCheckedSympt.Contains(drowSymptome!ID)) Then
                    //_colSelSympt.Add(drowSymptome)

                    if (rootNode == null)
                        rootNode = SNode;
                    System.Data.DataRow ParentSympRow = rootNode.Tag;
                    System.Data.DataRow Parent = drowSymptome;
                    System.Data.DataRow drow = Parent;
                    symptNode = rootNode.Nodes["Sympt" + drow["ID"]];
                    if (symptNode == null) {
                        symptNode = rootNode.Nodes.Add("Sympt" + drow["ID"], !Information.IsDBNull(drow["ShortText"]) ? drow["ShortText"] : drow["Text"]);
                        //If Not colLevels.Contains(drow!ID) Then colLevels.Add(symptNode)
                        //symptNode.Nodes.Add("DUMMY", "DUMMY").Tag = qry
                        symptNode.Tag = drow;
                        if (mnuQueryOn.Checked && (_colCheckedSympt.Contains(drow["ID"]) || _colCheckedNodes.Contains(symptNode.Name))) {
                            symptNode.Checked = true;
                        }
                        symptNode.ImageIndex = (modGlobal.NodeTypes.Symptom);
                        //AndAlso drowSymptome!Level = 0 Then
                        if (symptNode != null) {
                            symptNode.Nodes.Add("DUMMY", "DUMMY").Tag = qrySympt;
                        }
                    }
                    if (!string.IsNullOrEmpty(QuerySympt) && !_colSelSympt.Contains(drowSymptome["ID"])) {
                        _colSelSympt.Add(symptNode);
                    }
                    //rootNode = symptNode
                    //symptNode = rootNode.Nodes.Add("Sympt" & drowSymptome!ID, drowSymptome!ShortText)
                    //symptNode.Nodes.Add("DUMMY", "DUMMY").Tag = qry
                    //symptNode.Tag = drowSymptome
                    if (mnuQueryOn.Checked && _colCheckedSympt.Contains(drowSymptome["ID"])) {
                        symptNode.Checked = true;
                        symptNode.EnsureVisible();
                    }
                    symptNode.ImageIndex = (modGlobal.NodeTypes.Symptom);

                    //Else
                    //MsgBox("checked")
                    //End If
                }
                tvSympt.Sort();
            } else {
                qry = "Text = 'AKZB794'";
            }
        } finally {
            //If dr Is Nothing = False Then
            //dr.Close()
            //dr = Nothing
            //End If
            _blnSkipAfterCheck = false;
        }
    }
}

