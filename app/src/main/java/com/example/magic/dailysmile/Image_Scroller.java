/**
 * Daily Smile App
 * Authors: Magic Tan & Joel Aro
 * Version: 1.0
 */

package com.example.magic.dailysmile;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

    private GestureDetectorCompat gDetector;
    private ImageView image;
    private TextView imageTitle;
    private static int currentImage = 0;

    // Link to server location where images information are stored
    private static String baseurl   = "http://104.131.67.54/DailySmile/";
    // The directory where all files related to Daily Smile will be saved
    private static File datadir     = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/DailySmile");
    private static File dataloc     = new File(datadir + "/data.xml");

    private static int PARSE_DATA = 0;

    public static   List<XMLData> data;
    private static  List<Bitmap> images;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image__scroller);

        // TODO: Create addViews method
        images = new ArrayList<Bitmap>();

        // Download data.xml from server to device
        new downloadData().execute("data.xml");

        findViews();

        // Loads the font from the assets folder and assigns it to the title
        Typeface tf = Typeface.createFromAsset(getAssets(), "crayon kids.ttf");
        imageTitle.setTypeface(tf);

        this.gDetector = new GestureDetectorCompat(this, this);
        gDetector.setOnDoubleTapListener(this);
    }

    public void findViews() {
        image = (ImageView) findViewById(R.id.mainImage);
        imageTitle  = (TextView) findViewById(R.id.imageTitle);
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

            new downloadImage().execute(data.get(currentImage).getLink());
            // Downloads each image
            imageTitle.setText(data.get(currentImage).getTitle());
        }
    }

    private class downloadImage extends AsyncTask<String, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(Image_Scroller.this, R.style.progressStyle);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Downloading...");
            progressDialog.show();
            //progressDialog.show(Image_Scroller.this, null, "Downloading...");
        }

        @Override
        protected Bitmap doInBackground(String... params) {
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

                return bMap;
            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);

            progressDialog.dismiss();

            // Adds drawable to the images list, app will be able to iterate through the list
            images.add(bm);
            // Initial image when user opens app will be this
            image.setImageBitmap(roundCornerImage(bm, 20));
            imageTitle.setText(data.get(currentImage).getTitle());
        }

        // Loads the image with a rounded border
        public Bitmap roundCornerImage(Bitmap src, float round) {
            Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            Canvas canvas = new Canvas(result);
            canvas.drawARGB(0, 0, 0, 0);

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setShader(shader);

            final Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
            final RectF rectF = new RectF(rect);

            canvas.drawRoundRect(rectF, round, round, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(src, rect, rect, paint);

            return result;
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
                this.finish();
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
        currentImage++;
        // TODO: decrease currentImage on left swipes
        if(currentImage > data.size() - 1)
            currentImage = 0;

        /*
        Will download the image and then assign the title on the same thread. Instead of loading
        the image and title in two separate threads
         */
        new downloadImage().execute(data.get(currentImage).getLink());

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }
}
