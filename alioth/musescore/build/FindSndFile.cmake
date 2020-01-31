find_path(SNDFILE_INCLUDE_DIR sndfile.h PATHS ${PROJECT_SOURCE_DIR}/dependencies/include;)

if (MINGW)
      set(CMAKE_FIND_LIBRARY_SUFFIXES ".dll")
else (MINGW)
      set(CMAKE_FIND_LIBRARY_SUFFIXES ".lib")
endif (MINGW)
find_library(SNDFILE_LIBRARY NAMES libsndfile-1 PATHS ${DEPENDENCIES_DIR} NO_DEFAULT_PATH)

message(STATUS ${SNDFILE_LIBRARY})

if (SNDFILE_INCLUDE_DIR AND SNDFILE_LIBRARY)
      set(SNDFILE_FOUND TRUE)
endif (SNDFILE_INCLUDE_DIR AND SNDFILE_LIBRARY)

if (SNDFILE_FOUND)
      message (STATUS "Found sndfile: ${SNDFILE_LIBRARY}")
else (SNDFILE_FOUND)
      message (FATAL_ERROR "Could not find: sndfile")
endif (SNDFILE_FOUND)
