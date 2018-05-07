package com.lzmouse.myguc.Notebook;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LecturesFragment extends Fragment implements LecturesAdapter.Listener, View.OnClickListener {

    public static final int LECTURES_TYPE = 0;
    public static final int TUTORIALS_TYPE = 1;
    public static final int FILES_TYPE = 2 ;

    private static final String TYPE_PARAM = "type";
    private static final String COLOR_PARAM = "color";
    private static final String SUBJECT_ID_PARAM = "subject_id";

    private Context context;
    private  int type;
    private long subjectId;
    private int color;
    private MyGucDatabaseHelper dbHelper;
    private DeleteTask deleteTask;
    public LecturesFragment() {

    }
    public static LecturesFragment newInstance(int type,long subjectId,int color) {
        LecturesFragment fragment = new LecturesFragment();
        Bundle args = new Bundle();
        args.putLong(SUBJECT_ID_PARAM,subjectId);
        args.putInt(TYPE_PARAM, type);
        args.putInt(COLOR_PARAM , color);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(TYPE_PARAM);
            subjectId = getArguments().getLong(SUBJECT_ID_PARAM);
            color = getArguments().getInt(COLOR_PARAM);
        }

    }
    private List<LecturesAdapter.Lecture> lectures;
    private LecturesAdapter adapter;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ProgressWheel progressWheel;
    private LoadDataTask loadDataTask;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v  =  inflater.inflate(R.layout.fragment_lectures, container, false);
        progressWheel = v.findViewById(R.id.progress_wheel);
        lectures = new ArrayList<>();
        recyclerView =  v.findViewById(R.id.rec);
        adapter =  new LecturesAdapter(this,lectures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
        recyclerView.setAdapter(adapter);

        fab  = v.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        return v;
    }

    private void loadData() {
        if(loadDataTask == null)
        {
            loadDataTask = new LoadDataTask();
            loadDataTask.execute();
        }
    }

    private class LoadDataTask extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideAll();
            progressWheel.setVisibility(View.VISIBLE);
            lectures.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(dbHelper.getReadableDatabase()!=null)
            {
                Cursor cursor = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.SOURCES_TABLE,
                        new String[]{MyGucDatabaseHelper.NAME_COL,"_id",MyGucDatabaseHelper.DATE_COL,
                                MyGucDatabaseHelper.SUBJECT_ID_COL,MyGucDatabaseHelper.TYPE_COL},
                        MyGucDatabaseHelper.SUBJECT_ID_COL + "=?" + " AND " + MyGucDatabaseHelper.TYPE_COL + "=?"
                        ,new String[]{subjectId +"",type + ""},null,null,null);
                if(cursor.moveToFirst())
                    for(int  i= 0;i<cursor.getCount();i++)
                        if(cursor.moveToPosition(i)) {
                            long id = cursor.getLong(cursor.getColumnIndex("_id"));
                            String name  = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.NAME_COL));
                            long date  = cursor.getLong(cursor.getColumnIndex(MyGucDatabaseHelper.DATE_COL));
                            LecturesAdapter.Lecture lecture =  new LecturesAdapter.Lecture(id,subjectId,name,date);
                            lectures.add(lecture);
                        }
                cursor.close();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadDataTask = null;
            progressWheel.setVisibility(View.GONE);
            if(lectures.size() > 0)
                adapter.notifyDataSetChanged();
            showAll();


        }
    }

    private void hideAll()
    {
        recyclerView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
    }
    private void showAll()
    {
        recyclerView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
    }
    @Override
    public void onLectureClick(LecturesAdapter.Lecture lecture, int pos) {
        Intent intent =  new Intent(context,EntryActivity.class);
        intent.putExtra(EntryActivity.SOURCE_PARAM,lecture);
        intent.putExtra(EntryActivity.COLOR_PARAM,color);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final LecturesAdapter.Lecture lecture, final int pos) {
        if (deleteTask == null) {
            SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Deep Deleting")
                    .setContentText("Do you want to delete the files from the phone?")
                    .setConfirmText("Yes")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            deleteTask = new DeleteTask();
                            deleteTask.execute(lecture, true, pos);
                        }
                    })
                    .setCancelButton("No,Only delete from the app", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            deleteTask =  new DeleteTask();
                            deleteTask.execute(lecture,false,pos);
                        }
                    }).setCancelText("Cancel");

            dialog.show();
        }
    }
    private  class DeleteTask extends AsyncTask<Object,Void,Void>
    {
        private SweetAlertDialog dialog;
        private int pos;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog =  new SweetAlertDialog(context,SweetAlertDialog.PROGRESS_TYPE);
            dialog.setTitleText("Deleting lecture");
            dialog.setContentText("Please wait...");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getProgressHelper().setBarColor(Helper.getColor(context,R.color.colorAccent));

        }

        @Override
        protected Void doInBackground(Object... obs) {
            boolean isDeep = (boolean) obs[1];
            LecturesAdapter.Lecture lecture = (LecturesAdapter.Lecture) obs[0];
            pos = (int) obs[2];
            if(isDeep)
                deepDelete(dbHelper,lecture.getId());
            fastDelete(dbHelper,lecture.getId());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deleteTask = null;
            dialog.dismiss();
            lectures.remove(pos);
            adapter.notifyItemRemoved(pos);

        }
    }
    public static void fastDelete(MyGucDatabaseHelper dbHelper,long id)
    {
        if(dbHelper.getWritableDatabase()!=null) {
            dbHelper.getWritableDatabase().delete(MyGucDatabaseHelper.SOURCES_TABLE, "_id=?", new String[]{id + ""});
            dbHelper.getWritableDatabase().delete(MyGucDatabaseHelper.ENTRIES_TABLE,
                    MyGucDatabaseHelper.SOURCE_ID_COL +"=?",new String[]{id+""});
        }

    }
    public static void deepDelete(MyGucDatabaseHelper dbHelper,long id)
    {
        if(dbHelper.getReadableDatabase()!=null)
        {
            Cursor cursor = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.ENTRIES_TABLE,
                    new String[]{"_id",MyGucDatabaseHelper.SOURCE_ID_COL,MyGucDatabaseHelper.PATH_COL,MyGucDatabaseHelper.TYPE_COL},MyGucDatabaseHelper.SOURCE_ID_COL +"=?",new String[]{id+""}
                    ,null,null,null);
            if(cursor.moveToFirst()) {
                for(int i = 0;i<cursor.getCount();i++) {
                    if(cursor.moveToPosition(i)) {
                        String path = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.PATH_COL));
                        int type = cursor.getInt(cursor.getColumnIndex(MyGucDatabaseHelper.TYPE_COL));
                        if (type != EntriesAdapter.Entry.TYPE_NOTE)
                            EntryActivity.deepDelete(path);
                    }
                }
            }
            cursor.close();
        }
    }
    @Override
    public void onClick(View view) {
        new MaterialDialog.Builder(context)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .title(String.format("Enter %s name",type == LECTURES_TYPE ? "lecture":"tutorial"))
                .input("Recursion 01", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        addLecture(input.toString());
                        dialog.dismiss();
                    }
                }).show();
    }

    private void addLecture(String name)
    {
        if(dbHelper.getWritableDatabase()!=null)
        {
            long date = System.currentTimeMillis();
            ContentValues values =  new ContentValues();
            values.put(MyGucDatabaseHelper.NAME_COL,name);
            values.put(MyGucDatabaseHelper.DATE_COL,date);
            values.put(MyGucDatabaseHelper.SUBJECT_ID_COL,subjectId);
            values.put(MyGucDatabaseHelper.TYPE_COL,type);
            long id = dbHelper.getWritableDatabase().insert(MyGucDatabaseHelper.SOURCES_TABLE,null,values);
            LecturesAdapter.Lecture lecture =  new LecturesAdapter.Lecture(id,subjectId,name,date);
            lectures.add(lecture);
            adapter.notifyItemInserted(lectures.size() - 1);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onStart() {
        dbHelper =  new MyGucDatabaseHelper(getActivity());
        loadData();
        super.onStart();
    }

    @Override
    public void onStop() {
        if(dbHelper !=null) {
            dbHelper.close();
            dbHelper =  null;
        }
        super.onStop();
    }
}
