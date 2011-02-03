package com.marakana;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import vincentlark.trac.tracdroid.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CameraActivity extends Activity {
	private static final String TAG = "CameraDemo";
	Preview preview;
	Button buttonClick;
	
	int ticket_id;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.camera_main);

		Bundle params = this.getIntent().getExtras();

		ticket_id = params.getInt("ticket_id");
		
		/*
		TwoLineListItem title = (TwoLineListItem) findViewById(R.id.preview_title);
		String ticket_summary = params.getString("ticket_summary");
	
		if (ticket_summary != null)
			title.getText1().setText(ticket_summary);
		
		String ticket_desc = params.getString("ticket_desc");
		if (ticket_desc != null)
			title.getText2().setText(ticket_desc);
		*/
		
		TextView title = (TextView) findViewById(R.id.preview_title);
		String ticket_summary = params.getString("ticket_summary");
	
		if (ticket_summary != null)
			title.setText(ticket_summary);
		/**/
		
		preview = new Preview(this);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);

		final Button buttonReshoot = (Button) findViewById(R.id.buttonReshoot);
		buttonReshoot.setVisibility(View.GONE);
		buttonReshoot.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				preview.camera.startPreview();
				preview.invalidate();	
			}
		});
		
		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				
				buttonReshoot.setVisibility(View.VISIBLE);
			}
		});
		
		final Button buttonFinish = (Button) findViewById(R.id.buttonFinish);
		buttonFinish.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String fileName = String.format("/sdcard/%d_%d.jpg", ticket_id, System.currentTimeMillis());
				
				try {
					preview.getPreviewBitmap().compress(CompressFormat.PNG, 8, new FileOutputStream(fileName));

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	            Intent resultIntent = new Intent();
	            resultIntent.putExtra("image_filename", fileName);
	            setResult(RESULT_OK, resultIntent);
	            finish();
			}
		});
		
		Log.d(TAG, "onCreate'd");
	}

	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	// Handles data for jpeg picture
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			String fileName = String.format("/sdcard/%d.jpg", System.currentTimeMillis());
			FileOutputStream outStream = null;
			
			try {
				// Write to SD Card
				outStream = new FileOutputStream(fileName);
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
	
				preview.setPreviewBitmap(BitmapFactory.decodeFile(fileName).copy(Bitmap.Config.RGB_565, true));
				Log.d(TAG, "onPictureTaken - base bitmap set");
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

}
