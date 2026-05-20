cmake_minimum_required(VERSION 3.22.1)
project(resono_dsp)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Compiler optimizations
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math -funroll-loops")

file(GLOB_RECURSE DSP_SOURCES
        dsp_engine.cpp
        dsp_bridge.cpp
        resono_dsp/**/*.cpp
        resono_dsp/**/*.c)
# -------------------------
# Android libraries
# -------------------------
find_library(log-lib log)
find_library(android-lib android)

# -------------------------
# Include directories
# -------------------------
include_directories(
        ${CMAKE_SOURCE_DIR}
        ${CMAKE_SOURCE_DIR}/resono_dsp
        ${CMAKE_SOURCE_DIR}/resono_dsp/filters
)


# -------------------------
# Build shared library
# -------------------------
add_library(resono_dsp SHARED
        dsp_engine.cpp
        dsp_bridge.cpp

        # Filters
        resono_dsp/filters/Biquad.cpp
)

target_link_libraries(
        resono_dsp
        ${log-lib}
        ${android-lib}
)
