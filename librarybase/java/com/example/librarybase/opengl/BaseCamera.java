package com.example.librarybase.opengl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;

import androidx.annotation.IntDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;

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
    private int mFrontCameraID = 0; // 前置相机ID
    private int mBackCameraID = 0; // 后置相机ID
    private @CameraFacingEnum int mFacing = Camera.CameraInfo.CAMERA_FACING_FRONT; // 当前相机朝向， 0：后置  1：前置
    private @CameraAspectRatioEnum int mAspectRatio = BASE_CAMERA_ASPECT_RATIO_16_9; // 当前相机的预览比例

    private List<Camera.Size> mPreviewSizes = null; // 当前相机支持的所有预览尺寸
    private List<Camera.Size> mPictureSizes = null; // 当前相机支持的所有拍照尺寸

    private int mPreviewWidth = 0; // 当前预览宽
    private int mPreviewHeight = 0; // 当前预览高
    private boolean mPreviewSizeChange = false; // 是否改变了预览尺寸，需要重新生成FBO
    private int mPictureWidth = 0; // 当前拍照宽
    private int mPictureHeight = 0; // 当前拍照高

    private Bitmap mPictureBitmap = null; // 拍照的结果图像
    private boolean mIsRenderPicture = false; // 是否需要渲染拍照帧

    private volatile boolean mIsPreviewFrameAvailable = false; // 下一预览帧是否已经可用，必须要发出onFrameAvailable之后才是要渲染的

    private SurfaceTexture mSurfaceTexture = null; // 获取相机的图像流
    private int mSurfaceTextureID = 0; // 获取相机的图像流纹理ID，与mSurfaceTexture绑定

    private int mPictureTextureID = 0; // 拍照帧的纹理ID

    private int mOutputTextureID = 0; // 用于输出给后续滤镜的相机原始帧的2D纹理
    private int mOutputFrameBufferID = 0; // 用于输出给后续滤镜的相机原始帧的FBO，与mOutputTexture绑定

    private boolean mHasInitGL = false; // 是否已经初始化GL资源

    private BaseOESTexturePainter mBaseOESTexturePainter = new BaseOESTexturePainter(); // 把相机原始OES纹理mSurfaceTextureID画到输出的2D纹理mOutputTexture上面

    private BaseCameraCallback mBaseCameraCallback; // 相机事件回调


    /**
     * 初始化GL资源，必须在GL线程
     * 初始化完GL资源之后才能配置相机
     */
    public void initGL() {
        mBaseOESTexturePainter.init();

        mSurfaceTextureID = BaseGLUtils.createExternalOESTextures();
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mPictureTextureID = BaseGLUtils.createTextures2D();

        mOutputTextureID = BaseGLUtils.createTextures2D();

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
                    //交换宽高，因为相机获取的尺寸永远都是宽>高
                    int newPictureWidth = pictureSize.height;
                    int newPictureHeight = pictureSize.width;
                    if (newPictureWidth != mPictureWidth || newPictureHeight != mPictureHeight) {
                        mPictureWidth = newPictureWidth;
                        mPictureHeight = newPictureHeight;
                    }
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
            mIsRenderPicture = false;
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
     * 拍照，拍照的结果在{@link BaseCameraCallback#onTakePictureEnd(Bitmap)}里接收
     */
    public void takePicture() {
        if (mCamera != null) {
            mCamera.takePicture(null, null, (data, camera) -> {
                if (data != null && mBaseCameraCallback != null) {
                    // 拍照的照片跟预览一样需要旋转，其中前置需要逆时针90°并左右镜像，后置需要顺时针90°
                    mPictureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        matrix.setRotate(-90.0f);
                        matrix.postScale(-1.0f, 1.0f);
                        mPictureBitmap = Bitmap.createBitmap(mPictureBitmap, 0, 0, mPictureBitmap.getWidth(), mPictureBitmap.getHeight(), matrix, false);
                    } else /*if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT)*/{
                        matrix.setRotate(90.0f);
                        mPictureBitmap = Bitmap.createBitmap(mPictureBitmap, 0, 0, mPictureBitmap.getWidth(), mPictureBitmap.getHeight(), matrix, false);
                    }
                    mBaseCameraCallback.onTakePictureEnd(mPictureBitmap); // 把拍照的结果图回调给外部
                    mIsRenderPicture = true;
                }
            });
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
            mIsPreviewFrameAvailable = false;
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
        mIsPreviewFrameAvailable = false;

        // 先停止当前的预览并释放相机
        stopPreview();
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
        startPreview();
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
            mIsPreviewFrameAvailable = true;
        }
    }

    /**
     * 获取输出纹理的宽
     *
     * @return 输出纹理的宽
     */
    public int getOutputTextureWidth() {
        if (mIsRenderPicture) {
            return mPictureWidth;
        } else {
            return mPreviewWidth;
        }
    }

    /**
     * 获取输出纹理的高
     *
     * @return 输出纹理的高
     */
    public int getOutputTextureHeight() {
        if (mIsRenderPicture) {
            return mPictureHeight;
        } else {
            return mPreviewHeight;
        }
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
        mBaseOESTexturePainter.release();
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
            // 拍照逻辑，直接生成拍照帧的纹理返回
            if (mIsRenderPicture) {
                GLES20.glBindTexture(GL_TEXTURE_2D, mPictureTextureID);
                android.opengl.GLUtils.texImage2D(GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mPictureBitmap, GL_UNSIGNED_BYTE, 0);
                return mPictureTextureID;
            }
            // 预览逻辑
            if (mPreviewSizeChange) {
                // 如果预览尺寸改变了，需要重新生成新的尺寸的FBO
                GLES20.glDeleteFramebuffers(1, new int[]{mOutputFrameBufferID}, 0);
                mOutputFrameBufferID = BaseGLUtils.createFBO(mOutputTextureID, mPreviewWidth, mPreviewHeight);
                mPreviewSizeChange = false;
            }
            mSurfaceTexture.updateTexImage();
            if (mIsPreviewFrameAvailable) {
                if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mBaseOESTexturePainter.renderToFBO(mSurfaceTextureID, mPreviewWidth, mPreviewHeight, mOutputTextureID, mOutputFrameBufferID, mPreviewWidth, mPreviewHeight, 6);
                } else if (mFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mBaseOESTexturePainter.renderToFBO(mSurfaceTextureID, mPreviewWidth, mPreviewHeight, mOutputTextureID, mOutputFrameBufferID, mPreviewWidth, mPreviewHeight, 7);
                }
            }
        }
        return mOutputTextureID;
    }


    public void setFaceDetectEnable(boolean enable) {
        if (mCamera != null) {
            if (enable) {
                mCamera.setFaceDetectionListener(((faces, camera) -> {
                    if (mBaseCameraCallback != null) {
                        mBaseCameraCallback.onFaceDetected(faces, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height);
                    }
                }));
                mCamera.startFaceDetection();
            } else {
                mCamera.stopFaceDetection();
            }
        }
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

        /**
         * 拍照结束，如果需要纹理还需要再调用一次render()
         * @param bitmap 拍照的结果
         */
        void onTakePictureEnd(Bitmap bitmap);

        /**
         * 人脸检测结果回调
         * @param faces 人脸数据
         * @param width 检测的宽
         * @param height 检测的高
         */
        void onFaceDetected(Camera.Face[] faces, int width, int height);
    }
}
