package com.example.datalight.receiver;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ViewFinderView extends View {
	private static final String TAG = ViewFinderView.class.getSimpleName();
	
//	private static final int POINT_SIZE = 6;
	
	private Rect framingRect;
	private Paint paint;
//	private ResultPoint[] resultPoints;
	
	public ViewFinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
	}
	
	public void setFramingRect(Rect framingRect) {
		this.framingRect = framingRect;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (framingRect != null) {
			canvas.drawRect(framingRect.left, framingRect.top, framingRect.right, framingRect.bottom, paint);
		}
		
//		if (resultPoints != null) {
//			Rect frame = cameraManager.getFramingRect();
//			Rect previewFrame = cameraManager.getFramingRectInPreview();    
//			
//			float scaleX = frame.width() / (float) previewFrame.width();
//			float scaleY = frame.height() / (float) previewFrame.height();
//
//			for (ResultPoint point : resultPoints) {
//				canvas.drawCircle(frame.left + (int) (point.getX() * scaleX),
//						frame.top + (int) (point.getY() * scaleY),
//						POINT_SIZE, paint);
//			}
//		}
	}
	
//	public void setResultPoints(ResultPoint[] resultPoints) {
//		this.resultPoints = resultPoints;
//		invalidate();
//	}
}