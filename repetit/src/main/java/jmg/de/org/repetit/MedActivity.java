package jmg.de.org.repetit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

import jmg.de.org.repetit.lib.ProgressClass;
import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.ContextMenuRecyclerView;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;
import me.texy.treeview.base.BaseNodeViewBinder;

import static android.R.id.input;
import static android.content.Context.MODE_PRIVATE;
import static jmg.de.org.repetit.lib.lib.libString.MakeFitForQuery;
import static org.apache.commons.codec.binary.Base64.*;


public class MedActivity extends Fragment {
    public final static int fragID = 0;
    private static final String TAG = "MedActivity";
    private final int ID_MENU_SAVE = 0;

    public MainActivity _main;

    protected Toolbar toolbar;
    private ViewGroup viewGroup;
    private TreeNode root;
    public TreeView treeView;
    private AppCompatEditText txtSearch;
    private ImageButton btnSearchAnd;
    private ImageButton btnSearchOr;
    private String _lastQuery;
    private String[] _txt;
    private ArrayList<Integer> Selected;
    private String[] finalArrSaves;
    private int finalCount;

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.med_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        int saves = _main.getPreferences(MODE_PRIVATE).getInt("saves", 0);
        String strSaves = _main.getPreferences(MODE_PRIVATE).getString("strSaves", null);
        String[] arrSaves = (!lib.libString.IsNullOrEmpty(strSaves) ? strSaves.replaceAll("^\"|\"$", "").split("\";\"") : null);
        if (arrSaves == null) saves = 0;
        else saves = arrSaves.length;
        //String[] strSaves = _main.getPreferences(MODE_PRIVATE).getString("strSaves",null).split(",");
        MenuItem mnuResults = menu.findItem(R.id.mnu_results);
        SubMenu subResults = mnuResults.getSubMenu();
        subResults.clear();
        for (int i = 0; i < saves; i++) {
            if (menu.findItem(ID_MENU_SAVE + i * 4) != null) continue;
            SubMenu item =
                    subResults.addSubMenu(Menu.NONE, ID_MENU_SAVE + i * 4, Menu.NONE, arrSaves[i]);
            item.add(Menu.NONE, ID_MENU_SAVE + i * 4 + 1, Menu.NONE, R.string.open);
            item.add(Menu.NONE, ID_MENU_SAVE + i * 4 + 2, Menu.NONE, R.string.rename);
            item.add(Menu.NONE, ID_MENU_SAVE + i * 4 + 3, Menu.NONE, R.string.delete);

            //MenuItemCompat.setActionView(item,new View(getContext()));
            //MenuItemCompat.getActionView(item).setOnLongClickListener(LongDeleteSave);
        }

