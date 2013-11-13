
#ifndef UTIL_H
#define	UTIL_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#include <pthread.h>
#include <stdio.h>
#include <dirent.h>
#include <sys/stat.h>
#include <fcntl.h>

#ifdef	__cplusplus
extern "C" {
#endif

typedef enum TraceLevel {
    TRACE_NONE = 0,
    TRACE_INFO = 1,
    TRACE_FINE = 2,
    TRACE_FINER = 3,
    TRACE_FINEST = 4
} TraceLevel;

void set_trace(TraceLevel new_level);
bool is_traceable(TraceLevel level);
void trace(TraceLevel level, const char *format, ...);

void log_print(const char *format, ...);
void log_open(const char* path);
void log_close();

void report_error(const char *format, ...);
void soft_assert(int condition, char* format, ...);

void mutex_unlock(pthread_mutex_t *mutex);
void mutex_lock(pthread_mutex_t *mutex);

const char* get_home_dir();
bool file_exists(const char* path);
bool dir_exists(const char* path);

int fclose_if_not_null(FILE* f);
int closedir_if_not_null(DIR *d);

void stopwatch_start();
void stopwatch_stop(TraceLevel level, const char* message);

char *replace_first(char *s, char c, char replacement);

/** opens a file in write-only exclusive mode with O_CREAT flag and mode 600 */
FILE* fopen600(const char* path);

int escape_strlen(const char* s);

char *escape_strcpy(char *dst, const char *src);

int unescape_strlen(const char* s);

char *unescape_strcpy(char *dst, const char *src);

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */

