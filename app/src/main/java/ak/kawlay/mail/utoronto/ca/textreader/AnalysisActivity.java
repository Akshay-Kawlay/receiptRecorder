package ak.kawlay.mail.utoronto.ca.textreader;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

    ArrayList<String> mCategoryList;
    private static final String[] items = {"Grand Total Expenditure", "This Year's Expenditure",
            "Total Expenditure Per Year", "This Month's Expenditure", "Monthly Expenditure This Year"};

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
        mCategoryList.add(0, "Analyze Per Category");
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(AnalysisActivity.this, android.R.layout.simple_spinner_item, mCategoryList){
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategory);
        spinnerCategory.setOnItemSelectedListener(this);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AnalysisActivity.this,
                        android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Double total = 0.0;
        Spinner spin = (Spinner)parent;
        //Spinner spinCategory = (Spinner)parent;

        if(spin.getId()==R.id.spinner) {
            analyzeExpenditure(position);
            switch (position) {
                case 0:
                    total = mDatabaseHelper.getTotalExpenditure();
                    break;
                case 1:
                    total = mDatabaseHelper.getTotalExpenditureThisYear();
                    break;
                case 2:
                    total = mDatabaseHelper.getTotalExpenditure();
                    break;
                case 3:
                    total = mDatabaseHelper.getTotalExpenditureThisMonth();
                    break;
                case 4:
                    total = mDatabaseHelper.getTotalExpenditureThisYear();
                    break;
                default:
                    Log.e("AnalysisActivity", "getQueryData: Invalid query request");

            }
        }
        if(spin.getId()==R.id.spinnerCategory){

            if(position==0){
                return;
            }
            String category = mCategoryList.get(position);

            Toast.makeText(AnalysisActivity.this, "CATEGORY SELECTED: "+category, Toast.LENGTH_LONG).show();

            //TODO Create per month expense for selected category for this year
            //TODO Create per year expense for selected category for all years
        }

        totalExpense.setText(String.valueOf(total));
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        analyzeExpenditure(0);

    }

    private void analyzeExpenditure(int period){

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
        barChart.invalidate();
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
                //get Category wise expenditure for this month
                data = mDatabaseHelper.getCategoryWiseThisMonthExpenditure();
                break;
            case 4:
                //get Month wise total expenditure for this year
                data = mDatabaseHelper.getMonthWiseThisYearExpenditure();
                break;
            default:
                Log.e("AnalysisActivity", "getQueryData: Invalid query request");

        }
        return data;
    }

}
