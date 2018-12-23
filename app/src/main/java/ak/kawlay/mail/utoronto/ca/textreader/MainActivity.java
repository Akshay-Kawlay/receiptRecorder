package ak.kawlay.mail.utoronto.ca.textreader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import static java.lang.StrictMath.abs;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText editTextCategory;
    private Bitmap imageBitmap;
    private Intent startIntentHistory;
    private String mCurrentPhotoPath;
    private String mBudget = "100";
    DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button snapBtn = findViewById(R.id.snapBtn);
        Button historyBtn = findViewById(R.id.buttonHistory);
        Button analyzeBtn = findViewById(R.id.analyzeBtn);
        Button budgetBtn = findViewById(R.id.setBudgetBtn);

        imageView = findViewById(R.id.imageViewFullScreen);
        editTextCategory = findViewById(R.id.editTextCategory);
        mDatabaseHelper = new DatabaseHelper(this);     //create database to store records

        /*TEST BED*/
        //mDatabaseHelper.load_testbed();

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

        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        budgetBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                builder.setTitle("Monthly Budget");

                // Set up the input
                final EditText input = new EditText(getApplicationContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBudget = input.getText().toString();
                        SharedPreferences pref = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("budget", mBudget);
                        edit.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

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
                Log.e("MainActivity: dispatchTakePictureIntent::", "Could not create image file" + ex.toString());
                Toast.makeText(MainActivity.this, "Could not create image file", Toast.LENGTH_LONG).show();
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
            imageBitmap = rotatedBitmap;

            detectText();
        }
    }

    private void checkBudget(){
        Double expense = mDatabaseHelper.getTotalExpenditureThisMonth();
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        String budget = pref.getString("budget", "100");
        Double limit = Double.parseDouble(budget);
        //Double limit = Double.parseDouble(mBudget);
        Double warning = 0.8;
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        if (expense > limit*warning && expense < limit){
            //Toast.makeText(MainActivity.this, "Warning: Monthly Budget limit approaching", Toast.LENGTH_LONG).show();
            builder.setTitle("Budget approaching")
                    .setMessage("Warning: Approaching Monthly Budget limit for this month\n\nCurrent: $"+ expense.toString() + "\t\t\tLimit: $"+limit.toString())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .show();
        }
        else if (expense >= limit){
            //Toast.makeText(MainActivity.this, "Warning: Going over budget", Toast.LENGTH_LONG).show();
            builder.setTitle("Over Budget")
                    .setMessage("Warning: Going over budget for this month\n\nCurrent: $"+ expense.toString() + "\t\t\tLimit: $"+limit.toString())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .show();
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

    private void processText(FirebaseVisionText text) {
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No Text detected", Toast.LENGTH_LONG).show();
            manualFill();
            return;
        }

        String maxAmount = "-1.0";
        Rect maxAmountRect = new Rect(0, 0, 0, 0);

        List<String> name = new ArrayList<String>();
        name.add("Unknown");
        int topMostLine = Integer.MAX_VALUE;
        int minDiff = Integer.MAX_VALUE;
        int TOTAL_position = -1;
        boolean flag = true;
        Rect nameBoundingBox = new Rect(0, 0, 0, 0);

        for (FirebaseVisionText.Block block : text.getBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                String lineText = line.getText();
                Rect lineFrame = line.getBoundingBox();
                if (lineText.toUpperCase().startsWith("TOTAL")) {
                    Toast.makeText(MainActivity.this, "Detected: " + lineText, Toast.LENGTH_LONG).show();
                    TOTAL_position = lineFrame.top;
                    String tempStr[] = lineText.split(" ");
                    if (tempStr.length > 1) {
                        maxAmount = tempStr[1];
                        maxAmountRect.top = lineFrame.top;
                        maxAmountRect.bottom = lineFrame.bottom;
                        maxAmountRect.left = lineFrame.left;
                        maxAmountRect.right = lineFrame.right;
                        flag = false;
                    }
                }
                if (lineFrame.top < topMostLine) {
                    if (name.size() > 0) {
                        name.clear();
                    }
                    name.add(lineText);
                    topMostLine = lineFrame.top;
                    nameBoundingBox.top = lineFrame.top;
                    nameBoundingBox.bottom = lineFrame.bottom;
                    nameBoundingBox.left = lineFrame.left;
                    nameBoundingBox.right = lineFrame.right;
                }
            }
        }
        if (flag){
            for (FirebaseVisionText.Block block : text.getBlocks()) {
                for (FirebaseVisionText.Line line : block.getLines()) {
                    String lineText = line.getText();
                    Rect lineFrame = line.getBoundingBox();

                    int diff = abs(lineFrame.top - TOTAL_position);
                    if (diff < minDiff && !(lineText.toUpperCase().startsWith("TOTAL"))) {
                        minDiff = diff;
                        maxAmount = lineText;

                        maxAmountRect.top = lineFrame.top;
                        maxAmountRect.bottom = lineFrame.bottom;
                        maxAmountRect.left = lineFrame.left;
                        maxAmountRect.right = lineFrame.right;
                    }

                }
            }
        }

        drawBoundingBox(nameBoundingBox, maxAmountRect);
        try {
            Double purchaseAmount = Double.parseDouble(maxAmount.replace("$", ""));
            String Category = editTextCategory.getText().toString();
            fillRecord(name.get(0), purchaseAmount, Category);
        }catch (Exception e){
            Log.e("MainActivity: processText::", "maxAmount could not be parsed" + e.toString());
            Toast.makeText(MainActivity.this, "Could not read total amount", Toast.LENGTH_LONG).show();
            manualFill();
        }


    }

    private void manualFill(){

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Add Data Manually");
        builder.setMessage("ReceiptRecorder couldn't detect Name and Amount from the photo. Please add manually or take photo again");

        Context context = getApplicationContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the "Title" label, as noted in the comments
        final EditText nameBox = new EditText(context);
        nameBox.setHint("Name");
        layout.addView(nameBox); // Notice this is an add method

        // Add another TextView here for the "Description" label
        final EditText amountBox = new EditText(context);
        amountBox.setHint("Amount");
        layout.addView(amountBox); // Another add method

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input_name = "";
                Double input_amount = -1.0;
                String Category = editTextCategory.getText().toString();
                input_name = nameBox.getText().toString();
                input_amount = Double.parseDouble(amountBox.getText().toString());
                fillRecord(input_name, input_amount, Category);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void fillRecord(String name, Double amount, String category){
        String timeStamp = new SimpleDateFormat("EEE, d MMM yyyy HH:mm aaa").format(new Date());
        receiptRecord newRecord = new receiptRecord(amount, name, category, timeStamp, mCurrentPhotoPath);

        boolean check = mDatabaseHelper.addData(newRecord);
        if(check){
            Toast.makeText(MainActivity.this, "New record successfully added to database", Toast.LENGTH_LONG).show();
            checkBudget();
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
    }


}
