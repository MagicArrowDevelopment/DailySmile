/**
 * Daily Smile App
 * Authors: Magic Tan & Joel Aro
 * Version: 1.0
 */

package com.example.magic.dailysmile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;

import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Image_Scroller extends ActionBarActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    GestureDetectorCompat gDetector;
    ImageView image1;
    ImageView image2;
    TextView imageTitle;
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
    private static List<Drawable> images;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image__scroller);

        // TODO: Create addViews method
        images = new ArrayList<Drawable>();

        // Download data.xml from server to device
        new downloadData().execute("data.xml");



        // TODO: Move all of this to its own method
        //=========================================================================================
        image1 = (ImageView) findViewById(R.id.mainImage);
        image2 = (ImageView) findViewById(R.id.adImage);
        imageTitle = (TextView) findViewById(R.id.imageTitle);

        imgs = new int[]{R.drawable.ice, R.drawable.manga1, R.drawable.music};

        //image1.setImageResource(imgs[i]);
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

                // Open connection to server
                URL url = new URL(baseurl + params[0]);
                URLConnection cxn = url.openConnection();
                cxn.connect();

                File file = new File(datadir, "/" + params[0]);

                in  = new BufferedInputStream(url.openStream());
                out = new FileOutputStream(file);

                // Saves content of the serverfile to the device byte by byte
                byte data[] = new byte[1024];
                int count;
                while((count = in.read(data)) != -1) {
                    out.write(data, 0, count);
                }

                out.flush();
                out.close();
                in.close();

                // Parses data so that the information can be read by the app
                parseData(PARSE_DATA);
            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // Downloads each image
            for(XMLData x : data) {
                new downloadImage().execute(x.getLink());
            }
            imageTitle.setText(data.get(data.size() - 1).getTitle());
        }
    }

    private class downloadImage extends AsyncTask<String, Integer, Drawable> {
        @Override
        protected Drawable doInBackground(String... params) {
            URL url;
            BufferedOutputStream out;
            InputStream in;
            BufferedInputStream buf;

            try {
                url = new URL(params[0]);
                in = url.openStream();

                buf = new BufferedInputStream(in);
                Bitmap bMap = BitmapFactory.decodeStream(buf);
                if(in != null) {
                    in.close();
                }
                if(buf != null) {
                    buf.close();
                }

                return new BitmapDrawable(bMap);
            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);

            // Adds drawable to the images list, app will be able to iterate through the list
            images.add(drawable);
            // Initial image when user opens app will be this
            image1.setBackgroundDrawable(images.get(images.size() - 1));
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

        //this will be the different options menu
        // TODO: Optional menu selections - customizable settings, etc.
        boolean handle = false;
        switch (id)
        { //this statement will have different actions depending on the selection.
            case R.id.action_exit: //this will exit the app.
                finish();
                handle = true;
                break;
            default:
                handle = super.onOptionsItemSelected(item);
        }

        return handle;
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
        /*
        if (i == 2) {
            i = 0;
        }
        else {
            i += 1;
        }*/

        if(i > data.size() - 1) {
            i = 0;
        }

        //image1.setImageResource(imgs[i]);
        image1.setBackgroundDrawable(images.get(i));
        imageTitle.setText(data.get(i).getTitle());

        i++;

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }
}
