package ru.a402d.demorawbt;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickDemo(View v){
        String dataToPrint = "This is a printer test\n";
        sendTextToPrint(dataToPrint);
    }


    protected void sendTextToPrint(String dataToPrint){
        final String appPackageName = "ru.a402d.rawbtprinter";
        PackageManager pm = getPackageManager();

        // check app installed
        PackageInfo pi = null;
        if(pm != null) {
            try {
                pi = pm.getPackageInfo(appPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(pi == null){
            // go to install
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }else{
            // send to print
            Intent intentPrint = new Intent();
            intentPrint.setAction(Intent.ACTION_SEND);
            intentPrint.setPackage(appPackageName);
            intentPrint.putExtra(Intent.EXTRA_TEXT, dataToPrint);
            intentPrint.setType("text/plain");
            this.startActivity(intentPrint);

        }
    }
}
