package matt.updater;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    private static final String UPDATE_URL = "http://www.mshieldprotect.com.br/uploads/apps/mShield.apk";
    private static final String UPDATE_PATH = "/sdcard/Your Folder/YourApp.apk";
    private ProgressBar pbProgress;
    private Button btnDirectly;
    private Button btnDialog;
    private TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        btnDirectly = (Button) findViewById(R.id.btnDirectly);
        btnDialog = (Button) findViewById(R.id.btnDialog);
        pbProgress.setMax(100);

        //Force making all the dirs.
        File updateFile = new File(UPDATE_PATH);
        updateFile.mkdirs();
        updateFile.delete();

        //Changelog description sample
        final StringBuilder sb = new StringBuilder();
        sb.append("There's an update available, how do you want to proceed?\n\n");
        sb.append("Changelog\n");
        sb.append("• 1.2 - Bug adjustment.\n\n");
        sb.append("• 1.1 - Update Dialog button added.");

        //Setting our updater
        final MUpdater mUpdater = new MUpdater(this, UPDATE_URL, UPDATE_PATH, new MUpdater.MProgress()
        {
            @Override
            public void onProgressUpdate(int progress)
            {
                tvProgress.setText(String.valueOf(progress) + "%");
                pbProgress.setProgress(progress);
            }
        });

        //Update Directly Click
        btnDirectly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mUpdater.execute();
            }
        });

        //Update Dialog Click
        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mUpdater.showDialog(getResources().getString(R.string.app_name), sb, "Atualizar", "Cancelar");
            }
        });
    }
}