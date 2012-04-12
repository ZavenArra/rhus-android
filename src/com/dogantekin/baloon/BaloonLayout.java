package com.dogantekin.baloon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public class BaloonLayout extends LinearLayout {

    public BaloonLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    
    public BaloonLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void dispatchDraw(Canvas canvas) {       
        Paint panelPaint  = new Paint();
        panelPaint.setARGB(0, 0, 0, 0);
               
        RectF panelRect = new RectF();
        panelRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
        canvas.drawRoundRect(panelRect, 5, 5, panelPaint);
       
        RectF baloonRect = new RectF();
        baloonRect.set(0,0, getMeasuredWidth(), 2*(getMeasuredHeight()/3));
        panelPaint.setARGB(230, 50, 0, 0);       
        canvas.drawRoundRect(baloonRect, 10, 10, panelPaint);
       
        Path baloonTip = new Path();
        baloonTip.moveTo(5*(getMeasuredWidth()/8), 2*(getMeasuredHeight()/3));
        baloonTip.lineTo(getMeasuredWidth()/2, getMeasuredHeight());
        baloonTip.lineTo(3*(getMeasuredWidth()/4), 2*(getMeasuredHeight()/3));
       
        canvas.drawPath(baloonTip, panelPaint);
               
        super.dispatchDraw(canvas);
    }
}
