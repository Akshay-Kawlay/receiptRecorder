package ak.kawlay.mail.utoronto.ca.textreader;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class AnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    BarChart barChart;
    DatabaseHelper mDatabaseHelper;
    Spinner spinner;
    TextView totalExpense;
    private static final String[] items = {"CategoryWiseTotalExpenditure", "CategoryWiseThisYearExpenditure",
            "YearWiseTotalExpenditure"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        barChart = findViewById(R.id.bargraph);
        mDatabaseHelper = new DatabaseHelper(this);
        totalExpense = findViewById(R.id.textViewNumber);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setDrawBorders(true);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AnalysisActivity.this,
                        android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String total = "N/A";

        analyzeCategoryWiseTotalExpenditure(position);

       /* switch (position) {
            case 0:
                total = String.valueOf(mDatabaseHelper.getTotalExpenditure());
                break;
            case 1:
                total = String.valueOf(mDatabaseHelper.getTotalExpenditureThisYear());
                break;
            case 2:
                total = String.valueOf(mDatabaseHelper.getTotalExpenditure());
                break;
            case 3:
                //TODO Calculate Category wise this months total
                break;
            default:
                Log.e("AnalysisActivity", "getQueryData: Invalid query request");

        }*/

        totalExpense.setText(total);
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        analyzeCategoryWiseTotalExpenditure(0);

    }

    private void analyzeCategoryWiseTotalExpenditure(int period){

        Cursor rows = getQueryData(period);

        if(rows == null){
            Toast.makeText(AnalysisActivity.this, "No Data Available", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> CategoryList = new ArrayList<>();
        int i = 0;
        while(rows.moveToNext()){
            String category = rows.getString(0);
            Float sum = (float) rows.getDouble(1);
            barEntries.add(new BarEntry(sum, i));
            CategoryList.add(category);
            i++;
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Expenses");
        //barDataSet.setColors(new int[] {16776960,16737380,6579400,16744960,6618980,16760320}, this);

        BarData barData = new BarData(CategoryList,barDataSet);
        barChart.setData(barData);
    }

    private Cursor getQueryData(int queryNumber){
        Cursor data = null;
        switch (queryNumber) {
            case 0:
                data = mDatabaseHelper.getCategoryWiseTotalExpenditure();
                break;
            case 1:
                data = mDatabaseHelper.getCategoryWiseThisYearExpenditure();
                break;
            case 2:
                data = mDatabaseHelper.getYearWiseTotalExpenditure();
                break;
            case 3:
                //TODO get Category wise expenditure for this month
                break;
            case 4:
                //TODO get Month wise total expenditure for this year
                break;
            default:
                Log.e("AnalysisActivity", "getQueryData: Invalid query request");

        }
        return data;
    }

}
