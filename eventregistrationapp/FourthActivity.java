package com.java.eventregistrationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class FourthActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private EditText username;
    private EditText email;
    private EditText number;
    private EditText college;
    private Button submit;
    private CheckBox checkboxPrice;

    private Button Home;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 123;
    private static final int PDF_PERMISSION_REQUEST_CODE = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        username = findViewById(R.id.editTextName);
        email = findViewById(R.id.userEmail);
        number = findViewById(R.id.editTextNumber);
        college = findViewById(R.id.editTextCollege);
        checkboxPrice = findViewById(R.id.checkboxPrice);
        submit = findViewById(R.id.buttonSubmit);
        Home = findViewById(R.id.button4);

        if (!checkStoragePermission()) {
            requestStoragePermission();
        }

        Home.setOnClickListener(v -> {
            Intent intent = new Intent(FourthActivity.this, MainActivity.class);
            startActivity(intent);
        });

        submit.setOnClickListener(v -> {
            // Get user input from EditText fields
            String name = username.getText().toString().trim();
            String em = email.getText().toString().trim();
            String num = number.getText().toString().trim();
            String col = college.getText().toString().trim();
            boolean priceAccepted = checkboxPrice.isChecked();

            if (priceAccepted && !name.isEmpty() && !em.isEmpty() && !num.isEmpty() && !col.isEmpty()) {
                String encodedEmail = Base64.encodeToString(em.getBytes(), Base64.NO_WRAP);
                usersRef.child(encodedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(FourthActivity.this, "Already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("username ", name);
                            userInfo.put("email", em);
                            userInfo.put("number", num);
                            userInfo.put("college ", col);

                            usersRef.child(name).setValue(userInfo)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(FourthActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e("DatabaseError", "Error saving user information: " + task.getException().getMessage());
                                            Toast.makeText(FourthActivity.this, "Error saving user information", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            String registrationData = "Name: " + name + "\n" +
                                    "Email: " + em + "\n" +
                                    "Number: " + num + "\n" +
                                    "College: " + col;

                            Bitmap qrCode;
                            try {
                                qrCode = generateQRCode(registrationData);
                                qrCodeImageView.setImageBitmap(qrCode);
                                Toast.makeText(FourthActivity.this, "Tap the QR code to download", Toast.LENGTH_SHORT).show();
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("DatabaseError", "Error: " + error.getMessage());
                    }
                });
            } else {
                showErrorMessage("Please fill in all required fields.");
            }
        });

        qrCodeImageView.setOnClickListener(view -> {
            if (checkStoragePermission()) {
                downloadQrCodeAsPdf();
            } else {
                requestStoragePermission();
            }
        });
    }

    private Bitmap generateQRCode(String data) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 400, 400);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return bitmap;
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO,},
                STORAGE_PERMISSION_REQUEST_CODE
        );
    }

    private void downloadQrCodeAsPdf() {
        // Get the bitmap from the QR code ImageView
        BitmapDrawable drawable = (BitmapDrawable) qrCodeImageView.getDrawable();
        Bitmap qrCodeBitmap = drawable.getBitmap();

        // Generate a unique filename for the PDF
        String fileName = "QRCode_" + System.currentTimeMillis() + ".pdf";

        try {
            // Get the application's external storage directory
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (externalFilesDir != null) {
                // Create a file within the internal storage directory
                File pdfFile = new File(externalFilesDir, fileName);

                savePdf(pdfFile, qrCodeBitmap);
                openPdfWithIntent(this, pdfFile);
            } else {
                Log.e("PDF_PATH", "External files directory is null");
                Toast.makeText(this, "QR Code Saved as PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR code PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdf(File pdfFile, Bitmap qrCodeBitmap) {
        try {
            // Create a file within the internal storage directory
            OutputStream outputStream = new FileOutputStream(pdfFile);

            // Initialize iText PDF writer
            PdfWriter pdfWriter = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            // Add QR code image to the PDF
            Image qrCodeImage = new Image(ImageDataFactory.create(bitmapToBytes(qrCodeBitmap)));
            document.add(qrCodeImage);

            // Close the document
            document.close();

            // Notify the MediaStore about the new file
            notifyMediaStore(this, pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR code PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyMediaStore(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, "com.java.eventregistrationapp.fileprovider", file);

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        context.sendBroadcast(mediaScanIntent);
    }

    private void openPdfWithIntent(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, "com.java.eventregistrationapp.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Storage permission granted, you can now perform your operations
                if (checkStoragePermission()) {
                    downloadQrCodeAsPdf();
                } else {
                    Toast.makeText(this, "Failed to get storage permission", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Storage permission is required to download QR Code as PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

}