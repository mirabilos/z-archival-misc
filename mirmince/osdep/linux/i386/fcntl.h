#ifndef MIRMINCE_OSDEP_LINUX_I386_FCNTL_H
#define MIRMINCE_OSDEP_LINUX_I386_FCNTL_H

/*#define O_EXEC*/
#define O_RDONLY		0x00000
#define O_RDWR			0x00002
/*#define O_SEARCH*/
#define O_WRONLY		0x00001
#ifdef _ALL_SOURCE
#define O_ACCMODE		0x00003
#endif
#define O_APPEND		0x00400
/*#define O_CLOEXEC*/
#define O_CREAT			0x00040
#define O_DIRECTORY		0x10000
/*#define O_DSYNC*/
#define O_EXCL			0x00080
#define O_NOCTTY		0x00100
#define O_NOFOLLOW		0x20000
#define O_NONBLOCK		0x00800
/*#define O_RSYNC*/
#define O_SYNC			0x01000
#define O_TRUNC			0x00200
/*#define O_TTY_INIT*/
#ifdef _ALL_SOURCE
#define O_DIRECT		0x04000
#define O_LARGEFILE		0x08000
#define O_NOATIME		0x40000
#endif

#endif
