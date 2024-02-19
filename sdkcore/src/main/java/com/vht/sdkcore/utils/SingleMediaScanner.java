package com.vht.sdkcore.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    CallbackShowFile callbackShowFile;
    private MediaScannerConnection mMs;
    private File mFile;

    public SingleMediaScanner(Context context, File f, CallbackShowFile callbackShowFile) {
        mFile = f;
        this.callbackShowFile = callbackShowFile;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    public void onMediaScannerConnected() {
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }

    public void onScanCompleted(String path, Uri uri) {
        callbackShowFile.openFile(uri);
        mMs.disconnect();
    }

    public  interface CallbackShowFile {
        public void openFile(Uri uri);
    }
}