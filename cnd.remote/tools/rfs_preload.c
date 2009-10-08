/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <memory.h>
#include <errno.h>
#include <unistd.h>
#include <sys/stat.h>
#include  <stdarg.h>
#include  <string.h>
#include  <limits.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <fcntl.h>

#include "rfs_protocol.h"
#include "rfs_preload_socks.h"
#include "rfs_util.h"

/** the name of the directory under control, including trailing "\" */
static const char *my_dir = 0;
static int my_dir_len;

static int __thread inside_open = 0;

#define get_real_addr(name) _get_real_addr(#name, name);

static inline void *_get_real_addr(const char *name, void* wrapper_addr) {
    void *res;
    int saved_errno = errno;
    res = dlsym(RTLD_NEXT, name);
    if (res && res == wrapper_addr) {
        res = dlsym(RTLD_NEXT, name);
    }
    if (!res) {
        res = dlsym(RTLD_DEFAULT, name);
    }
    errno = saved_errno;
    return res;
}

#if TRACE
static void dbg_print_addr(const char* name) {
    void* addr = dlsym(RTLD_NEXT, name);
    trace("\t%s=%X\n", name, addr);
}
#endif

#if TRACE
static inline void print_dlsym() {
    const char* names[] = {
        "lstat", "_lstat", "stat", "_lxstat", "fstat", "lstat64", "fstat64", "_fxstat",
        "open", "fopen", "open64", "pthread_self",
        "readdir", "access", "utime", 0
    };
    int i = 0;
    while (names[i]) {
        dbg_print_addr(names[i]);
        i++;
    }
}
#endif

/**
 * Called upon opening a file; returns "boolean" success
 * @return true means "ok" (either file is in already sync,
 * or it has been just synched, or or the path isn't under control;
 * false means that the file is ourm, but can't be synched
 */
static int on_open(const char *path, int flags) {
    if (inside_open != 1) {
        trace("%s inside_open == %d   returning\n", path, inside_open);
        return true; // recursive call to open
    }
    static int __thread inside = 0;
    if (inside) {
        trace("%s recursive - returning\n", path);
        return true; // recursive!
    }
    if (flags & (O_TRUNC |  O_WRONLY | O_RDWR | O_CREAT)) { // don't need existent content
        trace("%s O_TRUNC |  O_WRONLY | O_RDWR | O_CREAT - returning\n", path);
        return true;
    }
    if (my_dir == 0) { // isn't yet initialized?
        trace("%s not yet initialized - returning\n", path);
        return true;
    }
    inside = 1;

    if (path[0] != '/') {
        static __thread char real_path[PATH_MAX];
        if ( realpath(path, real_path)) {
            path = real_path;
        } else {
            fprintf(stderr, "Can not resolve path %s : %s\n", path, strerror(errno));
            //if (errno == ENOENT) {
            //    char pwd[PATH_MAX];
            //    getcwd(pwd, sizeof pwd);
            //    fprintf(stderr, "\t(current directory: %s)\n", pwd);
            //}
            inside = 0;
            return false;
        }
    }

    if (strncmp(my_dir, path, my_dir_len) != 0) {
        trace("%s is not mine\n", path);
        inside = 0;
        return true;
    }
    int result = false;
    int sd = get_socket(true);
    if (sd == -1) {
        trace("On open %s: sd == -1\n", path);
    } else {
        //struct rfs_request;
        trace_sd("sending request");
        trace("Sending \"%s\" to sd=%d\n", path, sd);
        enum sr_result send_res = pkg_send(sd, pkg_request, path);
        if (send_res == sr_failure) {
            perror("send");
        } else if (send_res == sr_reset) {
            perror("Connection reset by peer when sending request");
        } else { // success
            trace("Request for \"%s\" sent to sd=%d\n", path, sd);
            const int maxsize = 256;
            char buffer[maxsize + sizeof(int)];
            struct package *pkg = (struct package *) &buffer;
            enum sr_result recv_res = pkg_recv(sd, pkg, maxsize);
            if (recv_res == sr_failure) {
                perror("Error receiving response");
            } else if (recv_res == sr_reset) {
                perror("Connection reset by peer when receiving response");
            } else { // success
                if (pkg->kind == pkg_reply) {
                    trace("Got %s for %s, flags=%d, sd=%d\n", pkg->data, path, flags, sd);
                    if (pkg->data[0] == response_ok) {
                        result = true;
                    } else if (pkg->data[0] == response_failure) {
                        result = false;
                    } else {
                        trace("Protocol error, sd=%d\n", sd);
                        result = false;
                    }
                } else {
                    trace("Protocol error: get pkg_kind %d instead of %d\n", pkg->kind, pkg_reply);
                }
            }
        }
    }
    inside = 0;
    return result;
}

static pid_t real_fork(const char* function_name, pid_t (*wrapper_addr)(void)) {
    pid_t result;
    static pid_t (*prev)(void);
        if (!prev) {
            prev = (pid_t (*)(void)) _get_real_addr(function_name, wrapper_addr);
        }
        if (prev) {
            result = prev();
        } else {
            trace("Could not find original \"%s\" function\n", function_name);
            errno = EFAULT;
            result = -1;
        }
    if (result == 0) {
        release_socket(); // child
    } else {
        trace("%s -> %ld\n", function_name, result);
    }
    return result;
}

pid_t fork() {
    real_fork("fork", fork);
}

//pid_t vfork() {
//    real_fork("vfork", vfork);
//}

