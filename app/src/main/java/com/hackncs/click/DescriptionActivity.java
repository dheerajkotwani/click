package com.hackncs.click;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class DescriptionActivity extends AppCompatActivity {

    private TextView title, faculty, posted_on;
    private WebView description;
    private int index = 0;
    private String linkstr = "";
    public Context context;
    private Notice notice;
    private String TOKEN;
    private String USER_NAME;
    private Menu menu;
    ProgressDialog P;
    Button Dbutton;

    private void init() {
        title = (TextView) findViewById(R.id.nTitle);
        faculty = (TextView) findViewById(R.id.nFaculty);
        description = (WebView) findViewById(R.id.nDescription);
        posted_on = (TextView) findViewById(R.id.ndate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new FontAwesomeModule());
        context = this;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        TOKEN = sp.getString("com.hackncs.click.TOKEN", "");
        USER_NAME = sp.getString("com.hackncs.click.USERNAME", "");

        Intent i = getIntent();
        String action = i.getAction();
        //Log.i("action:",action);
        //Log.i("openen:","opened");
        P = new ProgressDialog(context);


        if (Intent.ACTION_VIEW.equals(action)) {

            P.setMessage("Opening...");
            P.show();
            P.setIndeterminate(false);
            P.setCancelable(true);

            String url = i.getData().toString();
//            Log.d("url", url);
            String noticeid = url.substring(url.indexOf('=') + 1);
            String get_url = Endpoints.notice_by_pk + noticeid;
//            Log.d("Notice Id",noticeid);
//            Log.d("URL",get_url);
            StringRequest newNoticeRequest = new StringRequest(Request.Method.GET, get_url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jo = new JSONObject(response);
//                                Log.i("Json:","notice received");
                                notice = Notice.getNoticeObject(jo);
                                P.dismiss();
                                populateView();
                            } catch (JSONException e) {
//                                Log.i("JsonError:",e.getMessage());
                                e.printStackTrace();

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            Log.i("JsonError:",error.getMessage());
//                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", "token " + TOKEN);
                    params.put("username", USER_NAME);
                    return params;
                }
            };
            RequestQueue rq = Volley.newRequestQueue(this);
            rq.add(newNoticeRequest);
        } else {
            Bundle b = getIntent().getExtras();
            notice = (Notice) b.get("Notice");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        populateView();


    }

    private void populateView() {

        setContentView(R.layout.activity_description);

        Dbutton = (Button) findViewById(R.id.downloadB);

        Dbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Asyn().execute(Endpoints.media + notice.mAttachment_link);
            }
        });
        Dbutton.setVisibility(View.GONE);

        if (notice != null) {
            if (notice.mAttachment) {
                Dbutton.setVisibility(View.VISIBLE);

            } else {
                Dbutton.setVisibility(View.GONE);
            }

            if (notice.mAttachment) {
                index = notice.mAttachment_link.lastIndexOf("/");
                linkstr = notice.mAttachment_link.substring(index + 1);
            }

            init();
            title.setText(notice.mTitle);
            faculty.setText(notice.mPosted_by);
            posted_on.setText(notice.date);
            description.loadData(notice.mNotice_description, "text/html", null);
        }
    }

    private void startShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String share_url = "http://infoconnect.jssaten.ac.in/notice/?notice_id=" + notice.mId;
        Bundle bundle = new Bundle();
        bundle.putString(Intent.EXTRA_TITLE, notice.mTitle);
        bundle.putString(Intent.EXTRA_TEXT, notice.mNotice_description);
        bundle.putString(Intent.EXTRA_TEXT, share_url + "\n\nSent via InfoConnect");
        sendIntent.putExtras(bundle);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notice, menu);

        this.menu = menu;

        menu.getItem(1).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_star)
                .colorRes(R.color.white)
                .actionBarSize());
        menu.getItem(0).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_share_alt)
                .colorRes(R.color.white)
                .actionBarSize());
        try {
//            Log.i("Stared Notices",new OfflineDatabaseHandler(DescriptionActivity.this).getStarredNoticesIds().toString());
            if (new OfflineDatabaseHandler(DescriptionActivity.this).getStarredNoticesIds().contains(notice.mId)) {
                menu.getItem(1).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_star)
                        .colorRes(R.color.golden)
                        .actionBarSize());
            }
        } catch (Exception e) {
//            Log.i("Error",e.toString());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_share) {
            startShareIntent();
        } else if (id == R.id.menu_item_star) {
            if (!new OfflineDatabaseHandler(DescriptionActivity.this).getStarredNoticesIds().contains(notice.mId)) {
                starNotice();
            } else {
                removeStarredNotice();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void removeStarredNotice() {
        String URL = Endpoints.delete_starred_notice;
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, URL + notice.mId + "/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jo;
                        try {
                            jo = new JSONObject(response);
                            menu.getItem(1).setIcon(new IconDrawable(context, FontAwesomeIcons.fa_star)
                                    .colorRes(R.color.white)
                                    .actionBarSize());
                            Toast.makeText(context, jo.getString("message"), Toast.LENGTH_SHORT).show();

                            //removeStarredNotice();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new OfflineDatabaseHandler(context).deleteNotice(notice);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Token " + TOKEN);
                return params;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(stringRequest);
    }

    private void starNotice() {
        String URL = Endpoints.add_starred_notice;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL + notice.mId + "/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jo;
                        try {
                            jo = new JSONObject(response);

                            menu.getItem(1).setIcon(new IconDrawable(context, FontAwesomeIcons.fa_star)
                                    .colorRes(R.color.golden)
                                    .actionBarSize());
                            boolean result = new OfflineDatabaseHandler(DescriptionActivity.this).insertNotice(notice);
//                            Log.i("Stared Notices",new OfflineDatabaseHandler(DescriptionActivity.this).getStarredNoticesIds().toString());
                            Toast.makeText(DescriptionActivity.this, jo.getString("message"), Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "token " + TOKEN);
                return params;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(stringRequest);
        if (notice.mAttachment)
            new Asyn().execute(Endpoints.media + notice.mAttachment_link);
    }

    private class Asyn extends AsyncTask<String, Void, Void> {

        ProgressDialog P = new ProgressDialog(context);

        @Override
        protected void onPreExecute() {
            //verifyStoragePermissions();
            File folder = new File(Environment.getExternalStorageDirectory() + "/InfoConnect");
            boolean status = folder.exists();
            if (!status) {
                status = folder.mkdir();
            }
            P.setMessage("Downloading...");
            P.show();
            P.setIndeterminate(false);
            P.setCancelable(true);
            Toast.makeText(context, "Download starting...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            downloadAttachment(params[0]);
//            Log.d("---->", params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            RelativeLayout parentView = (RelativeLayout) findViewById(R.id.activity_description);
            Snackbar.make(parentView, "Download Complete!", Snackbar.LENGTH_SHORT).setAction("Open", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File selectedFile = new File(Environment.getExternalStorageDirectory().toString() + "/InfoConnect/" + linkstr);
                    if (selectedFile.isFile()) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", selectedFile);
                        if (selectedFile.getName().endsWith(".doc") || selectedFile.getName().endsWith(".docx")) {
                            intent.setDataAndType(uri, "application/msword");
                        } else if (selectedFile.getName().endsWith(".pdf")) {
                            intent.setDataAndType(uri, "application/pdf");
                        } else if (selectedFile.getName().endsWith(".ppt") || selectedFile.getName().endsWith(".pptx")) {
                            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                        } else if (selectedFile.getName().endsWith(".xls") || selectedFile.getName().endsWith(".xlsx")) {
                            intent.setDataAndType(uri, "application/vnd.ms-excel");
                        } else if (selectedFile.getName().endsWith(".rtf")) {
                            intent.setDataAndType(uri, "application/rtf");
                        } else if (selectedFile.getName().endsWith(".jpg") || selectedFile.getName().endsWith(".jpeg") || selectedFile.getName().endsWith(".png")) {
                            intent.setDataAndType(uri, "image/jpeg");
                        } else if (selectedFile.getName().endsWith(".txt")) {
                            intent.setDataAndType(uri, "text/plain");
                        } else {
                            intent.setDataAndType(uri, "*/*");
                        }
                        startActivity(intent);
                    }
                }
            }).show();
            P.dismiss();
            super.onPostExecute(Void);
        }
    }

    public void downloadAttachment(String sUrl) {
        int count;
        try {
            URL url = new URL(sUrl);
            URLConnection conection = url.openConnection();
            conection.connect();
//            Log.d("--->",sUrl);

            int lengthOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(Environment
                    .getExternalStorageDirectory().toString() + "/InfoConnect/"
                    + linkstr);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

