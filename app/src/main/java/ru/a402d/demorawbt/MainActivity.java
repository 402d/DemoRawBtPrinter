package ru.a402d.demorawbt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            }
        }

    }


    /**
     * Checks and if the application is not installed, then offers to download it from the Play Market
     */
    protected void sendToPrint(Intent intent) {
        final String appPackageName = "ru.a402d.rawbtprinter";
        PackageManager pm = getPackageManager();

        // check app installed
        PackageInfo pi = null;
        if (pm != null) {
            try {
                pi = pm.getPackageInfo(appPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (pi == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            TextView title = new TextView(this);
            title.setText(R.string.dialog_title);
            title.setBackgroundColor(Color.DKGRAY);
            title.setPadding(10, 10, 10, 10);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.WHITE);
            title.setTextSize(14);
            ImageView image = new ImageView(this);
            image.setImageResource(R.drawable.baseline_print_black_48);
            builder.setMessage(R.string.dialog_message)
                    .setView(image).setCustomTitle(title);
            builder.setPositiveButton(R.string.btn_install, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // send to print
            intent.setPackage(appPackageName);
            startActivity(intent);

        }
    }

    /*  ==============================================================
     *   RAWBT SCHEME FOR HTML & JS. You may call from Android to.
     *  ================================================================*/

    @OnClick(R.id.test1)
    public void test1(Button button) {
        String textToPrint = String.format(Locale.ROOT, demoStr, 1);

        // 1) UTF-8 text .  Not available send esc command with chr 128-255 :(

        String url = "rawbt:" + textToPrint;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        sendToPrint(intent);
        button.setText("x");
    }

    @OnClick(R.id.test2)
    public void test2(Button button) {

        // 2) Send RAW DATA to printer. You must initiate printer yourself

        byte[] bytesToPrint = new byte[0];
        try {
            bytesToPrint = String.format(Locale.ROOT, demoStr, 2).getBytes("cp866");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // encode byte[] to base64
        String base64ToPrint = Base64.encodeToString(bytesToPrint, Base64.DEFAULT);

        // call intent with rawbt:base64,
        String url = "rawbt:base64," + base64ToPrint;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        sendToPrint(intent);
        button.setText("x");
    }

    /*  ==============================================================
     *   Intent.SEND  EXTRA_TEXT
     *   Not available send esc command with chr 128-255 :(
     *  ================================================================*/

    /**
     * @see <a href="https://developer.android.com/training/sharing/send#send-text-content">developer.android</a>
     */
    @OnClick(R.id.test3)
    public void test3(Button button) {

        String textToPrint = String.format(Locale.ROOT, demoStr, 3);

        // EXTRA_TEXT class String
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, textToPrint);
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test4)
    public void test4(Button button) {
        String textToPrint = String.format(Locale.ROOT, demoStr, 4);

        StringBuilder sb = new StringBuilder();
        sb.append("It is CharSequence object\n");
        sb.append(textToPrint);

        // EXTRA_TEXT class CharSequence
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, (CharSequence) sb);
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

    private File generateInternalTempTxtIsWrongForFileScheme(int i) {
        // internal storage = permission denied
        String textToPrint = String.format(Locale.ROOT, demoStr, i);

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //  it's private storage.
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        File sharePath = new File(getFilesDir(), "share");
        if (sharePath.mkdir()) {
            Toast.makeText(this, "share dir created", Toast.LENGTH_SHORT).show();
        }

        final File file = new File(sharePath.getPath(), "temp.txt");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(textToPrint);
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return file;
    }


    @OnClick(R.id.test5)
    public void test5(Button button) {
        // HACK
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // !! DON'T USE file://internal storage .
        File file = generateInternalTempTxtIsWrongForFileScheme(5);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("text/plain");
        sendToPrint(intent);
        button.setText("x");
    }

    @OnClick(R.id.test6)
    public void test6(Button button) {
        // HACK
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // !! DON'T USE file://internal storage .
        File file = generateInternalTempTxtIsWrongForFileScheme(6);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/plain");
        sendToPrint(intent);
        button.setText("x");
    }


    private File generateExternalTempTxt(int i) {
        // External storage is  not always available, because the user can mount the external storage as USB storage and in some cases remove it from the device.
        String textToPrint = String.format(Locale.ROOT, demoStr, i);
        final File file = new File(getExternalCacheDir(), "temp.txt");
        FileWriter writer;
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
    public void test7(Button button) {
        // HACK
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // !! DON'T USE
        File file = generateExternalTempTxt(7);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("text/plain");
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test8)
    public void test8(Button button) {
        // HACK
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
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
        intent.setDataAndType(uri, "text/plain"); // CORRECT set together !!!

        sendToPrint(intent);

        button.setText("x");
    }

    /*
           SAVE IN PUBLIC DIR
     */
    private File generateInPublicDir(int i) {
        // Environment.DIRECTORY_DOCUMENTS must be exists. I am not check it in this demo!

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "rawbt.txt");
        String textToPrint = String.format(Locale.ROOT, demoStr, i);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            writer.write(textToPrint);
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return file;
    }

    @OnClick(R.id.test9)
    public void test9(Button button) {
        // HACK
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // !! DON'T USE
        File file = generateInPublicDir(9);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("text/plain");
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test10)
    public void test10(Button button) {
        // HACK
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // !! DON'T USE
        File file = generateInPublicDir(10);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/plain"); // CORRECT set together !!!
        sendToPrint(intent);

        button.setText("x");
    }


    /*
         ANDROID.RESOURCE
     */

    @OnClick(R.id.test11)
    public void test11(Button button) {
        Uri uri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.ic_launcher);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/png");
        sendToPrint(intent);

        button.setText("x");
    }

    @OnClick(R.id.test12)
    public void test12(Button button) {

        Uri uri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.ic_launcher);
        Log.d("TEST", uri.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/png"); // CORRECT set together !!!
        sendToPrint(intent);

        button.setText("x");
    }


    /*
             CONTENT SCHEME
     */

    @OnClick(R.id.test13)
    public void test13(Button button) {
        File file = generateInternalTempTxtIsWrongForFileScheme(13);
        Uri uri = FileProvider.getUriForFile(this, "ru.a402d.demorawbt.fileprovider", file);

        Log.d("TEST", uri.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/plain"); // CORRECT set together !!!
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        sendToPrint(intent);

        button.setText("x");
    }


    /**
     * @see <a href="https://developer.android.com/training/printing/custom-docs#java">printing/custom-docs</a>
     */
    @OnClick(R.id.test14)
    public void test14(Button button) {
        /**
         * It is better to make this class universal.
         * delegete computePageCount() & drawPage() into class Render.
         */
        class DemoDocumentAdapter extends PrintDocumentAdapter {

            private Activity mParentActivity;
            private PrintAttributes currentAttributes;
            public void setCurrentAttributes(PrintAttributes currentAttributes) {
                this.currentAttributes = currentAttributes;
                final int density_w = currentAttributes.getResolution().getHorizontalDpi();
                final int density_h = currentAttributes.getResolution().getVerticalDpi();

                margin_left = Math.max(margin_left,(int) (density_w * (float) currentAttributes.getMinMargins().getLeftMils() / MILS_PER_INCH));
                margin_top = Math.max(margin_top,(int) (density_h * (float) currentAttributes.getMinMargins().getTopMils() / MILS_PER_INCH));
                margin_right = Math.max(margin_right,(int) (density_w * (float) currentAttributes.getMinMargins().getRightMils() / MILS_PER_INCH));
                margin_bottom = Math.max(margin_bottom,(int) (density_h * (float) currentAttributes.getMinMargins().getBottomMils() / MILS_PER_INCH));

            }

            private int totalPages ;
            private int mRenderPageWidth, mRenderPageHeight;
            boolean mIsPortrait = false;
            private PrintDocumentInfo printDocumentInfo ;

            // margin
            private  int margin_left=0;
            private  int margin_right=0;
            private  int margin_top=0;
            private  int margin_bottom=0;


            public DemoDocumentAdapter(Activity activity) {
                mParentActivity = activity;
            }

            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                Log.d("TEST", "onLayout s");
                currentAttributes = newAttributes;

                // Respond to cancellation request
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }

                boolean shouldLayout = false; // redraw need only on changes page width, height or orientation

                final int density_w = currentAttributes.getResolution().getHorizontalDpi();
                final int density_h = currentAttributes.getResolution().getVerticalDpi();

                margin_left = Math.max(margin_left,(int) (density_w * (float) currentAttributes.getMinMargins().getLeftMils() / MILS_PER_INCH));
                Log.d("TEST", "margin_left "+String.valueOf(margin_left));

                margin_right = Math.max(margin_right,(int) (density_w * (float) currentAttributes.getMinMargins().getRightMils() / MILS_PER_INCH));
                final int contentWidth = (int) (density_w * (float) currentAttributes.getMediaSize()
                        .getWidthMils() / MILS_PER_INCH) - margin_left - margin_right;
                if (mRenderPageWidth != contentWidth) {
                    mRenderPageWidth = contentWidth;
                    shouldLayout = true;
                }

                margin_top = Math.max(margin_top,(int) (density_h * (float) currentAttributes.getMinMargins().getTopMils() / MILS_PER_INCH));
                margin_bottom = Math.max(margin_bottom,(int) (density_h * (float) currentAttributes.getMinMargins().getBottomMils() / MILS_PER_INCH));

                final int contentHeight = (int) (density_h * (float) currentAttributes.getMediaSize()
                        .getHeightMils() / MILS_PER_INCH) - margin_top - margin_bottom;
                if (mRenderPageHeight != contentHeight) {
                    mRenderPageHeight = contentHeight;
                    shouldLayout = true;
                }

                boolean isPortrait = currentAttributes.getMediaSize().isPortrait();
                if (mIsPortrait != isPortrait) {
                    mIsPortrait = isPortrait;
                    shouldLayout = true;
                }

                if (!shouldLayout) {
                    Log.d("TEST", "onLayout() - Finished. No Re-Layout required.");
                    callback.onLayoutFinished(printDocumentInfo,false);
                    return;
                }

                // Compute the expected number of printed pages
                // totalPages =  computePageCount(currentAttributes);
                totalPages = 1; // I draw only 1 page for demo

                printDocumentInfo = new PrintDocumentInfo
                        .Builder("print_output.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(totalPages)
                        .build();

                // Content layout reflow is complete
                callback.onLayoutFinished(printDocumentInfo, true);
                Log.d("TEST", "onLayout f");
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                Log.d("TEST", "onWrite s");

                // Create a new PdfDocument with the requested page attributes
                PrintAttributes pdfAttributes = new PrintAttributes.Builder()
                        .setMediaSize(currentAttributes.getMediaSize())
                        .setResolution(currentAttributes.getResolution())
                        .setMinMargins(new PrintAttributes.Margins(
                                (int)margin_left*MILS_PER_INCH/72,
                                (int)margin_top*MILS_PER_INCH/72,
                                (int)margin_right*MILS_PER_INCH/72,
                                (int)margin_bottom*MILS_PER_INCH/72
                        )).build();
                PrintedPdfDocument mPdfDocument = new PrintedPdfDocument(mParentActivity, pdfAttributes);

                SparseIntArray writtenPages = new SparseIntArray();

                // Iterate over each page of the document, check if it's in the output range.
                for (int i = 0; i < totalPages; i++) {
                    // Check to see if this page is in the output range.
                    if (containsPage(pages, i)) {
                        // If so, add it to writtenPagesArray. writtenPagesArray.size()
                        // is used to compute the next output page index.
                        writtenPages.append(writtenPages.size(), i);
                        PdfDocument.Page page = mPdfDocument.startPage(i);

                        // check for cancellation
                        if (cancellationSignal.isCanceled()) {
                            callback.onWriteCancelled();
                            mPdfDocument.close();
                            mPdfDocument = null;
                            return;
                        }

                        // Draw page content for printing
                        drawPage(page, currentAttributes);

                        // Rendering is complete, so page can be finalized.
                        mPdfDocument.finishPage(page);
                    }
                }

                // Write PDF document to file
                try {
                    mPdfDocument.writeTo(new FileOutputStream(
                            destination.getFileDescriptor()));
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                    return;
                } finally {
                    mPdfDocument.close();
                    mPdfDocument = null;
                }

                PageRange[] writtenPageRange = computeWrittenPageRanges(writtenPages);
                // Signal the print framework the document is complete
                callback.onWriteFinished(writtenPageRange);
                Log.d("TEST", "onWrite f");

            }


            /**
             *  Render method
             *
             */
            private int computePageCount(PrintAttributes printAttributes) {
                return 1;
            }

            private static final int MILS_PER_INCH = 1000;

            /**
             * Render method
             */
            private void drawPage(PdfDocument.Page page, PrintAttributes printAttributes) {
                // place picture in pdf file
                Canvas canvasPage = page.getCanvas();

                // useful
                boolean isPortrait = printAttributes.getMediaSize().isPortrait();

                // units are in points (1/72 of an inch)
                // create picture printable size
                Bitmap bmp = Bitmap.createBitmap(canvasPage.getWidth(), canvasPage.getHeight(), Bitmap.Config.ARGB_8888);

                // draw border for example in printable
                Canvas canvasBmp = new Canvas(bmp);

                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                canvasBmp.drawRect(0,0, canvasBmp.getWidth(), canvasBmp.getHeight(),paint);

                paint.setColor(Color.WHITE);
                float borderWidth = 3;
                canvasBmp.drawRect(borderWidth, borderWidth,
                        canvasBmp.getWidth()-borderWidth,
                        canvasBmp.getHeight()-borderWidth,
                        paint);

                paint.setColor(Color.BLACK);
                paint.setTextSize(11);
                canvasBmp.drawText("size "+String.valueOf(canvasBmp.getWidth())+"x"+String.valueOf(canvasBmp.getHeight()), 24, 24, paint);
                canvasBmp.drawText("media "+currentAttributes.getMediaSize().getId(), 54, 150, paint);
                paint.setTextSize(24);
                canvasBmp.drawText("Hello, BMP!", 54, 50, paint);

                canvasPage.drawBitmap(bmp, 0, 0, null);

                // direct on page

                paint.setColor(Color.BLACK);
                paint.setTextSize(24);
                canvasPage.drawText("Hello, PAGE!", 54, 100, paint);


            }

            /**
             * Function that converts the selected pages to be written in form of PageRange array.
             * @param writtenPages a SparseIntArray that contains the pages that must be written.
             * @return a PageRange array containing the resulting ranges.
             */
            private PageRange[] computeWrittenPageRanges(SparseIntArray writtenPages) {
                List<PageRange> pageRanges = new ArrayList<>();

                int start = -1;
                int end;
                final int writtenPageCount = writtenPages.size();
                for (int i = 0; i < writtenPageCount; i++) {
                    if (start < 0) {
                        start = writtenPages.valueAt(i);
                    }
                    int oldEnd = end = start;
                    while (i < writtenPageCount && (end - oldEnd) <= 1) {
                        oldEnd = end;
                        end = writtenPages.valueAt(i);
                        i++;
                    }
                    if (start >= 0) {
                        PageRange pageRange = new PageRange(start, end);
                        pageRanges.add(pageRange);
                    }
                    start = -1;
                }

                PageRange[] pageRangesArray = new PageRange[pageRanges.size()];
                pageRanges.toArray(pageRangesArray);
                return pageRangesArray;
            }

            /**
             * Checks if a given page number is contained in a PageRange array.
             * @param pageRanges The PageRange array of written pages.
             * @param numPage The page number to check against the PageRange array.
             * @return true if the page is contained in the PageRange array. False otherwise.
             */
            private boolean containsPage(PageRange[] pageRanges, int numPage) {
                for (PageRange pr : pageRanges) {
                    if ((numPage >= pr.getStart()) && (numPage <= pr.getEnd())) return true;
                }
                return false;
            }

            private void print(PrintManager printManager){
                String jobName = getString(R.string.app_name) + " Document";
                PrintAttributes printAttributes = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(new PrintAttributes.Resolution("Default", "72dpi: 1pt=1dot", 72, 72))
                        .setMinMargins(new PrintAttributes.Margins(500,500,500,500))
                        .build();
                setCurrentAttributes(printAttributes);
                printManager.print(jobName, this, printAttributes);
            }
        }


        //create object of print adapter
        DemoDocumentAdapter printAdapter = new DemoDocumentAdapter(this);
        //create object of print manager in your device
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        if (printManager != null) {
            printAdapter.print(printManager);
        } else {
            Toast.makeText(this, "Print service not available", Toast.LENGTH_LONG).show();
        }

        button.setText("x");
    }


    /**
     * see manifest :
     *    <uses-permission android:name="ru.a402d.rawbtprinter.PERMISSION" />
     *
     */
    @OnClick(R.id.test15)
    public void test15(Button button) {
        String textToPrint = String.format(Locale.ROOT, demoStr, 15);

        Intent intent = new Intent("ru.a402d.rawbtprinter.action.PRINT_RAWBT");
        intent.putExtra("ru.a402d.rawbtprinter.extra.DATA",textToPrint);
        intent.setPackage("ru.a402d.rawbtprinter");
        startService(intent);
        button.setText("x");
    }
    @OnClick(R.id.test16)
    public void test16(Button button) {

        byte[] bytesToPrint = new byte[0];
        try {
            bytesToPrint = String.format(Locale.ROOT, demoStr, 16).getBytes("cp866");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // encode byte[] to base64
        String base64ToPrint = "base64,"+Base64.encodeToString(bytesToPrint, Base64.DEFAULT);

        Intent intent = new Intent("ru.a402d.rawbtprinter.action.PRINT_RAWBT");
        intent.putExtra("ru.a402d.rawbtprinter.extra.DATA",base64ToPrint);
        intent.setPackage("ru.a402d.rawbtprinter");
        startService(intent);
        button.setText("x");
    }

}
