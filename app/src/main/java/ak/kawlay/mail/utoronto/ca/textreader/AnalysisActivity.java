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
    Spinner spinnerCategory;
    TextView totalExpense;
    private int yearPosition;
    private int categoryPosition;
    private static final int ALL = 0;
    private static final int GRAND_TOTAL = 1;
    private static final int MONTHLY_PER_YEAR = 1;
    private static final int THIS_MONTH = 2;

    ArrayList<String> mCategoryList;
    ArrayList<String> mYearList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        barChart = findViewById(R.id.bargraph);
        mDatabaseHelper = new DatabaseHelper(this);
        totalExpense = findViewById(R.id.textViewNumber);
        spinner = findViewById(R.id.spinner);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        totalExpense.setTextSize(25);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setDrawBorders(true);
        barChart.invalidate();

        mCategoryList = mDatabaseHelper.getAllCategories();
        mCategoryList.add(ALL, "All Categories");
        mCategoryList.add(MONTHLY_PER_YEAR,"Monthly per year");
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(AnalysisActivity.this, android.R.layout.simple_spinner_item, mCategoryList);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategory);
        spinnerCategory.setOnItemSelectedListener(this);


        mYearList = mDatabaseHelper.getAllYears();
        mYearList.add(ALL, "All Years");
        mYearList.add(GRAND_TOTAL, "Grand Total");
        mYearList.add(THIS_MONTH, "This Month");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AnalysisActivity.this, android.R.layout.simple_spinner_item, mYearList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Double total = -1.0;
        Spinner spin = (Spinner)parent;

        if(spin.getId()==R.id.spinner) {
            yearPosition = position;
        }
        if(spin.getId()==R.id.spinnerCategory){
            categoryPosition = position;
        }

        if(yearPosition == ALL && categoryPosition == ALL){         //Grand Total for each year
            total = mDatabaseHelper.getTotalExpenditure();
            drawBarChart(0, null, null);
        }
        else if(yearPosition == GRAND_TOTAL && categoryPosition == ALL){    //Grand Total for each category
            total = mDatabaseHelper.getTotalExpenditure();
            drawBarChart(1,null, null);
        }
        else if(yearPosition == THIS_MONTH && categoryPosition == ALL){    //This month total for each category
            total = mDatabaseHelper.getTotalExpenditureThisMonth();
            drawBarChart(2,null, null);
        }
        else if(yearPosition > THIS_MONTH && categoryPosition == ALL){   //Selected year total for each category
            String year = mYearList.get(yearPosition);
            total = mDatabaseHelper.getTotalExpenditureForYear(year);
            drawBarChart(3, year, null);
        }
        else if(yearPosition == ALL && categoryPosition > MONTHLY_PER_YEAR){   //Each year total for selected category
            String category = mCategoryList.get(categoryPosition);
            total = mDatabaseHelper.getTotalExpenditureForCategory(category);
            drawBarChart(4, null, category);
        }
        else if(yearPosition > THIS_MONTH && categoryPosition == MONTHLY_PER_YEAR){   //Monthly total expenses for selected year
            String year = mYearList.get(yearPosition);
            total = mDatabaseHelper.getTotalExpenditureForYear(year);
            drawBarChart(5, year, null);
        }
        else if(yearPosition > THIS_MONTH && categoryPosition > MONTHLY_PER_YEAR){  //Selected year total for selected category
            String category = mCategoryList.get(categoryPosition);
            String year = mYearList.get(yearPosition);
            total = mDatabaseHelper.getTotalExpenditureForYearAndCategory(year, category);
            drawBarChart(6, year, category);
        }
        else if((yearPosition == GRAND_TOTAL && categoryPosition != ALL) || (yearPosition == ALL && categoryPosition == MONTHLY_PER_YEAR)
                || (yearPosition == THIS_MONTH && categoryPosition > ALL)){    //Invalid cases
            Toast.makeText(AnalysisActivity.this, "Selected query does not make sense", Toast.LENGTH_LONG).show();
            totalExpense.setText("N/A");
            barChart.clear();
            return;
        }

        totalExpense.setText(String.valueOf(total));
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        drawBarChart(0, null, null);

    }

    private void drawBarChart(int query, String year, String category){
        Cursor rows = getQueryData(query, year, category);

        if(rows == null){
            Toast.makeText(AnalysisActivity.this, "No Data Available", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> CategoryList = new ArrayList<>();
        int i = 0;
        while(rows.moveToNext()){
            String column0 = rows.getString(0);
            Float sum = (float) rows.getDouble(1);
            barEntries.add(new BarEntry(sum, i));
            CategoryList.add(column0);
            i++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Expenses");

        BarData barData = new BarData(CategoryList,barDataSet);
        barChart.invalidate();
        barChart.setData(barData);
    }

    private Cursor getQueryData(int queryNumber, String year, String category){
        Cursor data = null;
        switch (queryNumber) {
            case 0:
                data = mDatabaseHelper.getYearWiseTotalExpenditure();
                break;
            case 1:
                data = mDatabaseHelper.getCategoryWiseTotalExpenditure();
                break;
            case 2:
                data = mDatabaseHelper.getCategoryWiseThisMonthExpenditure();
                break;
            case 3:
                data = mDatabaseHelper.getCategoryWiseExpenditureForYear(year);
                break;
            case 4:
                data = mDatabaseHelper.getYearWiseCategoryExpenditure(category);
                break;
            case 5:
                data = mDatabaseHelper.getMonthWiseExpenditureForYear(year);
                break;
            case 6:
                data = mDatabaseHelper.getMonthWiseExpenditureForYearAndCategory(year,category);
                break;
            default:
                Log.e("AnalysisActivity", "getQueryData: Invalid query request");
        }

        return data;
    }
}
