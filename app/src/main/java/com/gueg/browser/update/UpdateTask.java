package com.gueg.browser.update;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;
import android.widget.Toast;
import com.gueg.browser.R;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class UpdateTask extends AsyncTask<String,Integer,String> {

    public static final String UPDATE_LINK = "https://dl.dropboxusercontent.com/s/jbwi5eqjcvbod8g/update.txt?dl=0";
    public static final int SHOW_TOAST = 0;
    public static final int NO_TOAST = 1;
    private Activity context;
    private ProgressDialog dialog;

    private int toast;

    public UpdateTask(Activity c, int toast) {
        context = c;
        dialog = new ProgressDialog(c, R.style.ProgressDialogTheme);
        dialog.setTitle("Téléchargement de la mise à jour...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        this.toast = toast;
    }

    protected String doInBackground(String... sUrl) {
        // PARSING MANIFEST
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/update.txt";
        try {
            URL url = new URL(UPDATE_LINK);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder manifest = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;

            while ((line = br.readLine()) != null) {
                manifest.append(line);
                manifest.append("~");
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        // READING MANIFEST
        int latestVersion = Integer.decode(manifest.substring(0, manifest.indexOf("~")));
        manifest.substring(manifest.indexOf("~"));
        int currentVersion = 999;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentVersion = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new File(path).delete();

        // DOWNLOADING LATEST VERSION
        if (currentVersion < latestVersion) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
            String path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/browser.apk";
            try {
                URL url = new URL(manifest.substring(manifest.indexOf("~")).replace("~",""));
                URLConnection connection = url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(path2);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return path2;
        }
        Toast.makeText(context, "Aucune mise à jour disponible.", Toast.LENGTH_SHORT).show();
        return null;
    }

    // begin the installation by opening the resulting file
    @Override
    protected void onPostExecute(String path) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
        if(path!=null) {
            /*
                Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                i.setData(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.gueg.browser.provider", new File(path)));
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(i);
            */
            File toInstall = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.gueg.browser.provider", toInstall);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } else {
                Uri apkUri = Uri.fromFile(toInstall);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } else {
            if(toast==SHOW_TOAST)
                Toast.makeText(context, "Aucune mise à jour disponible.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
        super.onProgressUpdate(values);
    }
}
