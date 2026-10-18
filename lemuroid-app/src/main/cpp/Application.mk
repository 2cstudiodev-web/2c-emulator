APP_ABI := arm64-v8a
APP_PLATFORM := android-23
# c++_shared makes the build package libc++_shared.so, which some prebuilt cores
# (MAME 2016) link against dynamically and otherwise fail to load.
APP_STL := c++_shared
