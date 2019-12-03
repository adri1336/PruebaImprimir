package com.example.pruebaimprimir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                doPhotoPrint();
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }

    private void doPhotoPrint()
    {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        photoPrinter.printBitmap("imagen - test print", bitmap);
    }

    private void doPrint() {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = this.getString(R.string.app_name) + " Document";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        printManager.print(jobName, new MyPrintDocumentAdapter(this),
                null); //
    }


    private class MyPrintDocumentAdapter extends PrintDocumentAdapter
    {
        private Context context;
        private PrintedPdfDocument myPdfDocument;
        private int pageHeight, pageWidth;
        private int totalPages = 1;

        public MyPrintDocumentAdapter(MainActivity mainActivity)
        {
            this.context = mainActivity;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras)
        {
            myPdfDocument = new PrintedPdfDocument(context, newAttributes);
            pageHeight = newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
            pageWidth = newAttributes.getMediaSize().getWidthMils() / 1000 * 72;

            if(cancellationSignal.isCanceled())
            {
                callback.onLayoutCancelled();
                return;
            }

            if(totalPages > 0)
            {
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder("print_output.pdf");
                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT);
                builder.setPageCount(totalPages);
                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            }
            else callback.onLayoutFailed("Page count is zero.");
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback)
        {
            PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = myPdfDocument.startPage(newPage);
            if(cancellationSignal.isCanceled())
            {
                callback.onWriteCancelled();
                myPdfDocument.close();
                myPdfDocument = null;
                return;
            }

            drawPage(page);
            myPdfDocument.finishPage(page);

            try
            {
                myPdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
            }
            catch(IOException e)
            {
                callback.onWriteFailed(e.toString());
                return;
            }
            finally
            {
                myPdfDocument.close();
                myPdfDocument = null;
            }

            callback.onWriteFinished(pages);
        }

        private void drawPage(PdfDocument.Page page)
        {
            Canvas canvas = page.getCanvas();

            // units are in points (1/72 of an inch)
            int titleBaseLine = 72;
            int leftMargin = 54;

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(36);
            canvas.drawText("Test Title", leftMargin, titleBaseLine, paint);

            paint.setTextSize(11);
            canvas.drawText("Test paragraph", leftMargin, titleBaseLine + 25, paint);

            paint.setColor(Color.BLUE);
            canvas.drawRect(100, 100, 172, 172, paint);
        }

    }
}
