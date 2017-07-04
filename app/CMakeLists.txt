#指定CMake的最小版本
cmake_minimum_required(VERSION 3.4.1)

set(libs_DIR src/main/jniLibs/${ANDROID_ABI})

set(libs_include_DIR src/main/cpp)

#确保 CMake 可以在编译时定位您的标头文件以及源文件
include_directories(${lib_include_DIR})

link_directories(${libs_DIR})

#添加我们自己的模块，名字是wakeup，SHARED可分享的，以及配置源文件或者头文件的路径
add_library( wakeup
             SHARED
             src/main/cpp/wakeup.cpp
             src/main/cpp/Interface.h
           )
#找到log这个模块
find_library(log-lib
              log
            )
#把我们自己的模块和动态库bdEASRAndroid模块，找到log这个模块，关联起来
target_link_libraries(wakeup
                      bdEASRAndroid
                      ${log-lib}
                      )

# 注意：bdEASRAndroid so库不要带版本号 比如：bdEASRAndroid.v1.10.10
# jniLibs 里面也不要带版本号