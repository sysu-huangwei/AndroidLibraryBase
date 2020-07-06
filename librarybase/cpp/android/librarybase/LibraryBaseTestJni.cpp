//
// Created by HW on 2020/4/23.
//

#include <cstring>
#include "LibraryBaseTestJni.h"

#include "AndroidHelper.h"
#include "JniHelper.h"
#include "BaseLog.h"

#include "CL/cl.h"

#define CLASS_NAME_PATH "com/example/librarybase/LibraryBaseTest"

namespace librarybase {

    class LibraryBaseTestJni {
    public:
        static void runTest(JNIEnv *env, jclass obj) {

            char *packageName = AndroidHelper::getAndroidPackageName(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: packageName = %s", packageName);
            SAFE_DELETE_ARRAY(packageName);

            char *androidDataDir = AndroidHelper::getAndroidDataDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidDataDir = %s", androidDataDir);
            SAFE_DELETE_ARRAY(androidDataDir);

            char *androidCacheDir = AndroidHelper::getAndroidCacheDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidCacheDir = %s", androidCacheDir);
            SAFE_DELETE_ARRAY(androidCacheDir);

            char *androidFilesDir = AndroidHelper::getAndroidFilesDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidFilesDir = %s", androidFilesDir);
            SAFE_DELETE_ARRAY(androidFilesDir);

            char *androidExternalCacheDir = AndroidHelper::getAndroidExternalCacheDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidExternalCacheDir = %s", androidExternalCacheDir);
            SAFE_DELETE_ARRAY(androidExternalCacheDir);

            char *androidExternalFilesDir = AndroidHelper::getAndroidExternalFilesDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidExternalFilesDir = %s", androidExternalFilesDir);
            SAFE_DELETE_ARRAY(androidExternalFilesDir);

            char *androidExternalStorageDirectory = AndroidHelper::getAndroidExternalStorageDirectory(env);
            LOGD("LibraryBaseTestJni::runTest: androidExternalStorageDirectory = %s", androidExternalStorageDirectory);
            SAFE_DELETE_ARRAY(androidExternalStorageDirectory);

            int androidCpuCount = AndroidHelper::getAndroidCpuCount();
            LOGD("LibraryBaseTestJni::runTest: androidCpuCount = %d", androidCpuCount);

            int androidSDKVersion = AndroidHelper::getAndroidSDKVersion(env);
            LOGD("LibraryBaseTestJni::runTest: androidSDKVersion = %d", androidSDKVersion);

            bool mkDirsSuccess = AndroidHelper::mkDirs(env, "/mnt/sdcard/Android/data/com.example.androidlibrarybase/files/librarybase");
            LOGD("LibraryBaseTestJni::runTest: mkDirs = %s mkDirsSuccess = %d", "mnt/sdcard/librarybase", mkDirsSuccess);

            bool deleteFileSuccess = AndroidHelper::deleteFile(env, "/mnt/sdcard/Android/data/com.example.androidlibrarybase/files/librarybase");
            LOGD("LibraryBaseTestJni::runTest: deleteFile = %s deleteFileSuccess = %d", "mnt/sdcard/librarybase", deleteFileSuccess);

            cl_int errorCode;

          cl_platform_id  platforms;
          cl_uint         num_platforms;
          errorCode = clGetPlatformIDs(1, &platforms, &num_platforms);
          LOGE("LibraryBaseTestJni::clGetPlatformIDs: hw1 errorCode = %d num_platforms = %d, platforms = %p", errorCode, num_platforms, platforms);

          cl_device_type deviceType = CL_DEVICE_TYPE_GPU;

          cl_device_id     devices;
          cl_uint          num_devices;
          errorCode = clGetDeviceIDs(platforms, deviceType, 1, &devices, &num_devices);
          LOGE("LibraryBaseTestJni::clGetPlatformIDs: hw1 errorCode = %d devices = %p, num_devices = %d", errorCode, devices, num_devices);

          cl_context_properties prop[] = { CL_CONTEXT_PLATFORM, reinterpret_cast<cl_context_properties>(platforms), 0 };
          cl_context context = clCreateContextFromType(prop, deviceType, NULL, NULL, &errorCode);
          LOGE("LibraryBaseTestJni::clCreateContextFromType: hw1 errorCode = %d context = %p", errorCode, context);

          cl_command_queue command_queue = clCreateCommandQueue(context, devices, 0, &errorCode);
          LOGE("LibraryBaseTestJni::clCreateCommandQueue: hw1 errorCode = %d command_queue = %p", errorCode, command_queue);

          const char* source = "__kernel void adder(__global const int* a, __global const int* b, __global int* result1, __global int* result2)"
                               "{"
                               "  int idx = get_global_id(0);"
                               "  result1[idx] = a[idx]*2;"
                               "  result2[idx] = -b[idx]*2;"
                               "}";

          size_t size = strlen(source);

          cl_program program = clCreateProgramWithSource(context, 1, &source, &size, &errorCode);
          LOGE("LibraryBaseTestJni::clCreateProgramWithSource: hw1 errorCode = %d program = %p  size = %d", errorCode, program, size);

          errorCode = clBuildProgram(program, 1, &devices, 0, 0, 0);
          LOGE("LibraryBaseTestJni::clBuildProgram: hw1 errorCode = %d program = %p", errorCode, program);

          errorCode = clGetProgramBuildInfo(program, devices, CL_PROGRAM_BUILD_LOG, 0, NULL, &size);
          char* message = new char[size+1];
          errorCode = clGetProgramBuildInfo(program, devices, CL_PROGRAM_BUILD_LOG, size+1, message, NULL);
          LOGE("LibraryBaseTestJni::clGetProgramBuildInfo: hw1 errorCode = %d message = %s, size = %d", errorCode, message, size);
          delete [] message;

          cl_kernel adder = clCreateKernel(program, "adder", &errorCode);
          LOGE("LibraryBaseTestJni::clCreateKernel: hw1 errorCode = %d adder = %p", errorCode, adder);

          const int DATA_SIZE = 100;
          int *a = new int[DATA_SIZE];
          int *b = new int[DATA_SIZE];
          for (int i = 0; i < DATA_SIZE; ++i) {
            a[i] = i;
            b[i] = i;
          }

          cl_mem cl_a = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, sizeof(cl_int) * DATA_SIZE, &a[0], &errorCode);
          LOGE("LibraryBaseTestJni::clCreateBuffer a: hw1 errorCode = %d", errorCode);
          cl_mem cl_b = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, sizeof(cl_int) * DATA_SIZE, &b[0], &errorCode);
          LOGE("LibraryBaseTestJni::clCreateBuffer b: hw1 errorCode = %d", errorCode);
          cl_mem cl_res1 = clCreateBuffer(context, CL_MEM_WRITE_ONLY, sizeof(cl_int) * DATA_SIZE, NULL, &errorCode);
          LOGE("LibraryBaseTestJni::clCreateBuffer res1: hw1 errorCode = %d", errorCode);
          cl_mem cl_res2 = clCreateBuffer(context, CL_MEM_WRITE_ONLY, sizeof(cl_int) * DATA_SIZE, NULL, &errorCode);
          LOGE("LibraryBaseTestJni::clCreateBuffer res2: hw1 errorCode = %d", errorCode);

