package ak.kawlay.mail.utoronto.ca.textreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

public class FullScreenActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        imageView = findViewById(R.id.imageViewFullScreen);
        String photopath = getIntent().getExtras().getString("ca.utoronto.mail.kawlay.ak.textreader.PHOTOPATH");
        if(photopath==null || photopath=="NULL"){
            Toast.makeText(FullScreenActivity.this, "Image Path is corrupted", Toast.LENGTH_LONG).show();
        }
        else{
            Picasso.get()
                    .load(new File(photopath))
                    .centerCrop()
                    .fit()
                    .into(imageView);
        }

    }
}
