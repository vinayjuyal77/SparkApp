package com.app.spark.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.app.spark.R;
import com.app.spark.image_cropper.CropActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * Created by king on 30/11/16.
 */
public class ImagePickerUtil {
    static File mFileCaptured;
    static Uri mImageCaptureUri;
    static ImagePickerListener imagePickerListener;
    static Activity context;
    static File fileProfilePic = null;
    static String tag;
    static File appDir;

    static int picWidth ;
    static int picHeight;

    static boolean cropImage = false;
    static boolean compressImage = true;






    public static void selectImage(final Context context, final ImagePickerListener imagePickerListener, final String tag, final boolean compressImage, boolean onlyCam, boolean onlyGallery, boolean isCropImage) {
        final CharSequence[] items = {context.getString(R.string.camera), context.getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setTitle(null);
        if (onlyCam || onlyGallery) {

            if (onlyCam && isCropImage) {
                pickFromCameraWithCrop((Activity) context, imagePickerListener, tag);
            } else if (onlyCam) {
                pickFromCamera((Activity) context, imagePickerListener, tag, compressImage);
            } else if (onlyGallery && isCropImage) {
                pickFromGalleryWithCrop((Activity) context, imagePickerListener, tag);
            } else
                pickFromGallery((Activity) context, imagePickerListener, tag, compressImage);


        } else {
            builder.setItems(items, (dialog, item) -> {

                if (items[item].equals(context.getString(R.string.camera))) {
                    if (isCropImage) {
                        pickFromCameraWithCrop((Activity) context, imagePickerListener, tag);
                    } else {
                        pickFromCamera((Activity) context, imagePickerListener, tag, compressImage);
                    }
                } else if (items[item].equals(context.getString(R.string.gallery))) {
                    if (isCropImage) {
                        pickFromGalleryWithCrop((Activity) context, imagePickerListener, tag);
                    } else {
                        pickFromGallery((Activity) context, imagePickerListener, tag, compressImage);
                    }
                }
            });
            builder.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void pickFromCamera(Activity context, ImagePickerListener imagePickerListener, String tag, boolean compressImage) {
        ImagePickerUtil.tag = tag;
        ImagePickerUtil.compressImage = compressImage;
        cropImage = false;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                String appDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getString(R.string.app_name);
                appDir = new File(appDirPath);
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }

                mFileCaptured = new File(appDir, tag + ".png");

                mImageCaptureUri = FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() + ".provider",
                        mFileCaptured);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                intent.setClipData(ClipData.newRawUri("", mImageCaptureUri));
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            context.startActivityForResult(intent, 10000);

        } catch (Exception e) {
            String error = e.getLocalizedMessage();
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void videoFromCamera(Activity context, ImagePickerListener imagePickerListener, String tag) {
        ImagePickerUtil.tag = tag;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivityForResult(intent, 700);
            }
        } catch (Exception e) {
            String error = e.getLocalizedMessage();
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void pickFromCameraWithCrop(Activity context, ImagePickerListener imagePickerListener, String tag) {
        ImagePickerUtil.tag = tag;
        cropImage = true;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                String appDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getString(R.string.app_name);
                appDir = new File(appDirPath);
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }

                mFileCaptured = new File(appDir, tag + ".png");

                mImageCaptureUri = FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() + ".provider",
                        mFileCaptured);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                intent.setClipData(ClipData.newRawUri("", mImageCaptureUri));
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            context.startActivityForResult(intent, 10000);

        } catch (Exception e) {
            String error = e.getLocalizedMessage();
        }
    }



    public static void pickFromGallery(Activity context, ImagePickerListener imagePickerListener, String tag, boolean compressImage) {
        ImagePickerUtil.tag = tag;
        ImagePickerUtil.compressImage = compressImage;
        cropImage = false;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        context.startActivityForResult(Intent.createChooser(intent, "Select File"), 20000);
    }

    public static void pickFromGalleryWithCrop(Activity context, ImagePickerListener imagePickerListener, String tag) {
        ImagePickerUtil.tag = tag;
        cropImage = true;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        context.startActivityForResult(Intent.createChooser(intent, "Select File"), 20000);
    }

    public static void pickFile(Activity context, ImagePickerListener imagePickerListener, String tag) {

        ImagePickerUtil.tag = tag;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        context.startActivityForResult(Intent.createChooser(intent, "Select File"), 30000);
    }

    public static void pickAudio(Activity context, ImagePickerListener imagePickerListener, String tag) {

        ImagePickerUtil.tag = tag;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        context.startActivityForResult(Intent.createChooser(intent, "Select File"), 30000);
    }

    public static void pickVideo(Activity context, ImagePickerListener imagePickerListener, String tag) {

        ImagePickerUtil.tag = tag;
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.context = context;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        context.startActivityForResult(Intent.createChooser(intent, "Select File"), 30000);
    }

    public static void cropUserImage(Activity context, String path, String tag,int picWidth,int picHeight, ImagePickerListener imagePickerListener) {
        ImagePickerUtil.imagePickerListener = imagePickerListener;
        ImagePickerUtil.tag = tag;
        ImagePickerUtil.context=context;
        ImagePickerUtil.picWidth=picWidth;
        ImagePickerUtil.picHeight=picHeight;
        Intent intent = new Intent(context, CropActivity.class);
        intent.putExtra("uri", Uri.fromFile(new File(path)));
        intent.putExtra("tag", tag);
        intent.putExtra("picWidth", picWidth);
        intent.putExtra("picHeight", picHeight);
        context.startActivityForResult(intent, 500);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                fileProfilePic = compressToFile(resultUri.getPath());
                imagePickerListener.onImagePicked(fileProfilePic, tag);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else if (requestCode == 500 && resultCode == Activity.RESULT_OK) {
            String filepath = data.getStringExtra("uri");
            fileProfilePic = compressToFile(filepath);
            imagePickerListener.onImagePicked(fileProfilePic, tag);
        } else if (resultCode == RESULT_OK) {
            if (requestCode == 10000) {
                Bitmap bm = null;
                if (cropImage) {
                    Intent intent = new Intent(context, CropActivity.class);
                    intent.putExtra("uri", mImageCaptureUri);
                    intent.putExtra("tag", tag);
                    intent.putExtra("picWidth", getwidth(mImageCaptureUri));
                    intent.putExtra("picHeight", getHeight(mImageCaptureUri));
                    context.startActivityForResult(intent, 500);
                } else {
                    if (ImagePickerUtil.compressImage) {
                        mFileCaptured = writeToFile(mImageCaptureUri);
                        mFileCaptured = compressToExistingFile(mFileCaptured.getPath(), mFileCaptured);
                        imagePickerListener.onImagePicked(mFileCaptured, tag);
                    } else {
                        mFileCaptured = writeToFile(mImageCaptureUri);
                        imagePickerListener.onImagePicked(mFileCaptured, tag);
                    }
                }

            } else if (requestCode == 10001) {
                imagePickerListener.onImagePicked(fileProfilePic, tag);
            } else if (requestCode == 20000) {
                Bitmap bm = null;
                if (data != null) {
                    if (cropImage) {
                        Intent intent = new Intent(context, CropActivity.class);
                        intent.putExtra("uri", data.getData());
                        intent.putExtra("tag", tag);
                        intent.putExtra("picWidth", picWidth);
                        intent.putExtra("picHeight", picHeight);
                        context.startActivityForResult(intent, 500);
                    } else {
                        if (ImagePickerUtil.compressImage) {
                            fileProfilePic = writeToFile(data.getData());
                            fileProfilePic = compressToFile(fileProfilePic.getPath());
                            imagePickerListener.onImagePicked(fileProfilePic, tag);
                        } else {
                            fileProfilePic = writeToFile(data.getData());
                            imagePickerListener.onImagePicked(fileProfilePic, tag);
                        }
                    }
                }
            } else if (requestCode == 20001) {
                imagePickerListener.onImagePicked(fileProfilePic, tag);
            } else if (requestCode == 700) {
                if (data != null) {
                    Uri uri = data.getData();
                    saveMediaToGallery(uri);
                }
            } else if (requestCode == 30000) {
                if (data != null) {
                    try {
                        File selectedFile = null;
                        Uri uri = data.getData();
                        // String mimeType = context.getContentResolver().getType(uri);
                        String fileName = queryName(context.getContentResolver(), uri);
                        String filenameArray[] = fileName.split("\\.");
                        String[] extension = filenameArray[filenameArray.length - 1].split(".");
                        String ext;
                        if (extension.length > 1)
                            ext = extension[1];
                        else
                            ext = extension[0];
                        fileName = tag + "." + ext;

                        InputStream input = context.getContentResolver().openInputStream(uri);
                        try {


                            selectedFile = new File(context.getFilesDir(), fileName);
                            OutputStream output = new FileOutputStream(selectedFile);
                            try {
                                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                int read;

                                while ((read = input.read(buffer)) != -1) {
                                    output.write(buffer, 0, read);
                                }

                                output.flush();
                            } finally {
                                output.close();
                            }
                        } finally {
                            input.close();
                        }

                        fileProfilePic = selectedFile;
                        imagePickerListener.onImagePicked(fileProfilePic, tag);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void saveMediaToGallery(Uri uri) {
        try {
            File selectedFile = null;
            String fileName = queryName(context.getContentResolver(), uri);
            String filePath = generatePath(uri, context);

            String[] filenameArray = filePath.split("\\.");
            String extension = filenameArray[filenameArray.length - 1];
            fileName = tag + "." + extension;

            InputStream input = context.getContentResolver().openInputStream(uri);
            try {
                selectedFile = new File(context.getFilesDir(), fileName);
                OutputStream output = new FileOutputStream(selectedFile);
                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
            fileProfilePic = selectedFile;
            imagePickerListener.onImagePicked(fileProfilePic, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
       Get File Path from URI returned
        */
    public static String generatePath(Uri uri, Context context) {
        String filePath = null;
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat) {
            filePath = generateFromKitkat(uri, context);
        }

        if (filePath != null) {
            return filePath;
        }

        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath == null ? uri.getPath() : filePath;
    }

    @TargetApi(19)
    private static String generateFromKitkat(Uri uri, Context context) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String wholeID = DocumentsContract.getDocumentId(uri);

            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Video.Media.DATA};
            String sel = MediaStore.Video.Media._ID + "=?";
            Cursor cursor = null;
            if (context.getContentResolver().getType(uri).contains("audio"))
                cursor = context.getContentResolver().
                        query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{id}, null);
            else {
                cursor = context.getContentResolver().
                        query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{id}, null);
            }

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
        }
        return filePath;
    }

    private static File writeToFile(Uri fileUri) {
        File selectedFile = null;
        String fileName = queryName(context.getContentResolver(), fileUri);
        String filenameArray[] = fileName.split("\\.");
        String extension = filenameArray[filenameArray.length - 1];
        fileName = tag + "_" + System.currentTimeMillis() + "." + extension;

        try {
            InputStream input = context.getContentResolver().openInputStream(fileUri);
            selectedFile = new File(context.getFilesDir(), fileName);
            OutputStream output = new FileOutputStream(selectedFile);

            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            output.close();
            input.close();

            Bitmap bm = decodeFile(selectedFile.getAbsolutePath(), getwidth(fileUri),getHeight(fileUri) , ScalingLogic.CROP);
            bm = fixOrientation(bm, selectedFile.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(selectedFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }

        return selectedFile;
    }


    public static int getwidth(Uri uri) {
        BitmapFactory.Options options   = new  BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
        int imageWidth = options.outWidth;

        return options.outWidth;
    }

    public static int getHeight(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
        // val imageHeight = options.outHeight
        return options.outHeight;
    }

    private static File compressToExistingFile(String filePath, File file) {
        try {
            Bitmap bm = decodeFile(filePath, picWidth, picHeight, ScalingLogic.CROP);
            bm = fixOrientation(bm, filePath);
            FileOutputStream outputStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
        return file;
    }

    private static File compressToFile(String filePath) {
        File selectedFile = null;
        try {
            Bitmap bm = decodeFile(filePath, getwidth(Uri.parse(filePath)), getHeight(Uri.parse(filePath)), ScalingLogic.CROP);
            bm = fixOrientation(bm, filePath);
            FileOutputStream outputStream;

            selectedFile = new File(context.getFilesDir(), tag + ".png");
            outputStream = new FileOutputStream(selectedFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
        return selectedFile;
    }

    private static Bitmap decodeFile(String path, int dstWidth, int dstHeight,
                                     ScalingLogic scalingLogic) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth,
                dstHeight, scalingLogic);
        Bitmap unscaledBitmap = BitmapFactory.decodeFile(path, options);

        return unscaledBitmap;
    }

    public enum ScalingLogic {
        CROP, FIT
    }

    private static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                           ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcWidth / dstWidth;
            } else {
                return srcHeight / dstHeight;
            }
        } else {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcHeight / dstHeight;
            } else {
                return srcWidth / dstWidth;
            }
        }
    }


    private static void deleteDirRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteDirRecursive(child);

        fileOrDirectory.delete();
    }

    private static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }


    public static Bitmap fixOrientation(Bitmap bm, String filePath) {
        Bitmap bitmap = null;
        ExifInterface ei = null;

        try {
            ei = new ExifInterface(filePath);
            if (ei != null) {
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap = rotateImage(bm, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap = rotateImage(bm, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap = rotateImage(bm, 270);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                        bitmap = bm;
                        break;
                 /*   case ExifInterface.ORIENTATION_UNDEFINED:
                       // if(bm.getWidth()>bm.getHeight())
                        bitmap = rotateImage(bm, 0);e
                        break;*/
                    default:
                        bitmap = bm;
                        break;
                }
                if (bitmap != null)
                    return bitmap;
                else
                    return bm;
            } else {
                return bm;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return bm;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    private static void bitmapToFile(Bitmap bmap, String name) {
        //create a file to write bitmap data
        File f = new File(context.getCacheDir(), name);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

//Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapdata);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ImagePickerListener {
        void onImagePicked(File imageFile, String tag);
    }





}
