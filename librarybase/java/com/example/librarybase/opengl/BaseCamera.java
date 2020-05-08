package com.example.librarybase.opengl;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;

import java.io.IOException;
import java.util.List;

/**
 * User: HW
 * Date: 2020/5/7
 * Description: 相机逻辑封装
 */
public class BaseCamera {

    private Camera mCamera = null; // 相机实例
    private Camera.Size mPreviewSize = null; // 当前预览尺寸

    private SurfaceTexture mSurfaceTexture = null; // 获取相机的图像流
    private int mSurfaceTextureID = 0; // 获取相机的图像流纹理ID，与mSurfaceTexture绑定

    private int mOutputTexture = 0; // 用于输出给后续滤镜的相机原始帧的2D纹理
    private int mOutputFrameBuffer = 0; // 用于输出给后续滤镜的相机原始帧的FBO，与mOutputTexture绑定

    private Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter(); // 把相机原始OES纹理mSurfaceTextureID画到输出的2D纹理mOutputTexture上面

    private BaseCameraCallback mBaseCameraCallback; // 相机事件回调


    /**
     * 初始化相机，必须在GL线程
     */
    public void init() {
        mBase2DTexturePainter.init(true);

        mSurfaceTextureID = BaseGLUtils.createExternalOESTextures();
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (mBaseCameraCallback != null) {
                    mBaseCameraCallback.onFrameAvailable();
                }
            }
        });

        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
            }
        }

        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            if (previewSizes.size() > 0) {
                parameters.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
                mPreviewSize = previewSizes.get(0);
            }

            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            if (pictureSizes.size() > 0) {
                parameters.setPictureSize(pictureSizes.get(0).width, pictureSizes.get(0).height);
            }

            mCamera.setParameters(parameters);

            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mOutputTexture = BaseGLUtils.createTextures2D();
        mOutputFrameBuffer = BaseGLUtils.createFBO(mOutputTexture, mPreviewSize.width, mPreviewSize.height);
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * 设置相机事件回调
     *
     * @param baseCameraCallback 相机事件回调
     */
    public void setBaseCameraCallback(BaseCameraCallback baseCameraCallback) {
        mBaseCameraCallback = baseCameraCallback;
    }

    /**
     * 获取当前预览尺寸
     *
     * @return 当前预览尺寸
     */
    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    /**
     * 释放相机和其他资源，必须在GL线程
     */
    public void release() {
        if (mCamera != null) {
            mCamera.release();
        }
        if (mSurfaceTextureID > 0) {
            GLES20.glDeleteTextures(1, new int[]{mSurfaceTextureID}, 0);
            mSurfaceTextureID = 0;
        }
        mBase2DTexturePainter.release();
    }

    /**
     * 把相机当前帧数据绘制到一个2D纹理上，必须在GL线程
     *
     * @return 相机当前帧数据的2D纹理
     */
    public int render() {
        if (mBase2DTexturePainter != null) {
            mSurfaceTexture.updateTexImage();
            mBase2DTexturePainter.renderToFBO(mSurfaceTextureID, mPreviewSize.width, mPreviewSize.height, mOutputTexture, mOutputFrameBuffer, mPreviewSize.width, mPreviewSize.height);
            return mOutputTexture;
        }
        return 0;
    }


    /**
     * 相机事件回调
     */
    public interface BaseCameraCallback {

        /**
         * 新的可用相机帧数据就绪（可以拿来渲染了）
         */
        void onFrameAvailable();
    }
}
