package com.surajms.signatureview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.surajms.signatureview.R;

import java.io.ByteArrayOutputStream;

public class SignatureView extends View {

    int penMinWidth;

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int penColor = Color.BLACK;
    private int backgroundColor;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    SignatureListener listener;

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);


        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SignatureView,
                0, 0);

        penMinWidth      = typedArray.getDimensionPixelSize(convertDpToPx(R.styleable.SignatureView_minPenWidth), convertDpToPx(4));
        backgroundColor  = typedArray.getColor(R.styleable.SignatureView_backgroundColor, Color.TRANSPARENT);
        penColor         = typedArray.getColor(R.styleable.SignatureView_penColor, Color.BLACK);

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(penColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(penMinWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
//        canvasPaint.setColor(backgroundColor);
        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        if(listener != null)
            listener.onViewChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    public Canvas startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
        if(listener != null)
            listener.onCleared();
        return drawCanvas;
    }

    public Canvas getDrawCanvas() {
        return drawCanvas;
    }

    public void setPenColor(int color){
        penColor = color;
        drawPaint.setColor(penColor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                if(event.getHistorySize() == 0)
                    drawCanvas.drawPoint(touchX, touchY,drawPaint);
                listener.onSignatureStarted();
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public Bitmap toBitmap(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public Bitmap toBitmap(int scaled, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        canvasBitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
        byte[] byteArray = stream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private int convertDpToPx(float dp){
        return Math.round(getContext().getResources().getDisplayMetrics().density * dp);
    }

    public void setListener(SignatureListener listener) {
        this.listener = listener;
    }

    public interface SignatureListener {
        void onSignatureStarted();
        void onViewChanged();
        void onCleared();
    }
}