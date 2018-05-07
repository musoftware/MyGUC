package com.lzmouse.myguc.Main;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.Intranet.FavoritesActivity;
import com.lzmouse.myguc.Intranet.FilesActivity;
import com.lzmouse.myguc.Intranet.IntranetActivity;
import com.lzmouse.myguc.IntroActivity;
import com.lzmouse.myguc.Login.Student;
import com.lzmouse.myguc.Login.LoginActivity;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.Notebook.NotebookActivity;
import com.lzmouse.myguc.R;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity implements MainAdapter.Listener {

    private static final String TAG = "MainActivity";
    private LoginTask loginTask;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        if(getSharedPreferences("INTRO",MODE_PRIVATE).getBoolean("IS_FIRST",true))
            startActivity(new Intent(this,IntroActivity.class));
        setContentView(R.layout.activity_main);
        List<MainAdapter.Item> items = new ArrayList<>();
        items.add(new MainAdapter.Item("Intranet",R.drawable.ic_fac));
        items.add(new MainAdapter.Item("Downloads",R.drawable.ic_download));
        items.add(new MainAdapter.Item("Favorites",R.drawable.ic_fav));
        items.add(new MainAdapter.Item("Notebook",R.drawable.ic_notebook));
        RecyclerView recyclerView =  findViewById(R.id.rec);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        MainAdapter adapter = new MainAdapter(this,this, items);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }
    private  enum LoginResult{SUCCESS,FAILED,FIRST_LOGIN};
    private class LoginTask extends AsyncTask<Void,Void,LoginResult>
   {
       private SweetAlertDialog dialog;
       private MyGucDatabaseHelper dpHelper;
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           dpHelper = new MyGucDatabaseHelper(MainActivity.this);
           dialog = new SweetAlertDialog(MainActivity.this,SweetAlertDialog.PROGRESS_TYPE);
           dialog.getProgressHelper().setBarColor(Helper.getColor(MainActivity.this,R.color.colorAccent));
           dialog.setTitleText("Please wait...");
           dialog.setContentText("Logging in");
           dialog.show();
       }
       @Override
       protected LoginResult doInBackground(Void... voids) {
            boolean isSavedNull = false;
           if(dpHelper.getReadableDatabase()==null) {
               isSavedNull = true;

           }
           Cursor cursor = dpHelper.getReadableDatabase().query(MyGucDatabaseHelper.ACCOUNT_TABLE
                   ,new String[] {MyGucDatabaseHelper.USER_NAME_COL,MyGucDatabaseHelper.PASSWORD_COL}
                   ,null,null,null,null,null);
           if(!cursor.moveToFirst()) {
               isSavedNull = true;
               cursor.close();
           }
           String username = "";
           String password = "";
           if(isSavedNull) {
              Student student =  Student.getInstance();
              if(student == null)
                  return LoginResult.FIRST_LOGIN;
              username = student.getUsername();
              password =student.getPassword();
           }
           else {
               username = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.USER_NAME_COL));
               password = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.PASSWORD_COL));
               cursor.close();
           }
           if(username == null || password == null)
               return LoginResult.FIRST_LOGIN;

          if(LoginActivity.testLogin(username,password))
           {
               Student.getInstance(username,password);
               return LoginResult.SUCCESS;
           }
           return LoginResult.FAILED;
       }

       @Override
       protected void onPostExecute(LoginResult result) {
           super.onPostExecute(result);
           dpHelper.close();
           String message = "You need to login first";
           int type = SweetAlertDialog.WARNING_TYPE;
           switch (result)
           {

               case SUCCESS:
                   dialog.dismissWithAnimation();
                   if(Student.getInstance()!=null) {
                       Intent i = new Intent(MainActivity.this, IntranetActivity.class);
                       startActivity(i);
                   }
                   break;
               case FAILED:
                   message  = "Please update your username and password";
                   type = SweetAlertDialog.ERROR_TYPE;
               default:
                   dialog.setTitleText("Logging in");
                   dialog.setContentText(message);
                   dialog.changeAlertType(type);
                   dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                       @Override
                       public void onClick(SweetAlertDialog sweetAlertDialog) {
                           Intent i =  new Intent(MainActivity.this,LoginActivity.class);
                           Student student = Student.getInstance();
                           if(student != null) {
                               i.putExtra(LoginActivity.USER_NAME_EXTRA, Student.getInstance().getUsername());
                               i.putExtra(LoginActivity.PASSWORD_EXTRA, Student.getInstance().getPassword());
                           }
                           startActivityForResult(i,LoginActivity.REQUEST_CODE);
                       }

                   });
           }
           loginTask = null;
       }
       @Override
       protected void onProgressUpdate(Void... values) {
           super.onProgressUpdate(values);
       }

       @Override
       protected void onCancelled(LoginResult result) {
           super.onCancelled(result);
       }

       @Override
       protected void onCancelled() {
           super.onCancelled();
       }


   }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case LoginActivity.REQUEST_CODE:
                if(resultCode == RESULT_OK && Student.getInstance()!=null)
                {
                    Intent i = new Intent(MainActivity.this, IntranetActivity.class);
                    startActivity(i);

                }
                else
                    Log.d(TAG,"Failed testLogin");
                break;
        }
    }

    @Override
    public void onClick(MainAdapter.Item item) {
        Intent i;
        Log.d(TAG,item.getText());
        switch (item.getText())
        {
            case "Intranet":
                if(loginTask!=null)
                    return;
                loginTask = new LoginTask();
                loginTask.execute();
                return;
            case "Favorites":
                i  =  new Intent(this,FavoritesActivity.class);
                break;
            case "Notebook":
                i =  new Intent(this, NotebookActivity.class);
                break;
            case "Downloads":
            default:
                i =  new Intent(this, FilesActivity.class);
                break;

        }
        startActivity(i);
    }
}
