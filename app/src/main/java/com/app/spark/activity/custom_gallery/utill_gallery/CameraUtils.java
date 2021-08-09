package com.app.spark.activity.custom_gallery.utill_gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.spark.R;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.CameraVideoPicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;

import java.util.List;

public class CameraUtils implements ImagePickerCallback, VideoPickerCallback {

    private Activity activity;

    private CameraImagePicker cameraPicker;

    private CameraVideoPicker cameraVideoPicker;

    private ImagePicker imagePicker;

    private VideoPicker videoPicker;

    private String pickerPath;

    private OnCameraResult onCameraResult;

    public static final int PERMISSION_CODE = 11;

    public CameraUtils(Activity activity, OnCameraResult onCameraResult) {
        this.activity = activity;
        this.onCameraResult = onCameraResult;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_CODE);
    }

    private void takePicture() {
        cameraPicker = new CameraImagePicker(activity);
        cameraPicker.shouldGenerateMetadata(true);
        cameraPicker.shouldGenerateThumbnails(true);
        cameraPicker.setImagePickerCallback(this);
        pickerPath = cameraPicker.pickImage();
    }


    private void takeVideo() {
        Bundle extras = new Bundle();
        // For capturing Low quality videos; Default is 1: HIGH
        //extras.putInt(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        // Set the duration of the video
        extras.putInt(MediaStore.EXTRA_DURATION_LIMIT, 60);
        cameraVideoPicker = new CameraVideoPicker(activity);
        cameraVideoPicker.shouldGenerateMetadata(true);
        cameraVideoPicker.shouldGeneratePreviewImages(true);
        cameraVideoPicker.setVideoPickerCallback(this);
        cameraVideoPicker.setExtras(extras);
        pickerPath = cameraVideoPicker.pickVideo();
    }

    private void pickImageSingle() {
        imagePicker = new ImagePicker(activity);
        imagePicker.shouldGenerateMetadata(true);
        imagePicker.shouldGenerateThumbnails(true);
        imagePicker.setImagePickerCallback(this);
        imagePicker.pickImage();
    }


    private void pickVideoSingle() {
        videoPicker = new VideoPicker(activity);
        videoPicker.shouldGenerateMetadata(true);
        videoPicker.setVideoPickerCallback(this);
        videoPicker.pickVideo();
    }

    @Override
    public void onImagesChosen(List<ChosenImage> list) {
        if (onCameraResult != null) {
            onCameraResult.onSuccess(list);
        }
    }

    @Override
    public void onError(String s) {
        if (onCameraResult != null) {
            onCameraResult.onError(s);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Picker.PICK_IMAGE_DEVICE) {
                if (imagePicker == null) {
                    imagePicker = new ImagePicker(activity);
                    imagePicker.setImagePickerCallback(this);
                }
                imagePicker.submit(data);
            } else if (requestCode == Picker.PICK_IMAGE_CAMERA) {
                if (cameraPicker == null) {
                    cameraPicker = new CameraImagePicker(activity);
                    cameraPicker.setImagePickerCallback(this);
                    cameraPicker.reinitialize(pickerPath);
                }
                cameraPicker.submit(data);
            }else if (requestCode == Picker.PICK_VIDEO_DEVICE) {
                if (videoPicker == null) {
                    videoPicker = new VideoPicker(activity);
                    videoPicker.setVideoPickerCallback(this);
                }
                videoPicker.submit(data);
            } else if (requestCode == Picker.PICK_VIDEO_CAMERA) {
                if (cameraVideoPicker == null) {
                    cameraVideoPicker = new CameraVideoPicker(activity);
                    cameraVideoPicker.setVideoPickerCallback(this);
                    cameraVideoPicker.reinitialize(pickerPath);
                }
                cameraVideoPicker.submit(data);
            }
        }
    }
    @Override
    public void onVideosChosen(List<ChosenVideo> list) {
        if(onCameraResult!=null){
            onCameraResult.onVideoSuccess(list);
        }
    }

    public interface OnCameraResult {
        void onSuccess(List<ChosenImage> images);

        void onVideoSuccess(List<ChosenVideo> videos);

        void onError(String error);
    }
}
