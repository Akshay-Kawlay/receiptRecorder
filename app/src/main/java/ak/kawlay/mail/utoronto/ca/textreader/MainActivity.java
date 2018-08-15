package ak.kawlay.mail.utoronto.ca.textreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
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

    private  ImageView imageView;
    private  Bitmap imageBitmap;
    private StringBuilder detectedList;
    private Intent startIntent;
    private String mCurrentPhotoPath;

    //to do: create database to store catagory -> date,time -> picture path with name,amount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button snapBtn = (Button) findViewById(R.id.snapBtn);
        Button detectBtn = (Button) findViewById(R.id.detectBtn);
        imageView = (ImageView) findViewById(R.id.imageView);
        startIntent = new Intent(getApplicationContext(), SecondActivity.class);

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                dispatchTakePictureIntent();
            }
        });

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText();
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
            imageView.setImageBitmap(rotatedBitmap);
            imageBitmap = rotatedBitmap;

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
                            Double amount = Double.parseDouble(str);
                            if (amount > maxAmountPaid) {
                                maxAmountPaid = amount;
                                maxAmountRect.top = elementFrame.top;
                                maxAmountRect.bottom = elementFrame.bottom;
                                maxAmountRect.left = elementFrame.left;
                                maxAmountRect.right = elementFrame.right;
                            }

                            Log.i("AMOUNT="+elementText, str);
                            break;
                        }
                    }
                }
            }
        }

        detectedList = new StringBuilder();
        for(String s : name){
            detectedList.append(s);
            detectedList.append(" : ");
        }
        detectedList.append("$");
        detectedList.append(maxAmountPaid);

        startIntent.putExtra("ca.utoronto.mail.kawlay.ak.textreader.SOMETHING", detectedList.toString());
        startActivity(startIntent);


    }

}
