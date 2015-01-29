package com.gnr.square.square;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends ActionBarActivity {
    public static final String MY_PREFS_NAME = "gnrcredentials";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        loadSquareWebsite();


        Button changeViewBtn = (Button) findViewById(R.id.changeviewbtn);
        changeViewBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                showSettings(view);
            }

        });

        Button checkInBtn = (Button) findViewById(R.id.checkinbtn);
        checkInBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                String restoredText = prefs.getString("name", null);

                if (restoredText != null) {
                    String name = prefs.getString("name", "");
                    String email = prefs.getString("email", "");

                    if (name.length()>0 && email.length()>0) {
                        Toast.makeText(getApplicationContext(), "CHECKING IN!",
                                Toast.LENGTH_LONG).show();

                        Checkin cin= new Checkin();
                        cin.execute(name, email);
                    }else{
                        Toast.makeText(getApplicationContext(), "PLEASE SET YOUR NAME AND EMAIL FIRST!",
                                Toast.LENGTH_LONG).show();
                        showSettings(view);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "PLEASE SET YOUR NAME AND EMAIL FIRST!",
                            Toast.LENGTH_LONG).show();
                    showSettings(view);
                }

            }
        });
    }

    private void showSettings(View view) {
        Intent i = new Intent(getApplicationContext(), Settings.class);
        view.getContext().startActivity(i);
    }

    private void loadSquareWebsite() {
        WebView webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(getString(R.string.squareUrl));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    //Private AsyncTask class for checking in
    private class Checkin extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String response= postData(params[0], params[1]);
            String value= null;
            try {
                JSONObject jObj= new JSONObject(response);
                value= (String)jObj.get("response");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("response", response);

            return value;
        }

        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }


        public String postData(String name, String email) {
            String jresponse= null;

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://www.green-red.com/square/checkin");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name",
                        name));
                nameValuePairs.add(new BasicNameValuePair("email",
                        email));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                String json_string = EntityUtils.toString(response.getEntity());
                jresponse= json_string;

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return jresponse;
        }
    }
}
