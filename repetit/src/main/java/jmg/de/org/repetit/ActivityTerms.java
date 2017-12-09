package jmg.de.org.repetit;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ListView;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;

/**
 * Created by hmnatalie on 08.12.17.
 */

public class ActivityTerms extends AppCompatActivity {
    private  EditText txtTerm;
    private EditText txtMeaning;
    private ListView lstMeanings;
    private String strTerm;
    private dbSqlite db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lib.gStatus = "Activity_Terms onCreate";
        setContentView(R.layout.activity_terms);
        txtTerm = (EditText)findViewById(R.id.txtTerm);
        txtMeaning = (EditText)findViewById(R.id.txtMeaning);
        lstMeanings = (ListView)findViewById(R.id.lstMeanings);
        strTerm = getIntent().getStringExtra("term");
        db = ((repApplication)getApplication()).db;
    }
}
