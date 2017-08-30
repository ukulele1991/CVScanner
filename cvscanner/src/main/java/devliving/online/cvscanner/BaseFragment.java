package devliving.online.cvscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;

import devliving.online.cvscanner.util.ImageSaveTask;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 8/29/17.
 */

public abstract class BaseFragment extends Fragment implements ImageSaveTask.SaveCallback {

    public interface ImageProcessorCallback{
        void onImageProcessingFailed(String reason, @Nullable Exception error);
        void onImageProcessed(Uri imageUri);
    }

    protected boolean isBusy = false;
    protected ImageProcessorCallback mCallback = null;

    protected void loadOpenCV(){
        if(!OpenCVLoader.initDebug()){
            //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, getActivity().getApplicationContext(), mLoaderCallback);
        }
        else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    protected abstract void onOpenCVConnected();
    protected abstract void onOpenCVConnectionFailed();
    protected abstract void onAfterViewCreated();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            if(status == LoaderCallbackInterface.SUCCESS){
                onOpenCVConnected();
            }
            else{
                onOpenCVConnectionFailed();
            }
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onAfterViewCreated();
        loadOpenCV();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof ImageProcessorCallback){
            mCallback = (ImageProcessorCallback) context;
        }
    }

    @Override
    public void onSaveTaskStarted() {
        isBusy = true;
    }

    @Override
    public void onSaved(Uri savedUri) {
        if(mCallback != null) mCallback.onImageProcessed(savedUri);
        isBusy = false;
    }

    @Override
    public void onSaveFailed(Exception error) {
        if(mCallback != null) mCallback.onImageProcessingFailed("Failed to save image", error);
        isBusy = false;
    }

    protected synchronized void saveCroppedImage(Bitmap bitmap, int rotation, Point[] quadPoints){
        if(!isBusy){
            new ImageSaveTask(getContext(), bitmap, rotation, quadPoints, this)
            .execute();
        }
    }
}