/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hezd.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.hezd.zxing.R;
import com.hezd.zxing.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final long ANIMATION_DELAY = 80L;
    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;

    private int i = 0;// 添加的
    private Rect mRect;// 扫描线填充边界
//    private GradientDrawable mDrawable;// 采用渐变图作为扫描线
    private Drawable lineDrawable;// 采用图片作为扫描线
    /**
     * 取景框边角线的边长
     **/
    private int lineLengh;
    /**
     * 取景框边角线的线的高度
     **/
    private int lineHeight;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        lineLengh = dip2px(getContext(), 17);
        lineHeight = dip2px(getContext(), 4);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);

//        int left = getResources().getColor(R.color.lightgreen);
//        int center = getResources().getColor(R.color.green);
//        int right = getResources().getColor(R.color.lightgreen);
        mRect = new Rect();
        lineDrawable = getResources().getDrawable(R.drawable.zx_code_line);
//        mDrawable = new GradientDrawable(
//                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{left,
//                left, center, right, right});

        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<ResultPoint>(5);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                context.getResources().getDisplayMetrics());
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return;
        }

        Rect frame = cameraManager.getFramingRect();
        if (frame == null) {
            return;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 画扫描框外部的暗色背景
        // 设置蒙板颜色
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        // 头部
        canvas.drawRect(0, 0, width, frame.top, paint);
        // 左边
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        // 右边
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        // 底部
        canvas.drawRect(0, frame.bottom, width, height, paint);

        if (resultBitmap != null) {
            // 在扫描框中画出预览图
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            drawTextBelow(canvas, frame);
            drawTextAbove(canvas, frame);
            // 画出四个角
            paint.setColor(getResources().getColor(R.color.color_scan_line));
            // 左上角
            canvas.drawRect(frame.left, frame.top, frame.left + lineLengh,
                    frame.top + lineHeight, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + lineHeight,
                    frame.top + lineLengh, paint);
            // 右上角
            canvas.drawRect(frame.right - lineLengh, frame.top, frame.right,
                    frame.top + lineHeight, paint);
            canvas.drawRect(frame.right - lineHeight, frame.top, frame.right,
                    frame.top + lineLengh, paint);
            // 左下角
            canvas.drawRect(frame.left, frame.bottom - lineHeight, frame.left
                    + lineLengh, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - lineLengh, frame.left
                    + lineHeight, frame.bottom, paint);
            // 右下角
            canvas.drawRect(frame.right - lineLengh, frame.bottom - lineHeight,
                    frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - lineHeight, frame.bottom - lineLengh,
                    frame.right, frame.bottom, paint);

            // 在扫描框中画出模拟扫描的线条
            // 设置扫描线条颜色为绿色
            paint.setColor(getResources().getColor(R.color.color_scan_line));
            // 设置绿色线条的透明值
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            // 透明度变化
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

            // 画出固定在中部的线条
            // int middle = frame.height() / 2 + frame.top;
            // canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
            // middle + 2, paint);

            // 将扫描线修改为上下走的线
            if ((i += 5) < frame.bottom - frame.top) {
                /* 以下为用渐变线条作为扫描线 */
                // 渐变图为矩形
                // mDrawable.setShape(GradientDrawable.RECTANGLE);
                // 渐变图为线型
                // mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                // 线型矩形的四个圆角半径
                // mDrawable
                // .setCornerRadii(new float[] { 8, 8, 8, 8, 8, 8, 8, 8 });
                // 位置边界
                // mRect.set(frame.left + 10, frame.top + i, frame.right - 10,
                // frame.top + 1 + i);
                // 设置渐变图填充边界
                // mDrawable.setBounds(mRect);
                // 画出渐变线条
                // mDrawable.draw(canvas);

				/* 以下为图片作为扫描线 */
                mRect.set(frame.left - 6, frame.top + i - 6, frame.right + 6,
                        frame.top + 6 + i);
                lineDrawable.setBounds(mRect);
                lineDrawable.draw(canvas);

                // 刷新
                invalidate();
            } else {
                i = 0;
            }

            // 重复执行扫描框区域绘制(画四个角及扫描线)
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }

    }


    private void drawTextBelow(Canvas canvas, Rect frame) {
        int width = canvas.getWidth();
        paint.setColor(getResources().getColor(R.color.scan_below_text));
        paint.setTextSize(getResources().getDimension(R.dimen.xlarge_text_size));
        // 画第一行字
        final float textWidthPart1 = paint.measureText(getContext().getString(R.string.msg_target_tv1));// 取出文字宽度
        float xPart1 = (width - textWidthPart1) / 2;// 文字开始位置
        // 根据 drawTextGravityBottom 文字在扫描框上方还是下文，默认下方
        float yPart1 = frame.bottom + dip2px(getContext(), 60);
        canvas.drawText(getContext().getString(R.string.msg_target_tv1), xPart1, yPart1, paint);
        // 画第二行字
        final float textWidthPart2 = paint.measureText(getContext().getString(R.string.msg_target_tv2));// 取出文字宽度
        float xPart2 = (width - textWidthPart2) / 2;// 文字开始位置
        // 根据 drawTextGravityBottom 文字在扫描框上方还是下文，默认下方
        float yPart2 = yPart1 + dip2px(getContext(), 30);
        canvas.drawText(getContext().getString(R.string.msg_target_tv2), xPart2, yPart2, paint);
    }


    private void drawTextAbove(Canvas canvas, Rect frame) {
        int width = canvas.getWidth();
        paint.setColor(getResources().getColor(R.color.white));
        paint.setTextSize(getResources().getDimension(R.dimen.xlarge_text_size));
        // 画第一行字
        final float textWidthPart1 = paint.measureText(getContext().getString(R.string.msg_default_status_part1));// 取出文字宽度
        float xPart1 = (width - textWidthPart1) / 2;// 文字开始位置
        // 根据 drawTextGravityBottom 文字在扫描框上方还是下文，默认下方
        float yPart1 = frame.top - dip2px(getContext(), 50);
        canvas.drawText(getContext().getString(R.string.msg_default_status_part1), xPart1, yPart1, paint);
        // 画第二行字
        final float textWidthPart2 = paint.measureText(getContext().getString(R.string.msg_default_status_part2));// 取出文字宽度
        float xPart2 = (width - textWidthPart2) / 2;// 文字开始位置
        // 根据 drawTextGravityBottom 文字在扫描框上方还是下方，默认下方
        float yPart2 = yPart1 + dip2px(getContext(), 30);
        canvas.drawText(getContext().getString(R.string.msg_default_status_part2), xPart2, yPart2, paint);
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

    public void recycleLineDrawable() {
//        if (mDrawable != null) {
//            mDrawable.setCallback(null);
//        }
        if (lineDrawable != null) {
            lineDrawable.setCallback(null);
        }
    }
}
