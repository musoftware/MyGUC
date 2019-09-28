package com.lzmouse.myguc.Intranet;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.Login.NTLMAuthenticator;
import com.lzmouse.myguc.Login.Student;
import com.lzmouse.myguc.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MetActivity extends DataActivity {

    private static final String TAG = "MET";
    private static final String MET_LINK = "http://met.guc.edu.eg/Courses/Undergrad.aspx";
    private static final String COURSE_LINK = "http://met.guc.edu.eg/Courses/CourseEdition.aspx?crsEdId=";

    private Document document;

    @Override
    protected void init() {
        recyclerView.setAdapter(linkAdapter);
        if (defLink == null) {
            new StudyGroupTask().execute(MET_LINK);
        }
    }

    private Document getMainDocument()
    {
        if(document !=  null)
            return document;
        try {
            return document = Jsoup.connect(MET_LINK).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void onClick(final LinksAdapter.Link link) {
        if (link instanceof LinksAdapter.ElementalLink) {
            LinksAdapter.ElementalLink elementalLink = (LinksAdapter.ElementalLink) link;

            Log.d(TAG, "clicked");
            switch (elementalLink.getElementType()) {
                case STUDY_GROUP:
                    new MajorTask().execute(elementalLink.getDiv());
                    break;
                case MAJOR:
                    new SemesterTask().execute(elementalLink.getDiv());
                    break;
                case SEMESTER:
                    new CoursesTask().execute(elementalLink.getDiv());
                    break;
            }
            paths.add(new PathsAdapter.ElemenalPath(elementalLink.getName(),elementalLink.getDiv(),elementalLink.getElementType()));
            if (paths.size() > 1)
                paths.get(paths.size() - 2).setShowArrow(true);
            pathsAdapter.notifyDataSetChanged();
        } else if (link instanceof LinksAdapter.Course) {
            LinksAdapter.Course course = (LinksAdapter.Course) link;
            new FilesTask().execute(COURSE_LINK + course.getId());
            paths.add(new PathsAdapter.Path(course.getName(),course.getId(), PathsAdapter.PathType.COURSE));
            if (paths.size() > 1)
                paths.get(paths.size() - 2).setShowArrow(true);
            pathsAdapter.notifyDataSetChanged();
        } else {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout,"This is a protected file you needs to login",Snackbar.LENGTH_LONG)
            .setAction("Login", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(link.getLink()));
                    startActivity(i);
                }
            })
            .show();


        }
    }

    @Override
    public void onFavStateChanged(LinksAdapter.Link link, boolean newState, int pos) {

    }

    @Override
    public void onPathClick(PathsAdapter.Path path, int pos) {
        List<PathsAdapter.Path> removedPaths =  new ArrayList<>();
        for(int i = pos + 1;i<paths.size();i++)
            removedPaths.add(paths.get(i));
        paths.removeAll(removedPaths);
        pathsAdapter.notifyDataSetChanged();
        if(path.getPath().equals("HOME"))
        {
            new StudyGroupTask().execute(MET_LINK);
            return;
        }
        if(path instanceof PathsAdapter.ElemenalPath)
        {
            PathsAdapter.ElemenalPath elemenalPath = (PathsAdapter.ElemenalPath) path;
            switch (elemenalPath.getElementType()) {
                case STUDY_GROUP:
                    new MajorTask().execute(elemenalPath.getPath());
                    break;
                case MAJOR:
                    new SemesterTask().execute(elemenalPath.getPath());
                    break;
                case SEMESTER:
                    new CoursesTask().execute(elemenalPath.getPath());
                    break;
            }
        }
        else
        {
            new FilesTask().execute(COURSE_LINK + path.getPath());

        }
    }



    private class StudyGroupTask extends DataTask<String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            paths.clear();
            paths.add(new PathsAdapter.Path("Faculties","HOME"));
            pathsAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(String... ls) {
            final String path = ls[0];

                for (Element element : getMainDocument().getElementsByAttributeValue("class", "stdyGrpLst")) {
                    links.add(LinksAdapter.ElementalLink.fromElement(element, LinksAdapter.ElementType.STUDY_GROUP));
                }


            return null;
        }


    }

    private class MajorTask extends DataTask<String> {

        @Override
        protected Void doInBackground(String... ss) {
            String id = ss[0];
            for (Element element : getMainDocument().getElementById(id).children()) {
                if (element.attr("class").equals("majorLst"))
                    links.add(LinksAdapter.ElementalLink.fromElement(element, LinksAdapter.ElementType.MAJOR));
            }
            return null;
        }
    }

    private class SemesterTask extends DataTask<String> {

        @Override
        protected Void doInBackground(String... ss) {
            String id = ss[0];
            for (Element element : getMainDocument().getElementById(id).children()) {
                if (element.attr("class").equals("semesterLst"))
                    links.add(LinksAdapter.ElementalLink.fromElement(element, LinksAdapter.ElementType.SEMESTER));
            }
            return null;
        }
    }

    private class CoursesTask extends DataTask<String> {

        @Override
        protected Void doInBackground(String... ss) {
            String id = ss[0];
            for (Element element : document.getElementById(id).children()) {
                if (element.attr("class").equals("coursesLst")) {
                    String courseID = element.attr("href").split("=")[1];
                    links.add(new LinksAdapter.Course(element.text(), courseID));
                }
            }
            return null;
        }
    }

    private class FilesTask extends DataTask<String> {

        @Override
        protected Void doInBackground(String... ss) {
            String link = ss[0];
            try {
                Document document = Jsoup.connect(link).get();
                for (Element element : document.getElementsByAttributeValueEnding("id", "materialDataLink")) {
                    String url = element.attr("href").replaceFirst("..", "http://met.guc.edu.eg");
                    links.add(new LinksAdapter.Link(element.text(), url, false, true));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}

