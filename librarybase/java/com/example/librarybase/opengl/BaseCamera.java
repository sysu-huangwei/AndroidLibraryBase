package com.example.librarybase.opengl;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;

import androidx.annotation.IntDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

/**
 * User: HW
 * Date: 2020/5/7
 * Description: 相机逻辑封装
 */
public class BaseCamera implements SurfaceTexture.OnFrameAvailableListener {

    /**
     * 前置&后置摄像头
     */
    @IntDef({Camera.CameraInfo.CAMERA_FACING_BACK, Camera.CameraInfo.CAMERA_FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraFacingEnum {
    }

    /**
     * 相机预览比例
     */
    public final static int BASE_CAMERA_ASPECT_RATIO_16_9 = 0;
    public final static int BASE_CAMERA_ASPECT_RATIO_4_3 = 1;
    public final static int BASE_CAMERA_ASPECT_RATIO_1_1 = 2;
    @IntDef({BASE_CAMERA_ASPECT_RATIO_16_9, BASE_CAMERA_ASPECT_RATIO_4_3, BASE_CAMERA_ASPECT_RATIO_1_1})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraAspectRatioEnum {
    }

    private Camera mCamera = null; // 相机实例
    private int mPreviewWidth = 0; // 预览宽
    private int mPreviewHeight = 0; // 预览高
    private boolean mPreviewSizeChange = false; // 是否改变了预览尺寸，需要重新生成FBO
    private int mFrontCameraID = 0; // 前置相机ID
    private int mBackCameraID = 0; // 后置相机ID
    private @CameraFacingEnum int mFacing = Camera.CameraInfo.CAMERA_FACING_FRONT; // 当前相机朝向， 0：后置  1：前置

    private List<Camera.Size> mPreviewSizes = null;
    private List<Camera.Size> mPictureSizes = null;
    private @CameraAspectRatioEnum int mAspectRatio = BASE_CAMERA_ASPECT_RATIO_16_9;

    private volatile boolean mIsWaitingRender = false; // 是否正在等待渲染，必须要发出onFrameAvailable之后才是要渲染的

    private SurfaceTexture mSurfaceTexture = null; // 获取相机的图像流
    private int mSurfaceTextureID = 0; // 获取相机的图像流纹理ID，与mSurfaceTexture绑定

    private int mOutputTexture = 0; // 用于输出给后续滤镜的相机原始帧的2D纹理
    private int mOutputFrameBuffer = 0; // 用于输出给后续滤镜的相机原始帧的FBO，与mOutputTexture绑定

    private boolean mHasInitGL = false; // 是否已经初始化GL资源

    private Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter(); // 把相机原始OES纹理mSurfaceTextureID画到输出的2D纹理mOutputTexture上面

    private BaseCameraCallback mBaseCameraCallback; // 相机事件回调


    /**
     * 初始化GL资源，必须在GL线程
     * 初始化完GL资源之后才能配置相机
     */
    public void initGL() {
        mBase2DTexturePainter.init(true);

        mSurfaceTextureID = BaseGLUtils.createExternalOESTextures();
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mOutputTexture = BaseGLUtils.createTextures2D();

        mHasInitGL = true;

        if (mBaseCameraCallback != null) {
            mBaseCameraCallback.onInitGLComplete();
        }
    }

    /**
     * 初始化相机
     */
    public void initCamera() {
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

        if (mCamera == null) {
            if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(mFrontCameraID);
            } else {
                mCamera = Camera.open(mBackCameraID);
            }
        }
    }

    /**
     * 配置相机，必须在初始化完GL资源之后才能配置相机
     */
    public void setupCamera() {
        if (mCamera != null && mHasInitGL) {
            Camera.Parameters parameters = mCamera.getParameters();

            mPreviewSizes = parameters.getSupportedPreviewSizes();
            if (mPreviewSizes != null) {
                Collections.sort(mPreviewSizes, (o1, o2) -> o2.width - o1.width);
                Camera.Size previewSize = getSuitableSize(mPreviewSizes, mAspectRatio);
                if (previewSize != null) {
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    //交换宽高，因为相机获取的尺寸永远都是宽>高
                    int newPreviewWidth = previewSize.height;
                    int newPreviewHeight = previewSize.width;
                    if (newPreviewWidth != mPreviewWidth || newPreviewHeight != mPreviewHeight) {
                        mPreviewWidth = newPreviewWidth;
                        mPreviewHeight = newPreviewHeight;
                        mPreviewSizeChange = true;
                    }
                }
            }

            mPictureSizes = parameters.getSupportedPictureSizes();
            if (mPictureSizes != null) {
                Collections.sort(mPictureSizes, (o1, o2) -> o2.width - o1.width);
                Camera.Size pictureSize = getSuitableSize(mPictureSizes, mAspectRatio);
                if (pictureSize != null) {
                    parameters.setPictureSize(pictureSize.width, pictureSize.height);
                }
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
     * 获取最合适的预览尺寸
     * @param sizes 所有可选的尺寸
     * @param aspectRatio 当前的预览比例
     * @return 最合适的预览尺寸
     */
    private Camera.Size getSuitableSize(List<Camera.Size> sizes, @CameraAspectRatioEnum int aspectRatio) {
        if (sizes != null && sizes.size() > 0) {
            float ratioMin, ratioMax;
            float e = 0.05f;
            if (aspectRatio == BASE_CAMERA_ASPECT_RATIO_16_9) {
                ratioMin = 16.0f/9.0f;
                ratioMax = 16.0f/9.0f;
            } else if (aspectRatio == BASE_CAMERA_ASPECT_RATIO_4_3) {
                ratioMin = 4.0f/3.0f;
                ratioMax = 4.0f/3.0f;
            } else /*if (aspectRatio == BASE_CAMERA_ASPECT_RATIO_1_1)*/ {
                ratioMin = 1.0f/1.0f;
                ratioMax = 1.0f/1.0f;
            }
            ratioMin -= e;
            ratioMax += e;

            for (Camera.Size size: sizes) {
                float ratio = (float)size.width / (float)size.height;
                if (ratioMin < ratio && ratio < ratioMax) {
                    return size;
                }
            }
        }
        return null;
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
     * 获取当前预览比例
     * @return 当前预览比例
     */
    public @CameraAspectRatioEnum int getAspectRatio() {
        return mAspectRatio;
    }

    /**
     * 设置预览比例
     * @param aspectRatio 预览比例
     */
    public void setAspectRatio(@CameraAspectRatioEnum int aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            stopPreview();
            setupCamera();
            startPreview();
        }
    }

    /**
     * 切换前后置摄像头
     */
    public void switchCameraFacing() {
        mIsWaitingRender = false;

        // 先停止当前的预览并释放相机
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        // 打开新的方向的相机
        if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCamera = Camera.open(mBackCameraID);
            mFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCamera = Camera.open(mFrontCameraID);
            mFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        // 配置相机
        setupCamera();

        //开始预览
        mCamera.startPreview();
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
            mIsWaitingRender = true;
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
     * 释放GL资源，必须在GL线程
     */
    public void releaseGL() {
        mHasInitGL = false;
        if (mSurfaceTextureID > 0) {
            GLES20.glDeleteTextures(1, new int[]{mSurfaceTextureID}, 0);
            mSurfaceTextureID = 0;
        }
        mBase2DTexturePainter.release();
    }

    /**
     * 释放相机
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 把相机当前帧数据绘制到一个2D纹理上，必须在GL线程
     *
     * @return 相机当前帧数据的2D纹理
     */
    public int render() {
        if (mHasInitGL) {
            mSurfaceTexture.updateTexImage();
            // 如果预览尺寸改变了，需要重新生成新的尺寸的FBO
            if (mPreviewSizeChange) {
                GLES20.glDeleteFramebuffers(1, new int[]{mOutputFrameBuffer}, 0);
                mOutputFrameBuffer = BaseGLUtils.createFBO(mOutputTexture, mPreviewWidth, mPreviewHeight);
                mPreviewSizeChange = false;
            }
            if (mIsWaitingRender) {
                if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mBase2DTexturePainter.renderToFBO(mSurfaceTextureID, mPreviewWidth, mPreviewHeight, mOutputTexture, mOutputFrameBuffer, mPreviewWidth, mPreviewHeight, 6);
                } else if (mFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mBase2DTexturePainter.renderToFBO(mSurfaceTextureID, mPreviewWidth, mPreviewHeight, mOutputTexture, mOutputFrameBuffer, mPreviewWidth, mPreviewHeight, 7);
                }
            }
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

        /**
         * 初始化GL资源完毕，后续才能配置相机
         */
        void onInitGLComplete();
    }
}
