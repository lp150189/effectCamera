package com.example.effectcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.PrivateCredentialPermission;


import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity implements EffectsRecorder.EffectsListener,
MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener{

    private Camera mCamera;
    private CameraPreview mPreview;
    private EffectsRecorder mEffectsRecorder;
    private CamcorderProfile mProfile;
    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("ERROR", "Error creating media file, check storage permissions: "
                   );
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("ERROR", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("ERROR", "Error accessing file: " + e.getMessage());
            }
        }
    };
	protected boolean isRecording;
	protected MediaRecorder mMediaRecorder;
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview);

        // Create an instance of Camera
        mCamera = getCameraInstance(getCameraId());

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        
        if (preview == null) Log.e("nigga", "piece of shitt");
        preview.addView(mPreview);
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    mCamera.takePicture(null, null, mPicture);
                }
            }
        );
        

        // Add a listener to the Capture button
        final Button captureButton2 = (Button) findViewById(R.id.button_capture2);
        captureButton.setOnClickListener(
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
            	 
                 if (CameraActivity.this.isRecording) {
                     // stop recording and release camera
                	 CameraActivity.this.mMediaRecorder.stop();  // stop the recording
                     releaseMediaRecorder(); // release the MediaRecorder object
                     mCamera.lock();         // take camera access back from MediaRecorder

                     // inform the user that recording has stopped
                     setCaptureButtonText("Capture");
                     isRecording = false;
                 } else {
                     // initialize video camera
                     if (prepareVideoRecorder()) {
                         // Camera is available and unlocked, MediaRecorder is prepared,
                         // now you can start recording
                         mMediaRecorder.start();

                         // inform the user that recording has started
                         setCaptureButtonText("Stop");
                         isRecording = true;
                     } else {
                         // prepare didn't work, release the camera
                         releaseMediaRecorder();
                         // inform user
                     }
                 }
             }

			private void setCaptureButtonText(String string) {
				captureButton2.setText(string);
				
			}
         }
     );
     mEffectsRecorder = new EffectsRecorder(this);

        // TODO: Confirm none of the foll need to go to initializeEffectsRecording()
        // and none of these change even when the preview is not refreshed.
     mEffectsRecorder.setCamera(mCamera);
     //mEffectsRecorder.setCameraFacing(info.facing);
     mEffectsRecorder.setProfile(mProfile);
     mEffectsRecorder.setEffectsListener(this);
     mEffectsRecorder.setOnInfoListener(this);
     mEffectsRecorder.setOnErrorListener(this);
  // See android.hardware.Camera.Parameters.setRotation for
     // documentation.
     int rotation = 0;
     

     mEffectsRecorder.setPreviewDisplay(
    		 mPreview.getHolder(),
    		 mPreview.getWidth(),
             mPreview.getHeight());
     
     //leaving null for right now
     mEffectsRecorder.setEffect(2, EffectsRecorder.EFFECT_GF_BIG_MOUTH);
     mEffectsRecorder.setProfile(CamcorderProfile.get( CamcorderProfile.QUALITY_720P) );
     mEffectsRecorder.startPreview();
     
}
    
    
    
    
    public int getCameraId() { 
        Log.d("ID", "getCameraId()");
        int cameraId = -1;
        // Search for the back facing camera (or any camera)
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK || numberOfCameras == 1) {
                Log.d("ID", "CameraInfo.CAMERA_FACING_BACK = " 
                      + (info.facing == CameraInfo.CAMERA_FACING_BACK));
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    public static Camera getCameraInstance(int cameraId){
    	Log.d("ID", "getCameraInstance("+cameraId+")");
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
            Camera.Parameters cp = c.getParameters();
            Log.d("ID", "getCameraInstance("+cameraId+"): Camera.Parameters = " 
            + cp.flatten());
        } catch (Exception e) {
            Log.d("ID", "Camera.open("+cameraId+") exception="+e);
        }
        Log.d("ID", "getCameraInstance("+cameraId+") = "+c);
        return c; // returns null if camera is unavailable
    }
    
    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();
        
        // Step 1: Unlock and set camera to MediaRecorder
        if(mCamera == null){
        	
        }
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("Exception", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("EXception", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    
	@Override
	public void onEffectsUpdate(int effectId, int effectMsg) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onEffectsError(Exception exception, String filePath) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		
	}
}