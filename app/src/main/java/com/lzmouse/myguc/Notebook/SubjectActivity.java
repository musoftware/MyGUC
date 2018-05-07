package com.lzmouse.myguc.Notebook;

import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.lzmouse.myguc.R;

import java.io.File;

public class SubjectActivity extends AppCompatActivity implements FileChooserDialog.FileCallback{

    public static final String SUBJECT_EXTRA  = "subject";
    private SubjectsAdapter.Subject subject;
    private FilesFragment filesFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        fillData(subject = getSubject(savedInstanceState));
        ViewPager pager =  findViewById(R.id.pager);
        pager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }
    @NonNull
    private SubjectsAdapter.Subject getSubject(Bundle bundle)
    {
        if(bundle !=  null)
            return (SubjectsAdapter.Subject) bundle.getSerializable(SUBJECT_EXTRA);
        return (SubjectsAdapter.Subject) getIntent().getSerializableExtra(SUBJECT_EXTRA);
    }
    private void fillData(SubjectsAdapter.Subject subject)
    {
        if(subject ==null) {
            finish();
            return;
        }
        getSupportActionBar().setTitle(subject.getName());
        TextView name =  findViewById(R.id.name);
        TextView ls = findViewById(R.id.lectures);
        TextView ts = findViewById(R.id.tuts);
        TextView fs = findViewById(R.id.files);
        name.setText(subject.getName());
        name.setText(subject.getName());
        name.setBackgroundColor(subject.getColor());
        fs.setText(String.format("Files: %02d", subject.getFiles()));
        ts.setText(String.format("Tutorials: %02d", subject.getTuts()));
        ls.setText(String.format("Lectures: %02d", subject.getLectures()));
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        if(filesFragment!=null)
            filesFragment.onFileSelection(dialog,file);
    }

    @Override
    public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

    }

    private  class FragmentAdapter extends FragmentStatePagerAdapter
    {

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                default:
                    return LecturesFragment.newInstance(LecturesFragment.LECTURES_TYPE, subject.getUid(), subject.getColor());
                case 1:
                    return LecturesFragment.newInstance(LecturesFragment.TUTORIALS_TYPE, subject.getUid(), subject.getColor());
                case 2:
                    return filesFragment = FilesFragment.newInstance(subject.getUid());
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "Lecturers";
                case 1:
                    return "Tutorials";
                case 2:
                    return "Files";
            }
            return "";
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putSerializable(SUBJECT_EXTRA,subject);
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
