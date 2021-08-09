package com.app.spark.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.app.spark.fragment.profile.ProfileFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Build.VERSION_CODES.R;

public class DownloadTask
{
        private static final String TAG = "Download Task";
        private Context context;

        private String downloadUrl = "", downloadFileName = "" ;
        private ProgressDialog progressDialog;
         File outputFile = null;
        public DownloadTask(Context context, String downloadUrl) {
            this.context = context;

            this.downloadUrl = downloadUrl;



            downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/'), downloadUrl.length());//Create file name by picking download file name from URL
            Log.e(TAG, downloadFileName);

            //Start Downloading Taskfule
            new DownloadingTask().execute();





        }










private class DownloadingTask extends AsyncTask<Void, Void, Void> {

    File apkStorage = null;


    @Override
    protected void onPreExecute() {
//        super.onPreExecute();
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setMessage("Loading...");
//        progressDialog.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onPostExecute(Void result) {
        try {
            if (outputFile != null) {
 //               progressDialog.dismiss();
//                ContextThemeWrapper ctw = new ContextThemeWrapper( context, null);
//                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
//                alertDialogBuilder.setTitle("Message");
//                alertDialogBuilder.setMessage("Do you want to open your Invoice");
//                alertDialogBuilder.setCancelable(false);
//                alertDialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                    }
//                });
//
//                alertDialogBuilder.setNegativeButton("Open",new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/connectedindia/" + downloadFileName+".pdf");  // -> filename = maven.pdf
//                        Uri path = Uri.fromFile(pdfFile);
//                        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
//                        if(path.toString().contains(".pdf")) {
//                            pdfIntent.setDataAndType(path, "application/pdf");
//                        }else if(path.toString().contains(".doc")||path.toString().contains(".docx"))
//                        {
//                            pdfIntent.setDataAndType(path, "application/msword");
//                        }
//                        else
//                        {
//                            pdfIntent.setDataAndType(path, "application/pdf");
//                        }
//                        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                        try{
//
//                            context.startActivity(pdfIntent);
//
//                        }catch(ActivityNotFoundException e){
//                            Toast.makeText(context, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//                alertDialogBuilder.show();
          //      Toast.makeText(context, "You Can See your local data in Connectindia folder", Toast.LENGTH_SHORT).show();
            //    ProfileFragment profileFragment =new ProfileFragment();

             //   profileFragment.getdata(outputFile.getPath());
            } else {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 30000);

                Log.e(TAG, "Download Failed");

            }
        } catch (Exception e) {
            e.printStackTrace();

            //Change button text if exception occurs

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 30000);
            Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());

   //         progressDialog.dismiss();
            Toast.makeText(context, "Download Failed with Exception", Toast.LENGTH_SHORT).show();

        }


        super.onPostExecute(result);
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            URL url = new URL(downloadUrl);//Create Download URl
            HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
            c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
         //   c.setDoOutput(true);
            c.connect();//connect the URL Connection

            //If Connection response is not OK then show Logs
            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage());

            }


            //Get File if SD card is present
            if (new CheckForSDCard().isSDCardPresent()) {

                apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + "connectedindia");
            } else
                Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

            //If File is not present create directory
            if (!apkStorage.exists()) {
                apkStorage.mkdir();
                Log.e(TAG, "Directory Created.");
            }

            outputFile = new File(apkStorage, downloadFileName);//Create Output file in Main File


            //Create New File if not present
            if (!outputFile.exists()) {
                outputFile.createNewFile();
                Log.e(TAG, "File Created");
            }

            String output_string = outputFile.getPath();

             Log.e("+++++++++1",outputFile.getPath());
             Log.e("+++++++++2",outputFile.getAbsolutePath());
             Log.e("+++++++++3",outputFile.getName());
             Log.e("+++++++++4",output_string.substring(output_string.lastIndexOf('/'), output_string.length()));
             Log.e("+++++++++5",apkStorage.getPath());
             Log.e("+++++++++6",apkStorage.getAbsolutePath());
             Log.e("+++++++++7",Environment.getExternalStorageDirectory() + "/" + "connectedindia" + outputFile.getName());


            FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

            InputStream is = c.getInputStream();//Get InputStream for connection

            byte[] buffer = new byte[2048];//Set buffer type
            int len1 = 0;//init length
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);//Write new file
            }

            //Close all connection after doing task
            fos.close();
            is.close();

        } catch (Exception e) {

            //Read exception if something went wrong
            e.printStackTrace();
            outputFile = null;
            Log.e(TAG, "Download Error Exception " + e.getMessage());
    //        progressDialog.dismiss();
   //        Toast.makeText(context, "Download Failed with Exception", Toast.LENGTH_SHORT).show();




        }

        return null;
    }
}

}

