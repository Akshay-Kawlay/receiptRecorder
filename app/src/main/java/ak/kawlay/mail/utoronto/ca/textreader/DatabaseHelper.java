package ak.kawlay.mail.utoronto.ca.textreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.util.ArrayList;
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

    public Double getTotalExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT sum(" + COL5 + ") FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        Double sum = -1.0;
        while(data.moveToNext()){
            sum = data.getDouble(0);
        }
        return sum;
    }

    public Double getTotalExpenditureThisYear(){
        SQLiteDatabase db = this.getWritableDatabase();
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String query = "SELECT sum(" + COL5 + ") FROM " + TABLE_NAME +
                      " WHERE " + COL7 + "==" + "'"+year+"'";
        Cursor data = db.rawQuery(query, null);
        Double sum = -1.0;
        while(data.moveToNext()){
            sum = data.getDouble(0);
        }
        return sum;
    }

    public Double getTotalExpenditureThisMonth(){
        SQLiteDatabase db = this.getWritableDatabase();
        String month = new SimpleDateFormat("MMM").format(new Date());
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String query = "SELECT sum(" + COL5 + ") FROM " + TABLE_NAME +
                      " WHERE " + COL6 + "==" + "'"+month+"'" + " and " + COL7 + "==" + "'"+year+"'";
        Cursor data = db.rawQuery(query, null);
        Double sum = -1.0;
        while(data.moveToNext()){
            sum = data.getDouble(0);
        }
        return sum;
    }

    public Cursor getCategoryWiseThisMonthExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String month = new SimpleDateFormat("MMM").format(new Date());
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String query = "SELECT " + COL3 +", sum(" + COL5 + ")"+
                " FROM " + TABLE_NAME +
                " WHERE " + COL6 + "==" + "'"+month+"'" + " and " + COL7 + "==" + "'"+year+"'" +
                " GROUP BY " + COL3 +
                " ORDER BY sum(" + COL5 + ") DESC";
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getCategoryWiseThisYearExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String query = "SELECT " + COL3 +", sum(" + COL5 + ")"+
                " FROM " + TABLE_NAME +
                " WHERE " + COL7 + "==" + "'"+year+"'" +
                " GROUP BY " + COL3 +
                " ORDER BY sum(" + COL5 + ") DESC";
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getCategoryWiseTotalExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL3 +", sum(" + COL5 + ")"+
                      " FROM " + TABLE_NAME +
                      " GROUP BY " + COL3 +
                      " ORDER BY sum(" + COL5 + ") DESC";
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getYearWiseTotalExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL7 +", sum(" + COL5 + ")"+
                " FROM " + TABLE_NAME +
                " GROUP BY " + COL7 +
                " ORDER BY " + COL7 + " DESC";
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public Cursor getMonthWiseThisYearExpenditure(){
        SQLiteDatabase db = this.getWritableDatabase();
        String year = new SimpleDateFormat("yyyy").format(new Date());

        String query = "SELECT " + COL6 +", sum(" + COL5 + ")"+
                " FROM " + TABLE_NAME +
                " WHERE " + COL7 + "==" + "'"+year+"'" +
                " GROUP BY " + COL6 +
                " ORDER BY sum(" + COL5 + ") DESC";
        Cursor data = db.rawQuery(query, null);

        return data;
    }

    public ArrayList<String> getAllCategories(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT distinct(" + COL3 + ") FROM " + TABLE_NAME;

        Cursor data = db.rawQuery(query, null);
        ArrayList<String> list = new ArrayList<>();
        while(data.moveToNext()){
            list.add(data.getString(0));
        }
        return list;
    }

    public void load_testbed(){

        add_testbed_data("NANDOS", "Some Date","FOOD" ,12.80,"Jan", "2016" );
        add_testbed_data("Hakka", "Some Date","FOOD" ,16.40,"Feb", "2017" );
        add_testbed_data("Pizza Nova", "Some Date","FOOD" ,10.90,"Aug", "2018" );

        add_testbed_data("IMAX", "Some Date","MOVIE" ,20.22,"Jul", "2016" );
        add_testbed_data("SILVER CITY", "Some Date","MOVIE" ,12.54,"Feb", "2017");
        add_testbed_data("INOX", "Some Date","MOVIE" ,18.65,"Aug", "2018");

        add_testbed_data("PENCIL", "Some Date","SCHOOL" ,7.22,"Nov", "2016" );
        add_testbed_data("PAPER", "Some Date","SCHOOL" ,12.54,"Jun", "2017");
        add_testbed_data("ERASER", "Some Date","SCHOOL" ,9.65,"Aug", "2018");

        add_testbed_data("HOST", "Some Date","FOOD" ,15.34,"Mar", "2018" );
        add_testbed_data("KFC", "Some Date","FOOD" ,14.90,"Sep", "2018" );
        add_testbed_data("BURGER KING", "Some Date","FOOD" ,8.96,"Aug", "2018" );

        add_testbed_data("CINEPLEX", "Some Date","MOVIE" ,10.22,"Jul", "2018" );
        add_testbed_data("STAR CITY", "Some Date","MOVIE" ,12.54,"Feb", "2017");
        add_testbed_data("THEATRE", "Some Date","MOVIE" ,13.65,"Apr", "2018");

        add_testbed_data("BOOKS", "Some Date","SCHOOL" ,30.22,"Nov", "2016" );
        add_testbed_data("PEN", "Some Date","SCHOOL" ,5.54,"Jun", "2018");
        add_testbed_data("TAPE", "Some Date","SCHOOL" ,9.65,"Aug", "2018");

    }

    public void add_testbed_data(String Name, String Date, String Category, Double amount, String month, String year){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, Name);
        contentValues.put(COL2, Date);
        contentValues.put(COL3, Category);
        contentValues.put(COL4, "NULL");
        contentValues.put(COL5, amount);
        contentValues.put(COL6, month);
        contentValues.put(COL7, year);

        Log.i(TAG, "addData: Adding "+ Name + " to " + TABLE_NAME);

        db.insert(TABLE_NAME, null, contentValues);

    }
}
