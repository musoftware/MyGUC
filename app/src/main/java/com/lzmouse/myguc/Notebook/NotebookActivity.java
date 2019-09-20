package com.lzmouse.myguc.Notebook;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NotebookActivity extends AppCompatActivity implements SubjectsAdapter.Listener,ColorChooserDialog.ColorCallback {

    private View fabLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private SubjectsAdapter adapter;
    private List<SubjectsAdapter.Subject> subjects;
    private String newSubName;
    private MyGucDatabaseHelper dbHelper;
    private ProgressWheel progressWheel;
    private LoadDataTask loadDataTask;
    private DeleteTask deleteTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        progressWheel = findViewById(R.id.progress_wheel);
        fabLayout = findViewById(R.id.fab_layout);
        fab = findViewById(R.id.fab2);

        subjects = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);

        adapter = new SubjectsAdapter(this, subjects);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

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
            subjects.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(dbHelper.getReadableDatabase()!=null)
            {
                Cursor cursor = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.SUBJECTS_TABLE,
                        new String[]{MyGucDatabaseHelper.NAME_COL,"_id",MyGucDatabaseHelper.DATE_COL,MyGucDatabaseHelper.COLOR_COL},
                        null,null,null,null,null);
                if(cursor.moveToFirst())
                    for(int  i= 0;i<cursor.getCount();i++)
                        if(cursor.moveToPosition(i)) {
                            long id = cursor.getLong(cursor.getColumnIndex("_id"));
                            String name  = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.NAME_COL));
                            long date  = cursor.getLong(cursor.getColumnIndex(MyGucDatabaseHelper.DATE_COL));
                            int color = cursor.getInt(cursor.getColumnIndex(MyGucDatabaseHelper.COLOR_COL));

                            Cursor c = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.SOURCES_TABLE,
                                    new String[]{"_id"},
                                    MyGucDatabaseHelper.SUBJECT_ID_COL + "=?" + " AND " + MyGucDatabaseHelper.TYPE_COL + "=?"
                                    ,new String[]{id +"",LecturesFragment.LECTURES_TYPE + ""},null,null,null);
                            int ls = c.getCount();
                            c.close();
                            c = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.SOURCES_TABLE,
                                    new String[]{"_id"},
                                    MyGucDatabaseHelper.SUBJECT_ID_COL + "=?" + " AND " + MyGucDatabaseHelper.TYPE_COL + "=?"
                                    ,new String[]{id +"",LecturesFragment.TUTORIALS_TYPE + ""},null,null,null);
                            int ts = c.getCount();
                            c.close();
                            SubjectsAdapter.Subject subject =  new SubjectsAdapter.Subject(id,name,date,ls,ts,0,color);
                            subjects.add(subject);
                        }
                cursor.close();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressWheel.setVisibility(View.GONE);
            if(subjects.size() > 0)
                hideFabLayout();
            else
                showFabLayout();
            loadDataTask = null;

        }
    }



    public void onFabClick(View v) {

        new MaterialDialog.Builder(this)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .title("Enter subject name")
                .input("CS", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        dialog.dismiss();
                       newSubName = input.toString().trim();
                        new ColorChooserDialog.Builder(NotebookActivity.this, R.string.pick_color)
                                .titleSub(R.string.color) // title of dialog when viewing shades of a color
                                .accentMode(false)  // when true, will display accent palette instead of primary palette
                                .preselect(Helper.getColor(NotebookActivity.this,R.color.colorPrimary))  // optionally preselects a color
                                .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                                .show(NotebookActivity.this); // an AppCompatActivity which implements ColorCallback
                    }
                }).show();
    }
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        if(newSubName != null && dbHelper.getWritableDatabase()!=null)
        {
            long date = System.currentTimeMillis();
            ContentValues values =  new ContentValues();
            values.put(MyGucDatabaseHelper.NAME_COL,newSubName);
            values.put(MyGucDatabaseHelper.DATE_COL,date);
            values.put(MyGucDatabaseHelper.COLOR_COL,selectedColor);
            long id = dbHelper.getWritableDatabase().insert(MyGucDatabaseHelper.SUBJECTS_TABLE,
                    null,values);
            if(subjects.size()==0)
                hideFabLayout();
            SubjectsAdapter.Subject subject = new SubjectsAdapter.Subject(id,newSubName,date,selectedColor);
            subjects.add(subject);
            adapter.notifyItemInserted(subjects.size() -1);
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        newSubName = null;
    }

    @Override
    public void onSubjectClick(SubjectsAdapter.Subject subject, int index) {
        Intent intent = new Intent(this, SubjectActivity.class);
        intent.putExtra(SubjectActivity.SUBJECT_EXTRA,subject);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final SubjectsAdapter.Subject subject, final int index) {
        if (deleteTask == null) {
            new MaterialDialog.Builder(this)
                    .title("Deleting " + subject.getName())
                    .content("Are you sure you want to delete this entirely?")
                    .positiveText("Yes")
                    .negativeText("No,Only from the app")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            deleteTask = new DeleteTask(subject.getUid(),index,true);
                            deleteTask.execute();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            deleteTask = new DeleteTask(subject.getUid(),index,false);
                            deleteTask.execute();
                        }
                    })
                    .show();

        }
    }
    private  class DeleteTask extends AsyncTask<Object,Void,Void>
    {
        private SweetAlertDialog dialog;
        private int pos;
        private boolean isDeep;
        private long id;
        public DeleteTask(long id,int pos,boolean isDeep)
        {
            this.id = id;
            this.isDeep = isDeep;
            this.pos = pos;
            if(dbHelper == null)
                dbHelper = new MyGucDatabaseHelper(NotebookActivity.this);

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog =  new SweetAlertDialog(NotebookActivity.this,SweetAlertDialog.PROGRESS_TYPE);
            dialog.setTitleText("Deleting subject");
            dialog.setContentText("Please wait...");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getProgressHelper().setBarColor(Helper.getColor(NotebookActivity.this,R.color.colorAccent));

        }

        @Override
        protected Void doInBackground(Object... obs) {
            if(isDeep)
                deepDelete(dbHelper,id);
            fastDelete(dbHelper,id);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deleteTask = null;
            dialog.dismiss();
            subjects.remove(pos);
            adapter.notifyItemRemoved(pos);

        }
    }
    public static void fastDelete(MyGucDatabaseHelper dbHelper,long id)
    {
        if(dbHelper.getWritableDatabase()!=null) {
            dbHelper.getWritableDatabase().delete(MyGucDatabaseHelper.SUBJECTS_TABLE, "_id=?", new String[]{id + ""});
            if(dbHelper.getReadableDatabase()!=null)
            {
                Cursor cursor = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.SOURCES_TABLE,
                        new String[]{"_id"},MyGucDatabaseHelper.SUBJECT_ID_COL +"=?",new String[]{id+""}
                        ,null,null,null);
                if(cursor.moveToFirst()) {
                    for(int i = 0;i<cursor.getCount();i++)
                        if(cursor.moveToPosition(i)) {
                            long sourceID = cursor.getLong(cursor.getColumnIndex("_id"));
                            LecturesFragment.fastDelete(dbHelper,sourceID);
                        }
                }
                cursor.close();
            }
        }

    }
    public static void deepDelete(MyGucDatabaseHelper dbHelper,long id)
    {
        if(dbHelper.getReadableDatabase()!=null)
        {
            Cursor cursor = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.SOURCES_TABLE,
                    new String[]{"_id",MyGucDatabaseHelper.SUBJECT_ID_COL},MyGucDatabaseHelper.SUBJECT_ID_COL +"=?",new String[]{id+""}
                    ,null,null,null);
            if(cursor.moveToFirst()) {
                for(int i = 0;i<cursor.getCount();i++)
                    if(cursor.moveToPosition(i)) {
                        long sourceID = cursor.getLong(cursor.getColumnIndex("_id"));
                        LecturesFragment.deepDelete(dbHelper,sourceID);
                    }
            }
            cursor.close();
        }
    }

    private void hideAll()
    {
        fabLayout.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);
    }
    private void hideFabLayout() {
        fabLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void showFabLayout() {
        fabLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        dbHelper = new MyGucDatabaseHelper(this);
        loadData();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(dbHelper !=null) {
            dbHelper.close();
            dbHelper = null;
        }
        super.onStop();
    }
}
