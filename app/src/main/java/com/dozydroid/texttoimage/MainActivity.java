package com.dozydroid.texttoimage;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    EditText etMain;
    ImageView imgMain;
    Button btnSetText, btnSaveImage;

    boolean textIsSet = false;

    private static final int STORAGE_PERMISSION_CODE = 7861;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMain = findViewById(R.id.etMain);
        imgMain = findViewById(R.id.imgMain);
        btnSetText = findViewById(R.id.btnSetText);
        btnSaveImage = findViewById(R.id.btnSaveImage);

        btnSetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etMain.getText().toString();
                if(!text.isEmpty()){
                    /*
                    * First Method
                    */
//                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                    paint.setTextSize(24);
//                    paint.setColor(Color.BLACK);
//                    paint.setTextAlign(Paint.Align.LEFT);
//
//                    float baseline = -paint.ascent();
//                    int width = (int) (paint.measureText(text) + 0.5f);
//                    int height = (int) (baseline + paint.descent() + 0.5f);
//
//                    Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                    Canvas canvas = new Canvas(image);
//                    canvas.drawText(text, 0, baseline, paint);
//
//                    imgMain.setImageBitmap(image);

                    /*
                    * Second Method
                    */
                    int color = Color.WHITE;
                    int width = etMain.getMeasuredWidth();
                    Bitmap img = drawText(text, width, color);
                    imgMain.setImageBitmap(img);

                    textIsSet = true;

                }else{
                    Toast.makeText(MainActivity.this, "Enter some text in the textbox.", Toast.LENGTH_SHORT).show();
                }

            }
        });//end btnSetText.onClickListener

        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textIsSet){
                    saveImage();
                }else{
                    Toast.makeText(MainActivity.this, "Set some image in text first!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    } // End onCreate

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private Bitmap drawText(String text, int textWidth, int color) {

        // Get text dimensions
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.parseColor("#ff00ff"));
        textPaint.setTextSize(72);

        StaticLayout mTextLayout = new StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // Create bitmap and canvas to draw to
        int mHeight  = getScreenHeight();
//        Bitmap b = Bitmap.createBitmap(textWidth, mTextLayout.getHeight(), Bitmap.Config.ARGB_4444);
        Bitmap b = Bitmap.createBitmap(textWidth, mHeight, Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);

        // Draw background
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        c.drawPaint(paint);

        // Draw text
        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();

        return b;
    } //End of drawText

    @AfterPermissionGranted(STORAGE_PERMISSION_CODE)
    private void saveImage(){
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this, perms)){
            BitmapDrawable draw = (BitmapDrawable) imgMain.getDrawable();
            Bitmap bitmap = draw.getBitmap();

            FileOutputStream outStream = null;
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/TextToImage");
            dir.mkdirs();
            String fileName = String.format("%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);
            try {
                outStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving Image! FileNotFoundException", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving Image! IOException", Toast.LENGTH_SHORT).show();
            }
        }else{
            EasyPermissions.requestPermissions(this,"We need permissions to save image",
                    STORAGE_PERMISSION_CODE, perms);
        }


    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        saveImage();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            saveImage();
        }
    }
}
