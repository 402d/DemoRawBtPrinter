package ru.a402d.demorawbt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String demoStr = "Тест %d пройден \n\n\n"; //  "Test %d completed.\n\n\n"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);

            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);

            }
        }

    }


    /**
     * Checks and if the application is not installed, then offers to download it from the Play Market
     */
    protected void sendToPrint(Intent intent){
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
            intent.setPackage(appPackageName);
            startActivity(intent);

        }
    }

    /*  ==============================================================
     *   RAWBT SCHEME FOR HTML & JS. You may call from Android to.
     *  ================================================================*/

    @OnClick(R.id.test1)
    public void test1(Button button){
        String textToPrint = String.format(Locale.ROOT,demoStr,1);

        // 1) UTF-8 text .  Not available send esc command with chr 128-255 :(

        String url = "rawbt:"+textToPrint;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        sendToPrint(intent);
        button.setText("x");
    }

    @OnClick(R.id.test2)
    public void test2(Button button){

        // 2) Send RAW DATA to printer. You must initiate printer yourself

        byte[] bytesToPrint = new byte[0];
        try {
            bytesToPrint = String.format(Locale.ROOT,demoStr,2).getBytes("cp866");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // encode byte[] to base64
        String base64ToPrint = Base64.encodeToString(bytesToPrint, Base64.DEFAULT);

        // call intent with rawbt:base64,
        String url = "rawbt:base64,"+base64ToPrint;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        sendToPrint(intent);
        button.setText("x");
    }

    /*  ==============================================================
     *   Intent.SEND  EXTRA_TEXT
     *   Not available send esc command with chr 128-255 :(
     *  ================================================================*/

    /**
     *  @see <a href="https://developer.android.com/training/sharing/send#send-text-content">developer.android</a>
     */
    @OnClick(R.id.test3)
    public void test3(Button button){

        String textToPrint = String.format(Locale.ROOT,demoStr,3);

        // EXTRA_TEXT class String
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,textToPrint);
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test4)
    public void test4(Button button){
        String textToPrint = String.format(Locale.ROOT,demoStr,4);

        StringBuilder sb = new StringBuilder();
        sb.append("It is CharSequence object\n");
        sb.append(textToPrint);

        // EXTRA_TEXT class CharSequence
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,(CharSequence) sb);
        sendToPrint(intent);

        button.setText("x");
    }


    /*  ==============================================================
     *   Intent.VIEW or Intent.SEND with EXTRA_STREAM
     *  ================================================================*/

    /*
     * @see <a href="https://developer.android.com/training/sharing/send#send-binary-content">send-binary-content</a>
     *
     */

    /**
     * file:// WRONG WAY. !! DON'T USE . FOR TEST SUPPORT OLD PROGRAMM ONLY
     */

    private File generateInternalTempTxtIsWrongForFileScheme(int i){
        // internal storage = permission denied
        String textToPrint = String.format(Locale.ROOT,demoStr,i);

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //  it's private storage.
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        File sharePath = new File(getFilesDir(), "share");
        if(sharePath.mkdir()){
            Toast.makeText(this,"share dir created",Toast.LENGTH_SHORT).show();
        }

        final File file = new File(sharePath.getPath(), "temp.txt");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(textToPrint);
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return file;
    }


    @OnClick(R.id.test5)
    public void test5(Button button){

        // HACK
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // !! DON'T USE file://internal storage .
        File file = generateInternalTempTxtIsWrongForFileScheme(5);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setType("text/plain");
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test6)
    public void test6(Button button){

        // HACK
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // !! DON'T USE file://internal storage .
        File file = generateInternalTempTxtIsWrongForFileScheme(6);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"text/plain");
        sendToPrint(intent);

        button.setText("x");
    }



    private File generateExternalTempTxt(int i){
        // External storage is  not always available, because the user can mount the external storage as USB storage and in some cases remove it from the device.
        String textToPrint = String.format(Locale.ROOT,demoStr,i);
        final File file = new File(getExternalCacheDir(), "temp.txt");
        FileWriter writer=null;
        try {
            writer = new FileWriter(file);
            writer.write(textToPrint);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    @OnClick(R.id.test7)
    public void test7(Button button){
        // HACK
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // !! DON'T USE
        File file = generateExternalTempTxt(7);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setType("text/plain");
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test8)
    public void test8(Button button){
        // HACK
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // !! DON'T USE
        File file = generateExternalTempTxt(8);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        /*
        !!!!!!!!!!!!!!!!!!!!!!!  WRONG !!!!!!!!!!!!!!!!!!!!!!!!
        intent.setData(uri);
        intent.setType("text/plain");
         */
        intent.setDataAndType(uri,"text/plain"); // CORRECT set together !!!

        sendToPrint(intent);

        button.setText("x");
    }

    /*
           SAVE IN PUBLIC DIR
     */
    private File generateInPublicDir(int i){
        // Environment.DIRECTORY_DOCUMENTS must be exists. I am not check it in this demo!

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "rawbt.txt");
        String textToPrint = String.format(Locale.ROOT,demoStr,i);
        FileWriter writer=null;
        try {
            writer = new FileWriter(file);
            writer.write(textToPrint);
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return file;
    }

    @OnClick(R.id.test9)
    public void test9(Button button){
        // HACK
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // !! DON'T USE
        File file = generateInPublicDir(9);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setType("text/plain");
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test10)
    public void test10(Button button){
        // HACK
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // !! DON'T USE
        File file = generateInPublicDir(10);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"text/plain"); // CORRECT set together !!!
        sendToPrint(intent);

        button.setText("x");
    }


    /*
         ANDROID.RESOURCE
     */

    @OnClick(R.id.test11)
    public void test11(Button button){
        Uri uri =  Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.ic_launcher);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setType("image/png");
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test12)
    public void test12(Button button){

        Uri uri =  Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/" + R.drawable.ic_launcher);
        Log.d("TEST",uri.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"image/png"); // CORRECT set together !!!
        sendToPrint(intent);

        button.setText("x");
    }


    /*
             CONTENT SCHEME
     */

    @OnClick(R.id.test13)
    public void test13(Button button){
        File file = generateInternalTempTxtIsWrongForFileScheme(13);
        Uri uri = FileProvider.getUriForFile(this, "ru.a402d.demorawbt.fileprovider", file);;
        Log.d("TEST",uri.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"text/plain"); // CORRECT set together !!!
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        sendToPrint(intent);

        button.setText("x");
    }

}
