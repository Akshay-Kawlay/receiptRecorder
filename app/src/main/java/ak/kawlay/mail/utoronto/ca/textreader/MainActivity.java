package ak.kawlay.mail.utoronto.ca.textreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText editTextCategory;
    private Bitmap imageBitmap;
    private StringBuilder detectedList;
    private Intent startIntentHistory;
    private String mCurrentPhotoPath;
    DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button snapBtn = findViewById(R.id.snapBtn);
        Button historyBtn = findViewById(R.id.buttonHistory);
        Button analyzeBtn = findViewById(R.id.analyzeBtn);

        imageView = findViewById(R.id.imageViewReceipt);
        editTextCategory = findViewById(R.id.editTextCategory);
        mDatabaseHelper = new DatabaseHelper(this);     //create database to store records

        /*TEST BED*/
        mDatabaseHelper.load_testbed();

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (editTextCategory.getText().toString().matches("")) {
                    Toast.makeText(MainActivity.this, "Please enter category of receipt", Toast.LENGTH_LONG).show();
                } else {
                    dispatchTakePictureIntent();
                }


            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startIntentHistory = new Intent(getApplicationContext(), ScrollingActivity.class);
                startActivity(startIntentHistory);
            }
        });

        analyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startAnalyzingIntent = new Intent(getApplicationContext(), AnalysisActivity.class);
                startActivity(startAnalyzingIntent);
            }
        });
    }


    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException ex){
                //error occured
            }
            if (photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,
                                                        "com.example.android.fileprovider",
                                                         photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap tempBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true);
            //imageView.setImageBitmap(rotatedBitmap);
            imageBitmap = rotatedBitmap;

            detectText();

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void detectText(){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processText(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void processText(FirebaseVisionText text){
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if (blocks.size() == 0){
            Toast.makeText(MainActivity.this, "No Text detected", Toast.LENGTH_LONG).show();
            return;
        }
        Double maxAmountPaid = 0.0;
        Rect maxAmountRect = new Rect(0,0,0,0);

        List<String> name = new ArrayList<String>();
        name.add("Unknown");
        int topMostLine = Integer.MAX_VALUE;
        int lowestElement = Integer.MIN_VALUE;
        Rect nameBoundingBox = new Rect(0,0,0,0);

        for (FirebaseVisionText.Block block : text.getBlocks()){
            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                Rect lineFrame = line.getBoundingBox();
                if (lineFrame.top < topMostLine){
                    if(name.size() > 0){
                        name.clear();
                    }
                    name.add(lineText);
                    topMostLine = lineFrame.top;
                    nameBoundingBox.top = lineFrame.top;
                    nameBoundingBox.bottom = lineFrame.bottom;
                    nameBoundingBox.left = lineFrame.left;
                    nameBoundingBox.right = lineFrame.right;

                }

                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    for (int i = 0; i < elementText.length(); i++){
                        if (elementText.charAt(i)=='$'){
                            String str = elementText.replace("$", "");
                            Rect elementFrame = element.getBoundingBox();
                            
                            if (elementFrame.bottom > lowestElement) {
                                Double amount = Double.parseDouble(str);
                                maxAmountPaid = amount;
                                lowestElement = elementFrame.bottom;
                                maxAmountRect.top = lineFrame.top;
                                maxAmountRect.bottom = lineFrame.bottom;
                                maxAmountRect.left = lineFrame.left;
                                maxAmountRect.right = lineFrame.right;
                            }

                            Log.i("AMOUNT="+elementText, str);
                            break;
                        }
                    }
                }
            }
        }

        drawBoundingBox(nameBoundingBox, maxAmountRect);

        String Category = editTextCategory.getText().toString();
        fillRecord(name.get(0), maxAmountPaid, Category);

    }

    private void fillRecord(String name, Double amount, String category){
        String timeStamp = new SimpleDateFormat("EEE, d MMM yyyy HH:mm aaa").format(new Date());
        receiptRecord newRecord = new receiptRecord(amount, name, category, timeStamp, mCurrentPhotoPath);

        boolean check = mDatabaseHelper.addData(newRecord);
        if(check){
            Toast.makeText(MainActivity.this, "New record successfully added to database", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(MainActivity.this, "New record could not be added to database", Toast.LENGTH_LONG).show();
        }
    }

    private void drawBoundingBox(Rect box1, Rect box2){
        Bitmap tempBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setColor(Color.RED);
        myPaint.setStrokeWidth(15);
        myPaint.setStyle(Paint.Style.STROKE);
        //Draw the image bitmap into the canvas
        tempCanvas.drawBitmap(imageBitmap, 0, 0, null);

        //Draw everything else you want into the canvas, in this example a rectangle with rounded edges
        tempCanvas.drawRect(box1,myPaint);
        tempCanvas.drawRect(box2,myPaint);

        //Attach the canvas to the ImageView
        imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
        //imageView.setImageBitmap(imageBitmap);
    }


}
