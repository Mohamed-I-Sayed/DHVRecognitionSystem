package com.qalex.veinrec;



import android.database.Cursor;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.opencv.ImageProcessor;


import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandlerMultiSurface;
import com.serenegiant.utils.CpuMonitor;
import com.serenegiant.utils.ViewAnimationHelper;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.opencv.core.Core.convertScaleAbs;

/**
 * @author Mohamed I. Sayed, Mohamed Taha, and Hala H. Zayed 
 * @date 01/10/2021
 * @version 1.0
 */

public final class MainActivity extends BaseActivity
        implements CameraDialog.CameraDialogParent {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "MainActivity";

    private static final boolean USE_SURFACE_ENCODER = false;


    private static final int PREVIEW_WIDTH = 320;

    private static final int PREVIEW_HEIGHT = 240;

    private static final int PREVIEW_MODE = 0;

    protected static final int SETTINGS_HIDE_DELAY_MS = 2500;

 
    private USBMonitor mUSBMonitor;
    private UVCCameraHandlerMultiSurface mCameraHandler;
    private UVCCameraTextureView mUVCCameraView;
    protected SurfaceView mResultView;
    private ToggleButton mCameraButton;

    private ImageButton mCaptureButton;
    private View mBrightnessButton, mContrastButton;
    private View mResetButton;
    private View mToolsLayout, mValueLayout;
    private SeekBar mSettingSeekbar;

    protected ImageProcessor mImageProcessor;
    private TextView mCpuLoadTv;
    private TextView mFpsTv;
    private final CpuMonitor cpuMonitor = new CpuMonitor();

    DataBase db;
    String DB_PATH;
    Bitmap resultBitmap;
    Mat blurred;
    Mat HistImage;

    Cursor all, test, user;
    List<byte[]> byteList;
    List<Integer> id;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate:");
        setContentView(R.layout.activity_main);
        mCameraButton = (ToggleButton) findViewById(R.id.camera_button);
        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);


        mUVCCameraView = (UVCCameraTextureView) findViewById(R.id.camera_view);
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);

        mResultView = (SurfaceView) findViewById(R.id.result_view);
        mResultView.setOnClickListener(mOnClickListener);


        mResetButton = findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(mOnClickListener);
        mSettingSeekbar = (SeekBar) findViewById(R.id.setting_seekbar);
        mSettingSeekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);


        mValueLayout = findViewById(R.id.value_layout);
        mValueLayout.setVisibility(View.INVISIBLE);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandlerMultiSurface.createHandler(this, mUVCCameraView,
                1, PREVIEW_WIDTH, PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_YUYV);


        db = new DataBase(this);

        all = db.GetAll();
        byteList = new ArrayList<>();
        id = new ArrayList<>();
        if (all.moveToFirst()) {

            do {
                byteList.add(all.getBlob(1));
                id.add(all.getInt(0));
            } while (all.moveToNext());

            db.close();

        }

        //test = db.GetAllTest();

        //DisplayContact(all);
    }

    public void DisplayContact(Cursor c) {
        Toast.makeText(this,
                "id: " + c.getString(0) + "\n",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.v(TAG, "onStart:");
        mUSBMonitor.register();
        if (mUVCCameraView != null)
            mUVCCameraView.onResume();

    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
        mCameraHandler.close();
        setCameraButton(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
        mCameraButton = null;
        //mCaptureButton = null;
        super.onDestroy();
    }

 
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                
                case R.id.reset_button:
                    resetSettings();
                    break;
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(
                final CompoundButton compoundButton, final boolean isChecked) {

            switch (compoundButton.getId()) {
                case R.id.camera_button:
                    if (isChecked && !mCameraHandler.isOpened()) {
                        CameraDialog.showDialog(MainActivity.this);
                    } else {
                        //stopPreview();
                    }
                    break;
            }
        }
    };

   
    private void setCameraButton(final boolean isOn) {
        if (DEBUG) Log.v(TAG, "setCameraButton:isOn=" + isOn);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCameraButton != null) {
                    try {
                        mCameraButton.setOnCheckedChangeListener(null);
                        mCameraButton.setChecked(isOn);
                    } finally {
                        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
                    }
                }
           
            }
        }, 0);
        updateItems();
    }

    private int mPreviewSurfaceId;

    private void startPreview() {
        if (DEBUG) Log.v(TAG, "startPreview:");
       
        mCameraHandler.startPreview();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                    if (st != null) {
                        final Surface surface = new Surface(st);
                        mPreviewSurfaceId = surface.hashCode();
                        mCameraHandler.addSurface(mPreviewSurfaceId, surface, false);
                   }
                    startImageProcessor(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                } catch (final Exception e) {
                    Log.w(TAG, e);
                }
            }
        });
        updateItems();
    }


    private final OnDeviceConnectListener mOnDeviceConnectListener
            = new OnDeviceConnectListener() {

        @Override
        public void onAttach(final UsbDevice device) {

        }

        @Override
        public void onConnect(final UsbDevice device,
                              final UsbControlBlock ctrlBlock, final boolean createNew) {

            if (DEBUG) Log.v(TAG, "onConnect:");
            mCameraHandler.open(ctrlBlock);
            startPreview();
            updateItems();
        }

        @Override
        public void onDisconnect(final UsbDevice device,
                                 final UsbControlBlock ctrlBlock) {

            if (DEBUG) Log.v(TAG, "onDisconnect:");
            if (mCameraHandler != null) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        //stopPreview();
                    }
                }, 0);
                updateItems();
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            //Toast.makeText(MainActivity.this,
              //      "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            setCameraButton(false);
        }
    };

	
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (DEBUG) Log.v(TAG, "onDialogResult:canceled=" + canceled);
        if (canceled) {
            setCameraButton(false);
        }
    }

    //================================================================================
    private boolean isActive() {
        return mCameraHandler != null && mCameraHandler.isOpened();
    }

    private boolean checkSupportFlag(final int flag) {
        return mCameraHandler != null && mCameraHandler.checkSupportFlag(flag);
    }

    private int getValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.getValue(flag) : 0;
    }

    private int setValue(final int flag, final int value) {
        return mCameraHandler != null ? mCameraHandler.setValue(flag, value) : 0;
    }

    private int resetValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.resetValue(flag) : 0;
    }

    private void updateItems() {
        runOnUiThread(mUpdateItemsOnUITask, 100);
    }

    private final Runnable mUpdateItemsOnUITask = new Runnable() {
        @Override
        public void run() {
            if (isFinishing()) return;
            final int visible_active = isActive() ? View.VISIBLE : View.INVISIBLE;
          
        }
    };

    private int mSettingMode = -1;


    private final void showSettings(final int mode) {
        if (DEBUG) Log.v(TAG, String.format("showSettings:%08x", mode));
        hideSetting(false);
        if (isActive()) {
            switch (mode) {
                case UVCCamera.PU_BRIGHTNESS:
                case UVCCamera.PU_CONTRAST:
                    mSettingMode = mode;
                    mSettingSeekbar.setProgress(getValue(mode));
                    ViewAnimationHelper.fadeIn(mValueLayout, -1, 0, mViewAnimationListener);
                    break;
            }
        }
    }

    private void resetSettings() {
        if (isActive()) {
            switch (mSettingMode) {
                case UVCCamera.PU_BRIGHTNESS:
                case UVCCamera.PU_CONTRAST:
                    mSettingSeekbar.setProgress(resetValue(mSettingMode));
                    break;
            }
        }
        mSettingMode = -1;
        ViewAnimationHelper.fadeOut(mValueLayout, -1, 0, mViewAnimationListener);
    }

   
    protected final void hideSetting(final boolean fadeOut) {
        removeFromUiThread(mSettingHideTask);
        if (fadeOut) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewAnimationHelper.fadeOut(mValueLayout, -1, 0, mViewAnimationListener);
                }
            }, 0);
        } else {
            try {
                mValueLayout.setVisibility(View.GONE);
            } catch (final Exception e) {
                // ignore
            }
            mSettingMode = -1;
        }
    }

    protected final Runnable mSettingHideTask = new Runnable() {
        @Override
        public void run() {
            hideSetting(true);
        }
    };

    
    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener
            = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(final SeekBar seekBar,
                                      final int progress, final boolean fromUser) {

            if (fromUser) {
                runOnUiThread(mSettingHideTask, SETTINGS_HIDE_DELAY_MS);
            }
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            runOnUiThread(mSettingHideTask, SETTINGS_HIDE_DELAY_MS);
            if (isActive() && checkSupportFlag(mSettingMode)) {
                switch (mSettingMode) {
                    case UVCCamera.PU_BRIGHTNESS:
                    case UVCCamera.PU_CONTRAST:
                        setValue(mSettingMode, seekBar.getProgress());
                        break;
                }
            }  
        }
    };

    private final ViewAnimationHelper.ViewAnimationListener
            mViewAnimationListener = new ViewAnimationHelper.ViewAnimationListener() {
        @Override
        public void onAnimationStart(@NonNull final Animator animator,
                                     @NonNull final View target, final int animationType) {

        }

        @Override
        public void onAnimationEnd(@NonNull final Animator animator,
                                   @NonNull final View target, final int animationType) {

            final int id = target.getId();
            switch (animationType) {
                case ViewAnimationHelper.ANIMATION_FADE_IN:
                case ViewAnimationHelper.ANIMATION_FADE_OUT: {
                    final boolean fadeIn = animationType == ViewAnimationHelper.ANIMATION_FADE_IN;
                    if (id == R.id.value_layout) {
                        if (fadeIn) {
                            runOnUiThread(mSettingHideTask, SETTINGS_HIDE_DELAY_MS);
                        } else {
                            mValueLayout.setVisibility(View.GONE);
                            mSettingMode = -1;
                        }
                    } else if (!fadeIn) {
                    }
                    break;
                }
            }
        }

        @Override
        public void onAnimationCancel(@NonNull final Animator animator,
                                      @NonNull final View target, final int animationType) {

        }
    };

    
    private volatile boolean mIsRunning;
    private int mImageProcessorSurfaceId;

	
    protected void startImageProcessor(final int processing_width, final int processing_height) {
        if (DEBUG) Log.v(TAG, "startImageProcessor:");
        mIsRunning = true;
        if (mImageProcessor == null) {
            mImageProcessor = new ImageProcessor(PREVIEW_WIDTH, PREVIEW_HEIGHT,	// src size
                    new MyImageProcessorCallback(processing_width, processing_height));	// processing size
            mImageProcessor.start(processing_width, processing_height);	// processing size
            final Surface surface = mImageProcessor.getSurface();
            mImageProcessorSurfaceId = surface != null ? surface.hashCode() : 0;
            if (mImageProcessorSurfaceId != 0) {
                mCameraHandler.addSurface(mImageProcessorSurfaceId, surface, false);
            }
        }
    }

    /**
     * stop image processing
     */
    protected void stopImageProcessor() {
        if (DEBUG) Log.v(TAG, "stopImageProcessor:");
        if (mImageProcessorSurfaceId != 0) {
          
		  
            mImageProcessorSurfaceId = 0;
        }
        if (mImageProcessor != null) {
            mImageProcessor.release();
            mImageProcessor = null;
        }
    }

    /**
     * callback listener from `ImageProcessor`
     */
    protected class MyImageProcessorCallback implements ImageProcessor.ImageProcessorCallback {
        private final int width, height;
        private final Matrix matrix = new Matrix();
        private Bitmap mFrame;
        int count = 0;
        protected MyImageProcessorCallback(
                final int processing_width, final int processing_height) {

            width = processing_width;
            height = processing_height;

			

        }

        @Override
        public void onFrame(final ByteBuffer frame) {

            if (mResultView != null) {
                final SurfaceHolder holder = mResultView.getHolder();
                if ((holder == null)
                        || (holder.getSurface() == null)
                        || (frame == null)) return;

						if (mFrame == null) {
                    mFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final float scaleX = mResultView.getWidth() / (float) width;
                    final float scaleY = mResultView.getHeight() / (float) height;
                    matrix.reset();
                    matrix.postScale(scaleX, scaleY);
                }

                try {

				
                    frame.clear();
                    mFrame.copyPixelsFromBuffer(frame);
					
                    mFrame = detectFeatures(mFrame);

					
                    final Canvas canvas = holder.lockCanvas();
                    if (canvas != null) {
                        try {
                            canvas.drawBitmap(mFrame, matrix, null);
                        } catch (final Exception e) {
                            Log.w(TAG, e);
                        } finally {
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                } catch (final Exception e) {
                    Log.w(TAG, e);
                }
            }
        }

        @Override
        public void onResult(final int type, final float[] result) {
            // do something
        }

        private Bitmap detectFeatures(Bitmap bitmap) {
           

            Mat rgba = new Mat();

            Utils.bitmapToMat(bitmap, rgba);

            if (rgba.empty())
                Log.w("Mat Error", "nullllll");

          

////////////////////////////////// Blurring ....................
            //Imgproc.GaussianBlur( rgba, blurred, new Size(5,5), 0, 0 );
            blurred = new Mat();

            Mat gray = new Mat();
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY);

            CLAHE clahe = Imgproc.createCLAHE(4, new Size(8, 8));
                 
            HistImage = new Mat();
        
            clahe.apply(gray, HistImage);
            Imgproc.medianBlur(HistImage, blurred, 5);
            Mat dst = new Mat();
            Mat src2 = new Mat();
            Imgproc.medianBlur(blurred, src2, 33);
			
            //Adding two images
            Core.addWeighted(blurred, 4, src2, -4, 100, dst);
            //Mat eq = new Mat();

            MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
            Mat descriptors1 = new Mat();
            //Definition of ORB keypoint detector and descriptor extractors
            ORB detector = ORB.create( 150, 1.2f,  8,  31,  0,  2,  ORB.HARRIS_SCORE, 31,  20);

            detector.detectAndCompute(dst,new Mat(),keypoints1,descriptors1);

            


            //Imgproc.medianBlur(dst, blurred, 7);
            resultBitmap = Bitmap.createBitmap(blurred.cols(), blurred.rows(), Bitmap.Config.ARGB_8888);
           
            Utils.matToBitmap(blurred, resultBitmap);


            gray.release();
            blurred.release();
            dst.release();
            src2.release();
            HistImage.release();
            descriptors1.release();
            rgba.release();
//

            return resultBitmap;
        }
        public void Matching(Mat descriptors1) {
            List<MatOfDMatch> matchesList = new ArrayList<MatOfDMatch>();;//= new 
            List<Integer> Max_Good = new ArrayList<>();

            float threshold = 0.8f;
            int count = 0;
            int max = 0;

            for (int k = 0 ; k < byteList.size();k++){

                Mat matDB = new Mat((byteList.get(k).length / 32), 32, 0);

                matDB.put(0, 0, byteList.get(k));
                DescriptorMatcher matcher;


                matcher = DescriptorMatcher.create(4);

                matcher.knnMatch(descriptors1,matDB,matchesList,2);

                count = 0;
                for(int i=0; i<matchesList.size(); i++) {
                    if(matchesList.get(i).rows() > 1) {
                        DMatch[] matches = matchesList.get(i).toArray();
                        if(matches[0].distance < threshold * matches[1].distance) {
                            count+=1;
                        }
                    }
                }

                Max_Good.add(count);

             
            }
            max = Collections.max(Max_Good);
            Log.w("Max", " " + max);
            if (max >= 17) {
                user = db.GetUserID(id.get(Max_Good.indexOf(max)));
                final Cursor c = db.GetUserName(user.getInt(3));
                Log.w("USERNAME", " " + c.getString(0));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(),"Identification Success"+"\n"+ c.getString(0) +" "+ user.getString(2) +" hand", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        public void displayMessage(final String name, final String hand){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(),"Identification Success"+"\n"+ name +" "+ hand +" hand", Toast.LENGTH_SHORT).show();
                }
            });


        }

    }

}

//cd /d  g:
//cd G:\andorid\android-sdk\platform-tools
