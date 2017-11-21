package jmg.de.org.repetit.lib;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import jmg.de.org.repetit.MainActivity;
import jmg.de.org.repetit.R;

public class dbSqlite extends SQLiteOpenHelper {
    //The Android's default system path of your application database.
    public String DB_PATH = "/data/data/jmg.de.org.repetit/databases/";
    public String original_path = null;
    private static String DB_NAME = "replitekent.sqlite";
    private static String DB_NAMEERR = "Errors.sqlite";
    public String dbname = DB_NAME;
    public SQLiteDatabase DataBase;

    public Context mContext;

    public String original_name;

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int P1, int P2) {

    }

    public dbSqlite(Context context, boolean blnErr) throws Throwable {
        super(context, (blnErr ? DB_NAMEERR : DB_NAME), null, 1);
        if (blnErr) dbname = DB_NAMEERR;
        if (context == null) {
            throw new RuntimeException("context is null!");
        }
        lib.gStatus = "getFilesDir";
        File FilesDir = context.getFilesDir();
        DB_PATH = Path.combine(FilesDir.getPath(), "databases");
        this.mContext = context;
        lib.gStatus = "getExternalStorageDirectory";
        String extPath = Environment.getExternalStorageDirectory().getPath();
        File F = new File(extPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            File F2 = context.getExternalFilesDir(null);
            if (F2 != null) F = F2;
            extPath = F.getPath();
            if (F.isDirectory() == false && !F.exists()) {
                F.mkdirs();
            }
        }
        if (F.isDirectory() && F.exists()) {
            String JMGDataDirectory = Path.combine(extPath, "repetit", "database");
            File F1 = new File(JMGDataDirectory);
            if (F1.isDirectory() == false && !F1.exists()) {
                F1.mkdirs();
                if (checkDataBase() && F1.exists()) {
                    try {
                        lib.copyFile(DB_PATH + dbname, Path.combine(JMGDataDirectory, dbname));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        lib.ShowException(context, e);
                    }
                }
            }
            if (F1.exists()) DB_PATH = JMGDataDirectory;
        }
        if (DB_PATH.endsWith("/") == false) {
            DB_PATH = DB_PATH + "/";
        }
        original_path = DB_PATH;
        original_name = dbname;
    }

    public final boolean createDataBase() {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            return true;
        } else {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                return copyDataBase();

            } catch (IOException e) {
                System.out.println(e.getMessage());
                if (mContext != null) lib.ShowException(mContext, e);
                return false;
                //throw new RuntimeException("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + dbname;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            System.out.println(e.getMessage());
            //database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null;
    }

    private boolean copyDataBase() throws IOException {

        //Open your local db as the input stream
        assert (mContext != null);
        lib.gStatus = "Copy Assets:" + DB_NAME;
        AssetManager A = mContext.getAssets();
        //int rID = mContext.getResources().getIdentifier("fortyonepost.com.lfas:raw/"+fileName, null, null);
        //InputStream myInput = A.open(DB_NAME);
        for (int ii = 0; ii <= 1; ii++) {
            // Path to the just created empty db
            String outFileName = DB_PATH + dbname;
            if (ii == 1) outFileName = outFileName.replace("kent","kent2");
            if ((new java.io.File(DB_PATH)).isDirectory() == false) {
                (new java.io.File(DB_PATH)).mkdirs();
            }
            //Open the empty db as the output stream

            File file = new File(outFileName);

            if (file.exists()) {
                file.delete();
            }
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream myOutput = new FileOutputStream(file);

            //transfer bytes from the inputfile to the outputfile
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] buffer = new byte[1024];
            String[] assetfiles;
            Locale L = Locale.getDefault();
            String folder;
            if ((L.equals(Locale.GERMAN)) || (L.equals(Locale.GERMANY))) {
                folder = "db";
            } else {
                folder = "dbEn";
            }
            if (ii == 1)
            {
                if (folder == "db") folder = "dbEn"; else folder = "db";
            }
            assetfiles = A.list(folder);
            Arrays.sort(assetfiles);
            byte[] buffer = new byte[1024];
            int length;
            try {
                for (int i = 0; i < assetfiles.length; i++) //I have definitely less than 10 files; you might have more
                {
                    String partname = assetfiles[i];
                    if (partname.startsWith("db") && partname.length() == 4) {
                        InputStream instream = A.open(folder + "/" + partname);
                        while ((length = instream.read(buffer, 0, 1024)) > 0) {
                            myOutput.write(buffer, 0, length);
                        }
                        instream.close();
                    }
                }

            } catch (Throwable ex) {
                Log.e("copyDatabase", ex.getMessage(), ex);
                if (file.exists()) {
                    file.delete();
                }
                return false;
            } finally {
                myOutput.flush();
                myOutput.close();
                //myInput.close();
            }
        }
        return true;
        //Close the streams


    }

    public final void openDataBase() {
        if (DataBase!=null)return;
        //Open the database
        String myPath = DB_PATH + dbname;
        if (Looper.myLooper() == Looper.getMainLooper() && mContext!=null && mContext instanceof MainActivity)
        {
            ((MainActivity) mContext).setTitle(dbname);
        }
        DataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        isClosed = false;
        stInsertRow = null;
        try {
            String sql = "CREATE TABLE IF NOT EXISTS \"Errors\" (\n" +
                    "\t`ID`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t`date`\tDATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                    "\t`ex`\tTEXT NOT NULL,\n" +
                    "\t`CodeLoc`\tTEXT,\n" +
                    "\t`comment`\tTEXT,\n" +
                    "\t`exported`\tINTEGER DEFAULT 0\n" +
                    ")";
            this.DataBase.execSQL(sql);
            this.DataBase.execSQL("PRAGMA foreign_keys = ON;");
        } catch (Throwable ex) {
            try {
                this.InsertError(ex, "OpenDatabase", "Create Table Errors");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public boolean isClosed = true;

    @Override
    public void close() {

        if (DataBase != null) {
            if (DataBase.isOpen()) DataBase.close();
            isClosed = true;
            DataBase = null;
        }
        stInsertRow = null;
        stInsertError = null;
        super.close();

    }

    public final android.database.Cursor query(String SQL) {
        if (DataBase == null) {
            openDataBase();
        }
        return DataBase.rawQuery(SQL, null);

    }

    public final android.database.Cursor query(String SQL, String[] params) {
        if (DataBase == null) {
            openDataBase();
        }
        return DataBase.rawQuery(SQL, params);

    }

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SQLiteStatement stInsertRow;

    public boolean InsertRow(String device, String address, Date date, Date dateFL, Date dateCL, Double altitude, Double latitude, Double longitude, Float acc, Float bearing, Float bacc, Float v, Float vacc, Integer steps, Double altitudeC, Double latitudeC, Double longitudeC, Float accC, Float temp, Float humidity) throws Throwable {

        if (DataBase == null) {
            openDataBase();
        }
        if (stInsertRow == null) {

            stInsertRow = DataBase.compileStatement("INSERT INTO Data (Device, Address, Date, DateFL, DateCL, " +
                    "Altitude, Latitude, Longitude, Accuracy, Bearing, BearingAccuracy, Speed, SpeedAccuracy, Steps, " +
                    "AltitudeC, LatitudeC, LongitudeC, AccuracyC, " +
                    "Temperature, Humidity) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        }
        this.DataBase.beginTransaction();
        try {
            SQLiteStatement st = stInsertRow;
            //ContentValues values = new ContentValues();
            st.clearBindings();
            st.bindString(1, device);
            st.bindString(2, address);
            st.bindString(3, dateFormat.format(date));
            if (dateFL == null) st.bindNull(4);
            else st.bindString(4, dateFormat.format(dateFL));
            if (dateCL == null) st.bindNull(5);
            else st.bindString(5, dateFormat.format(dateCL));
            if (altitude != null) st.bindDouble(6, altitude);
            if (latitude != null) st.bindDouble(7, latitude);
            if (longitude != null) st.bindDouble(8, longitude);
            if (acc != null) st.bindDouble(9, acc);
            if (bearing != null) st.bindDouble(10, bearing);
            if (bacc != null) st.bindDouble(11, bacc);
            if (v != null) st.bindDouble(12, v);
            if (vacc != null) st.bindDouble(13, vacc);
            if (steps != null) st.bindLong(14, steps);
            if (altitudeC != null) st.bindDouble(15, altitudeC);
            if (latitudeC != null) st.bindDouble(16, latitudeC);
            if (longitudeC != null) st.bindDouble(17, longitudeC);
            if (accC != null) st.bindDouble(18, accC);
            st.bindDouble(19, temp);
            st.bindDouble(20, humidity);
            st.executeInsert();
            //this.DataBase.insert("Data", null, values);
            this.DataBase.setTransactionSuccessful();
        } catch (Throwable ex) {
            Log.e("dbsqlite", "insert data", ex);
            return false;
        } finally {
            this.DataBase.endTransaction();
        }
        return true;
    }

    private SQLiteStatement stInsertError;

    public boolean InsertError(Throwable ex, String CodeLoc, String comment) throws Throwable {
        if (ex == null) return false;
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        String err = errors.toString();
        if (lib.libString.IsNullOrEmpty(comment)) comment = ex.getMessage();
        if (DataBase == null) {
            openDataBase();
        }
        if (stInsertError == null) {

            stInsertError = DataBase.compileStatement("INSERT INTO Errors (ex, CodeLoc, comment) VALUES(?,?,?)");
        }
        this.DataBase.beginTransaction();
        try {
            SQLiteStatement st = stInsertError;
            //ContentValues values = new ContentValues();
            st.clearBindings();
            st.bindString(1, err);
            st.bindString(2, CodeLoc);
            st.bindString(3, comment);
            st.executeInsert();
            //this.DataBase.insert("Data", null, values);
            this.DataBase.setTransactionSuccessful();
        } catch (Throwable eex) {
            return false;
        } finally {
            this.DataBase.endTransaction();
        }
        return true;
    }


}