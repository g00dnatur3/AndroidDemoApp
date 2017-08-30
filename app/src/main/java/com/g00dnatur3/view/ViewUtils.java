package com.g00dnatur3.view;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.TextureView;
import android.util.Size;

/**
 * Created by developer on 7/17/17.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ViewUtils {

    private static final String TAG = ViewUtils.class.getCanonicalName();

    public static void adjustAspectRatio(TextureView textureView, Size size, int orientation) {
        int videoWidth = size.getWidth();
        int videoHeight = size.getHeight();
        if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
            int _temp = videoWidth;
            videoWidth = videoHeight;
            videoHeight = _temp;
        }
        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;
        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);
        Matrix txform = new Matrix();
        textureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        textureView.setTransform(txform);
    }

}
