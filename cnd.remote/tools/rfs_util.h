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
#include <unistd.h>
#include <limits.h>

enum {
    true = 1,
    false = 0
};

#if TRACE

#if RFS_PRELOAD
static const char* prefix = "%u #RFS_PRLD[%d]: ";
#elif RFS_CONTROLLER
static const char* prefix = "%u #RFS_CNTL[%d]: ";
#else
#error either RFS_PRELOAD or RFS_CONTROLLER should be defined and nonzero
#endif

static unsigned long get_timestamp() {
    struct timespec tp;
    clock_gettime(CLOCK_REALTIME, &tp);
    return tp.tv_sec*1000000000+tp.tv_nsec;
}

FILE *trace_file;
#define trace(args...) { fprintf(trace_file, prefix, get_timestamp(), getpid()); fprintf(trace_file, ## args); fflush(trace_file); }
void trace_startup(const char* env_var) {
    char *file_name = getenv(env_var);
    if (file_name) {        
        trace_file = fopen(file_name, "a");
        if (trace_file) {
            fprintf(stderr, "Redirecting trace to %s\n", file_name);
            fprintf(trace_file, "\n\n--------------------\n");
            fflush(trace_file);
        } else {
            fprintf(stderr, "Redirecting trace to %s failed.\n", file_name);
            trace_file = stderr;
        }
    } else {
        trace_file = stderr;
    }
    char dir[PATH_MAX];
    getcwd(dir, sizeof dir);
    trace("started in %s\n", dir);
}
void trace_shutdown() {
    if (trace_file && trace_file != stderr) {
        fclose(trace_file);
    }
}
#else
#define trace_startup(...)
#define trace(...) 
#define trace_shutdown()
#endif
