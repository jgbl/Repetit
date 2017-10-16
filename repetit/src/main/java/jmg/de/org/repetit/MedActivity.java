package jmg.de.org.repetit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;

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
            initTreeView(v);
            return v;
        }
        catch (Throwable ex)
        {
            return null;
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

        public TreeNodeHolderMed(Context context, String Text, String path, int ID, String Name, String Beschreibung)
        {
            super(Text, path, context);
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
        dbSqlite db = new dbSqlite(getContext(), false);
        try
        {
            db.createDataBase();
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
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderMed(getContext(), Name, "Med" + ID, ID, Name, Beschreibung));
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
    private void builtTreeRep(String qry, boolean refresh)
    {
        boolean Initialized = true;
        final String CodeLoc = TAG + ".initTreeview";
        lib.gStatus = CodeLoc + " Start";
        int MedID;
        if (root.getChildren().size() > 0)
        {
            List<TreeNode> l = root.getChildren();
            l.clear();
            root.setChildren(l);
        }
        dbSqlite db = new dbSqlite(getContext(), false);
        try
        {
            db.createDataBase();
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
                        TreeNode treeNode = new TreeNode(new TreeNodeHolderMed(getContext(), Name, "Med" + ID, ID, Name, Beschreibung));
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

        Dim dtMeds As Data.DataTable = _clsdbRep.GetDataTable(SQL)
        If Not dtMeds.Rows.Count > 0 Then
        MsgBox(GetLang("NoMedFound", "Keine Medikamente gefunden!"))
        tv.Nodes.Clear()
        rootNode = tv.Nodes.Add("Root", mCon.ConnectionString)
        rootNode.Tag = mCon.ConnectionString
        rootNode.ImageIndex = (modGlobal.NodeTypes.folderDatabase)
                _RootNode = rootNode
        Else
        tv.Nodes.Clear()
        rootNode = tv.Nodes.Add("Root", mCon.ConnectionString)
        rootNode.Tag = mCon.ConnectionString
        rootNode.ImageIndex = (modGlobal.NodeTypes.folderDatabase)
                _RootNode = rootNode
        ' Alle Medikamente aus Datenbank aus lesen und


        ' einzeln hinzufügen

        'drowsMed = dtMeds.Rows '
        DirectCast(_clsdbRep.Medikamente.Select(FilterExpression, Sort), dsRep.MedikamenteRow())
        For I As Integer = 0 To dtMeds.Rows.Count - 1
        If I >=dtMeds.Rows.Count Then Exit For
        drMed = dtMeds.Rows(I)
        Dim sum As Integer = 0
        Dim ID As Integer = drMed !ID
        sum = drMed !Grade
        Dim nexts As Integer = 0
        While I <dtMeds.Rows.Count - 1 AndAlso dtMeds.Rows(I + 1) !ID = ID
        I += 1 :nexts += 1
        sum += dtMeds.Rows(I) !Grade
        End While
        For ii As Integer = 0 To nexts
        dtMeds.Rows(I - ii) !TotalGrade = sum
        dtMeds.Rows(I - ii) !Count = nexts + 1
        Next

                Next
        Dim meds () As DataRow = dtMeds.Select("", "TOTALGRADE DESC, Count DESC, Name ASC")
        symptNode = Nothing
        For i As Integer = 0 To meds.Length - 1

        If i >=meds.Length Then Exit For
        drMed = meds(i)
        MedID = CInt((drMed !ID))
        Childnode = rootNode.Nodes("Med" & MedID)
        If Childnode Is Nothing Then
            Childnode = rootNode.Nodes.Add("Med" & MedID, CStr(drMed !Name) &"(" & drMed
        !TotalGrade & "/" & drMed !Count & ")")
        Childnode.Tag = drMed ' Der Tag beinhaltet immer die ID
        Childnode.ImageIndex = (modGlobal.NodeTypes.Medikament)
                LSNode = Childnode.Nodes.Add("Leitsymptome" & Childnode.Name, GetLang("CardSympt", "Leitsymptome"))
        LSNode.Tag = drMed
        LSNode.ImageIndex = (modGlobal.NodeTypes.folderLeitsymptom)
                'addLeitsymptomeT(LSNode, drMed)
        LSNode.Nodes.Add("DUMMY", "DUMMY")
        symptNode = Childnode.Nodes.Add("Symptome" & Childnode.Name, GetLang("Sympt", "Symptome"))
        If symptNode Is Nothing Then
        Err.Raise(ErrfrmMain_initTreeviewCoulNotAddSymptNode, CodeLoc, "Could not add SymptNOde to " & Childnode.Name)
        Else
        symptNode.ImageIndex = (modGlobal.NodeTypes.folderSymptom)
                symptNode.Tag = drMed
        End If
        End If
        'symptNode.Nodes.Add("DUMMY", "DUMMY")
        If symptNode IsNot Nothing Then
        symptNode.Nodes.Add("Sympt" & drMed !SymptomID, drMed !Text & "(" & drMed !Grade & ")").
        ImageIndex = modGlobal.NodeTypes.undefined
        End If

        Next i

        If Not mCancel Then
        '*****************************************
        gStatus = CodeLoc & " oldkeyensurevisible"
        '*****************************************
        If oldKeyEnsureVisible And oldkey <>"" Then
            Try
        ChildNodes = tv.Nodes.Find(oldkey, True)
        Select Case ChildNodes.GetUpperBound(0)
        Case 0
        Childnode = ChildNodes(0)
        Case - 1
        Childnode = Nothing
        Case Else
        Throw New ApplicationException("ChildNode " & oldkey & " double!")
        End Select
        Catch e As Exception
        Childnode = Nothing
        End Try
        If Childnode Is Nothing = False Then
        Childnode.EnsureVisible()
        End If
        End If
        End If
        rootNode.Expand()
        End If
        Catch ex As Exception
        HandleError(ex, CodeLoc)
        Finally
                gStatus = "Init Treeview lasted: " & VB.Timer - t & " Seconds!"
        Try
        Me.Cursor = Cursors.Default
        tv.EndUpdate()
        tv.Visible = True
        tv.Enabled = True
        Me.Enabled = True
        Me.Show()
        gStatus = CodeLoc & " Show finished"
        Catch ex As Exception
        HandleError(ex, CodeLoc)
        End Try
        End Try

        Me.BringToFront()
        Windows.Forms.Application.DoEvents()
        End Sub


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

