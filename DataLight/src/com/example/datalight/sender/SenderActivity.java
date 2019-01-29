package com.example.datalight.sender;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.datalight.R;
import com.example.datalight.receiver.ReceiverActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class SenderActivity extends Activity {
	private static final String TAG = SenderActivity.class.getSimpleName();
	
	private static final int FILE_CHOOSER_REQUEST = 1234;

	// ui variables
	private ImageView qrCodeImageView;
	private TextView debugTextView;
	
	// SenderActivityManager
	private SenderActivityManager senderActivityManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sender);

		// init SenderActivityManager
		senderActivityManager = new SenderActivityManager(this);
				
		// init ui
		qrCodeImageView = (ImageView) findViewById(R.id.image_view);
		debugTextView = (TextView)findViewById(R.id.debug_text_view);
		
//		senderActivityManager.generateTestQR();
		
		 // choose file button
        Button chooseFileButton = (Button) findViewById(R.id.send_file_button);
        chooseFileButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent getContentIntent = FileUtils.createGetContentIntent();
			    Intent intent = Intent.createChooser(getContentIntent, "Select a file");
			    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
			}
		});
        
        // test send button
        Button test_send_button = (Button) findViewById(R.id.test_send_button);
        test_send_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				senderActivityManager.prepareFileForSending(null);
			}
		});
        
        // generate qr code
        Button generateQRButton = (Button) findViewById(R.id.generate_qr_button);
        generateQRButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				senderActivityManager.generateTestQR();
			}
		});
	}

	// --- overrides ----
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// after choosing a file
		case FILE_CHOOSER_REQUEST:   
			if (resultCode == RESULT_OK) {
				final Uri uri = data.getData();
				File file = FileUtils.getFile(this,  uri);
				senderActivityManager.prepareFileForSending(file);
			}
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sender, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		// stop camera
		senderActivityManager.stopCamera();
	}
	
	// --- public functions ---
	public void disableFileButtons() {
		 Button chooseFileButton = (Button) findViewById(R.id.send_file_button);
		 chooseFileButton.setEnabled(false);

		 // receiver button
		 Button receiverButton = (Button) findViewById(R.id.test_send_button);
		 receiverButton.setEnabled(false);
	}
	
	public void showQRCode(Bitmap qrCode) {
		qrCodeImageView.setImageBitmap(qrCode);
	}
	
	public void showDebugText(String debugText) {
		debugTextView.setText(debugText);
	}
}
