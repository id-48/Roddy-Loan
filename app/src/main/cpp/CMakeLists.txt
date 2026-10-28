cmake_minimum_required(VERSION 3.22.1)
project(emi_secure)

add_library(emi_secure SHARED secure_api.cpp)

find_library(log-lib log)
target_link_libraries(emi_secure ${log-lib})
