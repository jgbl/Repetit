package jmg.de.org.repetit;

/**
 * Created by jhmgbl on 23.09.17.
 */

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;


public class SQLiteDataActivity extends Fragment
{
    public final static int fragID = 2;
    jmg.de.org.repetit.lib.dbSqlite sqLiteHelper;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    ListAdapter listAdapter ;
    ListView LISTVIEW;
    boolean blndata;

    ArrayList<String> ID_Array = new ArrayList<>();
    ArrayList<String> date_Array = new ArrayList<>();
    ArrayList<String> ex_Array = new ArrayList<>();
    ArrayList<String> CodeLoc_Array = new ArrayList<>();
    ArrayList<String> comment_Array = new ArrayList<>();
    ArrayList<String> exported_Array = new ArrayList<>();
    public MainActivity _main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqLiteHelper = ((MainActivity)getActivity()).db;
        this.setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.activity_sqlite_data,container,false);

        LISTVIEW = (ListView) v.findViewById(R.id.listView1);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        menuInflater.inflate(R.menu.errors, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        try {
            switch (item.getItemId()) {
                case R.id.menu_gotolast:
                    if (LISTVIEW != null && listAdapter != null)
                        LISTVIEW.setSelection(listAdapter.getCount() - 1);
                    return true;
                case R.id.menu_deleteerrors:
                    if (sqLiteHelper != null && sqLiteHelper.DataBase != null) {
                        sqLiteHelper.DataBase.execSQL("DELETE FROM Errors");
                        ShowSQLiteDBdata();
                    }
                    return true;
                case R.id.menu_exporterrors:
                    if (sqLiteHelper != null) try {

                    } catch (Throwable throwable) {
                        Log.e("SQLiteDataActivity","exportErrors",throwable);
                        throwable.printStackTrace();
                    }
                    return true;
                case R.id.menu_close:
                    ((MainActivity)getActivity()).mPager.setCurrentItem(MedActivity.fragID);
                    return true;


            }
        }
        catch (Throwable e)
        {
            Log.e("Errors","menu",e);
        }
        return super.onOptionsItemSelected(item);
    }

    public void init(dbSqlite db) throws Throwable
    {
        sqLiteHelper = db;
        ShowSQLiteDBdata() ;

    }

    @Override
    public void onResume() {

        try {
            ShowSQLiteDBdata();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onPause() {

        if (cursor!= null) {
            cursor.close();
            cursor = null;
        }
        super.onPause();
    }

    @Override
    public void onStop() {

        if (cursor!= null) {
            cursor.close();
            cursor=null;
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {

        if (sqLiteHelper!= null) {
            sqLiteHelper.close();
        }
        super.onDestroy();
    }

    private void ShowSQLiteDBdata() throws Throwable {

        if (LISTVIEW == null) return;
        //sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        if (sqLiteHelper==null) sqLiteHelper = ((MainActivity)getActivity()).db;
        if (sqLiteHelper == null) return;
        cursor = sqLiteHelper.query((blndata?"SELECT * FROM Data":"SELECT * FROM Errors"));

        listAdapter = new jmg.de.org.repetit.lib.ListAdapter(SQLiteDataActivity.this, cursor);
        /*
                ID_Array,
                date_Array,
                ex_Array,
                CodeLoc_Array,
                comment_Array,
                exported_Array
        );
        */
        LISTVIEW.setAdapter(listAdapter);

        //cursor.close();
    }

    public void refresh() throws Throwable {
        ShowSQLiteDBdata();
    }
}



