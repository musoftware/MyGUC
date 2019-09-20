package com.lzmouse.myguc.Intranet;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class DataActivity extends AppCompatActivity implements LinksAdapter.Listener, PathsAdapter.IPathsAdapter {

    public static final String LINK_EXTRA = "link";
    protected static final String TAG = "AcIntranet";
    protected static final String FACULTIES = "Faculties";
    protected RecyclerView recyclerView;
    protected RecyclerView pathsView;
    protected LinksAdapter linkAdapter;
    protected PathsAdapter pathsAdapter;
    protected List<FacultyAdapter.Faculty> faculties;
    protected List<LinksAdapter.Link> links;
    protected List<PathsAdapter.Path> paths;
    protected boolean isFac, isDownloadMode;
    protected CookieManager cookieManager;

    protected MyGucDatabaseHelper dpHelper;
    protected ProgressWheel progressWheel;
    protected LinksAdapter.Link defLink;

    protected abstract void init();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intranet);
        //Initialize lists
        progressWheel = findViewById(R.id.progress_wheel);
        faculties = new ArrayList<>();
        links = new ArrayList<>();

        paths = new ArrayList<>();
        defLink = (LinksAdapter.Link) getIntent().getSerializableExtra(LINK_EXTRA);

        cookieManager = CookieManager.getInstance();


        // Creating recycler view
        recyclerView = findViewById(R.id.rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        linkAdapter = new LinksAdapter(this, this, links, false);
        pathsAdapter = new PathsAdapter(this, paths);
        pathsView = findViewById(R.id.paths);
        pathsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pathsView.setAdapter(pathsAdapter);

        init();
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
    @Override
    public void onBackPressed() {
        if(paths.size() > 1)
            onPathClick(paths.get(paths.size() - 2), paths.size() - 2);
        else
            super.onBackPressed();
    }
    protected void downloadFile(final LinksAdapter.Link link) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_URL, link.getLink());
        intent.putExtra(DownloadService.EXTRA_NAME, link.getName());
        startService(intent);
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

}
