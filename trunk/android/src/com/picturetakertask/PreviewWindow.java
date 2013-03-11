package com.picturetakertask;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This view displays a miniature preview from the camera.
 * 
 * Used in the main activity where the user fills in a form to set up a task, so they can see what the
 * task will take a picture of.
 */

public class PreviewWindow extends SurfaceView implements SurfaceHolder.Callback {

	//a holder of the surface
    private SurfaceHolder sHolder; 
    //a variable to control the camera
    private Camera mCamera;

    public PreviewWindow(Context context)
    {
        super(context);
        init();
    }
    
    public PreviewWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init()
    {
        //Get a surface
        sHolder = this.getHolder();
       
        //add the callback interface methods defined below as the Surface   View callbacks
        sHolder.addCallback(this);
       
        //tells Android that this surface will have its data constantly replaced
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);	
    }
    
    /**
     * Called when the surface is changed
     */
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
  	  Log.d("camerabasic", "PreviewWindow.surfaceChanged called");

         mCamera.startPreview();

         setCameraDisplayOrientation((Activity) getContext(), 0, mCamera);
    }
    
    /**
     * Set the camera orientation based on the phone rotation
     */
    public static void setCameraDisplayOrientation(Activity activity,
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * Called whenever the surface is created
     */
    public void surfaceCreated(SurfaceHolder holder)
    {
  	  	Log.d("camerabasic", "PreviewWindow.surfaceCreated called");

          // The Surface has been created, acquire the camera and tell it where
	        // to draw the preview.
	        mCamera = Camera.open();
	        try {
	           mCamera.setPreviewDisplay(holder);
	           
	        } catch (IOException exception) {
	            mCamera.release();
	            mCamera = null;
	        }
    }

    /**
     * Called whenever the surface is destroyed
     */
    public void surfaceDestroyed(SurfaceHolder holder)
    {
  	  	Log.d("camerabasic", "PreviewWindow.surfaceDestroyed called");
          //stop the preview
          mCamera.stopPreview();
          //release the camera
	        mCamera.release();
	        //unbind the camera from this object
	        mCamera = null;
    }
    
}
