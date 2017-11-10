package com.gueg.browser.thumbnails;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ThumbnailsSaver {

    public static void saveToInternalSorage(Context context, ArrayList<Thumbnail> thumbnails){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("thumbnails", Context.MODE_PRIVATE);
        if (!directory.exists())
            //noinspection ResultOfMethodCallIgnored
            directory.mkdir();

        for(Thumbnail t : thumbnails) {
            if(t!=null) {
                File imgPath = new File(directory, t.title + ".png");
                File urlPath = new File(directory, t.title + ".url");
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(imgPath);
                    t.image.compress(Bitmap.CompressFormat.PNG, 50, fos);
                    fos.close();

                    fos = new FileOutputStream(urlPath);
                    fos.write(t.url.getBytes("UTF-8"));
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ArrayList<Thumbnail> loadFromInternalSorage(Context context){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("thumbnails", Context.MODE_PRIVATE);
        File[] files = directory.listFiles();

        ArrayList<Thumbnail> thumbnails = new ArrayList<>();

        HashMap<String,String> urls = new HashMap<>();
        HashMap<String,Bitmap> imgs = new HashMap<>();

        for(File f : files) {
            if(f.getAbsolutePath().endsWith(".png")) {
                imgs.put(f.getName().replace(".png",""),BitmapFactory.decodeFile(f.getAbsolutePath()));
            } else if(f.getAbsolutePath().endsWith(".url")) {
                FileInputStream fis;
                try {
                    fis = new FileInputStream(f.getAbsolutePath());

                    StringBuilder fileContent = new StringBuilder("");
                    byte[] buffer = new byte[1024];
                    int n;
                    while ((n = fis.read(buffer)) != -1)
                        fileContent.append(new String(buffer, 0, n));

                    urls.put(f.getName().replace(".url",""),fileContent.toString());

                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Set<String> titles = urls.keySet();
        for(String title : titles) {
            if(imgs.containsKey(title)) {
                thumbnails.add(new Thumbnail(title, urls.get(title), imgs.get(title)));
            }
            else {
                thumbnails.add(new Thumbnail(title, urls.get(title), null));
            }
        }

        return thumbnails;
    }

    public static void clearStorage(Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("thumbnails", Context.MODE_PRIVATE);
        File[] files = directory.listFiles();

        for(File f : files) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }
}
