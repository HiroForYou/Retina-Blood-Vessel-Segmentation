package com.uni.cristhianwiki;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ModelActivity extends AppCompatActivity implements Runnable {

    public static final String INTENT_MODULE_ASSET_NAME = "INTENT_MODULE_ASSET_NAME";
    public static final String INTENT_INFO_VIEW_TYPE = "INTENT_INFO_VIEW_TYPE";
    public static final String TITLE = "TITLE";

    private static final int INPUT_TENSOR_WIDTH = 512;
    private static final int INPUT_TENSOR_HEIGHT = 512;
    private static final int CLASSNUM = 1;
    private static double THRESHOLD;

    private Toolbar toolbar;
    private ImageView mImageView;
    private Button mButtonSegment, buttonRestart, buttonShare, buttonSelect;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap = null, bitmap2Share;
    private Module mModule = null;
    private String mImageName = null;
    private String mModuleAssetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1
            );
        }

        setContentView(R.layout.activity_model);

        // Attaching the layout to the toolbar object
        toolbar = findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra(TITLE));

        mImageName = getInfoViewType() == 1 ? "retina.png": "brain.png";
        THRESHOLD = getInfoViewType() == 1 ? 0.006 : 0.5;

        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mImageName));
            mBitmap = Bitmap.createScaledBitmap(
                    mBitmap, INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT, true
            );
            bitmap2Share = mBitmap;
            mImageView = findViewById(R.id.imageView);
            mImageView.setImageBitmap(mBitmap);
        } catch (IOException e) {
            Log.e("ImageSegmentation", "Error leyendo assets", e);
            finish();
        }

        buttonRestart = findViewById(R.id.restartButton);
        buttonRestart.setOnClickListener(v -> {
                mImageName = getInfoViewType() == 1 ? "retina.png": "brain.png";
                try {
                    mBitmap = BitmapFactory.decodeStream(getAssets().open(mImageName));
                    mBitmap = Bitmap.createScaledBitmap(
                            mBitmap, INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT, true
                    );

                    //Log.d("ImageSegmentation",  "Dimensiones init " + mBitmap.getWidth() + " " + mBitmap.getHeight());
                    mImageView.setImageBitmap(mBitmap);
                    bitmap2Share = mBitmap;
                } catch (IOException e) {
                    Log.e("ImageSegmentation", "Error leyendo assets", e);
                    finish();
                }
        });

        buttonShare = findViewById(R.id.shareButton);
        buttonShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap2Share.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(
                    getContentResolver(), bitmap2Share, "Segmentación resultado", null
            );
            Uri imageUri =  Uri.parse(path);
            share.putExtra(Intent.EXTRA_STREAM, imageUri);
            startActivity(Intent.createChooser(share, "Seleccione una app"));
        });


        mButtonSegment = findViewById(R.id.segmentButton);
        mProgressBar = findViewById(R.id.progressBar);
        mButtonSegment.setOnClickListener(v -> {
                mButtonSegment.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mButtonSegment.setText(getString(R.string.run_model));

                Thread thread = new Thread(ModelActivity.this);
                thread.start();
        });

        buttonSelect = findViewById(R.id.selectButton);
        buttonSelect.setOnClickListener(v -> {
                final CharSequence[] options = { "Elegir de galería", "Cancelar" };
                androidx.appcompat.app.AlertDialog.Builder builder = 
                        new androidx.appcompat.app.AlertDialog.Builder(ModelActivity.this);
                builder.setTitle("Nueva imagen de prueba");

                builder.setItems(options, (dialog, item) -> {
                         if (options[item].equals("Elegir de galería")) {
                            Intent pickPhoto = new Intent(
                                    Intent.ACTION_PICK, 
                                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
                            );
                            startActivityForResult(pickPhoto , 1);
                        }
                        else if (options[item].equals("Cancelar")) {
                            dialog.dismiss();
                        }
                });
                builder.show();
            }
        );

        final String assetFilePath = Utils.assetFilePath(this, getModuleAssetName());
        if(assetFilePath == null || assetFilePath.isEmpty() || assetFilePath.trim().isEmpty()){
            showErrorDialog(v -> ModelActivity.this.finish());
        }else{
            final String moduleFileAbsoluteFilePath = new File(assetFilePath).getAbsolutePath();
            mModule = LiteModuleLoader.load(moduleFileAbsoluteFilePath);
        }
    }

    protected String getModuleAssetName() {
        if (!TextUtils.isEmpty(mModuleAssetName)) {
            return mModuleAssetName;
        }
        final String moduleAssetNameFromIntent = getIntent().getStringExtra(INTENT_MODULE_ASSET_NAME);
        mModuleAssetName = !TextUtils.isEmpty(moduleAssetNameFromIntent)
                ? moduleAssetNameFromIntent
                : "unet_brain_scripted_optimized.ptl";

        return mModuleAssetName;
    }

    protected String getInfoViewAdditionalText() {
        return "Archivo de red neuronal: " + getModuleAssetName();
    }

    protected int getInfoViewType() {
        return getIntent().getIntExtra(INTENT_INFO_VIEW_TYPE, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImage != null) {
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        mBitmap = BitmapFactory.decodeFile(picturePath);
                        Matrix matrix = new Matrix();
                        mBitmap = Bitmap.createBitmap(
                                mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(),
                                matrix, true);
                        mBitmap = Bitmap.createScaledBitmap(
                                mBitmap, INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT, true
                        );
                        bitmap2Share = mBitmap;
                        mImageView.setImageBitmap(mBitmap);
                        cursor.close();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_model, menu);
        menu.findItem(R.id.action_info).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            onMenuItemInfoSelected();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuItemInfoSelected() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(InfoViewFactory.newInfoView(
                        this, getInfoViewType(), getInfoViewAdditionalText())
                );

        builder.show();
    }

    @UiThread
    protected void showErrorDialog(View.OnClickListener clickListener) {
        final View view = InfoViewFactory.newErrorDialogView(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog)
                .setCancelable(false)
                .setView(view);
        final AlertDialog alertDialog = builder.show();
        view.setOnClickListener(v -> {
            clickListener.onClick(v);
            alertDialog.dismiss();
        });
    }

    @Override
    public void run() {
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                mBitmap, 0, 0, INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final long startTime = SystemClock.elapsedRealtime();
        final Tensor outputTensor = mModule.forward(IValue.from(inputTensor)).toTensor();
        final long inferenceTime = SystemClock.elapsedRealtime() - startTime;
        Log.d("ImageSegmentation",  "inference time (ms): " + inferenceTime);
        final float[] scores = outputTensor.getDataAsFloatArray();
        int[] intValues = new int[INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT];
        for (int j = 0; j < INPUT_TENSOR_HEIGHT; j++) {
            for (int k = 0; k < INPUT_TENSOR_WIDTH; k++) {
                for (int i = 0; i < CLASSNUM; i++) {
                    int idx_score_selected = i * (INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT) + j * INPUT_TENSOR_WIDTH + k;
                    double score = getInfoViewType() == 1 ?
                            Utils.sigmoid(scores[idx_score_selected]) :
                            scores[idx_score_selected];
                    //Log.d("ImageSegmentation",  "idx1: " + score);
                    if (score > THRESHOLD) {
                        //RESHOLD = score;
                        intValues[j * INPUT_TENSOR_WIDTH + k] = 0xFFFFFFFF;
                    }else{
                        intValues[j * INPUT_TENSOR_WIDTH + k] = 0xFF000000;
                    }
                }
            }
        }

        Bitmap bmpSegmentation = Bitmap.createScaledBitmap(
                mBitmap, INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT, true);
        Bitmap outputBitmap = bmpSegmentation.copy(bmpSegmentation.getConfig(), true);
        outputBitmap.setPixels(intValues, 0, outputBitmap.getWidth(), 0, 0,
                outputBitmap.getWidth(), outputBitmap.getHeight());
        final Bitmap transferredBitmap = Bitmap.createScaledBitmap(outputBitmap, mBitmap.getWidth(),
                mBitmap.getHeight(), true);
        bitmap2Share = transferredBitmap;

        runOnUiThread(() -> {
                mImageView.setImageBitmap(transferredBitmap);
                mButtonSegment.setEnabled(true);
                mButtonSegment.setText(getString(R.string.segment));
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

            }
        );
    }
}
