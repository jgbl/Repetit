package jmg.de.org.repetit;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private int IDTerm = - 1;
    private dbSqlite db;
    private ArrayAdapter<Meaning> listAdapter;
    private boolean isNewTerm;
    private Button btnAdd;
    private Button btnDeleteTerm;
    private Button btnClose;
    private LinearLayout llButtons;
    private LinearLayout llTerms;

    private class Meaning {
        int ID;
        int FachbegriffsID;
        String Text;

        public Meaning(int ID, int FachbegriffsID, String Text) {
            this.ID = ID;
            this.FachbegriffsID = FachbegriffsID;
            this.Text = Text;
        }

        @Override
        public String toString() {
            return Text;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lib.gStatus = "Activity_Terms onCreate";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setFinishOnTouchOutside(true);
        }
        setContentView(R.layout.activity_terms);
        txtTerm = (EditText) findViewById(R.id.txtTerm);
        txtMeaning = (EditText) findViewById(R.id.txtMeaning);
        txtMeaning.setText("");
        lstMeanings = (ListView) findViewById(R.id.lstMeanings);
        lstMeanings.setLongClickable(true);
        listAdapter = new ArrayAdapter<Meaning>(this, R.layout.simplerow);
        lstMeanings.setAdapter(listAdapter);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnDeleteTerm = (Button) findViewById(R.id.btnDeleteTerm);
        btnClose = (Button) findViewById((R.id.btnClose));
        llButtons = (LinearLayout)findViewById(R.id.llButtons);
        llTerms = (LinearLayout)findViewById(R.id.llTerms);
        llButtons.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lib.removeLayoutListener(llButtons.getViewTreeObserver(), this);
                int width = (llTerms).getWidth();
                int widthButton = (width-lib.dpToPx(10))/3;
                int widthButtons = btnAdd.getWidth() + btnDeleteTerm.getWidth() + btnClose.getWidth() + lib.dpToPx(20);
                if (widthButtons == 370)
                {
                    float scale = (float) width / (float) widthButtons;
                    btnAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX,btnAdd.getTextSize() * scale);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)btnAdd.getLayoutParams();
                    params.width = widthButton;
                    btnAdd.setLayoutParams(params);
                    btnDeleteTerm.setTextSize(TypedValue.COMPLEX_UNIT_PX,btnDeleteTerm.getTextSize() * scale);
                    params = (LinearLayout.LayoutParams)btnDeleteTerm.getLayoutParams();
                    params.width = widthButton;
                    btnDeleteTerm.setLayoutParams(params);
                    btnClose.setTextSize(TypedValue.COMPLEX_UNIT_PX,btnClose.getTextSize() * scale);
                    params = (LinearLayout.LayoutParams)btnClose.getLayoutParams();
                    params.width = widthButton;
                    btnClose.setLayoutParams(params);
                    btnClose.setWidth(widthButton);
                }

            }
        });

        if (savedInstanceState == null && getIntent() != null) strTerm = getIntent().getStringExtra("term");

        db = ((repApplication) getApplication()).db;

        if (db != null) {
            txtTerm.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    strTerm = s.toString();
                    IDTerm = -1;
                    updatelst();
                }
       });

            lstMeanings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    txtMeaning.setText(listAdapter.getItem(position).Text);
                }
            });

            lstMeanings.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    try {
                        lib.getMessageYesNo(ActivityTerms.this, getString(R.string.DeleteMeaning) + " " + listAdapter.getItem(position), getString(R.string.DeleteMeaning), false, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.deleteMeaning(listAdapter.getItem(position).ID);
                                listAdapter.remove(listAdapter.getItem(position));
                                txtMeaning.setText("");
                            }
                        }, null).show();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return true;
                }
            });

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strTerm = txtTerm.getText().toString();
                    if (strTerm != null && strTerm.length() > 0) {
                        int FachbegriffsID = db.InsertTerm(strTerm);
                        if (FachbegriffsID > -1 && txtMeaning.getText().toString().length() > 0) {
                            int ID = -1;
                            try {
                                ID = db.InsertMeaning(FachbegriffsID, txtMeaning.getText().toString());
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            if (ID > -1)
                                listAdapter.add(new Meaning(ID, FachbegriffsID, txtMeaning.getText().toString()));
                        }
                    }
                }
            });
            btnDeleteTerm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.deleteTerm(strTerm,IDTerm);
                    strTerm = "";
                    txtTerm.setText(strTerm);
                    listAdapter.clear();
                    txtTerm.setText("");
                }
            });
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            finish();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        txtTerm.setText(strTerm);
        updatelst();
    }

    private void updatelst() {
        strTerm = txtTerm.getText().toString();
        Cursor c = db.query("SELECT * FROM Fachbegriffe WHERE lower(Text) = '" + strTerm.toLowerCase() + "'");
        listAdapter.clear();
        IDTerm = -1;
        if (c.moveToFirst()) {
            isNewTerm = false;
            int ID = c.getInt(c.getColumnIndex("ID"));
            IDTerm = ID;
            Cursor cc = db.query("SELECT * FROM Bedeutungen WHERE FachbegriffsID = " + ID + "");
            if (cc.moveToFirst()) {
                do {
                    {
                        listAdapter.add(new Meaning(cc.getInt(cc.getColumnIndex("ID")), ID, cc.getString(cc.getColumnIndex("Text"))));
                    }
                } while (cc.moveToNext());
            }
            cc.close();
        } else {
            isNewTerm = true;
            IDTerm = -1;
        }
        c.close();
    }
}
