cmake_minimum_required(VERSION 3.22.1)

project("hikari-image")

find_library(jnigraphics-lib jnigraphics)
find_library(log-lib log)
find_library(android-lib android)

add_library(hikari-image SHARED hikari-image-pipeline.cpp)

target_link_libraries(hikari-image
        ${jnigraphics-lib}
        ${log-lib}
        ${android-lib})
