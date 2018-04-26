package jmg.de.org.repetit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jmg.de.org.repetit.lib.HackyViewPager;
import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;
import me.texy.treeview.TreeNode;
import me.texy.treeview.TreeView;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";
    private final Thread.UncaughtExceptionHandler defaultUEH;
    private TextView mTextMessage;
    private boolean isTV;
    private boolean isWatch;
    private ViewGroup Layout;
    public HackyViewPager mPager;
    public MyFragmentPagerAdapter fPA;
    public TreeView treeView;
    public dbSqlite db;
    public String lastQuery = "";
    public boolean blnSearchWholeWord;
    public ArrayList<Integer> selected = new ArrayList<>();
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread thread, Throwable ex)
                {
                    try
                    {
                        dbSqlite sql;
                        if (MainActivity.this.db != null)
                        {
                            sql = MainActivity.this.db;
                        }
                        else
                        {
                            sql = new dbSqlite(getApplicationContext(), false);
                        }
                        sql.createDataBase();
                        sql.InsertError(ex, "uncaughtExceptionHandler", null);
                        sql.close();
                    }
                    catch (Throwable throwable)
                    {
                        Log.e(TAG,"dbSqlite",throwable);
                        throwable.printStackTrace();
                    }
                    /*
                    // here I do logging of exception to a db
                    PendingIntent myActivity = PendingIntent.getActivity(MainActivity.this,
                            192837, new Intent(MainActivity.this, MainActivity.class),
                            PendingIntent.FLAG_ONE_SHOT);
                    AlarmManager alarmManager;
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            15000, myActivity);
                    System.exit(2);
                    */
                    // re-throw critical exception further to the os (important)
                    defaultUEH.uncaughtException(thread, ex);
                }
            };

    private Bundle savedInstanceState;
    public  static String versionName;
    public boolean blnSearchTerms;


    public MainActivity()
    {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (db == null)
        {
            try
            {
                db = new dbSqlite(this, false);
                if (db.createDataBase() == null) db.openDataBase();
                ((repApplication)getApplication()).db = db;
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
            }

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
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        if (db!=null)
        {
            savedInstanceState.putString("dbname", db.dbname);
            savedInstanceState.putString("dbpath", db.DB_PATH);
        }

        savedInstanceState.putString("lastquery", lastQuery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try {
            this.versionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        lib.gStatus = "MainActivity onCreate";
        setContentView(R.layout.activity_main_viewpager);
        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString("lastquery");

        } else {
            try {
                AcceptLicense();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        requestPermission(savedInstanceState);

    }

    private void init(Bundle savedInstanceState)
    {
        boolean blnStop = false;
        if (db == null)
        {
            try
            {
                db = new dbSqlite(this, false);
                String ex;
                if ((ex = db.createDataBase()) != null){
                    blnStop = true;
                    lib.getDialogOK(this, getString(R.string.DatabaseErrorOlderVersions) + "\n" + ex, getString(R.string.database), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    }).show();
                }
                else
                {
                    if (savedInstanceState!=null)
                    {
                        db.dbname= savedInstanceState.getString("dbname");
                        db.DB_PATH = savedInstanceState.getString("dbpath");
                    }

                    db.openDataBase();
                    try
                    {
                        db.DataBase.execSQL("ALTER TABLE Medikamente ADD COLUMN Polychrest INTEGER");
                    }
                    catch (Throwable eex)
                    {

                    }
                }
                ((repApplication)getApplication()).db = db;
            }
            catch (Throwable throwable)
            {
                blnStop = true;
                lib.ShowException(this,throwable);
            }

        }
        else if(savedInstanceState!=null)
        {
            String dbname = savedInstanceState.getString("dbname");
            String dbpath = savedInstanceState.getString("dbpath");
            if (dbname != null && dbpath != null && (!dbname.equalsIgnoreCase(db.dbname) || !dbpath.equalsIgnoreCase(db.DB_PATH)))
            {
                db.close();
                db.dbname = dbname;
                db.DB_PATH = dbpath;
                db.openDataBase();
            }


        }
        //lib.main = this;
        //getting the kind of userinterface: television or watch or else
        if (!blnStop) {
            int UIMode = lib.getUIMode(this);
            switch (UIMode) {
                case Configuration.UI_MODE_TYPE_TELEVISION:
                    isTV = true;
                    break;
                case Configuration.UI_MODE_TYPE_WATCH:
                    isWatch = true;
                    break;
            }



        /* Getting a reference to ViewPager from the layout */
            View pager = this.findViewById(R.id.pager);
            Layout = (ViewGroup) pager;
            mPager = (HackyViewPager) pager;
        /* Getting a reference to FragmentManager */
            FragmentManager fm = getSupportFragmentManager();

            setPageChangedListener();

        /* Creating an instance of FragmentPagerAdapter */
            if (fPA == null) {
                fPA = new MyFragmentPagerAdapter(fm, this, savedInstanceState != null);
            }

        /* Setting the FragmentPagerAdapter object to the viewPager object */
            mPager.setAdapter(fPA);

        }

    }

    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION ) {//&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init(savedInstanceState);
        }
    }



    private void requestPermission(Bundle savedInstanceState) {
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.savedInstanceState = savedInstanceState;
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
            init(savedInstanceState);
        }
    }


    private void AcceptLicense() throws Throwable
    {
        boolean blnLicenseAccepted = getPreferences(Context.MODE_PRIVATE).getBoolean("LicenseAccepted", false);
        if (!blnLicenseAccepted)
        {
            InputStream is = this.getAssets().open("LICENSE");
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            String strLicense = s.hasNext() ? s.next() : "";
            s.close();
            is.close();
            lib.yesnoundefined res = (lib.ShowMessageYesNo(this,
                    strLicense,
                    getString(R.string.licenseaccept),
                    true));

            lib.yesnoundefined res2 = lib.AcceptPrivacyPolicy(this, Locale.getDefault());

            lib.yesnoundefined res3 = lib.AcceptDisclaimer(this, Locale.getDefault());


            if (res == lib.yesnoundefined.yes && res2 == lib.yesnoundefined.yes && res3 == lib.yesnoundefined.yes)
            {
                getPreferences(Context.MODE_PRIVATE).edit().putBoolean("LicenseAccepted", true).commit();
            }
            else
            {
                finish();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item = menu.findItem(R.id.mnuSearchWholeWord);
        item.setChecked(blnSearchWholeWord);
        item = menu.findItem(R.id.mnuSearchTerms);
        item.setChecked(blnSearchTerms);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        try
        {
            switch (item.getItemId())
            {
                case R.id.mnuCredits:
                    InputStream is = this.getAssets().open("CREDITS");
                    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                    String strCredits = s.hasNext() ? s.next() : "";
                    s.close();
                    is.close();
                    String versionName = this.getPackageManager()
                                             .getPackageInfo(this.getPackageName(), 0).versionName;
                    Spannable spn = lib.getSpanableString(strCredits + "\nV" + versionName);
                    lib.ShowMessage(this, spn, "Credits");
                    break;
                case R.id.mnuPrivacyPolicy:
                    lib.yesnoundefined res2 = lib.AcceptPrivacyPolicy(this, Locale.getDefault());

                    if (res2 == lib.yesnoundefined.yes)
                    {
                        getPreferences(Context.MODE_PRIVATE).edit().putBoolean("PPAccepted", true).commit();
                    } else
                    {
                        getPreferences(Context.MODE_PRIVATE).edit().putBoolean("PPAccepted", false).commit();
                        finish();
                    }
                    break;
                case R.id.mnuContact:
                    Intent intent = new Intent(Intent.ACTION_SEND, Uri.fromParts("mailto", "jhmgbl2@t-online.de", null));
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"jhmgbl2@t-online.de"});
                    String versionName2 = this.getPackageManager()
                                              .getPackageInfo(this.getPackageName(), 0).versionName;
                    intent.putExtra(Intent.EXTRA_SUBJECT, "repetit " + versionName2);
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.Contact));
                    this.startActivity(Intent.createChooser(intent, getString(R.string.SendMail)));
                    break;
                case R.id.mnuHelp:
                    lib.ShowHelp(this,Locale.getDefault());
                    break;
                case R.id.mnuSwitchDB:
                    if (db!=null){
                        db.close();
                        lastQuery = null;
                        if(db.DB_PATH.equalsIgnoreCase(db.original_path))
                        {
                            if (db.dbname.indexOf("kent2") > -1)
                            {
                                db.dbname = db.dbname.replace("kent2", "kent");
                            } else
                            {
                                db.dbname = db.dbname.replace("kent", "kent2");
                            }
                        }
                        else
                        {
                            db.DB_PATH = db.original_path;
                            db.dbname = db.original_name;
                        }
                        db.openDataBase();
                        if (fPA.fragMed!=null)fPA.fragMed.refresh();
                        if (fPA.fragSymptoms!=null)fPA.fragSymptoms.refresh();
                        if (fPA.fragData!=null)fPA.fragData.refresh();
                        mPager.setCurrentItem(MedActivity.fragID);
                    }
                    break;
                case R.id.deselect_all:
                    treeView.deselectAll();
                    break;
                case R.id.collapse_all:
                    treeView.collapseAll();
                    break;
                case R.id.show_select_node:
                    Toast.makeText(getApplication(), getSelectedNodes(), Toast.LENGTH_LONG).show();
                    break;
                case R.id.mnuSearchWholeWord:
                    item.setChecked(item.isChecked() ^ true);
                    blnSearchWholeWord = item.isChecked();
                    break;
                case R.id.mnuSearchTerms:
                    item.setChecked(item.isChecked() ^ true);
                    blnSearchTerms = item.isChecked();
                    break;
                case R.id.mnuShowMed:
                    mPager.setCurrentItem(MedActivity.fragID);
                    break;
                case R.id.mnuShowSympt:
                    mPager.setCurrentItem(SymptomsActivity.fragID);
                    break;
                case R.id.mnuResetSearch:
                    if (mPager.getCurrentItem() == SymptomsActivity.fragID)
                    {
                        fPA.fragSymptoms.refresh();
                    } else
                    if (mPager.getCurrentItem() == MedActivity.fragID)
                    {
                        fPA.fragMed.refresh();
                    }
                    break;
                case R.id.mnuFindMeds:
                    break;
                case R.id.mnuFindMedsAdd:
                    String[] qry;
                    boolean blnAdd = false;
                    if (item.getItemId() == R.id.mnuFindMedsAdd) blnAdd = true;
                    if (!blnAdd && !lib.libString.IsNullOrEmpty(lastQuery)) {
                        lib.yesnoundefined res = (lib.ShowMessageYesNo(this, getString(R.string.alreadysearched), getString(R.string.continuesearch), false));
                        if (res != lib.yesnoundefined.yes) break;
                    }
                    if (mPager.getCurrentItem() == SymptomsActivity.fragID)
                    {
                        qry = fPA.fragSymptoms.getQueryMed(true, false, blnAdd, selected);
                        fPA.fragSymptoms.blnHasbeenRepertorised = true;
                    } else
                        if (mPager.getCurrentItem() == MedActivity.fragID)
                        {
                            qry = fPA.fragMed.getQueryMed(true, false, blnAdd, selected);
                        } else
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
                    lastQuery = qry[1];
                    fPA.fragMed._lastQuery = null;
                    fPA.fragMed.buildTreeRep(qryMedGrade, true, null, selected, null);
                    //((MainActivity)getActivity()).fPA.fragMed.buildTree("SELECT * FROM Medikamente WHERE " + qry, true);
                    break;
            }
        }
        catch (Throwable ex)
        {
            Log.e(TAG, "OptionsItemSelected", ex);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPageChangedListener()
    {
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
                } else
                    if (LastPosition == SymptomsActivity.fragID)
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
                } else
                    if (position == SettingsActivity.fragID)
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
                    } else
                        if (position == SymptomsActivity.fragID)
                        {
                            if (fPA != null && fPA.fragSymptoms != null)
                            {
                                treeView = fPA.fragSymptoms.treeView;
                                //searchQuizlet();
                            }

                        } else
                        {
                            //mnuAddNew.setEnabled(false);
                        }

                LastPosition = position;


            }

        };

        mPager.addOnPageChangeListener(pageChangeListener);

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


}
