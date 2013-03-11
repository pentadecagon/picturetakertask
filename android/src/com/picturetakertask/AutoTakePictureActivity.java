package com.picturetakertask;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

/**
 * This class simply takes one picture and then finishes.
 */

public class AutoTakePictureActivity extends Activity implements SurfaceHolder.Callback
{
      //a variable to store a reference to the Image View at the main.xml file
      private ImageView iv_image;
      //a variable to store a reference to the Surface View at the main.xml file
      private SurfaceView sv;
   
      //a bitmap to display the captured image
      private Bitmap bmp;
     
      //Camera variables
      //a surface holder
      private SurfaceHolder sHolder; 
      //a variable to control the camera
      private Camera mCamera;

      //a handler to delay the taking of the photo until the preview window is ready
      Handler handler = new Handler();

     
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	Log.d("camerabasic", "AutoTakePictureActivity.onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_take_picture);
       
        //get the Image View at the main.xml file
        iv_image = (ImageView) findViewById(R.id.imageView);
       
        //get the Surface View at the main.xml file
        sv = (SurfaceView) findViewById(R.id.surfaceView);
       
        //Get a surface
        sHolder = sv.getHolder();
       
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
    	  Log.d("camerabasic", "AutoTakePictureActivity.surfaceChanged called");

           mCamera.startPreview();

           setCameraDisplayOrientation(this, 0, mCamera);

	       //got to wait a bit or picture comes out all black
	       handler.postDelayed(new PictureTaker(), 3000);
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
       * Takes the picture
       */ 
      private class PictureTaker implements Runnable{

          @Override
          public void run() {
          	Log.d("camera", "taking pic in PictureTaker");
          	if (mCamera != null)
          	{
          		mCamera.takePicture(null, null, mCall);
          	}
          }
      }

      /**
       * Callback function that takes and stores the picture
       */ 
      Camera.PictureCallback mCall = new Camera.PictureCallback()
      {
      
        public void onPictureTaken(byte[] data, Camera camera)
        {
       	 	  Log.d("camerabasic", "onPictureTaken called");
              //decode the data obtained by the camera into a Bitmap
              bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
              //set the iv_image
              iv_image.setImageBitmap(bmp);
              FileOutputStream outStream = null;
                   try{
                   	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
             	              Environment.DIRECTORY_PICTURES), "CameraBasic");
                   	
                   	if (! mediaStorageDir.exists()){
               	        if (! mediaStorageDir.mkdirs()){
               	            Log.d("camerabasic", "failed to create directory");
               	            throw new RuntimeException("failed to create directory");
               	        }
               	    }
                   	
                   	String fileName = mediaStorageDir.getPath()+"/Image"+System.currentTimeMillis()+".jpg";
                   	
                       outStream = new FileOutputStream(fileName);
                       outStream.write(data);
                       outStream.close();
                       
                       File mediaFile = new File(fileName);
                       sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(mediaFile)));
                       
                   } catch (FileNotFoundException e){
                       Log.d("CAMERA", e.getMessage());
                   } catch (IOException e){
                       Log.d("CAMERA", e.getMessage());
                   }
                   
                   Task task = new Task(AutoTakePictureActivity.this);
                   task.setConfigFromSharedPreferences();
                   //check if the task will expire before the next iteration: if so, cancel it
                   if (task.checkIfTaskWillExpireBeforeNextIteration())
                   {
                	   Log.d("camera", "AutoTakePictureActivity: task will expire before next iteration so going to end it");
                	   
                	   task.expirePictureTakingService();
                   }
                   
                   finish();
        }
      };
      
      /**
       * Called whenever the surface is created
       */
      public void surfaceCreated(SurfaceHolder holder)
      {
    	  	Log.d("camerabasic", "AutoTakePictureActivity.surfaceCreated called");
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
    	  	Log.d("camerabasic", "surfaceDestroyed called");
            //stop the preview
            mCamera.stopPreview();
            //release the camera
	        mCamera.release();
	        //unbind the camera from this object
	        mCamera = null;
      }
}
