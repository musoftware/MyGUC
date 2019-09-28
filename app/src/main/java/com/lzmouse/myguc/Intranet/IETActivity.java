package com.lzmouse.myguc.Intranet;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IETActivity extends DataActivity {
    private final String IET_WEBSITE = "http://eee.guc.edu.eg/courses.html";
    private final String FILES_URL = "http://eee.guc.edu.eg/Courses/Electronics/COURSE_NAME/schedule.html";
    private final List<Course> courseList = new ArrayList<>();

    @Override
    protected void init() {
        recyclerView.setAdapter(linkAdapter);
        if (defLink == null) {
            paths.add(new PathsAdapter.Path("Semester", "SEMESTER"));
            addSemesters();
        }
    }

    public void addSemesters() {
        links.clear();
        for (int i = 2; i <= 9; i++) {
            Semester semester = new Semester("Semester: " + i, i);
            links.add(semester);
        }
        linkAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(LinksAdapter.Link link) {
        if (link instanceof Semester) {
            Semester semester = (Semester) link;
            paths.add(new PathsAdapter.Path(semester.getName(), semester.getNumber() + ""));
            if (paths.size() > 1)
                paths.get(paths.size() - 2).setShowArrow(true);
            pathsAdapter.notifyDataSetChanged();

            if (courseList.isEmpty())
                new GetCoursesTask().execute();
            new FilterCourses().execute(semester.getNumber());

        } else if (link instanceof Course) {
            paths.add(new PathsAdapter.Path(link.getName(), link.getName().replace(":", "")));


            if (paths.size() > 1)
                paths.get(paths.size() - 2).setShowArrow(true);
            pathsAdapter.notifyDataSetChanged();

            new FilesTask().execute(link.getName().replace(":", ""));
        } else {
            link.setName(link.getName() + ".pdf");
            downloadFile(link);
        }

    }


    @Override
    public void onFavStateChanged(LinksAdapter.Link link, boolean newState, int pos) {

    }

    @Override
    public void onPathClick(PathsAdapter.Path path, int pos) {
        List<PathsAdapter.Path> removedPaths = new ArrayList<>();
        Log.d(TAG,"POS: "+ pos );
        for (int i = pos + 1; i < paths.size(); i++)
            removedPaths.add(paths.get(i));
        paths.removeAll(removedPaths);
        pathsAdapter.notifyDataSetChanged();
        if (path.getPath().equals("SEMESTER"))
            addSemesters();
        else if (path.getName().startsWith("Semester:")) {

             Log.d(TAG,"Sems: " + path.getPath());
            if (courseList.isEmpty())
                new GetCoursesTask().execute();
            new FilterCourses().execute(Integer.parseInt(path.getPath()));
        } else {
            new FilesTask().execute(path.getPath());
        }

    }


    public class GetCoursesTask extends DataTask<Void> {

        @Override
        protected Void doInBackground(Void... strings) {

            try {
                Document document = Jsoup.connect(IET_WEBSITE).get();
                Element table = document.getElementsByClass("MsoNormalTable").get(0);
                for (Element element : table.getElementsByTag("a")) {
                    String name = element.text();

                    if (!name.trim().equals("")) {
                        Course course = new Course(name, element.attr("href"), getCode(name));
                        courseList.add(course);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private int getCode(String name) {

            String x = name.split(":")[0];
            int code = 8;
            for (int i = x.length() - 1; i >= 0; i--) {
                if (x.charAt(i) >= '0' && x.charAt(i) <= '9') {
                    code = Integer.parseInt(x.charAt(i) + "");
                }
            }

            return code;


        }
    }

    private class FilterCourses extends DataTask<Integer> {

        @Override
        protected Void doInBackground(Integer... ints) {
            int semester = ints[0];


            for (Course course : courseList) {
                if (course.getNumber() == semester)
                    links.add(course);
            }
            Log.d(TAG, courseList.size() + "");

            return null;
        }
    }

    private class FilesTask extends DataTask<String> {


        @Override
        protected Void doInBackground(String... strings) {


            String url = FILES_URL.replace("COURSE_NAME", strings[0]);
            try {
                Log.d(TAG, "URL: " + url);
                Document document = Jsoup.connect(url).get();
                for (Element element : document.getElementsByTag("a")) {
                    String text = element.text();
                    if (!text.isEmpty()) {
                        String link = element.attr("href");
                        link = url.replace("schedule.html", link);
                        links.add(new LinksAdapter.Link(text, link, false, true));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class Semester extends LinksAdapter.Link {

        private int number;

        public int getNumber() {
            return number;
        }

        public Semester(String name, int number) {
            super(name, "", false, false);
            this.number = number;
        }

    }

    private class Course extends LinksAdapter.Link {

        private int number;

        public Course(String name, String link, int number) {
            super(name, link, false, false);
            this.number = number;
        }

        public int getNumber() {
            return number;
        }
    }

}
