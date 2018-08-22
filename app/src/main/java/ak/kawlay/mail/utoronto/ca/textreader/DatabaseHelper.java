package ak.kawlay.mail.utoronto.ca.textreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "receipt_records";
    private static final String COL0 = "ID";
    private static final String COL1 = "name";
    private static final String COL2 = "date";
    private static final String COL3 = "category";
    private static final String COL4 = "path";
    private static final String COL5 = "amount";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 + " TEXT, " + COL2 + " TEXT, " + COL3 + " TEXT, " + COL4 + " TEXT, " + COL5 + " DOUBLE)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(receiptRecord item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, item.getName());
        contentValues.put(COL2, item.getDate());
        contentValues.put(COL3, item.getCategory());
        contentValues.put(COL4, item.getPhotoPath());
        contentValues.put(COL5, item.getAmount());

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
}
