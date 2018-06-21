package cn.lmcw.imagereview.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2018/6/19.
 */

public class FileUtils {

    public static String savaBitmap(Context ctx, String imgName, byte[] bytes) {
        String filePath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            FileOutputStream fos = null;
            try {
                filePath = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Download";
                File imgDir = new File(filePath);
                if (!imgDir.exists()) {
                    imgDir.mkdirs();
                }
                imgName = filePath + "/" + imgName;
                fos = new FileOutputStream(imgName);
                fos.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            return null;
        }

        return filePath;
    }
}
