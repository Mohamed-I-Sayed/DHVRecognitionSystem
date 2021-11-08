package com.qalex.veinrec;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by m7mD on 11/7/2021.
 */

public class Matching {

    Context context;

    Cursor user;
    DataBase db;
    List<byte[]> byteList;
    List<Integer> id;

    Mat blurred;
    Mat HistImage;
    Bitmap resultBitmap;

    List<MatOfDMatch> matchesList;
    List<Integer> Max_Good;
    DescriptorMatcher matcher;
    float threshold = 0.8f;

    public Matching(Context context) {

        this.context = context;
        db = new DataBase(context);
    }

    public Bitmap detectFeatrures(Bitmap bitmap) {


        Mat rgba = new Mat();

        Utils.bitmapToMat(bitmap, rgba);

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

        Core.addWeighted(blurred, 4, src2, -4, 100, dst);


        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();

        ORB detector = ORB.create( 150, 1.2f,  8,  31,  0,  2,  ORB.HARRIS_SCORE, 31,  20);

        detector.detectAndCompute(dst,new Mat(),keypoints1,descriptors1);


        verifyUser(descriptors1);

        Imgproc.medianBlur(dst, blurred, 7);

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

    private void verifyUser(Mat descriptors1) {


        matchesList = new ArrayList<MatOfDMatch>();
        Max_Good = new ArrayList<>();
        int count = 0;
        int max = 0;
//

        for (int k = 0 ; k < byteList.size();k++){


            Mat matDB = new Mat((byteList.get(k).length / 32), 32, 0);

            matDB.put(0, 0, byteList.get(k));

            matcher = DescriptorMatcher.create(4);
            matcher.knnMatch(descriptors1,matDB,matchesList,2);

            count = 0;
            for(int i=0; i<matchesList.size(); i++) {
                if(matchesList.get(i).rows() > 1) {
                    DMatch[] matches = matchesList.get(i).toArray();
                    if(matches[0].distance < threshold * matches[1].distance) {
                        count += 1;
                    }
                }
            }

            Max_Good.add(count);


        }

        max = Collections.max(Max_Good);
        Log.w("Max", " " + max);

        if (max >= 17) {
            user = db.GetUserID(id.get(Max_Good.indexOf(max)));
            Log.w("USERID", " " + id.get(Max_Good.indexOf(max)));
            final Cursor c = db.GetUserName(user.getInt(3));
            Log.w("USERNAME", " " + c.getString(0));

            Toast.makeText(context,"Verification Success"+"\n"+ user.getString(2) + " of " +c.getString(0), Toast.LENGTH_SHORT).show();


         }



    }

}
