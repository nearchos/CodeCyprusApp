package org.codecyprus.android_client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.codecyprus.android_client.R;
import org.codecyprus.android_client.sync.SyncService;
import org.codecyprus.th.model.Replies;
import org.codecyprus.th.model.Status;

import java.util.HashMap;

public class ActivityGenerateQRCode extends AppCompatActivity {

    public static final String TAG = "codecyprus";

    private ProgressBar progressBar;
    private View codeView;
    private View notCompletedView;
    private View noPrizeView;

    private ImageView imageView;

    private Gson gson = new Gson();

    private ProgressReceiver progressReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr_code);

        this.progressBar = findViewById(R.id.activity_generate_qr_code_progress_bar);
        this.codeView = findViewById(R.id.activity_generate_qr_code_qr_code);
        this.notCompletedView = findViewById(R.id.activity_generate_qr_code_not_completed);
        this.noPrizeView = findViewById(R.id.activity_generate_qr_code_no_prize);

        this.imageView = findViewById(R.id.activity_generate_qr_code_image_view);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        progressReceiver = new ProgressReceiver();
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

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(sessionUUID, BarcodeFormat.QR_CODE, size, size);
            final BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            final Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException we) {
            throw new RuntimeException(we);
        }

        final IntentFilter intentFilter = new IntentFilter(SyncService.ACTION_SCORE_COMPLETED);
        registerReceiver(progressReceiver, intentFilter);
        requestScore(sessionUUID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(progressReceiver);
    }


    private void requestScore(final String sessionUUID) {
        final Intent scoreIntent = new Intent(this, SyncService.class);
        scoreIntent.setAction(SyncService.ACTION_SCORE);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        scoreIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        progressBar.setVisibility(View.VISIBLE);
        startService(scoreIntent);
    }

    private class ProgressReceiver extends BroadcastReceiver
    {
        @Override public void onReceive(final Context context, final Intent intent)
        {
            final String payload = (String) intent.getSerializableExtra(SyncService.EXTRA_PAYLOAD);
            setProgressBarIndeterminateVisibility(false);

            if(payload != null) {
                Log.d(TAG, "intent.getAction() -> " + intent.getAction());

                final Replies.Reply reply = gson.fromJson(payload, Replies.Reply.class);
                if(reply.getStatus() == Status.OK) {
                    if(SyncService.ACTION_SCORE_COMPLETED.equals(intent.getAction())) {
                        final Replies.ScoreReply scoreReply = gson.fromJson(payload, Replies.ScoreReply.class);
                        final boolean completed = scoreReply.isCompleted();
                        final long score = scoreReply.getScore();

                        progressBar.setVisibility(View.GONE);
                        if(!completed) {
                            notCompletedView.setVisibility(View.VISIBLE);
                        } else if(score < 30) {
                            noPrizeView.setVisibility(View.VISIBLE);
                        } else {
                            codeView.setVisibility(View.VISIBLE);
                        }
                    }
                } else if(reply.getStatus() == Status.ERROR) {
                    final Replies.ErrorReply errorReply = gson.fromJson(payload, Replies.ErrorReply.class);
                    new DialogError(context, errorReply.getErrorMessages()).show();
                }
            } else {
                new DialogError(context, "Invalid null response from server").show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}