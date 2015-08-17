package br.uff.testeassinador.controller;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by matheus on 05/08/15.
 */
public class StorageController {

    private final String TAG = StorageController.class.getSimpleName();


    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public byte[] getFileData(String filename) throws FileNotFoundException {

        System.out.println("isExternalStorageReadable: " + isExternalStorageReadable());

        // Get the absolute path of External Storage Directory
        File file = new File("/storage/extSdCard/"+filename);

        int fileSizeBytes = (int) file.length();
        byte[] buffer = new byte[fileSizeBytes];

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return buffer;
    }
}
