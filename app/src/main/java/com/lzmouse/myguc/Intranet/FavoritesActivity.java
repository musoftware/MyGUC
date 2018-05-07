package com.lzmouse.myguc.Intranet;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.Login.LoginActivity;
import com.lzmouse.myguc.Login.Student;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FavoritesActivity extends AppCompatActivity implements LinksAdapter.Listener {

    private RecyclerView recyclerView;
    private LinksAdapter linkAdapter;
    private List<LinksAdapter.Link> links;
    private SQLiteOpenHelper dpHelper;
    private LoginTask loginTask;
    private LinksAdapter.Link selectedLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        links = new ArrayList<>();

        recyclerView =  findViewById(R.id.rec);
        linkAdapter =  new LinksAdapter(this,this,links,true);

        recyclerView.setAdapter(linkAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

    }
    private void fillLinks()
    {
        if(dpHelper.getReadableDatabase() != null)
        {
            links.clear();
            SQLiteDatabase reader =  dpHelper.getReadableDatabase();
            Cursor cursor =  reader.query(MyGucDatabaseHelper.FAVOURITES_TABLE,
                    new String[] { MyGucDatabaseHelper.PATH_COL,MyGucDatabaseHelper.NAME_COL},
                    null,null,null,null,null);
            if(cursor.moveToFirst())
            {
                while(true)
                {
                    String name = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.NAME_COL));
                    String path = cursor.getString(cursor.getColumnIndex(MyGucDatabaseHelper.PATH_COL));
                    LinksAdapter.Link link = new LinksAdapter.Link(name,path,true,false);
                    links.add(link);
                    if(!cursor.moveToNext())
                        break;
                }
            }
            cursor.close();
        }
        linkAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(LinksAdapter.Link link) {
       if(loginTask == null)
       {
           selectedLink = link;
           loginTask = new LoginTask();
           loginTask.execute();
       }
    }
    private  enum LoginResult{SUCCESS,FAILED,FIRST_LOGIN};
    private class LoginTask extends AsyncTask<Void,Void,LoginResult>
    {
        private SweetAlertDialog dialog;
        private MyGucDatabaseHelper dpHelper;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dpHelper = new MyGucDatabaseHelper(FavoritesActivity.this);
            dialog = new SweetAlertDialog(FavoritesActivity.this,SweetAlertDialog.PROGRESS_TYPE);
            dialog.getProgressHelper().setBarColor(Helper.getColor(FavoritesActivity.this,R.color.colorAccent));
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
                        Intent intent =  new Intent(FavoritesActivity.this,IntranetActivity.class);
                        intent.putExtra(IntranetActivity.LINK_EXTRA,selectedLink);
                        startActivity(intent);
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
                            Intent i =  new Intent(FavoritesActivity.this,LoginActivity.class);
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
                    Intent intent =  new Intent(FavoritesActivity.this,IntranetActivity.class);
                    intent.putExtra(IntranetActivity.LINK_EXTRA,selectedLink);
                    startActivity(intent);

                }
                else
                    Log.d("FAV","Failed testLogin");
                break;
        }
    }

    @Override
    public void onFavStateChanged(LinksAdapter.Link link, boolean newState,int pos) {
        SQLiteDatabase writer = dpHelper.getWritableDatabase();
        if(writer!=null) {

            writer.delete(MyGucDatabaseHelper.FAVOURITES_TABLE,
                    MyGucDatabaseHelper.PATH_COL +"=?",new String[]{link.getLink()});
            links.remove(pos);
            linkAdapter.notifyItemRemoved(pos);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        dpHelper = new MyGucDatabaseHelper(this);
        fillLinks();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dpHelper.close();
    }
}
