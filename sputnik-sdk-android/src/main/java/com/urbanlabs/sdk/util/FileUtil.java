package com.urbanlabs.sdk.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FileUtil {
	// Resources must be located in assets/ folder of Android project
	public static String ASSETS = "sputnik-sdk-assets.zip";
    /**
     *
     * @param path
     * @return
     */
    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
    /**
     *
     * @param assetManager
     * @param copyTo
     * @param path
     * @throws Exception
     */
    public static void copyFileOrDir(AssetManager assetManager, String copyTo, String path) throws Exception {
        String assets[] = null;
        assets = assetManager.list(path);
        if (assets.length == 0) {
        	if(path.indexOf(".zip") != -1)
        		extractArchive(assetManager, copyTo, path);
        	else
        		copyFile(assetManager, copyTo, path);
        } else {
            String fullPath = copyTo + File.separator + path;
            File dir = new File(fullPath);
            if (!dir.exists())
                dir.mkdir();
            for (int i = 0; i < assets.length; ++i) {
                String relPath = (path=="") ? assets[i] : path+File.separator+assets[i];
                if(assets[i].indexOf(".zip") != -1)
                    extractArchive(assetManager, copyTo, assets[i]);
                else
                    copyFileOrDir(assetManager, copyTo, relPath);
            }
        }
    }

    /**
     *
     * @param assetManager
     * @param copyTo
     * @param filename
     * @throws Exception
     */
    private static void copyFile(AssetManager assetManager, String copyTo, String filename) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        in = assetManager.open(filename);
        String newFileName = copyTo + File.separator + filename;
        out = new FileOutputStream(newFileName);

        byte[] buffer = new byte[1048576];
        int read;
        long total = 0;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            out.write(buffer, 0, read);
            if (total > 1024 * 1024) {
                total = 0;
                out.flush();
            }
        }
        in.close();
        out.flush();
        out.close();
    }

    /**
     * @param assetManager
     * @param copyTo
     * @param filename
     * @throws Exception
     */
    private static void copyFileNio(AssetManager assetManager, String copyTo, String filename) throws Exception {
        FileChannel in = null;
        FileChannel out = null;

        AssetFileDescriptor afd = assetManager.openFd(filename);
        String newFileName = copyTo + File.separator + filename;

        try {
            in = new FileInputStream(afd.getFileDescriptor()).getChannel();
            out = new FileOutputStream(newFileName).getChannel();

            long size = in.size();
            long transferred = in.transferTo(0, size, out);

            while(transferred != size){
                transferred += in.transferTo(transferred, size - transferred, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }
    }

    private static void extractFile(ZipInputStream zin, String outDir, String name, byte buffer[]) throws Exception {
        OutputStream out = null;
        String newFileName = outDir+File.separator+name;
        File test = new File(newFileName);
        if(!test.exists())
            test.createNewFile();
        out = new FileOutputStream(newFileName);

        int read;
        long total = 0;
        while ((read = zin.read(buffer)) != -1) {
            total += read;
            out.write(buffer, 0, read);
            if (total > 1024 * 1024) {
                total = 0;
                out.flush();
            }
        }
        out.flush();
        out.close();
    }

    private static void mkDirs(File outDir, String path) {
        File file = new File(outDir, path);
        if(!file.exists())
            file.mkdirs();
    }

    private static String dirPart(String name) {
        int part = name.lastIndexOf(File.separator);
        return part != -1 ? name.substring(0, part) : "";
    }

    public static void extractArchive(AssetManager assetManager, String extractTo, String archiveName) throws Exception {
        Log.v("[INFO]", "Extracting archive: "+archiveName);

        File to = new File(extractTo);
        InputStream in = assetManager.open(archiveName);
        ZipInputStream zin = new ZipInputStream(in);

        ZipEntry zipEntry;
        String zinFileName, dirName;
        byte[] buffer = new byte[1048576];
        while((zipEntry = zin.getNextEntry()) != null) {
            zinFileName = zipEntry.getName();
            if(zipEntry.isDirectory()) {
                mkDirs(to, zinFileName);
            } else {
                dirName = dirPart(zinFileName);
                if(!dirName.equals(""))
                    mkDirs(to, dirName);
                extractFile(zin, extractTo, zinFileName, buffer);
            }
        }

        zin.close();
        in.close();

        Log.v("[INFO]", "Done extracting archive: "+archiveName);
    }
    /**
     * @return Number of Mega bytes available on External storage
     */
    public static long getAvailableSpaceInMB(){
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        Log.d("", "Available space is "+availableSpace/SIZE_MB);
        return availableSpace/SIZE_MB;
    }
}