        super.onPrepareOptionsMenu(menu);

    }

    private int finalSaves;
    private String finalStrSaves;
    private  EditText input;
    /*private View.OnLongClickListener LongDeleteSave = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MenuItem item = (MenuItem) v.getParent();
                    String title = (item.getTitle()).toString();
                    lib.yesnoundefined res = lib.ShowMessageYesNo(getContext(), String.format(getString(R.string.deletesave), title), getString(R.string.delete), false);
                    if (res == lib.yesnoundefined.yes) {
                        String strSaves = _main.getPreferences(MODE_PRIVATE).getString("strSaves", null);
                        strSaves = strSaves.replace(title, "").replace(";;", "").replaceAll("^;|;$", "");
                        _main.getPreferences(MODE_PRIVATE).edit().putString("strSaves", strSaves).commit();
                        int count = item.getItemId() - ID_MENU_SAVE;
                        _main.getPreferences(MODE_PRIVATE).edit().remove("save" + count).commit();
                    }
                    return true;
                }
            };


        */
    DialogInterface.OnClickListener ClickListenerSave = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                String txt = input.getText().toString();
                if (!lib.libString.IsNullOrEmpty(txt) && !lib.libString.IsNullOrEmpty(finalStrSaves) && finalArrSaves != null && Arrays.asList(finalArrSaves).contains(txt)) {
                    dialog.dismiss();
                    input = new EditText(getContext());
                    AlertDialog dlg = lib.getInputBox(getContext(), getString(R.string.nameexists, txt), getString(R.string.name), txt, false, ClickListenerSave, null, input);
                    dlg.show();
                }
                else {
                    int lSaves = finalSaves;
                    Bundle b = new Bundle();
                    onSaveInstanceState(b);
                    lSaves += 1;
                    Parcel p = Parcel.obtain(); // i make an empty one here, but you can use yours
                    b.writeToParcel(p, 0);
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] bytes = p.marshall();
                        bos.write(bytes, 0, bytes.length);
                        String data = new String(encodeBase64(bos.toByteArray()), "UTF-8");
                        _main.getPreferences(MODE_PRIVATE).edit().putString("save" + lSaves, data).commit();
                        bos.close();
                    } catch (Exception e) {
                        lib.ShowException(getContext(), e);
                        Log.e(getClass().getSimpleName(), e.toString(), e);
                    } finally {
                        p.recycle();
                    }

                    String lStrSaves = finalStrSaves;
                    if (!lib.libString.IsNullOrEmpty(lStrSaves)) lStrSaves += ";";
                    else lStrSaves = "";
                    lStrSaves += "\"" + txt + "\"";
                    _main.getPreferences(MODE_PRIVATE).edit().putInt("saves", lSaves).commit();
                    _main.getPreferences(MODE_PRIVATE).edit().putString("strSaves", lStrSaves).commit();
                }


            }
            catch (Throwable ex)
            {
                lib.ShowException(getContext(),ex);
            }
        }
    };

    DialogInterface.OnClickListener ClickListenerRename = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                String txt = input.getText().toString();
                if (!lib.libString.IsNullOrEmpty(txt) && !lib.libString.IsNullOrEmpty(finalStrSaves) && finalArrSaves != null && Arrays.asList(finalArrSaves).contains(txt)) {
                    dialog.dismiss();
                    input = new EditText(getContext());
                    AlertDialog dlg = lib.getInputBox(getContext(), getString(R.string.nameexists, txt), getString(R.string.name), txt, false, ClickListenerRename, null, input);
                    dlg.show();
                }
                else
                {
                    if (!lib.libString.IsNullOrEmpty(txt)) {
                        finalArrSaves[finalCount - 1] = txt;
                        String strSaves = lib.arrStrToCSV(finalArrSaves);
                        _main.getPreferences(MODE_PRIVATE).edit().putString("strSaves", strSaves).commit();
                    }
                }


            }
            catch (Throwable ex)
            {
                lib.ShowException(getContext(),ex);
            }
        }
    };

    private final static int OpenResultCode = 1001;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (_main==null||data == null)return;
        Uri uri = data.getData();
        try
        {
            String file = uri.getPath(); // = lib.getRealFilePath(_main,uri);
            file = file.replace("/document/raw:","");
            if(!lib.libString.IsNullOrEmpty(file))
            {
                if (_main.db!=null){
                    _main.db.close();
                    File f = new File(file);
                    _main.db.DB_PATH = f.getParent() + "/";
                    _main.db.dbname = f.getName();
                    _main.db.openDataBase();
                    if (_main.fPA.fragMed!=null)_main.fPA.fragMed.refresh();
                    if (_main.fPA.fragSymptoms!=null)_main.fPA.fragSymptoms.refresh();
                    if (_main.fPA.fragData!=null)_main.fPA.fragData.refresh();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int saves = _main.getPreferences(MODE_PRIVATE).getInt("saves", 0);
            String strSaves = _main.getPreferences(MODE_PRIVATE).getString("strSaves", null);
            String[] arrSaves;
            if (strSaves != null) {
                arrSaves = strSaves.replaceAll("^\"|\"$", "").split("\";\"");
                saves = arrSaves.length;
            }
            else
            {
                arrSaves = null;
            }
            int ID = item.getItemId();
            finalSaves = saves;
            finalStrSaves = strSaves;
            finalArrSaves = arrSaves;
            switch (ID) {
                case R.id.mnu_save:
                    input = new EditText(getContext());
                    AlertDialog dlg = lib.getInputBox(getContext(), getString(R.string.save_result), getString(R.string.name), "", false, ClickListenerSave ,null,input);
                    dlg.show();
                    return true;
                case R.id.mnu_open:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, OpenResultCode);
                    return true;
                default:
                    if (ID >= this.ID_MENU_SAVE && ID < this.ID_MENU_SAVE + saves * 4) {
                        try {
                            //ComplexPreferences prefs2 = ComplexPreferences.getComplexPreferences(getContext(), "save", MODE_PRIVATE);
                            int count = (ID - ID_MENU_SAVE) / 4 + 1;
                            int sub = (ID - ID_MENU_SAVE) % 4;
                            switch (sub) {
                                case 0:
                                    break;
                                case 1:
                                    String bytes = _main.getPreferences(MODE_PRIVATE).getString("save" + count, null);
                                    if (!lib.libString.IsNullOrEmpty(bytes)) {
                                        //b = prefs2.getObject("save" + count, Bundle.class);
                                        Parcel p = Parcel.obtain(); // i make an empty one here, but you can use yours
                                        Bundle b;
                                        try {
                                            byte[] data = decodeBase64(bytes.getBytes("UTF-8"));
                                            p.unmarshall(data, 0, data.length);
                                            p.setDataPosition(0);
                                            b = p.readBundle();
                                        } finally {
                                            p.recycle();
                                        }
                                        _lastQuery = b.getString("lastquery");
                                        _txt = b.getStringArray("txt");
                                        Selected = b.getIntegerArrayList("Selected");
                                        if (!lib.libString.IsNullOrEmpty(_lastQuery)) {
                                            buildTreeRep(_lastQuery, true, _txt, Selected, b);
                                        }
                                    }
                                    return true;
                                case 2:
                                    input = new EditText(getContext());
                                    finalCount = count;
                                    dlg = lib.getInputBox(getContext(), getString(R.string.rename_result), getString(R.string.name), arrSaves[count - 1], false, ClickListenerRename ,null,input);
                                    dlg.show();
                                    return true;
                                case 3:
                                    lib.yesnoundefined res2 = lib.ShowMessageYesNo(getContext(), String.format(getString(R.string.deletesave), arrSaves[count - 1]), getString(R.string.delete), false);
                                    if (res2 == lib.yesnoundefined.yes) {
                                        arrSaves[count - 1] = null;//strSaves = strSaves.replace(arrSaves[count-1], "").replace("\"\"","").replace(";;", "").replaceAll("^;|;$", "");
                                        strSaves = lib.arrStrToCSV(arrSaves);
                                        SharedPreferences prefs = _main.getPreferences(MODE_PRIVATE);
                                        prefs.edit().putString("strSaves", strSaves).commit();
                                        prefs.edit().remove("save" + count).commit();
                                        for (int i = count; i < saves; i++) {
                                            prefs.edit().putString("save" + i, prefs.getString("save" + (i + 1), null)).commit();
                                        }
                                        if (count < saves) prefs.edit().remove("save" + saves);
                                        _main.getPreferences(MODE_PRIVATE).edit().putInt("saves", saves - 1).commit();
                                    }
                                    return true;
                                default:
                                    throw new RuntimeException("invalid menu");
                            }
                        } catch (Throwable ex) {
                            lib.ShowException(getContext(), ex);
                        }
                    }
                    break;
            }
        }
        catch (Throwable ex)
        {
            lib.ShowException(getContext(),ex);
        }

        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = _main.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        try {
            ContextMenuRecyclerView.RecyclerViewContextMenuInfo info = (ContextMenuRecyclerView.RecyclerViewContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.cmnuSearch:
                    String search;
                    if (info.treeNode.getValue() instanceof TreeNodeHolderMed) {
                        TreeNodeHolderMed h = (TreeNodeHolderMed) info.treeNode.getValue();
                        search = h.Name;
                    } else if (info.treeNode.getValue() instanceof TreeNodeHolderSympt) {
                        TreeNodeHolderSympt h = (TreeNodeHolderSympt) info.treeNode.getValue();
                        search = h.SymptomText;
                    } else {
                        throw new RuntimeException("TreeNodeHolder not found!");
                    }
                    Uri uri = Uri.parse("http://www.google.com/#q=" + search);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                case R.id.cmnuShowAll:
                    //lib.ShowMessage(getContext(),((TreeNodeHolder)info.treeNode.getValue()).Text,"Node");
                    treeView.collapseNode(info.treeNode);
                    info.treeNode.getChildren().clear();
                /*if (info.treeNode.getLevel() == 1)
                {
                    try
                    {
                        FirstLevelNodeViewBinderMed.buildTree(treeView,info.treeNode);
                    }
                    catch (Throwable throwable)
                    {
                        throwable.printStackTrace();
                    }
                }
                */
                    info.treeNode.holder.onNodeToggled(info.treeNode, true);
                    //treeView.toggleNode(info.treeNode);
                    //treeView.expandNode(info.treeNode);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        catch (Throwable ex)
        {
            lib.ShowException(getContext(),ex);
            return super.onContextItemSelected(item);
        }
    }

    public void initTreeView(View view, Bundle savedinstancestate) throws  Throwable {
        if (view!=null)initView(view);

        root = TreeNode.root();
        treeView = new TreeView(root, _main, new MyNodeViewFactoryMed());
        View view2 = treeView.getView();
        registerForContextMenu(view2);
        view2.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewGroup.addView(view2);
        if (_main.treeView == null) _main.treeView = treeView;

        if (lib.libString.IsNullOrEmpty(_lastQuery) && !lib.libString.IsNullOrEmpty(_main.lastQuery)) {
            String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                    "WHERE Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + _main.lastQuery + ")";
            qryMedGrade += " ORDER BY Medikamente.Name, SymptomeOfMedikament.GRADE DESC";
            buildTreeRep(qryMedGrade, true, null, Selected, savedinstancestate);
        } else if (!lib.libString.IsNullOrEmpty(_lastQuery)) {
            buildTreeRep(_lastQuery, true, _txt, Selected, savedinstancestate);
        } else {
            buildTree("SELECT * FROM Medikamente ORDER BY Name", true, savedinstancestate);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstancestate) {
        View v = null;
        try {
            v = inflater.inflate(R.layout.activity_med, container, false);
            txtSearch = (AppCompatEditText) v.findViewById(R.id.txtSearch);
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
            btnSearchAnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String txt = txtSearch.getText().toString();
                    if (!lib.libString.IsNullOrEmpty(txt)) searchSymptoms(txt, true);
                }
            });
            btnSearchOr = (ImageButton) v.findViewById(R.id.btnSearchOr);
            btnSearchOr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String txt = txtSearch.getText().toString();
                    if (!lib.libString.IsNullOrEmpty(txt)) searchSymptoms(txt, false);
                }
            });
            if (savedinstancestate != null) {
                _lastQuery = savedinstancestate.getString("lastquery");
                _txt = savedinstancestate.getStringArray("txt");
                Selected = savedinstancestate.getIntegerArrayList("Selected");
            }

            initTreeView(v, savedinstancestate);
            return v;
        } catch (Throwable ex) {
            lib.ShowException(_main, ex);
            return v;
        }
    }

    private void restoreTreeView(Bundle savedinstancestate) throws Throwable {
        if (treeView != null && savedinstancestate != null) {
            if (root != null) {
                ArrayList<Integer> expMed = savedinstancestate.getIntegerArrayList("expMed");
                ArrayList<Integer> expMedSymp = savedinstancestate.getIntegerArrayList("expMedSymp");
                if (expMed.size() == 0) return;
                for (TreeNode t : root.getChildren()) {
                    if (t.hasChild() == false || true) {
                        TreeNodeHolderMed h = (TreeNodeHolderMed) t.getValue();
                        if (expMed.contains(h.ID)) {
                            expMed.remove(new Integer(h.ID));
                            while (expMedSymp.size() > 0 && expMedSymp.get(0) == -99)
                                expMedSymp.remove(0);
                            if (t.hasChild() == false)
                                FirstLevelNodeViewBinderMed.buildTree(treeView, t);
                            else treeView.expandNode(t);
                            lib.gStatus = "expSympMed";
                            expSympMed(h.ID, t, expMedSymp, Selected);
                        }
                        if (expMed.size() == 0) break;

                    }
                }
                if (Selected.size() > 0) {
                    for (TreeNode t : root.getChildren()) {
                        TreeNodeHolderMed h = (TreeNodeHolderMed) t.getValue();
                        while (Selected.size() > 0 && h.ID == Selected.get(0)) {
                            Selected.remove(0);
                            SymptomsActivity.AddNodesRecursive(_main, 1, null, t, Selected.get(0), Selected.get(1), h.ID);
                            Selected.remove(0);
                            Selected.remove(0);
                        }
                    }
                }

            }

        }
    }

    private void expSympMed(int id, TreeNode t, ArrayList<Integer> expMedSymp, ArrayList<Integer> selected) throws Throwable {

        CheckSelected(id, t, selected);
        if (expMedSymp.size() == 0) return;
        if (-99 == expMedSymp.get(0) && -99 == expMedSymp.get(1)) {
            expMedSymp.remove(0);
            expMedSymp.remove(0);
            return;
        }


        for (TreeNode tt : t.getChildren()) {
            if (expMedSymp.size() == 0) return;

            if (-99 == expMedSymp.get(0) && -99 == expMedSymp.get(1)) {
                expMedSymp.remove(0);
                expMedSymp.remove(0);
                break;
            }
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
            if (h.ParentMedID == expMedSymp.get(0) && h.ID == expMedSymp.get(1)) {
                expMedSymp.remove(0);
                expMedSymp.remove(0);
                if (tt.hasChild() == false) SecondLevelNodeViewBinder.buildTree(treeView, tt);
                else treeView.expandNode(tt);
                if (expMedSymp.size() <= 0) {
                    CheckSelected(id, tt, selected);
                    break;
                }
                if (-99 == expMedSymp.get(0) && -99 == expMedSymp.get(1)) {
                    expMedSymp.remove(0);
                    expMedSymp.remove(0);
                    CheckSelected(id, tt, selected);
                } else {
                    expSympMed(id, tt, expMedSymp, selected);
                }

            }
        }
    }

    private void CheckSelected(int id, TreeNode t, ArrayList<Integer> selected) {
        for (TreeNode tt : t.getChildren()) {
            if (selected.size() <= 0) break;
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
            for (int i = 0; i < selected.size(); i += 3) {
                if (selected.get(i) == id && selected.get(i + 1) == h.ID) {
                    treeView.selectNode(tt, 1);
                    tt.setWeight(selected.get(i + 2));
                    selected.remove(i);
                    selected.remove(i);
                    selected.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("lastquery", _lastQuery);
        outState.putStringArray("txt", _txt);
        if (treeView != null) {
            if (root != null) {
                ArrayList<Integer> expMed = new ArrayList<>();
                ArrayList<Integer> expMedSymp = new ArrayList<>();
                ArrayList<Integer> Selected = new ArrayList<>();
                for (TreeNode t : root.getChildren()) {
                    if (t.hasChild() && t.isExpanded()) {
                        TreeNodeHolderMed h = (TreeNodeHolderMed) t.getValue();
                        expMed.add(h.ID);
                        getSympMed(h.ID, t, expMedSymp);
                    }
                }
                for (TreeNode t : treeView.getSelectedNodes()) {
                    if (t.getValue() instanceof TreeNodeHolderSympt) {
                        TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
                        Selected.add(h.ParentMedID);
                        Selected.add(h.ID);
                        Selected.add(t.getWeight());
                        //Selected.add(root.getWeight());
                    }
                }
                outState.putIntegerArrayList("expMed", expMed);
                outState.putIntegerArrayList("expMedSymp", expMedSymp);
                outState.putIntegerArrayList("Selected", Selected);
            }

        }
    }

    private void getSympMed(int Medid, TreeNode t, ArrayList<Integer> sympMed) {
        boolean hasChild = false;
        for (TreeNode tt : t.getChildren()) {
            if (tt.hasChild() && tt.isExpanded()) {
                TreeNodeHolderSympt h = (TreeNodeHolderSympt) tt.getValue();
                sympMed.add(Medid);
                sympMed.add(h.ID);
                getSympMed(Medid, tt, sympMed);
                hasChild = true;
            } else {

            }
        }
        if (hasChild) {
            sympMed.add(-99);
            sympMed.add(-99);
        }

    }

    public static String getWhereWhole(String column, String search) {
        return "WHERE " + column + " like '% " + search + " %' OR " + column + " like '" + search + " %' OR " + column + " like '% " + search + "' OR " + column + " like '" + search + "'";
    }

    private void searchSymptoms(String searchtxt, boolean AndFlag) {
        if (lib.libString.IsNullOrEmpty(searchtxt)) return;
        String[] txt = searchtxt.split("\\.");
        try {
            //String qry = "SELECT Medikamente.* FROM Symptome WHERE ";
            if (!lib.libString.IsNullOrEmpty(_main.lastQuery)) {
                lib.yesnoundefined res = (lib.ShowMessageYesNo(getContext(), getString(R.string.alreadysearched), getString(R.string.continuesearch), false));
                if (res != lib.yesnoundefined.yes) return;
            }
            String where = "";
            for (String s : txt) {
                String whereWhole = null;
                if (!lib.libString.IsNullOrEmpty(s)) {
                    s = MakeFitForQuery(s, true);
                    if (AndFlag) {
                        if (!(where.equalsIgnoreCase(""))) where += " AND ";
                        if (txt.length > 1)
                            where += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome " + (_main.blnSearchWholeWord ? getWhereWhole("Text", s) : "WHERE Text LIKE '%" + s + "%'") + ")";
                        else
                            where += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome " + (_main.blnSearchWholeWord ? getWhereWhole("ShortText", s) : "WHERE ShortText LIKE '%" + s + "%'") + ")";
                    } else {
                        if (!(where.equalsIgnoreCase(""))) where += " OR ";
                        where += "SymptomeOfMedikament.SymptomID IN (SELECT ID FROM Symptome " + (_main.blnSearchWholeWord ? getWhereWhole("ShortText", s) : "WHERE ShortText LIKE '%" + MakeFitForQuery(s, true) + "%'") + ")";
                    }
                }
            }
            if (!AndFlag || txt.length < 2) txt = null;
            //AddSymptomeQueryRecursive(root,qry,-1,true);
            String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                    "WHERE Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + where + ")";
            qryMedGrade += " ORDER BY Medikamente.Name, SymptomeOfMedikament.GRADE DESC";
            buildTreeRep(qryMedGrade, true, txt, null, null);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public String getSelectedNodes() {
        StringBuilder stringBuilder = new StringBuilder("You have selected: ");
        List<TreeNode> selectedNodes = treeView.getSelectedNodes();
        for (int i = 0; i < selectedNodes.size(); i++) {
            if (i < 5) {
                stringBuilder.append(selectedNodes.get(i).getValue().toString()).append(",");
            } else {
                stringBuilder.append("...and ").append(selectedNodes.size() - 5).append(" more.");
                break;
            }
        }
        return stringBuilder.toString();
    }

    public void refresh() throws Throwable {
        buildTree("SELECT * FROM Medikamente ORDER BY Name", true, null);
    }


    public class TreeNodeHolderMed extends TreeNodeHolder {
        public final int ID;
        public final String Name;
        public final String Beschreibung;
        public int totalGrade;
        public int count;

        public TreeNodeHolderMed(MainActivity context, int level, String Text, String path, int ID, String Name, String Beschreibung) {
            super(level, Text, path, context);
            this.ID = ID;
            this.Name = Name;
            this.Beschreibung = Beschreibung;
        }
    }


    public void buildTree(final String qry, final boolean refresh, final Bundle savedinstancestate) {
        final Context context = getContext();
        new AsyncTask<Void, ProgressClass, Integer>() {
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

            private void createProgress() {
                pd = new ProgressDialog(context);
                pd.setTitle(getString(R.string.repertorising));
                pd.setMessage(getString(R.string.startingRep));
                pd.setIndeterminate(false);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                ProgressClass pc = new ProgressClass(0, 100, context.getString(R.string.startingquery), false);
                publishProgress(pc);
                if (root.getChildren().size() > 0) {
                    List<TreeNode> l = root.getChildren();
                    l.clear();
                    root.setChildren(l);
                }
                dbSqlite db = ((MainActivity) getActivity()).db;
                try {

                    Cursor c = db.query(qry);
                    try {
                        if (c.moveToFirst()) {
                            final int ColumnNameId = c.getColumnIndex("Name");
                            final int ColumnIDId = c.getColumnIndex("ID");
                            final int ColumnBeschreibungId = c.getColumnIndex("Beschreibung");
                            int count = c.getCount();
                            do {
                                counter += 1;
                                if (count < 10 || counter % (count / 10) == 0) {
                                    pc.update(counter, count, context.getString(R.string.processingquery), false);
                                    publishProgress(pc);
                                }
                                int ID = c.getInt(ColumnIDId);
                                String Name = c.getString(ColumnNameId);
                                String Beschreibung = c.getString(ColumnBeschreibungId);
                                TreeNode treeNode = new TreeNode(new TreeNodeHolderMed((MainActivity) getActivity(), 0, Name, "Med" + ID, ID, Name, Beschreibung));
                                treeNode.setLevel(0);
                                root.addChild(treeNode);

                            } while (c.moveToNext());
                        }
                    } finally {
                        c.close();
                    }

                } catch (Throwable ex) {
                    this.ex = ex;
                } finally {
                    db.close();
                }


                return counter;

            }

            @Override
            protected void onProgressUpdate(ProgressClass... params) {
                try {
                    super.onProgressUpdate(params);
                    ProgressClass p = params[0];
                    if (pd != null) {
                        if (p.blnRestart) {
                            pd.dismiss();
                            createProgress();
                            pd.show();
                        }
                        pd.setProgress(p.counter);
                        if (p.msg != null && !p.msg.equalsIgnoreCase(oldmsg)) {
                            pd.setMessage(p.msg);
                            oldmsg = p.msg;
                        }
                        if (p.max > 0 && !(p.max == oldmax)) {
                            pd.setMax(p.max);
                            oldmax = p.max;
                        }
                    } else {
                        Log.i("dbsqlite", "no progress");
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void onPostExecute(final Integer result) {
                // continue what you are doing...

                pd.dismiss();
                if (refresh && treeView != null) treeView.refreshTreeView();
                if (savedinstancestate != null) try {
                    restoreTreeView(savedinstancestate);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                if (this.ex != null) lib.ShowException(context, ex);
            }


        }.execute();


    }


    public void buildTreeRep(final String qry, final boolean refresh, final String[] txt, final ArrayList<Integer> selected, final Bundle savedinstancestate) {
        final Context context = getContext();
        new AsyncTask<Void, ProgressClass, Integer>() {
            public Throwable ex;
            public int oldmax;
            public String oldmsg;
            ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                createProgress();
            }

            private void createProgress() {
                pd = new ProgressDialog(context);
                pd.setTitle(getString(R.string.repertorising));
                pd.setMessage(getString(R.string.startingRep));
                pd.setIndeterminate(false);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                ProgressClass pc = new ProgressClass(0, 100, context.getString(R.string.startingRep), false);
                publishProgress(pc);
                boolean Initialized = true;
                final String CodeLoc = TAG + ".initTreeview";
                lib.gStatus = CodeLoc + " Start";
                int MedID;
                int counter = 0;
                //ArrayList<TreeNodeHolderMed> arrMed = new ArrayList<>();
                if (root.getChildren().size() > 0) {
                    List<TreeNode> l = root.getChildren();
                    l.clear();
                    root.setChildren(l);
                }
                dbSqlite db = ((MainActivity) getActivity()).db;
                try {
                    Cursor c = db.query(qry);
                    try {
                        if (c.moveToFirst()) {
                            int ColumnNameId = c.getColumnIndex("Name");
                            int ColumnIDId = c.getColumnIndex("ID");
                            int ColumnBeschreibungId = c.getColumnIndex("Beschreibung");
                            int ColumnGrade = c.getColumnIndex("Grade");
                            pc.update(0, c.getCount(), context.getString(R.string.processingquery), false);
                            this.publishProgress(pc);
                            counter += 1;
                            pc.counter = counter;
                            this.publishProgress(pc);
                            do {
                                //if(!c.moveToNext()) break;
                                int ID = c.getInt(ColumnIDId);
                                String Name = c.getString(ColumnNameId);
                                String Beschreibung = c.getString(ColumnBeschreibungId);
                                int sum = 0;
                                int nexts = 0;
                                TreeNodeHolderMed hMed = new TreeNodeHolderMed((MainActivity) getActivity(), 0, Name, "Med" + ID, ID, Name, Beschreibung);
                                TreeNode treeNode = new TreeNode(hMed);
                                treeNode.setLevel(0);
                                root.addChild(treeNode);
                                int f = insertSymptom(c, treeNode, hMed, selected, ID, txt);
                                if (f == -2) f = 1;
                                if (f >= 0) {
                                    sum = c.getInt(ColumnGrade) * f;
                                    nexts += 1;
                                }
                                while (c.moveToNext() && c.getInt(ColumnIDId) == ID) {
                                    counter += 1;
                                    if (pc.max < 10 || counter % (pc.max / 10) == 0) {
                                        pc.counter = counter;
                                        this.publishProgress(pc);
                                    }
                                    f = insertSymptom(c, treeNode, hMed, selected, ID, txt);
                                    if (f == -2) f = 1;
                                    if (f >= 0) {
                                        nexts += 1;
                                        sum += c.getInt(ColumnGrade) * f;
                                    }
                                }
                                counter += 1;
                                hMed.totalGrade = sum;
                                hMed.count = nexts;
                                hMed.Text += "(" + hMed.totalGrade + "/" + hMed.count + ")";
                            } while (!c.isAfterLast());
                            List<TreeNode> l = root.getChildren();

                            Collections.sort(l, new Comparator<TreeNode>() {
                                @Override
                                public int compare(TreeNode lhs, TreeNode rhs) {
                                    TreeNodeHolderMed h1 = (TreeNodeHolderMed) lhs.getValue();
                                    TreeNodeHolderMed h2 = (TreeNodeHolderMed) rhs.getValue();
                                    if (h1.totalGrade > h2.totalGrade) return -1;
                                    if (h1.totalGrade == h2.totalGrade && h1.count > h2.count)
                                        return -1;
                                    if (h1.totalGrade == h2.totalGrade && h1.count == h2.count)
                                        return 0;
                                    return 1;
                                }
                            });
                            root.setChildren(l);
                            //if (refresh) treeView.refreshTreeView();

                        }
                    } finally {
                        c.close();
                    }

                } catch (Throwable ex) {
                    this.ex = ex;
                } finally {
                    db.close();
                }
                return counter;

            }

            @Override
            protected void onProgressUpdate(ProgressClass... params) {
                try {
                    super.onProgressUpdate(params);
                    ProgressClass p = params[0];
                    if (pd != null) {
                        if (p.blnRestart) {
                            pd.dismiss();
                            createProgress();
                            pd.show();
                        }
                        pd.setProgress(p.counter);
                        if (p.msg != null && !p.msg.equalsIgnoreCase(oldmsg)) {
                            pd.setMessage(p.msg);
                            oldmsg = p.msg;
                        }
                        if (p.max > 0 && !(p.max == oldmax)) {
                            pd.setMax(p.max);
                            oldmax = p.max;
                        }
                    } else {
                        Log.i("dbsqlite", "no progress");
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void onPostExecute(final Integer result) {
                // continue what you are doing...

                pd.dismiss();
                if (refresh && treeView != null) treeView.refreshTreeView();
                _lastQuery = qry;
                _txt = txt;
                if (savedinstancestate != null) try {
                    restoreTreeView(savedinstancestate);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                if (this.ex != null) lib.ShowException(context, ex);

            }


        }.execute();


    }

    private int insertSymptom(Cursor c, TreeNode treeNode, TreeNodeHolderMed hMed, ArrayList<Integer> Weight, int ID, String[] txt) {
        int res;
        final int ColumnTextId = c.getColumnIndex("Text");
        final int ColumnSymptomIDId = c.getColumnIndex("SymptomID");
        final int ColumnShortTextId = c.getColumnIndex("ShortText");
        final int ColumnKoerperTeilId = c.getColumnIndex("KoerperTeilID");
        final int ColumnParentSymptomId = c.getColumnIndex("ParentSymptomID");
        final int ColumnGradeId = c.getColumnIndex("Grade");
        int grade = -1;
        if (ColumnGradeId >= 0) {
            grade = c.getInt(ColumnGradeId);
        }
        int SympID = c.getInt(ColumnSymptomIDId);
        String Text = c.getString(ColumnTextId);
        String ShortText = c.getString(ColumnShortTextId);
        Integer KoerperTeilId = c.getInt(ColumnKoerperTeilId);
        Integer ParentSymptomId = c.getInt(ColumnParentSymptomId);
        boolean found = false;
        if (txt != null && txt.length > 0) {
            for (String t : txt) {
                if (ShortText.toLowerCase().contains(t.toLowerCase())) {
                    found = true;
                    break;
                }
            }
        } else {
            found = true;
        }
        if (!found) return -1;
        res = -1;
        if (Weight != null && Weight.size() > 0) {
            for (int i = 0; i < Weight.size(); i += 3) {
                int MedID = Weight.get(0 + i);
                int SympID2 = Weight.get(1 + i);
                int Weight2 = Weight.get(2 + i);
                if (MedID >= 0) {
                    if (MedID == hMed.ID && SympID == SympID2) {
                        res = Weight2;
                        break;
                    }
                } else if (SympID == SympID2) {
                    res = Weight2;
                    break;
                }
            }
        } else {
            res = -2;
        }
        ShortText += (grade >= 0 && res > 0 ? "[" + grade * res + "]" : "(" + grade + ")");
        TreeNode treeNode2 = new TreeNode(new TreeNodeHolderSympt(hMed.getContext(), 1, ShortText, "Sympt" + SympID, SympID, Text, ShortText, KoerperTeilId, ParentSymptomId, hMed.ID, grade));
        if (res >= 0) treeNode2.setWeight(res);
        try {
            SymptomsActivity.AddNodesRecursive(hMed.getContext(), 0, treeNode2, treeNode, ParentSymptomId, res, hMed.ID);
            return (res == -2 ? 1 : res);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return -1;
        }
        //treeNode2.setLevel(1);
        //treeNode.addChild(treeNode2);

    }

    //private String lastQuery = "";
    public String[] getQueryMed(boolean OrFlag, boolean Wide, boolean blnAdd, ArrayList<Integer> selected) {
        if (!blnAdd) {
            _main.lastQuery = "";
            selected.clear();
        }
        String qry = "";
        String qrySymptMed = _main.lastQuery;
        List<TreeNode> arr = treeView.getSelectedNodes();
        int count = arr.size();
        for (TreeNode t : arr) {
            if (t.getValue() instanceof TreeNodeHolderMed) continue;
            TreeNodeHolderSympt h = (TreeNodeHolderSympt) t.getValue();
            int found = selected.indexOf(new Integer(h.ID));
            if (found >= 0 && (found - 1) % 3 != 0) {
                found = -1;
                for (int i = 0; i < selected.size(); i = i + 3) {
                    if (selected.get(i + 1) == h.ID) {
                        found = i + 1;
                        break;
                    }
                }
            }
            ;
            if (found >= 0) continue;
            selected.add(-1);
            selected.add(h.ID);
            selected.add(t.getWeight());
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

