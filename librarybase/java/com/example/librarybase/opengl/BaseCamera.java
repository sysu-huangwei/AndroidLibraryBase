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
public class BaseCamera implements SurfaceTexture.OnFrameAvailableListener {

    private Camera mCamera = null; // 相机实例
    private int mPreviewWidth = 0; // 预览宽
    private int mPreviewHeight = 0; // 预览高
    private int mFrontCameraID = 0; // 前置相机ID
    private int mBackCameraID = 0; // 后置相机ID
    private int mFacing = Camera.CameraInfo.CAMERA_FACING_FRONT; // 当前相机朝向， 0：后置  1：前置

    private boolean isNeedSwitchCameraFacing = false; // 是否需要切换前后置摄像头，避免和绘制异步导致效果问题

    private SurfaceTexture mSurfaceTexture = null; // 获取相机的图像流
    private int mSurfaceTextureID = 0; // 获取相机的图像流纹理ID，与mSurfaceTexture绑定

    private int mOutputTexture = 0; // 用于输出给后续滤镜的相机原始帧的2D纹理
    private int mOutputFrameBuffer = 0; // 用于输出给后续滤镜的相机原始帧的FBO，与mOutputTexture绑定

    private Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter(); // 把相机原始OES纹理mSurfaceTextureID画到输出的2D纹理mOutputTexture上面

    private BaseCameraCallback mBaseCameraCallback; // 相机事件回调


    /**
     * 初始化，必须在GL线程
     */
    public void init() {
        mBase2DTexturePainter.init(true);

        mSurfaceTextureID = BaseGLUtils.createExternalOESTextures();
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        initCamera();
        setupCamera();

        mOutputTexture = BaseGLUtils.createTextures2D();
        mOutputFrameBuffer = BaseGLUtils.createFBO(mOutputTexture, mPreviewWidth, mPreviewHeight);
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraID = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraID = i;
            }
        }

        mCamera = Camera.open(mBackCameraID);
        mFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * 配置相机
     */
    private void setupCamera() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            if (previewSizes.size() > 0) {
                parameters.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
                //交换宽高，因为相机获取的尺寸永远都是宽>高
                mPreviewWidth = previewSizes.get(0).height;
                mPreviewHeight = previewSizes.get(0).width;
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
     * 切换前后置摄像头
     */
    public void switchCameraFacing() {
        isNeedSwitchCameraFacing = true;

        int nextFacing = 1 - mFacing; // 计算需要打开前置还是后置

        // 先停止当前的预览并释放相机
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        // 打开新的方向的相机
        if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCamera = Camera.open(mBackCameraID);
        } else {
            mCamera = Camera.open(mFrontCameraID);
        }

        // 配置相机
        setupCamera();

        //开始预览
        mCamera.startPreview();

        mFacing = nextFacing;
    }

    /**
     * 设置相机事件回调
     *
     * @param baseCameraCallback 相机事件回调
     */
    public void setBaseCameraCallback(BaseCameraCallback baseCameraCallback) {
        mBaseCameraCallback = baseCameraCallback;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mBaseCameraCallback != null) {
            mBaseCameraCallback.onFrameAvailable();
        }
    }

    /**
     * 获取当前预览尺寸宽
     *
     * @return 当前预览尺寸宽
     */
    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    /**
     * 获取当前预览尺寸高
     *
     * @return 当前预览尺寸高
     */
    public int getPreviewHeight() {
        return mPreviewHeight;
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
        mSurfaceTexture.updateTexImage();
        if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mBase2DTexturePainter.renderToFBO(mSurfaceTextureID, mPreviewWidth, mPreviewHeight, mOutputTexture, mOutputFrameBuffer, mPreviewWidth, mPreviewHeight, 6);
        } else if (mFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mBase2DTexturePainter.renderToFBO(mSurfaceTextureID, mPreviewWidth, mPreviewHeight, mOutputTexture, mOutputFrameBuffer, mPreviewWidth, mPreviewHeight, 7);
        }
        return mOutputTexture;
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
