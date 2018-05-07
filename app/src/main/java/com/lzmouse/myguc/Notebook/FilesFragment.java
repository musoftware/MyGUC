package com.lzmouse.myguc.Notebook;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.Intranet.FilesAdapter;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesFragment extends Fragment implements FilesAdapter.FileListener, View.OnClickListener {

    private static final String SUBJECT_ID_PARAM = "subject_id";

    public FilesFragment() {
        // Required empty public constructor
    }

   private long subjectId;
    private Context context;
    private MyGucDatabaseHelper dbHelper;
    private LoadDataTask loadDataTask;
    public static FilesFragment newInstance(long id) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putLong(SUBJECT_ID_PARAM, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjectId = getArguments().getLong(SUBJECT_ID_PARAM);
        }
    }

    private List<FilesAdapter.FileInfo> files;
    private FilesAdapter adapter;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ProgressWheel progressWheel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v  =  inflater.inflate(R.layout.fragment_lectures, container, false);
        progressWheel = v.findViewById(R.id.progress_wheel);
        files = new ArrayList<>();
        recyclerView =  v.findViewById(R.id.rec);
        adapter =  new FilesAdapter(v.getContext(),this,files);
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
            files.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(dbHelper.getReadableDatabase()!=null)
            {
                Cursor cursor = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.ENTRIES_TABLE,
                        new String[]{"_id",MyGucDatabaseHelper.SOURCE_ID_COL,MyGucDatabaseHelper.PATH_COL},
                        MyGucDatabaseHelper.SOURCE_ID_COL + "=?" + " AND " + MyGucDatabaseHelper.TYPE_COL + "=?"
                        ,new String[]{subjectId +"",LecturesFragment.FILES_TYPE + ""},null,null,null);
                if(cursor.moveToFirst())
                    for(int  i= 0;i<cursor.getCount();i++)
                        if(cursor.moveToPosition(i)) {
                            long id = cursor.getLong(cursor.getColumnIndex("_id"));
                            String path  = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.PATH_COL));
                            long source  = cursor.getLong(cursor.getColumnIndex(MyGucDatabaseHelper.SOURCE_ID_COL));
                            FileInfo fileInfo =  new FileInfo(id,source,new File(path));
                            files.add(fileInfo);
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
            if(files.size() > 0)
                adapter.notifyDataSetChanged();
            showAll();


        }
    }

    @Override
    public void onFileClick(FilesAdapter.FileInfo fileInfo, int pos) {
        Helper.openFile(context,fileInfo.getFile());
    }

    @Override
    public void onDeleteClick(FilesAdapter.FileInfo fileInfo, int pos) {
        if(fileInfo.getFile().delete())
        {
            if(dbHelper.getWritableDatabase()!=null)
            {
                dbHelper.getWritableDatabase().delete(MyGucDatabaseHelper.ENTRIES_TABLE,"_id=?" ,
                        new String []{((FileInfo)fileInfo).getId() + ""});
            }
            files.remove(pos);
            adapter.notifyItemRemoved(pos);
        }

    }

    @Override
    public void onShareClick(FilesAdapter.FileInfo fileInfo, int pos) {
        Helper.shareFile(context,fileInfo.getFile());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.fab:
                addFile();
                break;
        }
    }
    private void addFile()
    {
        File folder = new File(Environment.getExternalStorageDirectory(), "My Guc");
        folder.mkdirs();
        new FileChooserDialog.Builder(context).initialPath(folder.getAbsolutePath()).mimeType("*/*").show(((SubjectActivity)context));

    }
    public void onFileSelection(FileChooserDialog dialog, File file) {
        dialog.dismiss();
        if(dbHelper.getWritableDatabase() != null)
        {
            ContentValues values =  new ContentValues();
            values.put(MyGucDatabaseHelper.SOURCE_ID_COL,subjectId);
            values.put(MyGucDatabaseHelper.PATH_COL,file.getAbsolutePath());
            values.put(MyGucDatabaseHelper.TYPE_COL,LecturesFragment.FILES_TYPE);
            long id = dbHelper.getWritableDatabase().insert(MyGucDatabaseHelper.ENTRIES_TABLE,null,values);
            FileInfo fileInfo = new FileInfo(id,subjectId,file);
            files.add(fileInfo);
            adapter.notifyItemInserted(files.size() - 1);
            recyclerView.scrollToPosition(files.size() - 1);
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
    public static class FileInfo extends FilesAdapter.FileInfo{
        private long id, sourceId;
        public FileInfo(long id,long sourceId,File file) {
            super(file);
            this.id = id;
            this.sourceId = sourceId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getSourceId() {
            return sourceId;
        }

        public void setSourceId(long sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FileInfo fileInfo = (FileInfo) o;

            return id == fileInfo.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }
}
