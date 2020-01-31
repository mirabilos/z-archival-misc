TARGET = DSMIDIWiFi

SOURCES += dswifimidi.cpp
SOURCES += midi2udpthread.cpp
SOURCES += udp2midithread.cpp
SOURCES += wifimidiwindow.cpp

RESOURCES += images.qrc

CONFIG += link_pkgconfig
PKGCONFIG += alsa
QT += network widgets

HEADERS += midi2udpthread.h
HEADERS += settings.h
HEADERS += udp2midithread.h
HEADERS += wifimidiwindow.h

isEmpty(PREFIX) {
  PREFIX = /usr
}

target.path = $$PREFIX/bin
INSTALLS += target
