package com.lzmouse.myguc.Login;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.MyGucDatabaseHelper;
import com.lzmouse.myguc.R;

import java.io.IOException;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login";
    public static final String USER_NAME_EXTRA = "username";
    public static final String PASSWORD_EXTRA = "password";
    public static final int REQUEST_CODE  = 0;
    public static final int RESULT_FAILED = 0;

    private EditText _usernameText;
    private EditText _passwordText;
    private Button _loginButton;

    private LoginTask loginTask;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        _usernameText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _loginButton =  findViewById(R.id.btn_login);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });


    }
    private class LoginTask extends AsyncTask<Void,Void,Boolean>
    {
        private SweetAlertDialog dialog;
        private MyGucDatabaseHelper dpHelper;
        private String username,password;

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dpHelper = new MyGucDatabaseHelper(LoginActivity.this);
            dialog = new SweetAlertDialog(LoginActivity.this,SweetAlertDialog.PROGRESS_TYPE);
            dialog.getProgressHelper().setBarColor(Helper.getColor(LoginActivity.this,R.color.colorAccent));
            dialog.setTitleText("Please wait...");
            dialog.setContentText("Logging in");
            dialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            return testLogin(username,password);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            if(result) {
                Student.getInstance(username,password);
                MyGucDatabaseHelper dpHelper = new MyGucDatabaseHelper(LoginActivity.this);
                if(dpHelper.getWritableDatabase()!=null)
                {

                    boolean isUpdated = false;
                    if(dpHelper.getReadableDatabase()!=null) {
                        Cursor cursor = dpHelper.getReadableDatabase().query(MyGucDatabaseHelper.ACCOUNT_TABLE
                                , new String[]{MyGucDatabaseHelper.USER_NAME_COL, MyGucDatabaseHelper.PASSWORD_COL}
                                , null, null, null, null, null);
                        if (cursor.moveToFirst())
                        {
                            ContentValues val =  new ContentValues();
                            val.put(MyGucDatabaseHelper.USER_NAME_COL,username);
                            val.put(MyGucDatabaseHelper.PASSWORD_COL,password);
                            dpHelper.getWritableDatabase().update(MyGucDatabaseHelper.ACCOUNT_TABLE,val
                                    ,"_id=" + cursor.getInt(cursor.getColumnIndex("_id")),null);
                            isUpdated = true;
                        }
                        cursor.close();
                    }
                  if(!isUpdated)
                  {
                      ContentValues val =  new ContentValues();
                      val.put(MyGucDatabaseHelper.USER_NAME_COL,username);
                      val.put(MyGucDatabaseHelper.PASSWORD_COL,password);
                      dpHelper.getWritableDatabase().insert(MyGucDatabaseHelper.ACCOUNT_TABLE,null,val);
                  }
                }
                dpHelper.close();
                dialog.setTitleText("Logged in");
                dialog.setContentText("You can use intranet now");
                dialog.setConfirmText("Open");
                dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
            else {
                dialog.setTitleText("Failed to login ");
                dialog.setContentText("Something went wrong");
                dialog.hideConfirmButton();
                dialog.setCancelButton("Try again", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        _loginButton.setEnabled(true);
                        sweetAlertDialog.dismiss();
                    }
                });
                dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);

            }
            loginTask = null;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            Toast.makeText(this,"Please enter valid username and password",Toast.LENGTH_LONG);
            return;
        }

        _loginButton.setEnabled(false);
        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if(loginTask == null) {
            loginTask = new LoginTask(username,password);
            loginTask.execute();
        }
    }







    public boolean validate() {
        boolean valid = true;

        String email = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty()) {
            _usernameText.setError("enter a valid email address");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public static boolean testLogin(String username, String password) {
        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(new NTLMAuthenticator(username, password))
                .build();
        try {
            Response response = client.newCall(new Request.Builder().url("http://student.guc.edu.eg").build()).execute();
            Log.d(TAG, "Login in Result: " + response.isSuccessful());
           return response.isSuccessful();
        } catch (IOException e) {
            Log.e(TAG, "Failed to testLogin", e);
            return false;
        }
    }
}
