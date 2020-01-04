
crengine xcb target which was originally meant for openinkpot. The compile instructions in the readme work but I had to change some stuff.

# Building

Partial dependencies:

```
sudo apt install libxcb-image0-dev libxcb-keysyms1-dev  libfreetype6-dev libglib2.0-dev libcairo2-dev autoconf automake libtool pkg-config ragel gtk-doc-tools
```

Build command:

```
cmake -D DEVICE_NAME=v3 -D MAX_IMAGE_SCALE_MUL=2 -D CMAKE_BUILD_TYPE=Debug -Wdev  -D CMAKE_INSTALL_PREFIX=$(pwd)/out -D GUI=CRGUI_XCB -D DOC_DATA_COMPRESSION_LEVEL=1 -D DOC_BUFFER_SIZE=0x500000 -D HARFBUZZ_INCLUDE_DIRS=$(pwd)/../hb/include -D HARFBUZZ_LIBRARIES=$(pwd)/../hb/lib/libharfbuzz.so ..
```

If you run into trouble with make, try `make VERBOSE=1`.

# Changes


I had to change these lines in `crengine/src/lvfntman.cpp`:

```
#if USE_HARFBUZZ==1
#include <harfbuzz/hb.h>
#include <harfbuzz/hb-ft.h>
```

and in `crengine/src/lvtextfm.cpp`:

```
#if USE_HARFBUZZ==1
#include <harfbuzz/hb.h>
```

and in `cr3gui/CMakeLists.txt`:

```
#    SET (EXTRA_LIBS xcb xcb-aux xcb-atom xcb-image xcb-keysyms xcb-randr fontconfig )
    SET (EXTRA_LIBS xcb xcb-util xcb-keysyms xcb-shm xcb-image xcb-randr fontconfig )
```

and at the bottom:

```
if (${GUI} STREQUAL CRGUI_XCB)
    SET(LOGOCNV_SOURCES src/logoconv.cpp)
    ADD_EXECUTABLE(logocnv ${LOGOCNV_SOURCES})
    TARGET_LINK_LIBRARIES(logocnv crengine qimagescale ${STD_LIBS} ${EXTRA_LIBS})
endif (${GUI} STREQUAL CRGUI_XCB)
```


then in the `crengine/` dir:

```
git clone https://github.com/memononen/nanosvg
```

then change `crengine/CMakeLists.txt`:

```
INCLUDE_DIRECTORIES( 
  ${PNG_INCLUDE_DIR} 
  ${JPEG_INCLUDE_DIR} 
  ${ZLIB_INCLUDE_DIR} 
  ${FREETYPE_INCLUDE_DIRS}
  ${HARFBUZZ_INCLUDE_DIRS}
  ${ANTIWORD_INCLUDE_DIR}
  ${CHM_INCLUDE_DIR}
  ${CMAKE_CURRENT_SOURCE_DIR}/nanosvg/src
  ${CMAKE_CURRENT_SOURCE_DIR}/nanosvg/example
)
```

and:

```
SET(STD_LIBS
  ${JPEG_LIBRARIES} 
  ${FREETYPE_LIBRARIES} 
  ${HARFBUZZ_LIBRARIES}
  ${PNG_LIBRARIES} 
  ${ZLIB_LIBRARIES} 
  ${CHM_LIBRARIES}
  ${ANTIWORD_LIBRARIES}
  qimagescale
)
```

then change all content in crengine/crengine/CMakeLists.txt to:

```
SET (CRENGINE_SOURCES 
src/cp_stats.cpp  
src/lvstring.cpp  
src/props.cpp
src/lstridmap.cpp  
src/rtfimp.cpp
src/cri18n.cpp    
src/lvmemman.cpp        
src/lvstyles.cpp  
src/crtxtenc.cpp  
src/lvtinydom.cpp 
src/lvstream.cpp  
src/lvxml.cpp     
src/lvstsheet.cpp 
src/txtselector.cpp
#src/xutils.cpp
src/crtest.cpp
src/xxhash.c
)

if ( NOT ${GUI} STREQUAL FB2PROPS )
    SET (CRENGINE_SOURCES ${CRENGINE_SOURCES}
    src/fb3fmt.cpp
    src/docxfmt.cpp
    src/lvopc.cpp
    src/lvbmpbuf.cpp
    src/lvfnt.cpp      
    src/hyphman.cpp    
    src/lvfntman.cpp        
    src/crgui.cpp     
    src/lvimg.cpp           
    src/crskin.cpp    
    src/lvdrawbuf.cpp  
    src/lvdocview.cpp  
    src/lvpagesplitter.cpp  
    src/lvtextfm.cpp
    src/lvrend.cpp
    src/wolutil.cpp
    src/hist.cpp      
    src/chmfmt.cpp     
    src/epubfmt.cpp     
    src/pdbfmt.cpp     
    src/wordfmt.cpp     
    src/crconcurrent.cpp
    #src/xutils.cpp
    )
endif (NOT ${GUI} STREQUAL FB2PROPS)

ADD_LIBRARY(crengine STATIC ${CRENGINE_SOURCES})

SET (QIMAGESCALE_SOURCES 
  qimagescale/qimagescale.cpp
  qimagescale/qimagescale_neon.cpp
  qimagescale/qimagescale_sse4.cpp
)

ADD_LIBRARY(qimagescale STATIC ${QIMAGESCALE_SOURCES})
```


Then still in `crengine/` dir:

```
git clone https://github.com/harfbuzz/harfbuzz
cd harfbuzz
git checkout 2.6.4
./autogen.sh
./configure --prefix=$(pwd)/../hb
make
make install
```

in file `cr3gui/src` line 345:

```
    lvRect rc;
    if ( link->getRectEx( rc ) ) {
        CRLog::debug("link docRect { %d, %d, %d, %d }", rc.left, rc.top, rc.right, rc.bottom);
```

and in `crengine/include/lvdocviewprops.h` uncomment:

```
#define PROP_FONT_KERNING_ENABLED    "font.kerning.enabled"
```

