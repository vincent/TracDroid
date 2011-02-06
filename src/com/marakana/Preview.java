package com.marakana;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Parcel;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";

	SurfaceHolder mHolder;
	public Camera camera;
	protected ArrayList<Point> pointList;
	protected boolean inDrawingMode = false; 
	protected Paint paint;
	private Bitmap tmpBitmap;

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		pointList = new ArrayList<Point>();

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    paint.setColor(Color.RED);
	    paint.setStrokeWidth(10);
	    paint.setStyle(Style.STROKE);
	}

	// Called once the holder is ready
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);

			camera.setPreviewCallback(new PreviewCallback() {
				// Called for each frame previewed
				public void onPreviewFrame(byte[] data, Camera camera) {
					Log.d(TAG, "onPreviewFrame called at: " + System.currentTimeMillis());
					Preview.this.invalidate();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Called when the holder is destroyed
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	// Called when holder has changed
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		camera.startPreview();
		tmpBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "onDraw");
		
		if (inDrawingMode && tmpBitmap != null) {
			Log.d(TAG, "in drawing mode");

			Point lastPoint = null;
			Iterator<Point> it = pointList.iterator();
			
			Canvas tmpCanvas = new Canvas(tmpBitmap);
			
			while (it.hasNext()) {
				Point nextPoint = it.next();
				
				if (lastPoint != null) {
					Log.d(TAG, "drawing point " + nextPoint.toString());
				    canvas.drawLine(lastPoint.x, lastPoint.y, nextPoint.x, nextPoint.y, paint);
				    tmpCanvas.drawLine(lastPoint.x, lastPoint.y, nextPoint.x, nextPoint.y, paint);
				}
				lastPoint = nextPoint;
			}
			
			pointList.clear();
			inDrawingMode = false;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int posX = Math.round(event.getX());
		int posY = Math.round(event.getY());

		pointList.add(new Point(posX, posY));

		if (action == MotionEvent.ACTION_UP) {
			Log.d(TAG, "finish touch event: " + pointList.toString());
			inDrawingMode = true;

			// Without this, our onDraw is never called
			setWillNotDraw(false);
			Preview.this.invalidate();	
		}


		return true;
	}

	public Bitmap getPreviewBitmap() {
		return tmpBitmap;
	}

	public void setPreviewBitmap(Bitmap data) {
		tmpBitmap = data;
	}

}