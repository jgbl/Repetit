package jmg.de.org.repetit;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jmg.de.org.repetit.lib.lib;

/**
 * Created by hmnatalie on 08.12.17.
 */

public class ActivityTerms extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lib.gStatus = "Activity_Terms onCreate";
        setContentView(R.layout.activity_terms);

    }
}