void
__attribute__((constructor))
on_startup(void) {
    trace_startup("RFS_P", "RFS_PRELOAD_LOG", NULL);
//#if TRACE
//    print_dlsym();
//#endif
    //curr_dir = malloc(curr_dir_len = PATH_MAX);
    //getcwd(curr_dir, curr_dir_len);
    my_dir = getenv("RFS_CONTROLLER_DIR");
    if (!my_dir) {
        //my_dir = curr_dir;
        char* p = malloc(PATH_MAX);
        getcwd(p, PATH_MAX);
        my_dir = p;
    }
    my_dir_len = strlen(my_dir);
    if (my_dir[my_dir_len-1] == '/') {
        my_dir = strdup(my_dir);
    } else {
        my_dir_len++;
        void *p = malloc(my_dir_len + 1);
        strcpy(p, my_dir);
        strcat(p, "/");
        my_dir = p;
    }

    static int startup_count = 0;
    startup_count++;
    trace("RFS startup (%d) my dir: %s\n", startup_count, my_dir);

    release_socket();
    trace_sd("startup");

    const char* env_sleep_var = "RFS_PRELOAD_SLEEP";
    char *env_sleep = getenv(env_sleep_var);
    if (env_sleep) {
        int time = atoi(env_sleep);
        if (time > 0) {
            fprintf(stderr, "%s is set. Process %d, sleeping %d seconds...\n", env_sleep_var, getpid(), time);
            fflush(stderr);
            sleep(time);
            fprintf(stderr, "... awoke.\n");
            fflush(stderr);
        } else {
            fprintf(stderr, "Incorrect value, should be a positive integer: %s=%s\n", env_sleep_var, env_sleep);
            fflush(stderr);
        }
    }
}

void
__attribute__((destructor))
on_shutdown(void) {
    static int shutdown_count = 0;
    shutdown_count++;
    trace("RFS shutdown (%d)\n", shutdown_count);
    trace_shutdown();
    release_socket();
}

typedef struct pthread_routine_data {
    void *(*user_start_routine) (void *);
    void* arg;
} pthread_routine_data;

static void* pthread_routine_wrapper(void* data) {
    pthread_routine_data *prd = (pthread_routine_data*) data;
    trace("Starting user thread routine.\n");
    prd->user_start_routine(prd->arg);
    trace("User thread routine finished. Performing cleanup\n");
    free(data);
    release_socket();
    return 0;
}

int pthread_create(void *newthread,
        void *attr,
        void *(*user_start_routine) (void *),
        void *arg) {
    trace("pthread_create\n");
    static int (*prev)(void *, void*, void * (*)(void *), void*);
    if (!prev) {
        prev = (int (*)(void*, void*, void * (*)(void *), void*)) get_real_addr(pthread_create);
    }
    pthread_routine_data *data = malloc(sizeof (pthread_routine_data));
    // TODO: check for null???
    data->user_start_routine = user_start_routine;
    data->arg = arg;
    prev(newthread, attr, pthread_routine_wrapper, data);
}

#define real_open(function_name, path, flags) \
    inside_open++; \
    trace("%s %s %d\n", #function_name, path, flags); \
    va_list ap; \
    mode_t mode; \
    va_start(ap, flags); \
    mode = va_arg(ap, mode_t); \
    va_end(ap); \
    int result = -1; \
    if (on_open(path, flags)) { \
        static int (*prev)(const char *, int, ...); \
        if (!prev) { \
            prev = (int (*)(const char *, int, ...)) get_real_addr(function_name); \
        } \
        if (prev) {\
            result = prev(path, flags, mode); \
        } else { \
            trace("Could not find original \"%s\" function\n", #function_name); \
            errno = EFAULT; \
            result = -1; \
        } \
    } \
    trace("%s %s -> %d\n", #function_name, path, result); \
    inside_open--; \
    return result;

#define real_fopen(function, path, mode) \
    inside_open++; \
    trace("%s %s %s\n", #function, path, mode); \
    FILE* result = NULL; \
    if (on_open(path, O_RDONLY)) { \
        static FILE* (*prev)(const char *, const char *); \
        if (!prev) { \
            prev = (FILE* (*)(const char *, const char *)) get_real_addr(function); \
        } \
        if (prev) { \
            result = prev(path, mode); \
        } else { \
            trace("Could not find original \"%s\" function\n", #function); \
            errno = EFAULT; \
            result = NULL; \
        } \
    } \
    trace("%s %s -> %d\n", #function, path, result); \
    inside_open--; \
    return result;

int open(const char *path, int flags, ...) {
    real_open(open, path, flags);
}

#if _FILE_OFFSET_BITS != 64
int open64(const char *path, int flags, ...) {
    real_open(open64, path, flags);
}
#endif

int _open(const char *path, int flags, ...) {
    real_open(_open, path, flags);
}

int _open64(const char *path, int flags, ...) {
    real_open(_open64, path, flags);
}

int __open(const char *path, int flags, ...) {
    real_open(__open, path, flags);
}

int __open64(const char *path, int flags, ...) {
    real_open(__open64, path, flags);
}


//#ifdef __linux__
FILE *fopen(const char * filename, const char * mode) {
    real_fopen(fopen, filename, mode);
}

#if _FILE_OFFSET_BITS != 64
FILE *fopen64(const char * filename, const char * mode) {
    real_fopen(fopen64, filename, mode);
}
#endif

// TODO: int openat(int fd, const char *path, int flags, ...);
// TODO: int openat64(int fd, const char *path, int flags, ...);

//#endif
