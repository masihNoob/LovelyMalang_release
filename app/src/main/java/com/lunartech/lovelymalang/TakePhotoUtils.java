package com.lunartech.lovelymalang;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePhotoUtils {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static final String PREF_IMAGE_NAME = "pref_image_name";
    public static final String PREF_IMAGE_THUMB_NAME = "pref_image_thumb_name";

    public static final int MIN_SIDE = 1024;
    public static final int MAX_SIDE = 1024;
    public static final int TARGET_MAX_NUM_PIXELS = 1024 * 1024;

    public static final int GET_CAMERA = 1526;
    public static final int GET_GALLERY = 1530;

    public static final int THUMB_MIN_SIDE = 1024;
    public static final int THUMB_MAX_SIDE = 1024;
    public static final int THUMB_MAX_NUM_PIXELS = 1024 * 1024;

    public static final int UNCONSTRAINED = -1;

    public static final int GET_PHOTO = 0;
    public static final int GET_THUMB = 1;

    private static final TakePhotoUtils instance = new TakePhotoUtils();

    private File imageFile;
    private File imageThumbFile;

    private TakePhotoUtils() {
    }

    public static TakePhotoUtils getInstance() {
        return instance;
    }

    public Uri savePhotoAndGetUri(Context ctx, Bitmap bitmap, String title) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        // Bitmap result = AppUtil.resizeBitmapAspectRatioAndRotate(bitmap, 600, null);
        byte[] bitmapdata = bytes.toByteArray();
        Bitmap result = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        String path = Images.Media.insertImage(ctx.getContentResolver(), result, title, null);
        //L.e("title Uri = " + path);
        return Uri.parse(path);
    }

    public void launchCamera(Activity activity) {
        activity.startActivityForResult(createCameraIntent(activity), GET_CAMERA);
    }

    public void launchGallery(Activity activity) {
        activity.startActivityForResult(createGalleryIntent(activity), GET_GALLERY);
    }

    public void launchGalleryFrame(VenueActivity.PlaceholderFragment f) {
        f.startActivityForResult(createGalleryIntent(f.getContext()), GET_GALLERY);
    }

    public void launchGalleryFrameAmin(AdminActivity.PlaceholderFragment f) {
        f.startActivityForResult(createGalleryIntent(f.getContext()), GET_GALLERY);
    }

    private Intent createCameraIntent(Context ctx) {
        createImageName2(ctx);
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
    }

    private Intent createGalleryIntent(Context ctx) {
        createImageName(ctx);
        return new Intent(Intent.ACTION_PICK,
                Images.Media.INTERNAL_CONTENT_URI);
    }

    public Bitmap savePhotoAndGetThumbnail(Context ctx) {
        Bitmap thumb = null;

        if (imageFile == null) {
            //L.i("imageFile == null");
            imageFile = new File(Utils.getConfig(ctx, PREF_IMAGE_NAME));
            //L.i("stored image file = " + imageFile);
        }

        if (imageThumbFile == null) {
            //L.i("imageThumbFile == null");
            imageThumbFile = new File(Utils.getConfig(ctx, PREF_IMAGE_THUMB_NAME));
            //L.i("stored image thumb file = " + imageThumbFile);
        }

        if (imageFile.exists()) {
            Bitmap image = scalePhoto(ctx, imageFile, MIN_SIDE, MAX_SIDE, TARGET_MAX_NUM_PIXELS);
            //L.d("------ file = " + imageFile);
            savePicture(ctx, imageFile, image);


            thumb = ThumbnailUtils.extractThumbnail(image, THUMB_MIN_SIDE, THUMB_MIN_SIDE);

            savePicture(ctx, imageThumbFile, thumb);

            // if (image != null) {
            // image.recycle();
            // }
        } else {
            //L.w("imageFile.exists() == false");
        }

        return thumb;
    }

    /**
     * Do not forget to call AppUtil.isMediaStorageMounted()
     *
     * @param path path to the file
     * @param pic
     * @return true if this file was saved, false otherwise.
     */
    public static boolean savePicture(Context ctx, String path, Bitmap pic) {
        boolean saved = false;
        try {
            File pictureFile = new File(path);
            Uri uri = Uri.fromFile(pictureFile);
            ContentResolver cr = ctx.getContentResolver();
            OutputStream thumbOut = cr.openOutputStream(uri);
            saved = pic.compress(Bitmap.CompressFormat.JPEG, 85, thumbOut);
            thumbOut.close();
        } catch (Exception e) {
            //L.e(e);
        }

        if (!saved) {
            //L.i("failed to save " + path);
        }

        return saved;
    }

    public static boolean savePicture(Context ctx, File pictureFile, Bitmap pic) {
        boolean saved = false;
        try {
            Uri uri = Uri.fromFile(pictureFile);
            //L.d("uri = " + uri.getPath());
            ContentResolver cr = ctx.getContentResolver();
            OutputStream thumbOut = cr.openOutputStream(uri);
            saved = pic.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
        } catch (Exception e) {
            //L.e(e);
        }

        if (!saved) {
            //L.i("failed to save " + pictureFile.getPath());
        }

        return saved;
    }

    public Bitmap savePhotoAndGetThumbnail(Context ctx, Bitmap bitmap) {
        if (imageFile == null) {
            //L.i("imageFile == null");
            imageFile = new File(Utils.getConfig(ctx, PREF_IMAGE_NAME));
            //L.i("stored image file = " + imageFile);
        }

        if (imageThumbFile == null) {
            //L.i("imageThumbFile == null");
            imageThumbFile = new File(Utils.getConfig(ctx, PREF_IMAGE_THUMB_NAME));
            //L.i("stored image thumb file = " + imageThumbFile);
        }

        try {
            FileOutputStream fOut = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.close();
        } catch (IOException e) {
            Log.e("Error Write", e.getMessage());
            Utils.showMessage(ctx, e.getMessage());
        }
        return savePhotoAndGetThumbnail(ctx);
    }

    public Bitmap savePhotoAndGetThumbnail(Context ctx, URL url, String fileName, int returnType) {
        imageFile = new File(ctx.getFilesDir() + File.separator + fileName);
        imageThumbFile = new File(ctx.getFilesDir() + File.separator + "thumb_" + fileName);
        try {
            imageFile.createNewFile();
            imageThumbFile.createNewFile();
        } catch (IOException e) {
            //L.e(e);
        }

        Bitmap image = scalePhoto(url, MIN_SIDE, MAX_SIDE, TARGET_MAX_NUM_PIXELS);
        savePicture(ctx, imageFile, image);

        String exifOrientation = "";
        try {
            exifOrientation = getExifOrientation(imageFile.getAbsolutePath());
        } catch (IOException e) {
            //L.e(e);
        }
        image = resizeBitmapAspectRatioAndRotate(image, MAX_SIDE, exifOrientation);
        savePicture(ctx, imageFile, image);

        Bitmap thumb = createThumbnail(image, THUMB_MIN_SIDE, THUMB_MAX_SIDE, THUMB_MAX_NUM_PIXELS);
        savePicture(ctx, imageThumbFile, thumb);

        if (returnType == GET_PHOTO) {
            return image;
        } else {
            return thumb;
        }
    }

    public static Bitmap resizeBitmapAspectRatioAndRotate(Bitmap source,
                                                          int maxSideSize, String exifOrientation) {
        Bitmap bitmap = null;

        float largerSide = source.getHeight() >= source.getWidth() ? source
                .getHeight() : source.getWidth();
        float scale = maxSideSize / largerSide;

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix = matrixRotate(matrix, exifOrientation);

        bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                source.getHeight(), matrix, true);

        return bitmap;
    }


    public static Matrix matrixRotate(Matrix matrix, String exifOrientation) {
        if (matrix == null) {
            matrix = new Matrix();
        }

        if (exifOrientation != null) {
            if ("6".equals(exifOrientation)) {
                matrix.postRotate(90);
            } else if ("3".equals(exifOrientation)) {
                matrix.postRotate(180);
            } else if ("8".equals(exifOrientation)) {
                matrix.postRotate(270);
            } else if ("1".equals(exifOrientation)) {
            }
        }

        return matrix;
    }

    public static String getExifOrientation(String path) throws IOException {
        String exifOrientation = null;

        ExifInterface exif = new ExifInterface(path);
        exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);

        return exifOrientation;
    }

    public Bitmap getPhotoFromServerBySize(String url, int widht, int height) {

        Bitmap result = null;
        InputStream input;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        try {
            URL pictureURL = new URL(url);
            //L.d("pictureURL = " + pictureURL);
            input = pictureURL.openStream();
            result = BitmapFactory.decodeStream(input, null, options);
            input.close();
        } catch (MalformedURLException e) {
            //L.e(e);
        } catch (IOException e) {
            //L.e(e);
        }
        return result;
    }

    private Bitmap scalePhoto(Context ctx, File pictureFile, int minSideLength, int maxSideLength, int maxNumOfPixels) {
        //L.d("pictureFile = " + pictureFile.getPath() + ", minSideLength = " + minSideLength
          //      + ", maxNumOfPixels = " + maxNumOfPixels);

        Bitmap bitmap = null;

        ParcelFileDescriptor pfd = null;
        try {
            pfd = ctx.getContentResolver().openFileDescriptor(Uri.fromFile(pictureFile), "r");
        } catch (FileNotFoundException e) {
            //L.e(e);

            return null;
        }

        String exifOrientation = "";
        try {
            exifOrientation = getExifOrientation(pictureFile.getAbsolutePath());
        } catch (IOException e) {
            //L.e(e);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        FileDescriptor fd = pfd.getFileDescriptor();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        int inSampleSize = computeSampleSize(options.outWidth, options.outHeight, minSideLength, maxNumOfPixels);

        if (inSampleSize < 4) {
            options.inSampleSize = 1;
        } else {
            options.inSampleSize = inSampleSize;
        }

        //L.d("computeSampleSize return = " + options.inSampleSize);

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap source = BitmapFactory.decodeFileDescriptor(fd, null, options);

        //L.d("source.getHeight() = " + source.getHeight()
                //+ " source.getWidth() = " + source.getWidth());

        bitmap = resizeBitmapAspectRatioAndRotate(source, maxSideLength, exifOrientation);
        //L.d("returned bitmap.getHeight() = " + bitmap.getHeight()
          //      + " bitmap.getWidth() = " + bitmap.getWidth());

        try {
            pfd.close();
        } catch (IOException e) {
            //L.e(e);
        }
        return bitmap;
    }

    private Bitmap scalePhoto(URL pictureURL, int minSideLength, int maxSideLength, int maxNumOfPixels) {
        //L.d("pictureFile = " + pictureURL + ", minSideLength = " + minSideLength
          //      + ", maxNumOfPixels = " + maxNumOfPixels);

        Bitmap source = null;

        InputStream input;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            input = pictureURL.openStream();

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();
        } catch (IOException e) {
            //L.e(e);
        }

        int inSampleSize = computeSampleSize(options.outWidth, options.outHeight, minSideLength, maxNumOfPixels);

        options = new BitmapFactory.Options();
        if (inSampleSize < 4) {
            options.inSampleSize = 1;
        } else {
            options.inSampleSize = inSampleSize;
        }

        //L.d("computeSampleSize return = " + options.inSampleSize);

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        try {
            input = pictureURL.openStream();
            source = BitmapFactory.decodeStream(input, null, options);
            //L.d("source.getHeight() = " + source.getHeight()
              //      + " source.getWidth() = " + source.getWidth());
        } catch (IOException e) {
            //L.e(e);
        }

        return source;
    }

    private Bitmap createThumbnail(Bitmap bitmap, int minSideLength, int maxSideLength, int maxNumOfPixels) {
        //L.d("bitmap.getHeight() = " + bitmap.getHeight() + ", bitmap.getWidth() = " + bitmap.getWidth()
          //      + "minSideLength = " + minSideLength + ", maxNumOfPixels = " + maxNumOfPixels);

        Bitmap thumb = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        int inSampleSize = computeSampleSize(bitmap.getWidth(), bitmap.getHeight(),
                minSideLength, maxNumOfPixels);

        if (inSampleSize < 4) {
            options.inSampleSize = 1;
        } else {
            options.inSampleSize = inSampleSize;
        }

        //L.d("computeSampleSize return = " + options.inSampleSize);

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        thumb = resizeBitmapAspectRatioAndRotate(bitmap, maxSideLength, "");
        //L.d("returned thumb.getHeight() = " + thumb.getHeight()
          //      + " thumb.getWidth() = " + thumb.getWidth());

        return thumb;
    }

    private int computeSampleSize(double width, double height, int minSideLength, int maxNumOfPixels) {
        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
                .ceil(Math.sqrt(width * height / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
                .min(Math.floor(width / minSideLength), Math.floor(height / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static boolean isMediaStorageMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    private void createImageName(Context ctx) {
        long time = System.currentTimeMillis();

        //File dir;

        if (isMediaStorageMounted()) {
            //L.d("MOUNTED");
            //Environment.getExternalStorageDirectory()
            //dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rajaampat");
            //dir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + time + ".jpg");
            imageThumbFile = new File(Environment.getExternalStorageDirectory() + File.separator + time + "_thumb.jpg");
        } else {
            //L.d("UNMOUNTED");
            imageFile = ctx.getFileStreamPath(time + ".jpg");
            imageThumbFile = ctx.getFileStreamPath(time + "_thumb.jpg");
            //dir = imageFile.getParentFile();
        }

        Utils.setConfig(ctx, PREF_IMAGE_NAME, imageFile.toString());
        Utils.setConfig(ctx, PREF_IMAGE_THUMB_NAME, imageThumbFile.toString());

        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            //L.e(e);
            android.util.Log.d("log", "failed to create directory: "+e.getMessage());
        }

        try {
            imageThumbFile.createNewFile();
        } catch (IOException e) {
            //L.e(e);
        }
    }

    private void createImageName2(Context ctx) {
        String timeStamp = DATE_FORMAT.format(new Date());
        if (isMediaStorageMounted()) {
            //L.d("media storage is mounted");

            try {
                imageFile = new File(ctx.getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES), timeStamp + ".jpg");
            } catch (Exception e) {
                //AppUtil.showLongToast("It is not possible to save image on this device", Style.ALERT);
            }
            try {
                if (!imageFile.exists()) {
                    imageFile.createNewFile();
                }
            } catch (IOException e) {
                //L.e(e);
            }

            imageThumbFile = new File(ctx.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), timeStamp + "_thumb.jpg");

            try {
                if (!imageThumbFile.exists()) {
                    imageThumbFile.createNewFile();
                }
            } catch (IOException e) {
                //L.e(e);
            }

            //L.i("imageThumbFile = " + imageThumbFile);

            Utils.setConfig(ctx, PREF_IMAGE_NAME, imageFile.toString());
            Utils.setConfig(ctx, PREF_IMAGE_THUMB_NAME, imageThumbFile.toString());
        } else {
            //L.d("media storage is unmounted");
            createImageName(ctx);
        }
    }

    public boolean checkMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        double gigaAvailable = sdAvailSize / 1073741824;
        return gigaAvailable > 0.020;
    }

    /*
    public enum Type {
        CAMERA(0), GALLERY(1);

        private int id;

        Type(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
*/
    public File getFile() {
        return imageFile;
    }

}
