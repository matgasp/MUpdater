package matt.updater;

import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * #######################################
 * Created by Matt (matgasp) on 08/10/2016.
 * ############# Dependences #############
 * <uses-permission android:name="android.permission.INTERNET"></uses-permission>
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
 * #######################################
 */

public class MUpdater
{
    private DialogInterface.OnClickListener dialogInterface;
    private MProgress mProgress;
    private String lastError;
    private Context context;
    private Dialog dialog;
    private String path;
    private String url;

    public interface MProgress
    {
        void onProgressUpdate(int progress);
    }

    public MUpdater(Context context, String url, String path)
    {
        setUrl(url);
        setPath(path);
        setContext(context);
    }

    public MUpdater(Context context, String url, String path, MProgress mProgress)
    {
        this(context, url, path);
        setOnProgress(mProgress);
    }

    public void setOnProgress(MProgress mProgress)
    {
        this.mProgress = mProgress;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void setPath(String mPath)
    {
        this.path = mPath;
    }

    public void setUrl(String mUrl)
    {
        this.url = mUrl;
    }

    public void setLastError(String lastError)
    {
        this.lastError = lastError;
    }

    public void execute()
    {
        new MUpdaterAsyncTask().execute(url);
    }

    public void showDialog(String title, CharSequence message, String positiveButton, String negativeButton)
    {
        if (dialogInterface == null)
        {
            dialogInterface = new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case Dialog.BUTTON_POSITIVE:
                            execute();
                    }

                }
            };
        }

        Builder builder = new Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButton, dialogInterface);
        builder.setNegativeButton(negativeButton, dialogInterface);
        dialog = builder.create();
        dialog.show();
    }

    private class MUpdaterAsyncTask extends AsyncTask<String, int[], Boolean>
    {
        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(path);

                byte data[] = new byte[1024];
                int progress = 0;
                int bytesRead;
                while ((bytesRead = input.read(data)) != -1)
                {
                    progress += bytesRead;
                    publishProgress(new int[]{progress, fileLength});
                    output.write(data, 0, bytesRead);
                }

                output.flush();
                output.close();
                input.close();
            }
            catch (Exception e)
            {
                setLastError(e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(int[]... values)
        {
            super.onProgressUpdate(values);

            if (mProgress != null)
            {
                mProgress.onProgressUpdate(values[0][0] * 100 / values[0][1]);
            }
        }

        @Override
        protected void onPostExecute(Boolean isSuccess)
        {
            if (isSuccess)
            {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                context.startActivity(i);
            }
            else
            {
                Toast.makeText(context, "Something wrong have happened: " + lastError, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
