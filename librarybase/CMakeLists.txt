# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

if("${ANDROID_ABI}" STREQUAL "armeabi-v7a" OR "${ANDROID_ABI}" STREQUAL "armeabi-v7a with NEON")
    set(ANDROID_STL "gnustl_shared")
    set(ANDROID_TOOLCHAIN "gcc")
    set(STRIP_PATH "arm-linux-androideabi")
elseif ("${ANDROID_ABI}" STREQUAL "arm64-v8a")
    set(ANDROID_STL "c++_shared")
    set(ANDROID_TOOLCHAIN "clang")
    set(STRIP_PATH "aarch64-linux-android")
else()
    message(FATAL_ERROR "${ANDROID_ABI} is no support")
endif()

# 设置变量，表示库名字
set(MY_LIBRARY_NAME librarybase)

project(${MY_LIBRARY_NAME})


# 第三方依赖库路径
set(3rdBinarys_DIR ${PROJECT_SOURCE_DIR}/../3rdparty)

# 跨平台源码库路径
set(SourceCode_DIR ${PROJECT_SOURCE_DIR}/cpp/common)

# 安卓平台源码路径
set(AndroidCode_DIR ${PROJECT_SOURCE_DIR}/cpp/android)


# 设置cmake编译库输出的位置
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})


# 头文件目录
include_directories(
        ${3rdBinarys_DIR}
        ${SourceCode_DIR}
        ${SourceCode_DIR}/common
        ${AndroidCode_DIR}
        ${AndroidCode_DIR}/librarybase
        ${AndroidCode_DIR}/utils
        ${AndroidCode_DIR}/bitmap
        ${AndroidCode_DIR}/nativebitmap
)

# 设置变量，表示所有的源文件
file(
        GLOB SOURCE_FILES
        ${SourceCode_DIR}/*.cpp
        ${SourceCode_DIR}/common/*.cpp
        ${AndroidCode_DIR}/*.cpp
        ${AndroidCode_DIR}/librarybase/*.cpp
        ${AndroidCode_DIR}/utils/*.cpp
        ${AndroidCode_DIR}/bitmap/*.cpp
        ${AndroidCode_DIR}/nativebitmap/*.cpp
)


# 需要的库文件所在的目录
link_directories(
        ${3rdBinarys_DIR}
)

# 生成的库文件
add_library(${MY_LIBRARY_NAME} SHARED ${SOURCE_FILES})


#设置编译参数
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}  -fno-rtti -fno-exceptions -fPIC")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -fPIC")

set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -O3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3")


#设置编译参数 隐藏符号-fvisibility=hidden -fvisibility-inlines-hidden


# 库体积优化选项, 优先使用库内符号，未使用的代码不会被链接
SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -ffunction-sections -fdata-sections") 
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -ffunction-sections -fdata-sections")
SET(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-Bsymbolic -Wl,--gc-sections")

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
  # using Clang
  SET(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -dead_strip")
elseif ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "GNU")
  # using GCC
elseif ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Intel")
  # using Intel C++
elseif ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "MSVC")
  # using Visual Studio C++
endif()


# 需要链接的库文件
target_link_libraries(${MY_LIBRARY_NAME}
        android
        log
        jnigraphics
        EGL
        GLESv2
)

if(DEBUG_CPP)
    message(STATUS "${MY_LIBRARY_NAME} C++ Debug Mode")
else()
    add_custom_command(TARGET ${MY_LIBRARY_NAME}
        POST_BUILD
        COMMAND mkdir -p ${PROJECT_SOURCE_DIR}/obj/${ANDROID_ABI}  &&  cp ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI}/lib${MY_LIBRARY_NAME}.so ${PROJECT_SOURCE_DIR}/obj/${ANDROID_ABI}
        COMMAND ${ANDROID_NDK_PATH}/toolchains/${STRIP_PATH}-4.9/prebuilt/darwin-x86_64/bin/${STRIP_PATH}-strip --strip-all ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI}/lib${MY_LIBRARY_NAME}.so
    )
endif()
           