          errorCode = clSetKernelArg(adder, 0, sizeof(cl_mem), &cl_a);
          LOGE("LibraryBaseTestJni::clSetKernelArg c: hw1 errorCode = %d", errorCode);
          errorCode = clSetKernelArg(adder, 1, sizeof(cl_mem), &cl_b);
          LOGE("LibraryBaseTestJni::clSetKernelArg b: hw1 errorCode = %d", errorCode);
          errorCode = clSetKernelArg(adder, 2, sizeof(cl_mem), &cl_res1);
          LOGE("LibraryBaseTestJni::clSetKernelArg res1: hw1 errorCode = %d", errorCode);
          errorCode = clSetKernelArg(adder, 3, sizeof(cl_mem), &cl_res2);
          LOGE("LibraryBaseTestJni::clSetKernelArg res2: hw1 errorCode = %d", errorCode);

          size_t work_size = 512;
          errorCode = clEnqueueNDRangeKernel(command_queue, adder, 1, 0, &work_size, 0, 0, 0, 0);
          LOGE("LibraryBaseTestJni::clEnqueueNDRangeKernel: hw1 errorCode = %d", errorCode);

          int *res1 = new int[DATA_SIZE];
          errorCode = clEnqueueReadBuffer(command_queue, cl_res1, CL_TRUE, 0, sizeof(cl_int) * DATA_SIZE, res1, 0, NULL, NULL);
          LOGE("LibraryBaseTestJni::clEnqueueReadBuffer res1: hw1 errorCode = %d", errorCode);
          int *res2 = new int[DATA_SIZE];
          errorCode = clEnqueueReadBuffer(command_queue, cl_res2, CL_TRUE, 0, sizeof(cl_int) * DATA_SIZE, res2, 0, NULL, NULL);
          LOGE("LibraryBaseTestJni::clEnqueueReadBuffer res2: hw1 errorCode = %d", errorCode);

          for (int i = 0; i < DATA_SIZE; ++i) {
            LOGE("LibraryBaseTestJni res1[%d] = %d", i, res1[i]);
          }
          for (int i = 0; i < DATA_SIZE; ++i) {
            LOGE("LibraryBaseTestJni res2[%d] = %d", i, res2[i]);
          }

          delete [] a;
          delete [] b;
          delete [] res1;
          delete [] res2;

          clReleaseKernel(adder);
          clReleaseProgram(program);
          clReleaseMemObject(cl_a);
          clReleaseMemObject(cl_b);
          clReleaseMemObject(cl_res1);
          clReleaseMemObject(cl_res2);
          clReleaseCommandQueue(command_queue);
          clReleaseContext(context);
        }
    };

    // jni 接口映射
    static JNINativeMethod methods[] = {
            {"nativeRunTest", "()V", (void *) LibraryBaseTestJni::runTest},
    };

    int registerLibraryBaseTestNativeMethods(JNIEnv *env, void *reserved) {
        jclass cls = env->FindClass(CLASS_NAME_PATH);
        if (cls == NULL) {
            return JNI_ERR;
        }
        // 注册方法
        jint nRes = env->RegisterNatives(cls, methods, sizeof(methods) / sizeof(methods[0]));
        if (nRes < 0) {
            return JNI_ERR;
        }

        return JNI_OK;
    }

}