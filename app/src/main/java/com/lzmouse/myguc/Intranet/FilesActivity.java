package com.lzmouse.myguc.Intranet;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends AppCompatActivity implements FilesAdapter.FileListener {

    private List<FilesAdapter.FileInfo> files;
    private FilesAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        files =  new ArrayList<>();
        getFiles();
        RecyclerView recyclerView =  findViewById(R.id.rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter =  new FilesAdapter(this,this,files);
        recyclerView.setAdapter(adapter);
    }
    private void getFiles()
    {
        files.clear();
        File folder = new File(Environment.getExternalStorageDirectory(),"My Guc");
        if(folder.listFiles()!=null)
        {
            for(File file : folder.listFiles()) {
                if(file.isFile())
                    files.add(new FilesAdapter.FileInfo(file));
            }
        }

    }



    @Override
    public void onFileClick(FilesAdapter.FileInfo fileInfo, int pos) {
       Helper.openFile(this,fileInfo.getFile());
    }

    @Override
    public void onDeleteClick(FilesAdapter.FileInfo fileInfo, int pos) {
        if(fileInfo.getFile().delete()) {
            files.remove(pos);
            adapter.notifyItemRemoved(pos);
        }
    }

    @Override
    public void onShareClick(FilesAdapter.FileInfo fileInfo, int pos) {
        Helper.shareFile(this,fileInfo.getFile());
    }
}
