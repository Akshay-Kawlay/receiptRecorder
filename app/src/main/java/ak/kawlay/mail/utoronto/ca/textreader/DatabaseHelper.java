package ak.kawlay.mail.utoronto.ca.textreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "receipt_records";
    private static final String COL0 = "ID";
    private static final String COL1 = "name";
    private static final String COL2 = "date";
    private static final String COL3 = "category";
    private static final String COL4 = "path";
    private static final String COL5 = "amount";
    private static final String COL6 = "month";
    private static final String COL7 = "year";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 + " TEXT, " + COL2 + " TEXT, " + COL3 + " TEXT, " + COL4 + " TEXT, " + COL5 + " DOUBLE, " +
                COL6 + " TEXT, " + COL7 + " TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(receiptRecord item){
        SQLiteDatabase db = this.getWritableDatabase();
        String month = new SimpleDateFormat("MMM").format(new Date());
        String year = new SimpleDateFormat("yyyy").format(new Date());
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, item.getName());
        contentValues.put(COL2, item.getDate());
        contentValues.put(COL3, item.getCategory().toUpperCase());
        contentValues.put(COL4, item.getPhotoPath());
        contentValues.put(COL5, item.getAmount());
        contentValues.put(COL6, month);
        contentValues.put(COL7, year);

        Log.i(TAG, "addData: Adding "+ item.getName() + " to " + TABLE_NAME);

        long check = db.insert(TABLE_NAME, null, contentValues);

        if (check == -1){
            return false;
        }else{
            return true;
        }

    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getTotalExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT sum(" + COL5 + ") FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Double getTotalExpenditureThisYear(){
        SQLiteDatabase db = this.getWritableDatabase();
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String query = "SELECT sum(" + COL5 + ") FROM " + TABLE_NAME +
                      " WHERE " + COL7 + "==" + year;
        Cursor data = db.rawQuery(query, null);

        return data.getDouble(0);
    }

    public Cursor getCategoryWiseThisMonthExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String month = new SimpleDateFormat("MMM").format(new Date());
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String query = "SELECT " + COL3 +", sum(" + COL5 + ")"+
                " FROM " + TABLE_NAME +
                " WHERE " + "("+ COL6 + "==" + month + " and " + COL7 + "==" + year + ")" +
                " GROUP BY " + COL3;
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getCategoryWiseThisYearExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String query = "SELECT " + COL3 +", sum(" + COL5 + ")"+
                " FROM " + TABLE_NAME +
                " WHERE " + COL7 + "==" + year +
                " GROUP BY " + COL3;
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getCategoryWiseTotalExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL3 +", sum(" + COL5 + ")"+
                      " FROM " + TABLE_NAME +
                      " GROUP BY " + COL3;
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getYearWiseTotalExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL7 +", sum(" + COL5 + ")"+
                " FROM " + TABLE_NAME +
                " GROUP BY " + COL7;
        Cursor data = db.rawQuery(query, null);

        return data;
    }
}
