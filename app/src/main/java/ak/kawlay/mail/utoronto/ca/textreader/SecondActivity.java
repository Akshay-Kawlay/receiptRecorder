package ak.kawlay.mail.utoronto.ca.textreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        if (getIntent().hasExtra("ca.utoronto.mail.kawlay.ak.textreader.SOMETHING")){
            tv = (TextView) findViewById(R.id.textView);
            String text = getIntent().getExtras().getString("ca.utoronto.mail.kawlay.ak.textreader.SOMETHING");
            tv.setText("");
            tv.setText(text);

        }
    }

}
