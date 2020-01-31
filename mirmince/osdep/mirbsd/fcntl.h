#ifndef MIRMINCE_OSDEP_MIRBSD_FCNTL_H
#define MIRMINCE_OSDEP_MIRBSD_FCNTL_H

/*#define O_EXEC*/
#define O_RDONLY		0x0000
#define O_RDWR			0x0002
/*#define O_SEARCH*/
#define O_WRONLY		0x0001
#ifdef _ALL_SOURCE
#define O_ACCMODE		0x0003
#endif
#define O_APPEND		0x0008
/*#define O_CLOEXEC*/
#define O_CREAT			0x0200
/*#define O_DIRECTORY*/
#define O_DSYNC			O_SYNC
#define O_EXCL			0x0800
#define O_NOCTTY		0x8000
#define O_NOFOLLOW		0x0100
#define O_NONBLOCK		0x0004
#define O_RSYNC			O_SYNC
#define O_SYNC			0x0080
#define O_TRUNC			0x0400
/*#define O_TTY_INIT*/
#ifdef _ALL_SOURCE
#define O_SHLOCK		0x0010
#define O_EXLOCK		0x0020
#define O_ASYNC			0x0040
#define O_FSYNC			O_SYNC
#endif

#endif
