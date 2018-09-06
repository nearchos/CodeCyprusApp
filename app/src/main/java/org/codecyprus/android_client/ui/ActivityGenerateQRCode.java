package org.codecyprus.android_client.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.codecyprus.android_client.R;

public class ActivityGenerateQRCode extends Activity {

    private TextView textView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr_code);

        this.textView = findViewById(R.id.activity_generate_qr_code_text_view);
        this.imageView = findViewById(R.id.activity_generate_qr_code_image_view);

        final ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get session UUID
        final String sessionUUID = getIntent().getStringExtra("session");

        // decide optimal size for qr code
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        final int size = height > width ?
                width : // portrait
                height * 2 / 3; // landscape

        textView.setText(sessionUUID.substring(sessionUUID.length() - 10));
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(sessionUUID, BarcodeFormat.QR_CODE, size, size);
            final BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            final Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException we) {
            throw new RuntimeException(we);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }
}