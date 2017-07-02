package com.example.owner.mysocialmediaapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.owner.mysocialmediaapplication.helpers.BitmapHelper;
import com.example.owner.mysocialmediaapplication.helpers.HistogramHelper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Mark on 31/08/2016.
 */

public class SharePictureActivity extends AppCompatActivity {

    private Button cameraButton;
    private Button getPictureDBButton;
    private Button sharePicture;
    private ImageView iView;
    private ImageView histogramView;
    // private TextView exifView;
    private DatabaseHelper databaseHelper;
    private int PHOTO_ID = 101;
    private static final String TAG = "ImageHistogram";
    private Mat rgba;

    // This ensures that the OpenCV library is loaded & initialised - Needed for Histogram functionality
    // Using async initialization, which is recommended way for "production" - uses the OpenCV Manager
    // Can also look at Static Initialization - but deprecated for production as per documentation

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG,"OpenCV Manager Connected");
                    rgba = new Mat();
                    break;
                case LoaderCallbackInterface.INIT_FAILED:
                    Log.i(TAG,"Init Failed");
                    break;
                case LoaderCallbackInterface.INSTALL_CANCELED:
                    Log.i(TAG,"Install Cancelled");
                    // Let the user know what the issue with this is ....
                    makeToast("Please install OpenCV Manager so I can show you the histogram");
                    break;
                case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION:
                    Log.i(TAG,"Incompatible Version");
                    break;
                case LoaderCallbackInterface.MARKET_ERROR:
                    Log.i(TAG,"Market Error");
                    break;
                default:
                    Log.i(TAG,"OpenCV Manager Install");
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_picture);

        databaseHelper = new DatabaseHelper(this);
        iView = (ImageView) findViewById(R.id.picture_share);
        cameraButton = (Button)findViewById(R.id.picture_camera_button);
        sharePicture = (Button)findViewById(R.id.picture_share_button);
    //    exifView = (TextView)findViewById(R.id.exifview);
        histogramView = (ImageView) findViewById(R.id.histogram_image_view);
        getPictureDBButton = (Button) findViewById(R.id.get_pictureDB_button);

        setupEvents();
    }


    private void setupEvents() {

        // Get picture from the camera
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, PHOTO_ID);
            }
        });

        // Get picture from the database
        getPictureDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pictureDB ="";
                EditText getPictureDB = (EditText) findViewById(R.id.get_picture_entry);
                if (getPictureDB != null) {
                    pictureDB = getPictureDB.getText().toString();
                    if( pictureDB.length() == 0 ) {
                        getPictureDB.setError("Image name is required!");
                        return;
                    }
                }
                ImageHelper dbImage = databaseHelper.getImage(pictureDB);
                byte[] byteArray = dbImage.getImageByteArray();
                if (byteArray != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    iView.setImageBitmap(bitmap);
                    /**
                     // If using exif display
                     try {
                     ExifInterface exif = new ExifInterface(pictureDB);
                     ShowExif(exif);
                     } catch (IOException e) {
                     makeToast("Error getting exif data!");
                     }
                     */
                    // Using Histogram display
                    try {
                        drawHistogram(bitmap);
                    } catch (Exception e) {
                        makeToast("Unable to draw histogram");
                    }
                } else {
                    makeToast("Unable to retrieve image. Please check image name");
                }
            }
         });

        // Implement the "share picture" functionality
        sharePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable mDrawable = iView.getDrawable();
                if (mDrawable == null) {
                    makeToast("Get a valid picture first!");
                } else {
                    Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "Image to share", null);
                    Uri uri = Uri.parse(path);
                    // Manage share picture
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Share picture with..."));
                }
            }
        });
    }
    // Handle the picture obtained from the camera.
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_ID && resultCode == RESULT_OK) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try {
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                    System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            iView.setImageBitmap(thumbnail);
            makeToast("Image capture succeeded!");
        } else {
            makeToast("Image capture failed!");
        }
    }
    /** Looked at displaying exif data, fund that downloading image from internet stripped exif info
     * Need to use a library such as metadata-extractor: Java library for reading metadata from image files
     * Read from input stream - ImageMetadataReader.readMetadata(bis,true)

    private void ShowExif(ExifInterface exif)
    {
        String myAttribute="Exif information ---\n";
        myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
        myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
        myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        exifView.setText(myAttribute);
    }

    private String getTagString(String tag, ExifInterface exif)
    {
        return(tag + " : " + exif.getAttribute(tag) + "\n");
    }
    */
        private void makeToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

    // Implement the "calculation" functionality - display histogram of image retrieved from DB
    private void drawHistogram(Bitmap bitmap) {
        try {
            Utils.bitmapToMat(bitmap, rgba);
            Size rgbaSize = rgba.size();
            int histSize = 256;
            MatOfInt histogramSize = new MatOfInt(histSize);
            int histogramHeight = (int) rgbaSize.height;
            int binWidth = 5;
            MatOfFloat histogramRange = new MatOfFloat(0f, 256f);
            Scalar[] colorsRgb = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
            MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
            Mat[] histograms = new Mat[]{new Mat(), new Mat(), new Mat()};
            Mat histMatBitmap = new Mat(rgbaSize, rgba.type());
            for (int i = 0; i < channels.length; i++) {
                Imgproc.calcHist(Collections.singletonList(rgba), channels[i], new Mat(), histograms[i], histogramSize, histogramRange);
                Core.normalize(histograms[i], histograms[i], histogramHeight, 0, Core.NORM_INF);
                for (int j = 0; j < histSize; j++) {
                    Point p1 = new Point(binWidth * (j - 1), histogramHeight - Math.round(histograms[i].get(j - 1, 0)[0]));
                    Point p2 = new Point(binWidth * j, histogramHeight - Math.round(histograms[i].get(j, 0)[0]));
                    Imgproc.line(histMatBitmap, p1, p2, colorsRgb[i], 2, 8, 0);
                }
            }
            for (int i = 0; i < histograms.length; i++) {
                calculationsOnHistogram(histograms[i]);
            }
            // Display the histogram in the image view
            Bitmap histBitmap = Bitmap.createBitmap(histMatBitmap.cols(), histMatBitmap.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(histMatBitmap, histBitmap);
            BitmapHelper.showBitmap(this, histBitmap, histogramView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculationsOnHistogram(Mat histogram) {
        SparseArray<ArrayList<Float>> compartments = HistogramHelper.createCompartments(histogram);
        float sumAll = HistogramHelper.sumCompartmentsValues(compartments);
        float averageAll = HistogramHelper.averageValueOfCompartments(compartments);
        Log.i(TAG, "Sum: " + Core.sumElems(histogram));
        Log.i(TAG, "Sum of all compartments " + String.valueOf(sumAll));
        Log.i(TAG, "Average value of all compartments " + String.valueOf(averageAll));
        Log.i(TAG, " ");
        for (int i = 0; i < compartments.size(); i++) {
            float sumLast = HistogramHelper.sumCompartmentValues(i, compartments);
            float averageLast = HistogramHelper.averageValueOfCompartment(i, compartments);
            float averagePercentageLastCompartment = HistogramHelper.averagePercentageOfCompartment(i, compartments);
            float percentageLastCompartment = HistogramHelper.percentageOfCompartment(i, compartments);
            Log.i(TAG, "Sum of " + (i + 1) + " compartment " + String.valueOf(sumLast));
            Log.i(TAG, "Average value of the " + (i + 1) + " compartment " + String.valueOf(averageLast));
            Log.i(TAG, "Average percentage of the " + (i + 1) + " compartment " + String.valueOf(averagePercentageLastCompartment));
            Log.i(TAG, "Percentage of the " + (i + 1) + " compartment " + String.valueOf(percentageLastCompartment));
            Log.i(TAG, " ");
        }
        Log.i(TAG, " ");
    }


    // Needed to ensure the OpenCV is installed
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
