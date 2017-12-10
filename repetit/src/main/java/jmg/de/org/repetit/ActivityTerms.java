package jmg.de.org.repetit;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
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
    private EditText txtTerm;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setFinishOnTouchOutside(true);
        }
        setContentView(R.layout.activity_terms);
        txtTerm = (EditText) findViewById(R.id.txtTerm);
        txtMeaning = (EditText)findViewById(R.id.txtMeaning);
        txtMeaning.setText("");
        lstMeanings = (ListView)findViewById(R.id.lstMeanings);
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
        lstMeanings.setAdapter(listAdapter);
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
                    do
                    {
                        {
                            listAdapter.add(cc.getString(cc.getColumnIndex("Text")));
                        }
                    } while (cc.moveToNext());
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
                  strTerm = txtTerm.getText().toString();
                  if (strTerm != null )
                  {
                      int FachbegriffsID = db.InsertTerm(strTerm);
                      if (txtMeaning.getText().toString().length()>0)
                      {
                          boolean BedInserted = db.InsertMeaning(FachbegriffsID,txtMeaning.getText().toString());
                          if (BedInserted) listAdapter.add(txtMeaning.getText().toString());
                      }
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
