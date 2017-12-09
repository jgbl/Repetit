package jmg.de.org.repetit;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import jmg.de.org.repetit.lib.dbSqlite;
import jmg.de.org.repetit.lib.lib;

/**
 * Created by hmnatalie on 08.12.17.
 */

public class ActivityTerms extends AppCompatActivity {
    private TextView txtTerm;
    private EditText txtMeaning;
    private ListView lstMeanings;
    private String strTerm;
    private dbSqlite db;
    private ArrayAdapter<String> listAdapter;
    private boolean isNewTerm;
    private Button btnAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lib.gStatus = "Activity_Terms onCreate";
        setContentView(R.layout.activity_terms);
        txtTerm = (TextView)findViewById(R.id.txtTerm);
        txtMeaning = (EditText)findViewById(R.id.txtMeaning);
        lstMeanings = (ListView)findViewById(R.id.lstMeanings);
        btnAdd = (Button)findViewById(R.id.btnAdd);
        strTerm = getIntent().getStringExtra("term");
        db = ((repApplication)getApplication()).db;

        if (db!=null)
        {
            txtTerm.setText(strTerm);
            Cursor c = db.query("SELECT * FROM Fachbegriffe WHERE Text = '" + strTerm + "'");
            if (c.moveToFirst())
            {
                int ID = c.getInt(c.getColumnIndex("ID"));
                Cursor cc = db.query("SELECT * FROM Bedeutungen WHERE FachbegriffsID = " + ID + "");
                if (cc.moveToFirst())
                {
                    listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
                    do
                    {
                        {
                            listAdapter.add(cc.getString(cc.getColumnIndex("Text")));
                        }
                    } while (cc.moveToNext());
                    lstMeanings.setAdapter(listAdapter);
                }
            }
            else
            {
                isNewTerm = true;
            }

            btnAdd.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isNewTerm)
                    {
                      int FachbegriffsID = db.InsertTerm(strTerm);
                    }
                }
            });
        }
        else
        {
            finish();
        }
    }
}
