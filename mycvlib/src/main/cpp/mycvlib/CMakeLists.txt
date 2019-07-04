cmake_minimum_required(VERSION 3.9.6)

add_library(mycvlib SHARED
        jni/api.cpp
        src/mycvengine.cpp)

set(MY_MAIN_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/src)
set(MY_JNI_DIR ${CMAKE_CURRENT_SOURCE_DIR}/jni)

target_include_directories(mycvlib
        PUBLIC
            #$<INSTALL_INTERFACE:${MY_JNI_DIR}>
            $<BUILD_INTERFACE:${MY_MAIN_SRC_DIR}>
        PRIVATE
            ${MY_MAIN_SRC_DIR}
            /Users/ghesnsfeir/Programs/opencv410minimal/install/opencv/include/opencv4
        )

add_library(opencv-lib SHARED IMPORTED)
set_target_properties(opencv-lib
        PROPERTIES IMPORTED_LOCATION
        /Users/ghesnsfeir/Programs/OpenCV-android-sdk/sdk/native/libs/${ANDROID_ABI}/libopencv_java4.so)

# Include libraries needed for hello-jni lib
target_link_libraries(mycvlib
        opencv-lib
        android
        log)

target_compile_features(mycvlib PRIVATE cxx_std_11)

## Configure path to include directories
#include_directories(SYSTEM $ENV{VENDOR}/opencv/include )
#
## Set up OpenCV shared .so library so that it can
## be linked to your app
#add_library( cv_core-lib SHARED IMPORTED)
#set_target_properties( cv_core-lib
#        PROPERTIES IMPORTED_LOCATION
#        /Users/ghesnsfeir/Programs/OpenCV-android-sdk/
#        $ENV{VENDOR}/opencv/lib/${ANDROID_ABI}/libopencv_core.so )
#
#add_library( cv_imgproc-lib SHARED IMPORTED)
#set_target_properties( cv_imgproc-lib
#        PROPERTIES IMPORTED_LOCATION
#        $ENV{VENDOR}/opencv/lib/${ANDROID_ABI}/libopencv_imgproc.so )
#
#add_library( cv_imgcodecs-lib SHARED IMPORTED)
#set_target_properties( cv_imgcodecs-lib
#        PROPERTIES IMPORTED_LOCATION
#        $ENV{VENDOR}/opencv/lib/${ANDROID_ABI}/libopencv_imgcodecs.so )
#
#...
#
## jnigraphics lib from NDK is used for Bitmap manipulation in native code
#find_library( jnigraphics-lib jnigraphics )
#
## Link to your native app library
#target_link_libraries( my_native-lib ${jnigraphics-lib} cv_core-lib cv_imgproc-lib cv_imgcodecs-lib other-libs...)