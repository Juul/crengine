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

