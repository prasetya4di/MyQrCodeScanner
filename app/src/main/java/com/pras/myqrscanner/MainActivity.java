package com.pras.myqrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView cameraView;
    private ProcessCameraProvider processCameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.mainCameraView);

        cameraProvider = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    private void startCamera() {
        cameraProvider.addListener(() -> {
            try {
                processCameraProvider = this.cameraProvider.get();
                bindCameraPreview(processCameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String result) {
                showAlertDialog(result);
                cameraProvider.unbindAll();
            }

            @Override
            public void qrCodeNotFound() {
                Log.d("Scan result", "Qr code not found");
            }
        }));

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    private void showAlertDialog(String result) {
        ResultType resultType = ResultType.parse(result);

        if (resultType == ResultType.CODE) {
            showCodeTypeDialog(result, resultType);
        } else {
            showResultDialog(result, resultType);
        }
    }

    private void showCodeTypeDialog(String result, @NonNull ResultType resultType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View resultNumberLayout = inflater.inflate(R.layout.result_number_dialog, null);
        TextView tvResult = resultNumberLayout.findViewById(R.id.tvQrResult);
        TextView tvType = resultNumberLayout.findViewById(R.id.tvQrType);
        ImageView fbShare = resultNumberLayout.findViewById(R.id.facebookButton);
        ImageView twitterShare = resultNumberLayout.findViewById(R.id.twitterButton);
        ImageView whatsappShare = resultNumberLayout.findViewById(R.id.whatsappButton);
        fbShare.setOnClickListener(v -> shareToOtherApp(result, "com.facebook.katana"));
        twitterShare.setOnClickListener(v -> shareToOtherApp(result, "com.twitter.android"));
        whatsappShare.setOnClickListener(v -> shareToOtherApp(result, "com.whatsapp"));
        tvResult.setText(result);
        tvType.setText(resultType.typeName);
        builder.setView(resultNumberLayout)
                .setNegativeButton(R.string.dialog_button_close, (dialog, which) -> {
                    dialog.dismiss();
                    startCamera();
                });
        builder.show();
    }

    private void showResultDialog(String result, @NonNull ResultType resultType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View resultDialogLayout = inflater.inflate(R.layout.result_dialog, null);
        TextView tvResult = resultDialogLayout.findViewById(R.id.tvQrResult);
        TextView tvType = resultDialogLayout.findViewById(R.id.tvQrType);
        TextView tvMessage = resultDialogLayout.findViewById(R.id.resultMessage);
        tvResult.setText(result);
        tvType.setText(resultType.typeName);
        tvMessage.setText(getString(resultType.messageId));
        builder.setView(resultDialogLayout)
                .setPositiveButton(R.string.dialog_button_yes, (dialog, which) -> {
                    startActivity(resultType.intentResult(result));
                    startCamera();
                })
                .setNegativeButton(R.string.dialog_button_no, (dialog, which) -> {
                    dialog.dismiss();
                    startCamera();
                });
        builder.show();
    }

    private void shareToOtherApp(String content, String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage(packageName);

            shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, "Coba title aja hehehehe");
            shareIntent.putExtra(Intent.EXTRA_TEXT, content);
            // Start the specific social application
            startActivity(shareIntent);
        } else {
            String playStoreUrl = "http://play.google.com/store/apps/details?id=" + packageName;
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
            playStoreIntent.setData(Uri.parse(playStoreUrl));
            startActivity(playStoreIntent);
        }
        startCamera();
    }
}
