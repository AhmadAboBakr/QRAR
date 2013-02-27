package com.example.test;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.google.zxing.Result;
public class PicGraper {
	private  Bitmap image;
	private Mat mat;
	private  List<Point> points;
	private String result;
	public Bitmap getBitmap(){
		return image;
	}
	public Vector<Point> getPoints(){
		return (Vector<Point>)points;
	}
	public Mat getMat(){
		return mat;
	}
	public String  getText(){
		return result;
	}

	public PicGraper(Result barcodeData){
		mat=new Mat();
		result = barcodeData.getText();
		if(!result.contains("::::"))return;
		if(result.split("::::")[0].trim().toLowerCase().contains("AugmentedQR".toLowerCase())){
//			image=loadBitmap(result.split("::::")[1]);
//			if(image!=null){
//				Bitmap bmp32 = image.copy(Bitmap.Config.ARGB_8888, true);
//				Utils.bitmapToMat(bmp32,mat);		
				mat = Highgui.imread("/sdcard/"+ result.split("::::")[1].trim());
			System.out.println("width : "+mat.width());
//			}
//			else mat =null;
		}
		else {
			image =null;
			mat=null;
		}
		points = new Vector<Point>();
		Vector<Integer> x = new Vector<Integer>();
		
		x.add(new Integer((int)
				Math.min( Math.min(
						barcodeData.getResultPoints()[0].getX(),
						barcodeData.getResultPoints()[1].getX()
						),
						barcodeData.getResultPoints()[2].getX()
						)));
		x.add(new Integer((int)
				Math.min( Math.min(
						barcodeData.getResultPoints()[0].getY(),
						barcodeData.getResultPoints()[1].getY()
						),
						barcodeData.getResultPoints()[2].getY()
						)));

		x.add(new Integer((int)
				Math.max( Math.max(
						barcodeData.getResultPoints()[0].getX(),
						barcodeData.getResultPoints()[1].getX()
						),
						barcodeData.getResultPoints()[2].getX()
						)));		

		x.add(new Integer((int)
				Math.max( Math.max(
						barcodeData.getResultPoints()[0].getY(),
						barcodeData.getResultPoints()[1].getY()
						),
						barcodeData.getResultPoints()[2].getY()
						)));		
		int r =(int) Math.sqrt(
				Math.pow(x.elementAt(0)+x.elementAt(2),2) 
						+
				Math.pow(x.elementAt(1)+x.elementAt(3),2) 
				);
		System.out.println("r : " +r);
		points.add(new Point(x.elementAt(0).intValue(),x.elementAt(1).intValue()));
		points.add(new Point(x.elementAt(2).intValue(),x.elementAt(3).intValue()));
		double theta;
	}
	public static Bitmap loadBitmap(String src) {
		Bitmap bitmap =null;
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			bitmap = BitmapFactory.decodeStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
}
