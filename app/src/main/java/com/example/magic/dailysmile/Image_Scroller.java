/**
 * Daily Smile App
 * Authors: Magic Tan & Joel Aro
 * Version: 1.0
 */

package com.example.magic.dailysmile;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Image_Scroller extends ActionBarActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    GestureDetectorCompat gDetector;
    ImageView image1;
    ImageView image2;
    int[] imgs;
    int i = 0;

    // Link to server location where images information are stored
    private static String baseurl   = "http://104.131.67.54/DailySmile/";
    // The directory where all files related to Daily Smile will be saved
    private static File datadir     = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/DailySmile");
    private static File dataloc     = new File(datadir + "/data.xml");

    private static int PARSE_DATA = 0;

    public static List<XMLData> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image__scroller);

        // Download data.xml from server to device
        new downloadData().execute("data.xml");



        // TODO: Move all of this to its own method
        //=========================================================================================
        image1 = (ImageView) findViewById(R.id.mainImage);
        image2 = (ImageView) findViewById(R.id.adImage);

        imgs = new int[]{R.drawable.ice, R.drawable.manga1, R.drawable.music};

        image1.setImageResource(imgs[i]);
        image2.setImageResource(R.drawable.music);

        this.gDetector = new GestureDetectorCompat(this, this);
        gDetector.setOnDoubleTapListener(this);
        //=========================================================================================
    }

    public class downloadData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            InputStream in      = null;
            OutputStream out    = null;

            try {
                // Creates the app directory if it does not already exist
                if(!datadir.exists()) {
                    datadir.mkdirs();
                }

                URL url = new URL(baseurl + params[0]);
                URLConnection cxn = url.openConnection();
                cxn.connect();

                int lengthOfFile = cxn.getContentLength();
                File file = new File(datadir, "/" + params[0]);

                in  = new BufferedInputStream(url.openStream());
                out = new FileOutputStream(file);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while((count = in.read(data)) != -1) {
                    total += count;
                    out.write(data, 0, count);
                }

                out.flush();
                out.close();
                in.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            parseData(PARSE_DATA);


        }
    }

    public void parseData(int type) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp =  spf.newSAXParser();

            if (type == PARSE_DATA) {
                XMLHandler handler = new XMLHandler();
                InputStream ins = new FileInputStream(dataloc);
                Reader r = new InputStreamReader(ins, "UTF-8");

                InputSource is = new InputSource(r);
                is.setEncoding("UTF-8");

                sp.parse(is, handler);

                data = handler.getData();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is gift.
        getMenuInflater().inflate(R.menu.menu_image__scroller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
       /*if (i == 2) {
            i = 0;
        }
        else {
            i += 1;
        }
        image1.setImageResource(imgs[i]);
        */
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (i == 2) {
            i = 0;
        }
        else {
            i += 1;
        }
        image1.setImageResource(imgs[i]);

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }
}
