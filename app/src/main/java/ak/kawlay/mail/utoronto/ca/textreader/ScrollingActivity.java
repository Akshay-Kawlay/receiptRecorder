package ak.kawlay.mail.utoronto.ca.textreader;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class ScrollingActivity extends Activity {

    RecyclerView recyclerView;
    RecyclerAdapter adapter;

    List<receiptRecord> receiptList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra("ca.utoronto.mail.kawlay.ak.textreader.RECORD_LIST")){
            receiptList = (List<receiptRecord>) getIntent().getSerializableExtra("ca.utoronto.mail.kawlay.ak.textreader.RECORD_LIST");
        }

        adapter = new RecyclerAdapter(this, receiptList);
        recyclerView.setAdapter(adapter);

    }
}
