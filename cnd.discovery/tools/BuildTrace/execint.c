/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/* 
 * File:   execint.c
 * Author: ll155635
 *
 * Created on July 11, 2010, 3:10 PM
 */

#include <dlfcn.h>
#include <limits.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//#include <unistd.h>
#include <errno.h>
#include <sys/stat.h>

#ifdef TRACE
#define LOG(args...) fprintf(stderr, ## args)
#else
#define LOG(...)
#endif

/****************************************************************/
extern char *getcwd (char *__buf, size_t __size);
static struct stat buffer;

static void __logprint(const char* fname, char *const argv[], ...) {

    char* tools = getenv("__CND_TOOLS__");
    if (tools == NULL) {
        LOG("\nBuildTrace ERROR: __CND_TOOLS__ is not set!!!\n");
        return;
    }

    char* log = getenv("__CND_BUILD_LOG__");
    if (log == NULL) {
        LOG("\nBuildTrace ERROR: __CND_BUILD_LOG__ is not set!!!\n");
        return;
    }
    LOG("\nBuildTrace: TOOLS=%s\n\tLOG=%s\n", map, log);

    int shortName = 0;
    const char* key = strrchr(fname, '/');
    if (key == NULL) {
        key = fname;
        shortName = 1;
    } else {
        key++;
    }
    LOG("\nBuildTrace: key = %s\n", key);

    char* filters = strdup(tools);
    int found = 0;
    char* token;
    for(token = strtok(filters, ":"); token; token = strtok(NULL, ":")) {
        if(strcmp(token, key) == 0) {
            found = 1;
            break;
        }
    }

    if (found) {
        if (shortName == 0) {
            int status = stat(fname, &buffer);
            if (status != 0) {
                return;
            }
        }
        
        LOG("\nBuildTrace: found %s\n", *found);
        FILE* flog = fopen(log, "a");
        LOG("\nBuildTrace: opened file %s\n", env_log);

        if (flog == NULL) {
            LOG("\nBuildTrace ERROR: can not open %s!!!\n", env_log);
            return;
        }

        fprintf(flog, "called: %s\n", fname);
        char *buf = malloc(1024);
        getcwd(buf, 1024);
        fprintf(flog, "\t%s\n", buf);
        free(buf);
        char** par = (char**) argv;
        for (; *par != 0; par++) {
            fprintf(flog, "\t%s\n", *par);
        }
        fprintf(flog, "\n");
        fflush(flog);
        fclose(flog);
        LOG("\nBuildTrace: log closed\n");
    }
    return;
}

#define ORIG(func) _orig_##func
#define QUOTE(nm) #nm

// dirty hack
#define PARG , char** arg
#define PENV  , char** arg, const char** env
#define PVAR  , ...
#define ARG , arg
#define ENV , arg, env

#define INSTRUMENT(func, param, actual) \
int func (const char * p_original param) { \
    int prev_errno = errno; \
    char * p = strdup(p_original); \
    static int (* ORIG(func))(const char* p param) = NULL; \
    INIT(func); \
    LOG("BuildTrace: %s called. PATH=%s\n", QUOTE(func), p); \
    __logprint(p actual); \
    errno = prev_errno; \
    int ret = ORIG(func) (p actual); \
    prev_errno = errno; \
    LOG("BuildTrace: %s returned\n", QUOTE(func)); \
    free(p); \
    errno = prev_errno; \
    return ret; \
}

#define INIT(func) \
    if(!ORIG(func)) { \
        ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(ORIG(func) && ORIG(func)==func) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(!ORIG(func)) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)0 /*RTLD_DEFAULT*/, QUOTE(func)); \
    }

#define GETENV

INSTRUMENT(execv, PARG, ARG)
INSTRUMENT(execve, PENV, ENV)
INSTRUMENT(execvp, PARG, ARG)

#define RETURN(f) return f(name, (char **)argv)

#define CONVERT(from_func, to_func) \
int from_func(char *name, const char *first, ...) { \
    va_list args; \
    char* argv[128]; \
    char**  p; \
    char**  env; \
    va_start(args, first); \
    *p++ = (char*) first; \
    p = (char**)argv; \
    do { \
        *p = va_arg(args, char*); \
    } while(*p++); \
    GETENV; \
    va_end(args); \
    LOG("BuildTrace: %s converted to %s\n", QUOTE(from_func), QUOTE(to_func)); \
    RETURN(to_func); \
}

CONVERT(execl, execv)
CONVERT(execlp, execvp)

#undef RETURN
#undef GETENV
#define GETENV env = va_arg(args, char **)
#define RETURN(f) return f(name, (char **)argv, (const char**)env)

CONVERT(execle, execve)

static void
__attribute((constructor))
init_function(void) {
}

static void
__attribute((destructor))
fini_function(void) {
}
