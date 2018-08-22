package ak.kawlay.mail.utoronto.ca.textreader;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends Activity {

    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    DatabaseHelper mDatabaseHelper;
    List<receiptRecord> receiptList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabaseHelper = new DatabaseHelper(this);

        Cursor data = mDatabaseHelper.getData();
        receiptList = new ArrayList<receiptRecord>();

        while(data.moveToNext()){
            receiptRecord newRecord = new receiptRecord(data.getDouble(5), data.getString(1),
                    data.getString(3), data.getString(2), data.getString(4));
            receiptList.add(newRecord);
        }

        adapter = new RecyclerAdapter(this, receiptList);
        recyclerView.setAdapter(adapter);

    }
}
