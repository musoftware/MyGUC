package com.lzmouse.myguc.Notebook;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EntryActivity extends AppCompatActivity implements View.OnClickListener, EntriesAdapter.Listener,FileChooserDialog.FileCallback {

    public static final String SOURCE_PARAM = "source";
    public static final String COLOR_PARAM = "color";

    private List<EntriesAdapter.Entry> entries;
    private EntriesAdapter adapter;
    private LecturesAdapter.Lecture lecture;
    private RecyclerView recyclerView;
    private FloatingActionsMenu fabMenu;
    private ProgressWheel progressWheel;
    private LoadDataTask loadDataTask;
    private MyGucDatabaseHelper dbHelper;
    private EntriesAdapter.Entry entry;
    private int color;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_entry);
        color = getIntent().getIntExtra(COLOR_PARAM, Helper.getColor(this,R.color.colorPrimary));
        fabMenu = findViewById(R.id.fab);
        progressWheel = findViewById(R.id.progress_wheel);
        FloatingActionButton addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(this);
        FloatingActionButton addVideo = findViewById(R.id.add_video);
        addVideo.setOnClickListener(this);
        FloatingActionButton addRecord = findViewById(R.id.add_record);
        addRecord.setOnClickListener(this);
        FloatingActionButton addNote = findViewById(R.id.add_note);
        addNote.setOnClickListener(this);
        entries =  new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new EntriesAdapter(this,this,entries);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        lecture = (LecturesAdapter.Lecture) getIntent().getSerializableExtra(SOURCE_PARAM);
        getSupportActionBar().setTitle(lecture.getName());

    }
    private void loadData() {
        if(loadDataTask == null)
        {
            loadDataTask = new LoadDataTask();
            loadDataTask.execute();
        }
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        dialog.dismiss();
        File folder = new File(Environment.getExternalStorageDirectory(),"My Guc Records");
        folder.mkdirs();
        String recordName = Helper.getNameWithoutExt(file.getName());
        entry =  new EntriesAdapter.RecordEntry(0,lecture.getId(),"",
                System.currentTimeMillis(),recordName,file.getAbsolutePath());
        addEntry();
    }

    @Override
    public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

    }

    private class LoadDataTask extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideAll();
            progressWheel.setVisibility(View.VISIBLE);
            entries.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (dbHelper.getReadableDatabase() != null) {
                Cursor cursor = dbHelper.getReadableDatabase().query(MyGucDatabaseHelper.ENTRIES_TABLE,
                        new String[]{MyGucDatabaseHelper.NAME_COL, "_id", MyGucDatabaseHelper.DATE_COL,
                                MyGucDatabaseHelper.NOTE_COL,
                                MyGucDatabaseHelper.SOURCE_ID_COL, MyGucDatabaseHelper.TYPE_COL,
                                MyGucDatabaseHelper.DURATION_COL, MyGucDatabaseHelper.PATH_COL},
                        MyGucDatabaseHelper.SOURCE_ID_COL + "=?"
                        , new String[]{lecture.getId() + ""}, null, null, null);
                Log.d("Entry","Count " + cursor.getCount());
                if (cursor.moveToFirst())
                    for (int i = 0; i < cursor.getCount(); i++)
                        if (cursor.moveToPosition(i)) {
                            EntriesAdapter.Entry entry;
                            int type = cursor.getInt(cursor.getColumnIndex(MyGucDatabaseHelper.TYPE_COL));
                            long id = cursor.getLong(cursor.getColumnIndex("_id"));
                            long sourceId = cursor.getLong(cursor.getColumnIndex(MyGucDatabaseHelper.SOURCE_ID_COL));
                            String note = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.NOTE_COL));
                            String path = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.PATH_COL));
                            String name = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.NAME_COL));
                            long date = cursor.getLong(cursor.getColumnIndex(MyGucDatabaseHelper.DATE_COL));
                            long duration = cursor.getLong(cursor.getColumnIndex(MyGucDatabaseHelper.DURATION_COL));
                            switch (type) {
                                case EntriesAdapter.Entry.TYPE_IMAGE:
                                case EntriesAdapter.Entry.TYPE_VIDEO:
                                    entry = new EntriesAdapter.ImageEntry(id,
                                            sourceId, note, date, path, type == EntriesAdapter.Entry.TYPE_VIDEO);
                                    break;
                                case EntriesAdapter.Entry.TYPE_RECORD:
                                    entry =  new EntriesAdapter.RecordEntry(id,
                                            sourceId,note,date,name,path);
                                    break;
                                    default:
                                        entry = new EntriesAdapter.Entry(id,sourceId,note,date);
                                        break;
                            }
                            entries.add(entry);
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
            if(entries.size() > 0)
                adapter.notifyDataSetChanged();
            showAll();


        }
    }

    private void hideAll()
    {
        recyclerView.setVisibility(View.GONE);
        fabMenu.setVisibility(View.GONE);
    }
    private void showAll()
    {
        recyclerView.setVisibility(View.VISIBLE);
        fabMenu.setVisibility(View.VISIBLE);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.add_image:
                addImage(false);
                break;
            case R.id.add_video:
                addImage(true);
                break;
            case R.id.add_record:
                addRecord();
                break;
            default:
                addNote(false);
                break;
        }
    }
    private void addRecord()
    {
        new MaterialDialog.Builder(this)
                .title("Add audio")
                .positiveText("From phone")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        new FileChooserDialog.Builder(EntryActivity.this)
                                .mimeType("audio/*")
                                .show(getSupportFragmentManager());
                    }
                })
                .negativeText("Record voice")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        new MaterialDialog.Builder(EntryActivity.this)
                                .canceledOnTouchOutside(false)
                                .title("Give your record a name")
                                .input("Name", "", false, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                        File folder = new File(Environment.getExternalStorageDirectory(),"My Guc Records");
                                        folder.mkdirs();
                                        String recordName = input.toString();
                                        String recordPath = Helper.createNewFile(new File(folder.getAbsolutePath(),recordName+".wav"),recordName+".wav").getAbsolutePath();
                                        entry =  new EntriesAdapter.RecordEntry(0,lecture.getId(),"",
                                                System.currentTimeMillis(),recordName,recordPath);
                                        addEntry();

                                        AudioRecordService.startRecordingService(EntryActivity.this,recordPath,recordName);

//
                                    }
                                }).show();
                    }
                }).show();



    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            for(String path :  mPaths)
            {
                entry =  new EntriesAdapter.ImageEntry(0,lecture.getSubjectId(),
                           "",System.currentTimeMillis(),path,false);
                if(mPaths.size() == 1)
                    addNote(true);
                else
                    addEntry();
            }

        }
        else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_PATH);
            for(String path :  mPaths)
            {
                entry =  new EntriesAdapter.ImageEntry(0,lecture.getSubjectId(),
                        "",0,path,true);
                if(mPaths.size() == 1)
                    addNote(true);
                else
                    addEntry();
            }

        }

    }
    private void addEntry()
    {
        long id = saveItem(entry,entry.getType());
        Log.d("Entry",id+"");
        if(id!= -1)
            entry.setId(id);
        entries.add(entry);
        entry = null;
        adapter.notifyItemInserted(entries.size() - 1);
        recyclerView.scrollToPosition(entries.size() - 1);
    }
    private void addNote(boolean isOptional)
    {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE| InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .title("Note").canceledOnTouchOutside(false)
                .input("Write a note here" + (isOptional ? "(optional)" : ""), "", isOptional, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Editable input  = dialog.getInputEditText().getText();
                        if(entry == null)
                            entry =  new EntriesAdapter.Entry(0,lecture.getId(),"",System.currentTimeMillis());
                        if(input!=null)
                            entry.setNote(input.toString());
                        addEntry();
                        entry = null;
                    }
                }).negativeText("Cancel").cancelable(false).canceledOnTouchOutside(false).build();
        EditText editText = dialog.getInputEditText();
        editText.setSingleLine(false);
        editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        dialog.show();
    }
    private void addImage(final boolean isVideo) {
        if(!isVideo) {
            new ImagePicker.Builder(this)
                    .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                    .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
                    .directory(ImagePicker.Directory.DEFAULT)
                    .extension(ImagePicker.Extension.PNG)
                    .scale(600, 600)
                    .allowMultipleImages(true)
                    .enableDebuggingMode(true)
                    .build();
        }
        else
        {
            new VideoPicker.Builder(this)
                    .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                    .directory(VideoPicker.Directory.DEFAULT)
                    .extension(VideoPicker.Extension.MP4)
                    .enableDebuggingMode(true)
                    .build();
        }

    }

    public long  saveItem(EntriesAdapter.Entry entry,int type)
    {
        if(dbHelper.getWritableDatabase()!=null)
        {
            ContentValues values =  new ContentValues();
            values.put(MyGucDatabaseHelper.TYPE_COL,type);
            values.put(MyGucDatabaseHelper.SOURCE_ID_COL,lecture.getId());
            values.put(MyGucDatabaseHelper.NOTE_COL,entry.getNote());
            values.put(MyGucDatabaseHelper.DATE_COL,entry.getDate());
            switch ( type)
            {
                case EntriesAdapter.Entry.TYPE_IMAGE:
                case EntriesAdapter.Entry.TYPE_VIDEO:
                    EntriesAdapter.ImageEntry imageEntry = (EntriesAdapter.ImageEntry) entry;
                    values.put(MyGucDatabaseHelper.PATH_COL,imageEntry.getImagePath());
                    break;
                case EntriesAdapter.Entry.TYPE_RECORD:
                    EntriesAdapter.RecordEntry recordEntry =  (EntriesAdapter.RecordEntry) entry;
                    values.put(MyGucDatabaseHelper.PATH_COL,recordEntry.getRecordPath());
                    values.put(MyGucDatabaseHelper.NAME_COL,recordEntry.getName());
                    values.put(MyGucDatabaseHelper.DURATION_COL,recordEntry.getDuration(this));

                    break;
            }
           return dbHelper.getWritableDatabase().insert(MyGucDatabaseHelper.ENTRIES_TABLE,null,values);
        }
        return  -1;
    }






    @Override
    public void onEntryClick(EntriesAdapter.Entry entry, int pos) {
        switch (entry.getType())
        {
           case EntriesAdapter.Entry.TYPE_IMAGE: case EntriesAdapter.Entry.TYPE_VIDEO:
            File file = new File(((EntriesAdapter.ImageEntry)entry).getImagePath());
            Helper.openFile(this,file);
            break;
            default:
                onEntryNote(entry,pos);
                break;

        }
    }

    @Override
    public void onEntryDeleted(final EntriesAdapter.Entry entry, final int pos) {
        if(entry.getType() == EntriesAdapter.Entry.TYPE_NOTE)
        {
            fastDelete(dbHelper,entry.getId());
            entries.remove(pos);
            adapter.notifyItemRemoved(pos);

        }
        else
        {
            new MaterialDialog.Builder(this)
                    .title("Deleting media")
                    .content("Are you sure you want to delete this entirely?")
                    .positiveText("Yes")
                    .negativeText("No,Only from the app")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            fastDelete(dbHelper,entry.getId());
                            deepDelete(entry);
                            entries.remove(pos);
                            adapter.notifyItemRemoved(pos);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            fastDelete(dbHelper,entry.getId());
                            entries.remove(pos);
                            adapter.notifyItemRemoved(pos);
                        }
                    })
                    .show();

        }


    }

    public static void fastDelete(MyGucDatabaseHelper dbHelper,long id)
    {
        if(dbHelper.getWritableDatabase()!=null)
            dbHelper.getWritableDatabase().delete(MyGucDatabaseHelper.ENTRIES_TABLE,"_id=?",new String[]{id+""});

    }
    public static void deepDelete(EntriesAdapter.Entry entry)
    {
        int type = entry.getType();
        if(type == EntriesAdapter.Entry.TYPE_IMAGE || type == EntriesAdapter.Entry.TYPE_VIDEO)
        {
            EntriesAdapter.ImageEntry imageEntry = (EntriesAdapter.ImageEntry) entry;
            deepDelete(imageEntry.getImagePath());
        }
        else if(type == EntriesAdapter.Entry.TYPE_RECORD)
        {
            EntriesAdapter.RecordEntry imageEntry = (EntriesAdapter.RecordEntry) entry;
            deepDelete(imageEntry.getRecordPath());
        }
    }
    public static void deepDelete(String path)
    {
        boolean success = new File(path).delete();
        Log.d("Delete" ,"Deleting "  + path  + " " + success );
    }
    @Override
    public void onEntryShared(EntriesAdapter.Entry entry, int pos) {
        File file = null;
        switch (entry.getType()) {
            case EntriesAdapter.Entry.TYPE_IMAGE:
            case EntriesAdapter.Entry.TYPE_VIDEO:
                EntriesAdapter.ImageEntry imageEntry = (EntriesAdapter.ImageEntry) entry;
                file =  new File(imageEntry.getImagePath());
                break;
            case EntriesAdapter.Entry.TYPE_RECORD:
                EntriesAdapter.RecordEntry recordEntry = (EntriesAdapter.RecordEntry) entry;
                file =  new File(recordEntry.getRecordPath());
                break;
        }
        if(file!=null)
            Helper.shareFile(this,file);
        else
            Helper.shareText(this,entry.getNote());
    }

    @Override
    public void onEntryNote(final EntriesAdapter.Entry entry, int pos) {
        new MaterialDialog.Builder(this)
                .positiveText("Copy")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("My Guc", entry.getNote());
                        clipboard.setPrimaryClip(clip);                    }
                })
                .content(entry.getNote())
                .show();
    }

    @Override
    public void onPopUpMenu(final EntriesAdapter.Entry entry, View v, final int pos) {
        final PopupMenu menu = new PopupMenu(this,v);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.delete:
                        onEntryDeleted(entry,pos);
                        return true;
                    case R.id.share:
                        onEntryShared(entry,pos);
                        return true;
                }
                return false;
            }
        });
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.entry_menu,menu.getMenu());
        menu.show();
    }

    @Override
    public void onPlayPause(EntriesAdapter.RecordEntry recordEntry, int pos) {
        Helper.openFile(this,new File(recordEntry.getRecordPath()));
    }


    @Override
    protected void onStart() {
        if(dbHelper == null) {
            dbHelper = new MyGucDatabaseHelper(this);
            loadData();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(dbHelper!=null)
        {
            dbHelper.close();
            dbHelper = null;
        }
        super.onStop();
    }
}
