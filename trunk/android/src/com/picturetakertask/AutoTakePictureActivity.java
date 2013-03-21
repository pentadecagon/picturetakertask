package com.picturetakertask;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * This class simply takes one picture and then finishes.
 */

public class AutoTakePictureActivity extends Activity implements SurfaceHolder.Callback
{
      //a variable to store a reference to the Surface View at the main.xml file
      private SurfaceView sv = null;

      //Camera variables
      //a surface holder
      private SurfaceHolder sHolder; 
      //a variable to control the camera
      private Camera mCamera;

      //a handler to delay the taking of the photo until the preview window is ready
      Handler handler = new Handler();

      //the camera parameters
      private Parameters parameters;
      
      //name of album in gallery containing pictures produced by this activity
      public static final String ALBUM_NAME = "PictureTakerTask";
      
      //picture needs to be rotated before it is stored, otherwise it is stored at the wrong angle
      private float amountToRotatePictureBy;
     
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	Log.d("camerabasic", "AutoTakePictureActivity.onCreate called");
        super.onCreate(savedInstanceState);
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.auto_take_picture);

    }
    
    @Override
	protected void onResume() {
    	Log.d("camera", "AutoTakePictureActivity.onResume called");
		super.onResume();
		
		//add preview window, which activates camera
		sv = new SurfaceView(this);
		FrameLayout preview = (FrameLayout) findViewById(R.id.preview_window);
		preview.addView(sv);

        //get the Surface View at the main.xml file
        //sv = (SurfaceView) findViewById(R.id.surfaceView);
       
        //Get a surface
        sHolder = sv.getHolder();
       
        //add the callback interface methods defined below as the Surface   View callbacks
        sHolder.addCallback(this);
       
        //tells Android that this surface will have its data constantly replaced
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    @Override
    protected void onPause() {
    	
    	handler.removeCallbacksAndMessages(null);
    	
    	//remove preview window, which deactivates camera
    	FrameLayout preview = (FrameLayout) findViewById(R.id.preview_window);
    	preview.removeView(sv);
    	sv = null;

        super.onPause();
        Log.d("camera", "AutoTakePictureActivity.onPause called");
    }

    /**
     * Called when the surface is changed
     */
      public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
      {
    	  Log.d("camerabasic", "AutoTakePictureActivity.surfaceChanged called");

    	  //set display orientation (portrait or landscape) based on screen rotation
    	  setCameraDisplayOrientation(this, 0, mCamera);
    	  
    	  //set picture size to largest possible
    	  parameters = mCamera.getParameters();
    	  Camera.Size pictureSize = getBiggestPictureSize(parameters);   	  
    	  parameters.setPictureSize(pictureSize.width, pictureSize.height);
    	  parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    	  mCamera.setParameters(parameters);
    	  
          mCamera.startPreview();

          //set callback for when preview has been initialized
          mCamera.setOneShotPreviewCallback(oneShotPreviewCallback);
      }
      
      /**
       * Set the camera orientation based on the phone rotation
       */
      public void setCameraDisplayOrientation(Activity activity,
              int cameraId, android.hardware.Camera camera) {
          android.hardware.Camera.CameraInfo info =
                  new android.hardware.Camera.CameraInfo();
          android.hardware.Camera.getCameraInfo(cameraId, info);
          int rotation = activity.getWindowManager().getDefaultDisplay()
                  .getRotation();
          int degrees = 0;
          amountToRotatePictureBy = 0f;
          switch (rotation) {
              case Surface.ROTATION_0:
            	  amountToRotatePictureBy = 90f;
            	  degrees = 0;
            	  break;
              case Surface.ROTATION_90:
            	  amountToRotatePictureBy = 0f;
            	  degrees = 90;
            	  break;
              case Surface.ROTATION_180:
            	  degrees = 180;
            	  break;
              case Surface.ROTATION_270:
            	  amountToRotatePictureBy = 180f;
            	  degrees = 270;
            	  break;
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
       * Get the biggest picture size allowed by the device's camera
       */
      private Camera.Size getBiggestPictureSize(Camera.Parameters parameters) {
    	    Camera.Size result=null;

    	    for (Camera.Size size : parameters.getSupportedPictureSizes()) {
    	      if (result == null) {
    	        result=size;
    	      }
    	      else {
    	        int resultArea=result.width * result.height;
    	        int newArea=size.width * size.height;

    	        if (newArea > resultArea) {
    	          result=size;
    	        }
    	      }
    	    }

    	    return(result);
    	  }

      /**
       * Callback for when preview has been initialized
       */
      private Camera.PreviewCallback oneShotPreviewCallback = new Camera.PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] arg0, Camera arg1) {
			//have to delay the action by 1 sec or the picture turns out black
			handler.postDelayed(
				new Runnable(){
					@Override
					public void run() {
						if (mCamera != null)
						{
							mCamera.autoFocus(autoFocusCallback);
						}
					}
				}, 500);
		}	  
      };
      
      /**
       * Auto focus callback
       */
      private AutoFocusCallback autoFocusCallback = new AutoFocusCallback(){

    	  @Override
    	  public void onAutoFocus(boolean arg0, Camera arg1) {
    		  if (mCamera != null)
    		  {
    			  mCamera.takePicture(null, null, mCall);
    		  }
    	  }
      };

      /**
       * Callback function that takes and stores the picture
       */
      Camera.PictureCallback mCall = new Camera.PictureCallback()
      {
      
        public void onPictureTaken(byte[] data, Camera camera)
        {
       	 	  Log.d("camerabasic", "onPictureTaken called");
       	 	  new SavePhotoTask().execute(data);
       	 	finish();

        }
      };
      
      class SavePhotoTask extends AsyncTask<byte[], String, String> {
    	  @Override
          protected String doInBackground(byte[]... dataRaw) {

    		  byte[] data = dataRaw[0];
              FileOutputStream outStream = null;
              try{
              	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        	              Environment.DIRECTORY_PICTURES), ALBUM_NAME);
              	
              	if (! mediaStorageDir.exists()){
          	        if (! mediaStorageDir.mkdirs()){
          	            Log.d("camerabasic", "failed to create directory");
          	            throw new RuntimeException("failed to create directory");
          	        }
          	    }
              	
               //rotate bitmap by the necessary angle so it's saved upright
               Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
               int width = bmp.getWidth();
               int height = bmp.getHeight();
               Matrix matrix = new Matrix();               
               matrix.postRotate(amountToRotatePictureBy, width/2.0f, height/2.0f);
               Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0,width, height, matrix, true);
               
               Date date = new Date(System.currentTimeMillis());
       		   String datemod = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(date);
               String fileName = mediaStorageDir.getPath()+"/Image_"+datemod+".jpg";
              	
                  outStream = new FileOutputStream(fileName);
                  //outStream.write(data);
                  rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                  outStream.close();
                  
                  File mediaFile = new File(fileName);
                  sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(mediaFile)));
                  
              } catch (FileNotFoundException e){
                  Log.d("CAMERA", e.getMessage());
              } catch (IOException e){
                  Log.d("CAMERA", e.getMessage());
              }

              
    		  return null;
    	  }

      }
      
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
    	  	Log.d("camerabasic", "AutoTakePictureActivity.surfaceDestroyed called");
            //stop the preview
            mCamera.stopPreview();
            //release the camera
	        mCamera.release();
	        //unbind the camera from this object
	        mCamera = null;
      }
}
