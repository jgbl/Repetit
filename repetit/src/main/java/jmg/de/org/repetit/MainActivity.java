package jmg.de.org.repetit;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jmg.de.org.repetit.lib.HackyViewPager;
import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;
import me.texy.treeview.base.SelectableTreeAction;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mTextMessage;
    private boolean isTV;
    private boolean isWatch;
    private ViewGroup Layout;
    public HackyViewPager mPager;
    public MyFragmentPagerAdapter fPA;
    public TreeView treeView;
    public dbSqlite db;

    public MainActivity()
    {
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        if (db == null){
            db = new dbSqlite(this, false);
            db.createDataBase();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (db != null)
        {
            db.close();
            db = null;
        }
    }
@Override
protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        db = new dbSqlite(this, false);
            db.createDataBase();
        //lib.main = this;
        lib.gStatus = "MainActivity onCreate";
        //getting the kind of userinterface: television or watch or else
        int UIMode = lib.getUIMode(this);
        switch (UIMode)
        {
        case Configuration.UI_MODE_TYPE_TELEVISION:
        isTV = true;
        break;
        case Configuration.UI_MODE_TYPE_WATCH:
        isWatch = true;
        break;
        }

        if (savedInstanceState != null)
        {
        //JMGDataDirectory = savedInstanceState.getString("JMGDataDirectory");
        }

        setContentView(R.layout.activity_main_viewpager);

        /** Getting a reference to ViewPager from the layout */
        View pager = this.findViewById(R.id.pager);
        Layout = (ViewGroup) pager;
        mPager = (HackyViewPager) pager;
        /** Getting a reference to FragmentManager */
        FragmentManager fm = getSupportFragmentManager();

        setPageChangedListener();

        /** Creating an instance of FragmentPagerAdapter */
        if (fPA == null)
        {
        fPA = new MyFragmentPagerAdapter(fm, this, savedInstanceState != null);
        }

        /** Setting the FragmentPagerAdapter object to the viewPager object */
        mPager.setAdapter(fPA);





        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try
        {
            switch (item.getItemId())
            {
                case R.id.select_all:
                    treeView.selectAll();
                    break;
                case R.id.deselect_all:
                    treeView.deselectAll();
                    break;
                case R.id.expand_all:
                    treeView.expandAll();
                    break;
                case R.id.collapse_all:
                    treeView.collapseAll();
                    break;
                case R.id.expand_level:
                    treeView.expandLevel(1);
                    break;
                case R.id.collapse_level:
                    treeView.collapseLevel(1);
                    break;
                case R.id.show_select_node:
                    Toast.makeText(getApplication(), getSelectedNodes(), Toast.LENGTH_LONG).show();
                    break;
                case R.id.mnuShowMed:
                    mPager.setCurrentItem(MedActivity.fragID);
                    break;
                case R.id.mnuShowSympt:
                    mPager.setCurrentItem(SymptomsActivity.fragID);
                    break;
                case R.id.mnuFindMeds:
                    String[] qry;
                    if (mPager.getCurrentItem()==SymptomsActivity.fragID)
                    {
                        qry = fPA.fragSymptoms.getQueryMed(true, false);
                    }
                    else if (mPager.getCurrentItem()==MedActivity.fragID)
                    {
                        qry = fPA.fragMed.getQueryMed(true, false);
                    }
                    else
                    {
                        break;
                    }
                    if (lib.libString.IsNullOrEmpty(qry[1])) break;
                    mPager.setCurrentItem(MedActivity.fragID);
                    //String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                    //        "WHERE " + qry[0] + " AND Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + qry[1] + ")";
                    String qryMedGrade = "Select Medikamente.*, SymptomeOFMedikament.GRADE, SymptomeOFMedikament.SymptomID, Symptome.Text, Symptome.ShortText, Symptome.KoerperTeilID, Symptome.ParentSymptomID FROM SymptomeOfMedikament, Medikamente, Symptome " +
                            "WHERE Medikamente.ID = SymptomeOfMedikament.MedikamentID AND SymptomeOfMedikament.SymptomID = Symptome.ID AND (" + qry[1] + ")";
                    qryMedGrade += " ORDER BY Medikamente.Name, SymptomeOfMedikament.GRADE DESC";
                    fPA.fragMed.buildTreeRep(qryMedGrade, true);
                    //((MainActivity)getActivity()).fPA.fragMed.buildTree("SELECT * FROM Medikamente WHERE " + qry, true);
                    break;
            }
        }
        catch (Throwable ex)
        {
            Log.e(TAG,"OptionsItemSelected",ex);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPageChangedListener()
    {
        /** Defining a listener for pageChange */
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            int LastPosition = -1;

            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);

                if (LastPosition == MedActivity.fragID)
                {
                    try
                    {
                        if (fPA != null && fPA.fragSettings != null)
                        {
                            try
                            {
                                //fPA.fragSettings.saveResultsAndFinish(true);
                            }
                            catch (Throwable ex)
                            {
                                Log.e(".saveResultsAndFinish", ex.getMessage(), ex);
                            }
                    					/*
                    					if (lib.NookSimpleTouch())
                    					{
                    						RemoveFragSettings();
                    					}
                    					*/
                        }

                    }
                    catch (Throwable e)
                    {

                        lib.ShowException(MainActivity.this, e);
                    }
                    //mnuUploadToQuizlet.setEnabled(true);
                }
                else if (LastPosition == SymptomsActivity.fragID)
                {
                    if (fPA != null && fPA.fragMed != null)
                    {
                        //fPA.fragMed.removeCallbacks();
                    }
                }

                if (position == MedActivity.fragID)
                {
                    //mnuAddNew.setEnabled(true);
                    //mnuUploadToQuizlet.setEnabled(true);

                    if (fPA != null && fPA.fragMed != null)
                    {
                        treeView = fPA.fragMed.treeView;
                    /*
                        fPA.fragMed._txtMeaning1.setOnFocusChangeListener(new View.OnFocusChangeListener()
                        {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus)
                            {
                                fPA.fragMed._txtMeaning1.setOnFocusChangeListener(fPA.fragMed.FocusListenerMeaning1);
                                if (hasFocus)
                                {
                                    fPA.fragMed._scrollView.fullScroll(View.FOCUS_UP);
                                }

                            }
                        });
                        */
                    }
                }
                else if (position == SettingsActivity.fragID)
                {
                    if (fPA != null && fPA.fragSettings != null)
                    {
                        try
                        {
                            /*
                            int Language = fPA.fragSettings.getIntent().getIntExtra(
                                    "Language", org.de.jmg.learn.vok.Vokabel.EnumSprachen.undefiniert.ordinal());
                            fPA.fragSettings.spnLanguages.setSelection(Language);
                            fPA.fragSettings.setSpnMeaningPosition();
                            fPA.fragSettings.setSpnWordPosition();
                            fPA.fragSettings.setChkTSS();
                            */
                        }
                        catch (Throwable ex)
                        {
                            Log.e(".saveResultsAndFinish", ex.getMessage(), ex);
                        }
                    }
                }
                else if (position == SymptomsActivity.fragID)
                {
                    if (fPA != null && fPA.fragSymptoms != null)
                    {
                        treeView = fPA.fragSymptoms.treeView;
                        //searchQuizlet();
                    }

                }
                else
                {
                    //mnuAddNew.setEnabled(false);
                }

                LastPosition = position;


            }

        };

        /** Setting the pageChange listener to the viewPager */
        mPager.addOnPageChangeListener(pageChangeListener);

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



}
