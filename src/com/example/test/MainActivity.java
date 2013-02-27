package com.example.test;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;

public class MainActivity extends Activity implements CvCameraViewListener {
	private static final String TAG = "OCVSample::Activity";
	private CameraBridgeViewBase	mOpenCvCameraView;
	private MenuItem            	mLarger = null;
	private MenuItem            	mSmaller= null;


	private BaseLoaderCallback	mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public MainActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
		System.out.println("java camera");
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause()
	{
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mSmaller = menu.add("scale down");
		mLarger= menu.add("scale up");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String toastMesage = new String();
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mLarger) {
			mOpenCvCameraView.enableFpsMeter();
		}
		else if(item == mSmaller){
			mOpenCvCameraView.disableFpsMeter();

		}
		return true;
	}

	public void onCameraViewStarted(int width, int height) {
	}

	public void onCameraViewStopped() {
	}

	@SuppressLint("UseValueOf")
	public Mat onCameraFrame(Mat inputFrame) {
		Mat result=new Mat();
		//		Imgproc.resize(inputFrame, result, new Size(inputFrame.width()*2,inputFrame.height()*2));
		Imgproc.cvtColor(inputFrame, result, Imgproc.COLOR_BGR2RGB);
		Bitmap bmp = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(result, bmp);
		LuminanceSource source = new RGBLuminanceSource(bmp);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Reader reader = new MultiFormatReader();
		try {
			PicGraper graber = new PicGraper(reader.decode(bitmap));
			
			android.graphics.Point start= graber.getPoints().get(0);
			android.graphics.Point end= graber.getPoints().get(1);
			int temp;
			if(start.x>end.x){temp=start.x;start.x=end.x;end.x=temp;}
			if(start.y>end.y){temp=start.y;start.y=end.y;end.y=temp;}
			
			int width = end.x - start.x;
			int height= end.y - start.y;
			System.out.println(width+ " : " + height);
			//overLay(main, overlay, start, end);
			Mat aux2= graber.getMat();
			Mat aux=new Mat();
			//System.out.println(aux.width()	);
			if(!aux2.empty()){	
				Imgproc.resize(aux2, aux, new Size(width,height));
				System.out.println("/sdcard/"+graber.getText());
				Rect ROI = new Rect(start.x-10, start.y-10, width, height);
				System.out.println("/sdc" +"ard/"+graber.getText());
				Core.addWeighted(result.submat(ROI),0.2, aux,.8,0, result.submat(ROI));
			}
			
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ChecksumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return result;
	}
	public Mat overLay(Bitmap main,Bitmap overlay,android.graphics.Point start, android.graphics.Point end) {
		Mat mat,mat2;
		mat = new Mat();
		mat2=new Mat();
		Utils.bitmapToMat(overlay, mat);
		Imgproc.resize(mat, mat2, new Size(Math.abs(start.x-end.x), Math.abs(start.y-end.y)));
		Utils.matToBitmap(mat2,overlay);
		for(int i=start.x;i<end.x;++i)
			for(int j=start.y;j<end.y;++j){
				main.setPixel(i, j, overlay.getPixel(i-start.x,j-start.y ));
			}		
		mat = new Mat();
		Utils.bitmapToMat(main, mat);
		return mat;
	}
}