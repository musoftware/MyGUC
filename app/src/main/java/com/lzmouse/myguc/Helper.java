package com.lzmouse.myguc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Ahmed Ali on 3/19/2018.
 */

public class Helper {
    public static int getColor(Resources resources, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return resources.getColor(resId, null);
        else
            return resources.getColor(resId);
    }

    public static int getColor(Context context, int resId) {
        return getColor(context.getResources(), resId);
    }
    public static String getNameWithoutExt(String fname) {

        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            return fname.substring(0, pos);
        }
        return fname;
    }

    public static String getExtension(String fname) {

        String filenameArray[] = fname.split("\\.");
        if (filenameArray.length > 0) {
            return filenameArray[filenameArray.length - 1];
        }

        return "";
    }

    public static String getMimeType(String name) {
        String ext = getExtension(name).toLowerCase();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getMimeTypeFromExtension(ext);
    }

    public static void shareFile(Context context, String path, String mimType) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(mimType);
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
        context.startActivity(Intent.createChooser(i, "Choose application"));
    }
    public static void shareFile(Context context, File file) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(getMimeType(file.getName()));
        i.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".my.package.name.provider", file));
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(i, "Choose application"));
    }
    public static void shareFileWithText(Context context ,String path,String mime,String text)
    {
        Intent shareIntent;
        OutputStream out = null;
        Uri bmpUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".my.package.name.provider", new File(path));
        shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_TEXT,text);
        shareIntent.setType(mime);
        context.startActivity(Intent.createChooser(shareIntent,"Choose application"));
    }
    public static void shareFileWithText(Context context ,File file,String text)
    {
       shareFileWithText(context,file.getAbsolutePath(),getMimeType(file.getName()),text);
    }
    public static void shareText(Context context,String text)
    {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Guc");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(sharingIntent, "Choose application"));
    }
    public static Intent getFileIntent(Context context, String path, String mimType) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri =  FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".my.package.name.provider", new File(path));
        i.setDataAndType(uri, mimType);
        return i;
    }
    public static Intent getFileIntent(Context context,File file) {

        return getFileIntent(context,file.getAbsolutePath(),getMimeType(file.getName()));
    }
    public static void openFile(Context context, String path, String mimType) {
        context.startActivity(Intent.createChooser(getFileIntent(context,path,mimType), "Choose application"));
    }
    public static void openFile(Context context, File file) {
        context.startActivity(Intent.createChooser(getFileIntent(context,file.getAbsolutePath(),getMimeType(file.getName())), "Choose application"));
    }
    @Nullable
    public static Drawable getExtDrawable(Context context, File file) {
        Intent i = new Intent(Intent.ACTION_VIEW);

        i.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".my.package.name.provider", file), getMimeType(file.getName()));
        ResolveInfo info = context.getPackageManager().resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY);

        if(info == null)
            return null;

        return info.loadIcon(context.getPackageManager());


    }
    public static String getReadableBuffer(long buffer) {
        if (buffer <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB", "EB"};
        int digitGroups = (int) (Math.log10(buffer) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(buffer / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    public static Drawable getDrawable(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return context.getResources().getDrawable(resId, null);
        else
            return context.getResources().getDrawable(resId);

    }
    public static String htmlToString(String html)
    {
        if (Build.VERSION.SDK_INT >= 24)
            return Html.fromHtml(html , Html.FROM_HTML_MODE_LEGACY).toString();
        else
        {
            return Html.fromHtml(html).toString();
        }
    }
    public static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.getDefault());

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
