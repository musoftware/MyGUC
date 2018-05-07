package com.lzmouse.myguc.Intranet;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.Login.NTLMAuthenticator;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;
import com.lzmouse.myguc.Login.Student;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IntranetActivity extends AppCompatActivity implements FacultyAdapter.Listener, LinksAdapter.Listener, PathsAdapter.IPathsAdapter {
    public static final String LINK_EXTRA = "link";
    private static final String TAG = "AcIntranet";
    private static final String FACULTIES = "Faculties";
    private RecyclerView recyclerView;
    private RecyclerView pathsView;
    private LinksAdapter linkAdapter;
    private FacultyAdapter facultyAdapter;
    private PathsAdapter pathsAdapter;
    private List<FacultyAdapter.Faculty> faculties;
    private List<LinksAdapter.Link> links;
    private List<PathsAdapter.Path> paths;
    private WebView webView;
    private boolean isFac,isDownloadMode;
    private CookieManager cookieManager;

    private MyGucDatabaseHelper dpHelper;
    private ProgressWheel progressWheel;
    private OpenLinkTask openLinkTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intranet);
        //Initialize lists
        progressWheel = findViewById(R.id.progress_wheel);
        faculties = new ArrayList<>();
        links = new ArrayList<>();

        paths = new ArrayList<>();
        LinksAdapter.Link defLink = (LinksAdapter.Link) getIntent().getSerializableExtra(LINK_EXTRA);
        if(defLink ==  null)
            fillFaculties();
        cookieManager = CookieManager.getInstance();


        // Creating recycler view
        recyclerView =  findViewById(R.id.rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        facultyAdapter =  new FacultyAdapter(this,this,faculties);
        linkAdapter =  new LinksAdapter(this,this,links,false);
        recyclerView.setAdapter(facultyAdapter);
        pathsAdapter = new PathsAdapter(this,paths);
        pathsView =  findViewById(R.id.paths);
        pathsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        pathsView.setAdapter(pathsAdapter);


        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(!isDownloadMode)
                     view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                else
                {

                    }

            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                handler.proceed(Student.getInstance().getUsername(),Student.getInstance().getPassword());
                Log.d(TAG,"Logging in to:" + host);
            }

        });

        if(defLink != null)
        {
            recyclerView.setAdapter(linkAdapter);
            pathsView.setVisibility(View.VISIBLE);
            paths.add(new PathsAdapter.Path(FACULTIES,FACULTIES,false));
            openLink(defLink.getName(),defLink.getLink());

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        dpHelper =  new MyGucDatabaseHelper(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dpHelper.close();
    }
    public void sortLinks()
    {
        Collections.sort(links, new Comparator<LinksAdapter.Link>() {
            @Override
            public int compare(LinksAdapter.Link link, LinksAdapter.Link t1) {
                if(link.isFile() == t1.isFile())
                    return link.getName().compareTo(t1.getName()) ;
                else if(link.isFile() && !t1.isFile())
                    return 1;
                else
                    return -1;
            }
        });
    }
    public void openLink(String name, String path) {
        if(openLinkTask == null) {
            getSupportActionBar().setTitle(name);
            paths.add(new PathsAdapter.Path(name, path, false));
            if (paths.size() > 1)
                paths.get(paths.size() - 2).setShowArrow(true);
            pathsAdapter.notifyDataSetChanged();
            pathsView.scrollToPosition(paths.size() - 1);
            isDownloadMode = false;
            Log.d(TAG, path);
            if (name.equals(FACULTIES))
                fillFaculties();
            else {
                openLinkTask =  new OpenLinkTask();
                openLinkTask.execute(new LinksAdapter.Link(name,path,false,false));
            }
        }


    }
    private class OpenLinkTask extends AsyncTask<LinksAdapter.Link,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.GONE);
            pathsView.setVisibility(View.GONE);
            progressWheel.setVisibility(View.VISIBLE);
            links.clear();
        }

        @Override
        protected Void doInBackground(LinksAdapter.Link... links) {
            LinksAdapter.Link link = links[0];
            final String path = "http://student.guc.edu.eg" +link.getLink();

            OkHttpClient client = new OkHttpClient.Builder()
                    .authenticator(new NTLMAuthenticator(Student.getInstance().getUsername(), Student.getInstance().getPassword()))
                    .build();

            try {
                Response response = client.newCall(new Request.Builder().url(path).build()).execute();
                if(response.isSuccessful()) {
                    String html = response.body().string();
                    Log.d(TAG, html);
                    Log.d(TAG, "Processing...");


                    Log.d(TAG, "Going to get doc");

                    Document doc = Jsoup.parse(html);
                    Log.d(TAG, "Going to get tags");

                    String body = doc.getElementsByTag("pre").first().html();

                    String[] lines = body.split("<br>");
                    Log.d(TAG, lines.length + "");
                    IntranetActivity.this.links.clear();
                    for (int i = 1; i < lines.length; i++) {
                        try {
                            String line = lines[i];
                            if (line.isEmpty())
                                continue;
                            Log.d(TAG, line);
                            int start = line.lastIndexOf("\">");
                            int end = line.lastIndexOf("</a>");
                            Log.d(TAG, start + " " + end);


                            String name = Helper.htmlToString(line.substring(start + 2, end));
                            String url= Helper.htmlToString(line.substring(line.lastIndexOf("href=\"") + 6, line.lastIndexOf("\">")));
                            Log.d(TAG, name + " => " + url);
                            if (name.isEmpty() || url.isEmpty())
                                continue;

                            LinksAdapter.Link l = new LinksAdapter.Link(name, url, isLinkFav(url), !line.contains("dir"));
                            IntranetActivity.this.links.add(l);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    linkAdapter.notifyDataSetChanged();
                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "Error:", e);
                        }


                    }
                    sortLinks();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            openLinkTask = null;
            progressWheel.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            pathsView.setVisibility(View.VISIBLE);
            linkAdapter.notifyDataSetChanged();
        }
    }
    private void downloadFile(final LinksAdapter.Link link)
    {
        Intent intent = new Intent(this,DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_URL,link.getLink());
        intent.putExtra(DownloadService.EXTRA_NAME,link.getName());
        startService(intent);
    }

    @Override
    public void onFacultyClick(FacultyAdapter.Faculty faculty) {
        Log.d(TAG,faculty.getName() + " Clicked");
        recyclerView.setAdapter(linkAdapter);
        pathsView.setVisibility(View.VISIBLE);
        isDownloadMode = false;
        openLink(faculty.getName(),faculty.getLink());

    }
    @Override
    public void onClick(LinksAdapter.Link link) {
        if(!link.isFile())
            openLink(link.getName(),link.getLink());
        else
            downloadFile(link);
    }

    public boolean isLinkFav(String path)
    {
        if(dpHelper.getReadableDatabase()!=null)
        {
            SQLiteDatabase reader =  dpHelper.getReadableDatabase();
            Cursor cursor = reader.query(MyGucDatabaseHelper.FAVOURITES_TABLE,
                    new String[] {MyGucDatabaseHelper.PATH_COL},
                    MyGucDatabaseHelper.PATH_COL +"=?",
                    new String[] {path},null,null,null);
            boolean isFav =  cursor.moveToFirst();
            cursor.close();
            return isFav;
        }
        return false;
    }
    @Override
    public void onFavStateChanged(LinksAdapter.Link link, boolean newState,int post) {
        Log.d(TAG,"State changed to " + newState);
        SQLiteDatabase writer = dpHelper.getWritableDatabase();
        if(writer!=null) {
            if (newState) {
                Log.d(TAG,"Inserting new fav");
                ContentValues values = new ContentValues();
                values.put(MyGucDatabaseHelper.NAME_COL, link.getName());
                values.put(MyGucDatabaseHelper.PATH_COL, link.getLink());
                writer.insert(MyGucDatabaseHelper.FAVOURITES_TABLE, null, values);
                Log.d(TAG,"Inserted");
            } else {
                Log.d(TAG,"Deleting fav");
                writer.delete(MyGucDatabaseHelper.FAVOURITES_TABLE,
                        MyGucDatabaseHelper.PATH_COL +"=?",new String[]{link.getLink()});
                Log.d(TAG,"Deleted");
            }
        }
    }
    @Override
    public void onPathClick(PathsAdapter.Path path, int pos) {
        int end  = paths.indexOf(path);
        List<PathsAdapter.Path> removedPaths =  new ArrayList<>();
        for(int i = end;i<paths.size();i++)
            removedPaths.add(paths.get(i));
        paths.removeAll(removedPaths);
        pathsAdapter.notifyDataSetChanged();
        openLink(path.getName(),path.getPath());
    }

    @Override
    public void onBackPressed() {
        if(paths.size() > 1)
            onPathClick(paths.get(paths.size() - 2), paths.size() - 2);
        else
            super.onBackPressed();
    }

    private void fillFaculties()
    {
        isFac = true;
        if(pathsView!=null)
            pathsView.setVisibility(View.GONE);
        if(recyclerView != null)
            recyclerView.setAdapter(facultyAdapter);
        faculties.clear();
        paths.clear();
        paths.add(new PathsAdapter.Path(FACULTIES,FACULTIES,false));
        faculties.add(new FacultyAdapter.Faculty("Maths","/intranet/Faculties/Basic%20Sciences/Mathematics/",R.drawable.ic_math));
        faculties.add(new FacultyAdapter.Faculty("Physics","/intranet/Faculties/Basic%20Sciences/Physics/",R.drawable.ic_phsyics));
        faculties.add(new FacultyAdapter.Faculty("Pharmacy","/intranet/Faculties/Pharmacy%20&%20Biotechnology/",R.drawable.ic_pharmacy));
        faculties.add(new FacultyAdapter.Faculty("MET","/intranet/Faculties/Media%20Engineering%20Technology/",R.drawable.ic_met));
        faculties.add(new FacultyAdapter.Faculty("Applied Arts","/intranet/Faculties/Applied%20Arts%20&%20Science/",R.drawable.ic_app_arts));
        faculties.add(new FacultyAdapter.Faculty("Material Sciences","/intranet/Faculties/Engineering%20&%20Material%20Sciences/",R.drawable.ic_materails));
        faculties.add(new FacultyAdapter.Faculty("Information Technology","/intranet/Faculties/Information%20&%20Engineering%20Technology/",R.drawable.ic_it));
        faculties.add(new FacultyAdapter.Faculty("English Department","/intranet/Faculties/Language%20Centre/English/",R.drawable.ic_english));
        faculties.add(new FacultyAdapter.Faculty("German Department","/intranet/Faculties/Language%20Centre/German/",R.drawable.ic_german));
        faculties.add(new FacultyAdapter.Faculty("Law & Legal Studies","/intranet/Faculties/Law%20&%20Legal%20Studies/",R.drawable.ic_law));
        faculties.add(new FacultyAdapter.Faculty("MBA","/intranet/Faculties/MBA/",R.drawable.ic_mba));
        faculties.add(new FacultyAdapter.Faculty("Software","/intranet/Faculties/Software/",R.drawable.ic_software));
    }




    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {

        }


    }
}
