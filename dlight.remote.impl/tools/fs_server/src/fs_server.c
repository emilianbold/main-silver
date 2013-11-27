
#include "fs_common.h"
#include "fs_server.h"
#include "blocking_queue.h"
#include "exitcodes.h"
#include "dirtab.h"
#include "array.h"

#include <pthread.h>
#include <stddef.h>
#include <dirent.h> 
#include <limits.h> 
#include <stdio.h> 
#include <stdarg.h>
#include <string.h>
#include <ctype.h> 
#include <sys/stat.h>
#include <sys/types.h>
#include <grp.h>
#include <pwd.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <getopt.h>
#include <sys/stat.h> 
#include <fcntl.h>
#include <signal.h>

#define MAX_THREAD_COUNT 32
#define DEFAULT_THREAD_COUNT 4
static int rp_thread_count = DEFAULT_THREAD_COUNT;
static pthread_t rp_threads[MAX_THREAD_COUNT];
static pthread_t refresh_thread;

static blocking_queue req_queue;

static bool clear_persistence = false;
static bool log_flag = false;
static bool persistence = false;
static bool refresh = false;
static bool refresh_explicit = false;
static bool statistics = false;
static int refresh_sleep = 1;
//static bool shutting_down = false;

#define FS_SERVER_MAJOR_VERSION 1
#define FS_SERVER_MID_VERSION 0
#define FS_SERVER_MINOR_VERSION 13

typedef struct fs_entry {
    int /*short?*/ name_len;
    char* name;
    int /*short?*/ link_len;
    char* link;
    unsigned int  uid;
    unsigned int  gid;
    unsigned int mode;
    long size;
    long long mtime;
    char data[];
} fs_entry;

static struct {
    pthread_mutex_t mutex;
    bool proceed;
} state;

static bool state_get_proceed() {    
    bool proceed;
    mutex_lock(&state.mutex);
    proceed = state.proceed;
    mutex_unlock(&state.mutex);    
    return proceed;
}

static void state_set_proceed(bool proceed) {
    mutex_lock(&state.mutex);
    state.proceed = proceed;
    mutex_unlock(&state.mutex);    
}

static void state_init() {
    pthread_mutex_init(&state.mutex, NULL);
    state_set_proceed(true);
}

#define DECLARE_DECODE(type, type_name, maxlen) \
static const char* decode_##type_name (const char* text, type* result) { \
    *result = 0; \
    const char* p = text; \
    if (!isdigit(*p)) { \
        report_error("unexpected numeric value: '%c'\n", *p); \
        return NULL; \
    } \
    while (p - text < maxlen) { \
        char c = *(p++); \
        if (isdigit(c)) { \
            *result *= 10; \
            *result += c - '0'; \
        } else if (c == 0 || isspace(c)) { \
            return p; \
        } else { \
            report_error("unexpected numeric value: '%c'\n", c); \
            return NULL; \
        } \
    } \
    report_error("numeric value too long: '%s'\n", text); \
    return NULL; \
}

DECLARE_DECODE(int, int, 12)
DECLARE_DECODE(unsigned int, uint, 12)
DECLARE_DECODE(long, long, 20)
DECLARE_DECODE(long long, long_long, 20)

static bool is_prohibited(const char* abspath) {
    if (strcmp("/proc", abspath) == 0) {
        return true;
    } else if(strcmp("/dev", abspath) == 0) {
        return true;
    }
    #if linux
    if (strcmp("/run", abspath) == 0) {
        return true;
    }
    #endif
    return false;
}

/** 
 * Decodes in-place fs_raw_request into fs_request
 */
static fs_request* decode_request(char* raw_request, fs_request* request, int request_max_size) {
    request->kind = raw_request[0];
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
    //p++;
    int id;
    int path_len;
    const char* p;
    if (*raw_request == FS_REQ_QUIT) {
        id = 0;
        path_len = 0;
        p = "";
    } else {
        p = raw_request + 2;
        p = decode_int(p, &id);
        if (p == NULL) {
            return NULL;
        }
        //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
        p = decode_int(p, &path_len);
        if (p == NULL) {
            return NULL;
        }
        if (!path_len && *raw_request != FS_REQ_QUIT) {
            report_error("wrong (zero path) request: %s", raw_request);
            return NULL;
        }
        if (path_len > (request_max_size - sizeof (fs_request) - 1)) {
            report_error("wrong (too long path) request: %s", raw_request);
            return NULL;
        }
    }
    //fs_request->kind = request->kind;
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
    strncpy(request->path, p, path_len);
    request->path[path_len] = 0;
    unescape_strcpy(request->path, request->path);
    path_len = strlen(request->path);
    request->id = id;
    request->len = path_len;
    request->size = offsetof(fs_request, path)+path_len+1; //(request->path-&request)+len+1;
    return request;
}

static fs_entry* create_fs_entry(fs_entry *entry2clone) {
    int sz = sizeof(fs_entry) + entry2clone->name_len + entry2clone->link_len + 2;
    fs_entry *entry = malloc(sz);
    entry->name_len = entry2clone->name_len;
    entry->name = entry->data;
    strncpy(entry->name, entry2clone->name, entry->name_len);
    entry->name[entry->name_len] = 0;
    entry->link_len = entry2clone->link_len;
    if (entry->link_len) {
        entry->link = entry->data + entry->name_len + 1;
        strncpy((char*)entry->link, entry2clone->link, entry->link_len);
        entry->link[entry->link_len] = 0;
    } else {
        entry->link = "";
    }
    entry->gid = entry2clone->gid;
    entry->uid = entry2clone->uid;
    entry->mode = entry2clone->mode;
    entry->size = entry2clone->size;
    entry->mtime = entry2clone->mtime;
    return entry;
}


/** 
 * Creates a fs_entry on heap.
 * NB: modifies buf: can unescape and zero-terminate strings
 */
static fs_entry *decode_entry_response(char* buf, int buf_size) {

    // format: name_len name uid gid mode size mtime link_len link
    fs_entry tmp; // a temporary one since we don't know names size

    const char* p = decode_int(buf, &tmp.name_len);
    if (!p) { return NULL; }; // decode_int already printed error message
    
    tmp.name = (char*) p;
    if (p + tmp.name_len >= buf + buf_size) {
        report_error("wrong entry format: too long (%i) name: %s", tmp.name_len, buf);
        return NULL;
    }        
    tmp.name[tmp.name_len] = 0;
    unescape_strcpy(tmp.name, tmp.name);
    p += tmp.name_len + 1;
    tmp.name_len = strlen(tmp.name);

    p = decode_uint(p, &tmp.uid);
    if (!p) { return NULL; }; // decode_int already printed error message
    
    p = decode_uint(p, &tmp.gid);
    if (!p) { return NULL; };
    
    p = decode_uint(p, &tmp.mode);
    if (!p) { return NULL; };
    
    p = decode_long(p, &tmp.size);
    if (!p) { return NULL; };
    
    p = decode_long_long(p, &tmp.mtime);
    if (!p) { return NULL; };
    
    p = decode_int(p, &tmp.link_len);
    if (!p) { return NULL; };

    if (tmp.link_len) {        
        tmp.link = (char*) p;
        if (p + tmp.link_len >= buf + buf_size) {
            report_error("wrong entry format: too long (%i) link name: %s", tmp.link_len, buf);
            return NULL;
        }        
        tmp.link[tmp.link_len] = 0;
        unescape_strcpy(tmp.link, tmp.link);
        tmp.link_len = strlen(tmp.link);
    } else {
        tmp.link = "";
    }
    if (tmp.name_len > MAXNAMLEN) {
        report_error("wrong entry format: too long (%i) file name: %s", tmp.name_len, buf);
        return NULL;
    }
    if (tmp.link_len > PATH_MAX) {
        report_error("wrong entry format: too long (%i) link name: %s", tmp.link_len, buf);
        return NULL;
    }
    return create_fs_entry(&tmp);
}

static bool read_entries_from_cache_impl(array/*<fs_entry>*/ *entries, FILE* cache_fp, 
        const char *cache_path, char *buf, int buf_size, const char* path) {

    if (!fgets(buf, buf_size, cache_fp)) {            
        if (feof(cache_fp)) {
            report_error("error reading cache from %s for %s: preliminary EOF\n", cache_path, path);
        } else {
            report_error("error reading cache from %s for %s: %s\n", cache_path, path, strerror(errno));
        }        
        return false;
    }

    unescape_strcpy(buf, buf);
    if (strncmp(path, buf, strlen(path)) != 0) {
        report_error("error: first line in cache %s for %s is not '%s', but is '%s'", cache_path, path, path, buf);
            return false;
    }

    bool success = true;
    while (fgets(buf, buf_size, cache_fp)) {
        trace(TRACE_FINEST, "\tread entry: %s", buf);
        if (*buf == '\n' || *buf == 0) {
            trace(TRACE_FINEST, "an empty one; continuing...");
            continue;
        }
        fs_entry *entry = decode_entry_response(buf, buf_size);
        if (entry) {
            array_add(entries, entry);
        } else {
            report_error("error reading entry from cache (%s): %s\n", cache_path, buf);
            success = false;
            break;
        }
    }

    if (success && !feof(cache_fp)) { // we got here because fgets returned NULL, which means EOF or error
        report_error("error reading cache from %s for %s: %s\n", cache_path, path, strerror(errno));
        success = false;
    }

    return success;
}
    
static bool read_entries_from_cache(array/*<fs_entry>*/ *entries, dirtab_element* el, const char* path) {
    const char *cache_path = dirtab_get_element_cache_path(el);
    FILE* cache_fp = fopen(cache_path, "r");
    array_init(entries, 100);
    bool success = false;
    if (cache_fp) {
        int buf_size = PATH_MAX + 40;
        char *buf = malloc(buf_size);
        success = read_entries_from_cache_impl(entries, cache_fp, cache_path, buf, buf_size, path);
        free(buf);
        fclose(cache_fp);
    }
    array_truncate(entries);    
    return success;
}

static bool fs_entry_creating_visitor(char* name, struct stat *stat_buf, char* link, const char* abspath, void *data) {
    fs_entry tmp;
    tmp.name_len = strlen(name);
    tmp.name = name;
    tmp.uid = stat_buf->st_uid;
    tmp.gid = stat_buf->st_gid;
    tmp.mode = stat_buf->st_mode;
    tmp.size = stat_buf->st_size;
    tmp.mtime = get_mtime(stat_buf);
    bool is_link = S_ISLNK(stat_buf->st_mode);
    tmp.link_len = is_link ? strlen(link) : 0;
    tmp.link = is_link ? link : "";
    fs_entry* new_entry = create_fs_entry(&tmp);
    if (new_entry) {
        array_add((array*)data, new_entry);
    } else {
        report_error("error creating entry for %s\n", abspath);
    }
    return true;
}

static void read_entries_from_dir(array/*<fs_entry>*/ *entries, const char* path) {    
    array_init(entries, 100);
    visit_dir_entries(path, fs_entry_creating_visitor, entries);
    array_truncate(entries);
}

static bool response_entry_create(buffer response_buf, 
        const char *abspath, const char *basename, 
        buffer work_buf) {
    struct stat stat_buf;
    if (lstat(abspath, &stat_buf) == 0) {
        
        //int escaped_name_size = escape_strlen(entry->d_name);
        escape_strcpy(work_buf.data, basename);
        char *escaped_name = work_buf.data;
        int escaped_name_size = strlen(escaped_name);
        int work_buf_size = work_buf.size - (escaped_name_size + 1);
        
        bool link_flag = S_ISLNK(stat_buf.st_mode);

        int escaped_link_size = 0;
        char* escaped_link = "";
        
        if (link_flag) {
            char* link = work_buf.data + escaped_name_size + 1; 
            ssize_t sz = readlink(abspath, link, work_buf_size);
            if (sz == -1) {
                report_error("error performing readlink for %s: %s\n", abspath, strerror(errno));
                strcpy(work_buf.data, "?");
            } else {
                link[sz] = 0;
                escaped_link_size = escape_strlen(link);
                work_buf_size -= (sz + escaped_link_size + 1);
                if (work_buf_size < 0) {
                    report_error("insufficient space in buffer for %s\n", abspath);
                    return false;
                }
                escaped_link = link + sz + 1;
                escape_strcpy(escaped_link, link);
            }
        }
        snprintf(response_buf.data, response_buf.size, "%i %s %li %li %li %lu %lli %i %s\n",
                escaped_name_size,
                escaped_name,
                (long) stat_buf.st_uid,
                (long) stat_buf.st_gid,
                (long) stat_buf.st_mode,
                (unsigned long) stat_buf.st_size,
                get_mtime(&stat_buf),
                escaped_link_size,
                escaped_link);
        return true;
    } else {
        report_error("error getting stat for '%s': %s\n", abspath, strerror(errno));
        return false;
    }
}

typedef struct {
    const int request_id;
    buffer response_buf;
    buffer work_buf;
    FILE *cache_fp;
} response_ls_data;

static bool response_ls_plain_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p);
static bool response_ls_recursive_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p);

static void response_ls(int request_id, const char* path, bool recursive, bool inner) {

    fprintf(stdout, "%c %d %li %s\n", (recursive ? FS_RSP_RECURSIVE_LS : FS_RSP_LS),
            request_id, (long) strlen(path), path);

    buffer response_buf = buffer_alloc(PATH_MAX * 2); // TODO: accurate size calculation
    buffer work_buf = buffer_alloc((PATH_MAX + MAXNAMLEN) * 2 + 2);
    
    FILE *cache_fp = NULL;
    dirtab_element *el = NULL;
    if (persistence) {
        el = dirtab_get_element(path);
        dirtab_lock(el);
        dirtab_set_watch_state(el, DE_WSTATE_POLL);
        cache_fp = fopen600(dirtab_get_element_cache_path(el));
        if (cache_fp) {
            escape_strcpy(response_buf.data, path);
            fprintf(cache_fp, "%s\n", response_buf.data);
        } else {
            report_error("error opening cache file for %s: %s\n", path, strerror(errno));
            dirtab_unlock(el);
        }
    }
        
    response_ls_data data = { request_id, response_buf, work_buf, cache_fp };
    visit_dir_entries(path, response_ls_plain_visitor, &data);

    fprintf(stdout, "%c %d %li %s\n", FS_RSP_END, request_id, (long) strlen(path), path);
    fflush(stdout);
    
    if (el) {
        if (cache_fp) {
            fclose(cache_fp);
        }
        dirtab_set_state(el, DE_STATE_LS_SENT);
        dirtab_unlock(el);
    }

    if (recursive) {
        visit_dir_entries(path, response_ls_recursive_visitor, &data);
        if (!inner) {
            fprintf(stdout, "%c %d %li %s\n", FS_RSP_END, request_id, (long) strlen(path), path);
            fflush(stdout);
        }
    }

    buffer_free(&response_buf);    
    buffer_free(&work_buf);    
}

static bool response_ls_plain_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p) {
    
    response_ls_data *data = p;
    //trace("\tentry: '%s'\n", entry->d_name);            
    // on NFS entry->d_name may contain '/' or even be absolute!
    // for example, "/ws" directory can contain 
    // "bb-11u1", /ws/bb-11u1/packages" and "bb-11u1/packages" entries!
    // TODO: investigate how to process this properly
    // for now just ignoring such entries
    if (strchr(name, '/')) {
        report_error("skipping entry %s\n", name);
        return true;
    }
    if (response_entry_create(data->response_buf, child_abspath, name, data->work_buf)) {
        fprintf(stdout, "%c %d %s", FS_RSP_ENTRY, data->request_id, data->response_buf.data);
        if (data->cache_fp) {
            fprintf(data->cache_fp, "%s", data->response_buf.data); // trailing '\n' already there, added by form_entry_response
        }
    } else {
        report_error("error formatting response for '%s'\n", child_abspath);
    }
    
    return true;
}

static bool response_ls_recursive_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p) {
    response_ls_data *data = p;
    if (S_ISDIR(stat_buf->st_mode)) {
        response_ls(data->request_id, child_abspath, true, true);
    }
    return true;
}


static void response_stat(int request_id, const char* path) {  
    struct stat stat_buf;    
    if (stat(path, &stat_buf) == 0) {

        int buf_size = MAXNAMLEN * 2 + 80; // *2 because of escaping. TODO: accurate size calculation
        char* escaped_name = malloc(buf_size);
        const char* basename = strrchr(path, '/');
        if (!basename) {
            basename = path;
        }    
        
        escape_strcpy(escaped_name, basename);
        int escaped_name_size = strlen(escaped_name);
        fprintf(stdout, "%c %i %i %s %li %li %li %lu %lli\n",
                FS_REQ_STAT,
                request_id,
                escaped_name_size,
                escaped_name,
                (long) stat_buf.st_uid,
                (long) stat_buf.st_gid,
                (long) stat_buf.st_mode,
                (unsigned long) stat_buf.st_size,
                get_mtime(&stat_buf));
        fflush(stdout);
        free(escaped_name);        
    }  else {
        report_error("error getting stat for '%s': %s\n", path, strerror(errno));
    }
}

static void response_lstat(int request_id, const char* path) {    
    buffer response_buf = buffer_alloc(PATH_MAX * 2); // *2 because of escaping. TODO: accurate size calculation
    buffer work_buf = buffer_alloc((PATH_MAX + MAXNAMLEN) * 2 + 2);
    const char* basename = strrchr(path, '/');
    if (!basename) {
        basename = path;
    }
    if (response_entry_create(response_buf, path, basename, work_buf)) {
        fprintf(stdout, "%c %d %s", FS_RSP_ENTRY, request_id, response_buf.data);
        fflush(stdout);
//        if (cache_fp) {
//            fprintf(cache_fp, "%s",response_buf); // trailing '\n' already there, added by form_entry_response
//        }
    } else {
        report_error("error formatting response for '%s'\n", path);
    }
    buffer_free(&response_buf);
    buffer_free(&work_buf);
}

static void response_add_or_remove_watch(int request_id, const char* path, bool add) {
    dirtab_element *el = dirtab_get_element(path);
    dirtab_lock(el);
    dirtab_set_watch_state(el, add ? DE_WSTATE_POLL : DE_WSTATE_NONE);
    dirtab_set_state(el, DE_STATE_INITIAL);
    dirtab_unlock(el);
}

static void process_request(fs_request* request) {
    switch (request->kind) {
        case FS_REQ_LS:
            response_ls(request->id, request->path, false, false);
            break;
        case FS_REQ_RECURSIVE_LS:
            response_ls(request->id, request->path, true, false);
            break;
        case FS_REQ_STAT:
            response_stat(request->id, request->path);
            break;
        case FS_REQ_LSTAT:
            response_lstat(request->id, request->path);
            break;
        case FS_REQ_ADD_WATCH:
            response_add_or_remove_watch(request->id, request->path, true);
            break;
        case FS_REQ_REMOVE_WATCH:
            response_add_or_remove_watch(request->id, request->path, false);
            break;
        default:
            report_error("unexpected mode: '%c'\n", request->kind);
    }
    //fflush(stdout);
    //fflush(stderr);
}

static int entry_comparator(const void *element1, const void *element2) {
    const fs_entry *e1 = *((fs_entry**) element1);
    const fs_entry *e2 = *((fs_entry**) element2);
    int res = strcmp(e1->name, e2->name);
    return res;
}

static bool refresh_visitor(const char* path, int index, dirtab_element* el) {
    if (is_prohibited(path)) {
        trace(TRACE_FINE, "refresh manager: skipping %s\n", path);
        return true;
    }
    if (dirtab_get_watch_state(el) != DE_WSTATE_POLL) {
        trace(TRACE_FINE, "refresh manager: not polling %s\n", path);
        return true;
    }
    trace(TRACE_FINE, "refresh manager: visiting %s\n", path);
    
    array/*<fs_entry>*/ old_entries;
    array/*<fs_entry>*/ new_entries;
    dirtab_lock(el);
    dirtab_state state = dirtab_get_state(el);
    if (state == DE_STATE_REFRESH_SENT) {
        dirtab_unlock(el);
        trace(TRACE_FINE, "refresh notification already sent for %s\n", path);
        return true;
    }
    bool success = read_entries_from_cache(&old_entries, el, path);
    bool differs;
    if (success) {
        read_entries_from_dir(&new_entries, path);
        array_qsort(&old_entries, entry_comparator);
        array_qsort(&new_entries, entry_comparator);
        differs = false;
    } else {
        array_init(&new_entries, 4);
        report_error("error refreshing %s: error reading cache\n", path);
        differs = true;
    }
    
    if (!differs) {
        differs = array_size(&new_entries) != array_size(&old_entries);
    }
    if (!differs) {
        for (int i = 0; i < new_entries.size; i++) {
            fs_entry *new_entry = array_get(&new_entries, i);
            fs_entry *old_entry = array_get(&old_entries, i);
            if (new_entry->name_len != old_entry->name_len) {
                differs = true;
                trace(TRACE_INFO, "refresh manager: names differ (1) in directory %s: %s vs %s\n", path, new_entry->name, old_entry->name);
                break;
            }
            if (strcmp(new_entry->name, old_entry->name) != 0) {
                differs = true;
                trace(TRACE_INFO, "refresh manager: names differ (2) in directory %s: %s vs %s\n", path, new_entry->name, old_entry->name);
                break;
            }
            // names are same; check types (modes))
            if (new_entry->mode != old_entry->mode) {
                differs = true;
                trace(TRACE_INFO, "refresh manager: modes differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->mode, old_entry->mode);
                break;
            }
            // if links, then check links
            if (S_ISLNK(new_entry->mode)) {
                if (new_entry->link_len != old_entry->link_len) {
                    differs = true;
                    trace(TRACE_INFO, "refresh manager: links differ (1) for %s/%s: %s vs %s\n", path, new_entry->name, new_entry->link, old_entry->link);
                    break;
                }
                if (strcmp(new_entry->link, old_entry->link) != 0) {
                    differs = true;
                    trace(TRACE_INFO, "refresh manager: links differ (2) for %s/%s: %s vs %s\n", path, new_entry->name, new_entry->link, old_entry->link);
                    break;
                }                
            }
            // names, modes and link targets are same
            if (new_entry->uid != old_entry->uid) {
                differs = true;
                trace(TRACE_INFO, "refresh manager: uids differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->uid, old_entry->uid);
                break;
            }
            if (new_entry->gid != old_entry->gid) {
                differs = true;
                trace(TRACE_INFO, "refresh manager: gids differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->gid, old_entry->gid);
                break;
            }
            if (S_ISREG(new_entry->mode)) {
                if (new_entry->size != old_entry->size) {
                    differs = true;
                    trace(TRACE_INFO, "refresh manager: sizes differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->size, old_entry->size);
                    break;
                }
                if (new_entry->mtime != old_entry->mtime) {
                    differs = true;
                    trace(TRACE_INFO, "refresh manager: times differ for %s/%s: %lld vs %lld\n", path, new_entry->name, new_entry->mtime, old_entry->mtime);
                    break;
                }
            }
        }
    }

    if (differs) {
        trace(TRACE_INFO, "refresh manager: sending notification for %s\n", path);
        // trailing '\n' already there, added by form_entry_response
        fprintf(stdout, "%c 0 %li %s\n", FS_RSP_CHANGE, (long) strlen(path), path);
        fflush(stdout);
        dirtab_set_state(el, DE_STATE_REFRESH_SENT);
    }
    dirtab_unlock(el);
    array_free(&old_entries);
    array_free(&new_entries);
    return true;        
}

static void block_thread_signals() {
    sigset_t set;
    sigfillset(&set);
    //sigdelset(&set, SIGPIPE);
    int res = pthread_sigmask(SIG_BLOCK, &set, NULL);
    if (res) {
        report_error("error blocking signals for thread: %s\n", strerror(res));
    }
}

static void *refresh_loop(void *data) {    
    trace(TRACE_INFO, "Refresh manager started; sleep interval is %d\n", refresh_sleep);
    block_thread_signals();
    int pass = 0;
    while (dirtab_is_empty()) { //TODO: replace with notification?
        sleep(refresh_sleep ? refresh_sleep : 2);
    }
    while (state_get_proceed()) {
        pass++;
        trace(TRACE_INFO, "refresh manager, pass %d\n", pass);
        dirtab_flush(); // TODO: find the appropriate place
        stopwatch_start();
        dirtab_visit(refresh_visitor);
        stopwatch_stop(TRACE_INFO, "refresh cycle");
        if (refresh_sleep) {
            sleep(refresh_sleep);
        }
    }
    trace(TRACE_INFO, "refresh manager stopped\n");
    return NULL;
}

static void *rp_loop(void *data) {
    int thread_num = *((int*) data);
    trace(TRACE_INFO, "Thread #%d started\n", thread_num);
    block_thread_signals();
    while (true) {
        fs_request* request = blocking_queue_poll(&req_queue);
        if (request) {
            trace(TRACE_INFO, "thread[%d] request #%d sz=%d kind=%c len=%d path=%s\n", 
                    thread_num, request->id, request->size, request->kind, request->len, request->path);
            process_request(request);
            free(request);
        } else {
            if (!state_get_proceed()) {
                break;
            }
        }
    }
    trace(TRACE_INFO, "Thread #%d done\n", thread_num);
    return NULL;
}

static void lock_or_unlock(bool lock) {
    if (!persistence) {
        return;
    }
    const char* lock_file_name = "lock";
    static int lock_fd = -1;
    if (lock) {
        lock_fd = open(lock_file_name, O_WRONLY | O_CREAT, 0600);
        if (lock_fd < 0) {
            report_error("error opening lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_OPENING_LOCK_FILE);
        }
        if(lockf(lock_fd, F_TLOCK, 0)) {
            report_error("error locking lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_LOCKING_LOCK_FILE);
        }
    } else {
        if (lockf(lock_fd, F_ULOCK, 0)) {
            report_error("error unlocking lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_LOCKING_LOCK_FILE);
        }
        close(lock_fd);        
    }
}

static void exit_function() {
    dirtab_flush();
    lock_or_unlock(false);    
}

static void main_loop() {
    //TODO: handshake with version    
    int buf_size = 256 + PATH_MAX;
    char *raw_req_buffer = malloc(buf_size);
    char *req_buffer = malloc(buf_size);
    while(fgets(raw_req_buffer, buf_size, stdin)) {
        trace(TRACE_INFO, "request: %s", raw_req_buffer); // no LF since buffer ends it anyhow 
        log_print(raw_req_buffer);
        fs_request* request = decode_request(raw_req_buffer, (fs_request*) req_buffer, buf_size);
        if (request) {
            trace(TRACE_FINEST, "decoded request #%d sz=%d kind=%c len=%d path=%s\n", request->id, request->size, request->kind, request->len, request->path);
            if (request->kind == FS_REQ_QUIT) {
                break;
            }
            if (request->kind == FS_REQ_SLEEP) {
                int interval = 0;
                for (int i = 0; i < request->len; i++) {
                    char c = request->path[i];
                    if (isdigit(c)) {
                        interval = (interval*10) + (c - '0');
                    } else {
                        break;
                    }
                }
                if (interval) {
                    fprintf(stderr, "fs_server: sleeping %i seconds\n", interval);
                    sleep(interval);
                    fprintf(stderr, "fs_server: awoke\n");
                }
                continue;
            }
            if (rp_thread_count > 1) {
                fs_request* new_request = malloc(request->size);
                memcpy(new_request, request, request->size);
                blocking_queue_add(&req_queue, new_request);
            } else {
                process_request(request);
            }
       } else {
            report_error("incorrect request: %s", raw_req_buffer);
       }
    }
    free(req_buffer);
    free(raw_req_buffer);
}

static void usage(char* argv[]) {
    char *prog_name = strrchr(argv[0], '/');
    fprintf(stderr, 
            "Usage: %s [-t nthreads] [-v] [-p] [-r]\n"
            "   -t <nthreads> response processing threads count (default is %d)\n"
            "   -p log responses into persisnence\n"
            "   -r <nsec>  set refresh ON and sets refresh interval in seconds\n"
            "   -R <i|e>  refresh mode: i - implicit, e - explicit\n"
            "   -v <verbose-level>: print trace messages\n"
            "   -l log all requests into log file\n"
            "   -s statistics: print some statistics output to stderr\n"
            "   -d persistence directory: where to log responses (valid only if -p is set)\n"
            "   -c cleanup persistence upon startup\n"
            , prog_name ? prog_name : argv[0], DEFAULT_THREAD_COUNT);
}

void process_options(int argc, char* argv[]) {
    int opt;
    int new_thread_count, new_refresh_sleep, new_trace_level;
    TraceLevel default_trace_leve = TRACE_INFO;    
    while ((opt = getopt(argc, argv, "r:pv:t:lsd:cR:")) != -1) {
        switch (opt) {
            case 'R':
                if (optarg) {
                    if (*optarg == 'i') {
                        refresh_explicit = false;
                    } else if (*optarg == 'e') {
                        refresh_explicit = true;
                    } else {
                        report_error("incorrect value of -R flag: %s\n", optarg);
                        usage(argv);
                        exit(WRONG_ARGUMENT);
                    }
                }
                break;
            case 'd':
                if (optarg) {
                    dirtab_set_persistence_dir(optarg);
                }
                break;
            case 'c':
                clear_persistence = true;
                break;
            case 's':
                statistics = true;
                break;
            case 'r':
                refresh  = true;
                new_refresh_sleep = atoi(optarg);
                if (new_refresh_sleep >= 0) {
                    refresh_sleep = new_refresh_sleep;
                }
                break;
            case 'l':
                log_flag = true;
                break;
            case 'p':
                persistence  = true;
                break;
            case 'v':
                new_trace_level = atoi(optarg);
                switch (new_trace_level) {
                    case TRACE_NONE:
                    case TRACE_INFO:
                    case TRACE_FINE:
                    case TRACE_FINER:
                    case TRACE_FINEST:
                        set_trace(new_trace_level);
                        break;
                    default:
                        report_error("incorrect value of -v flag: %d. Defaulting to %d\n", 
                                new_trace_level, default_trace_leve);
                        set_trace(new_trace_level);
                        break;
                }
                break;
            case 't':
                new_thread_count = atoi(optarg);
                if (new_thread_count > 0) {
                    if (new_thread_count > MAX_THREAD_COUNT) {
                        report_error("incorrect value of -t flag: %d. Should not exceed %d.\n", new_thread_count, MAX_THREAD_COUNT);
                        rp_thread_count = MAX_THREAD_COUNT;
                    } else {
                        rp_thread_count = new_thread_count;
                    }
                }
                break;
            default: /* '?' */
                usage(argv);
                exit(WRONG_ARGUMENT);
                break;
        }
    }
    if (refresh && !persistence) {
        report_error("incorrect parameters combination: refresh without persistence does not work\n");
        usage(argv);
        exit(WRONG_ARGUMENT);
    }
}

static bool print_visitor(const char* path, int index, dirtab_element* el) {
    trace(TRACE_INFO, "%d %s\n", index, path);
    return true;
}

static void sigaction_wrapper(int sig, const struct sigaction* new_action, struct sigaction *old_action) {
    int rc = sigaction(sig, new_action, old_action);
    if (rc) {
        report_error("error setting signal handler\n");
        exit(FAILURE_SETTING_SIGNAL_HANDLER);
    }
}

static void shutdown();

static void signal_handler(int signal) {
    trace(TRACE_INFO, "exiting by signal %s (%d)\n", signal_name(signal), signal);
    shutdown();
}

//static void sigpipe_handler(int signal) {
//    log_print("exiting by signal %s (%d)\n", signal_name(signal), signal);
//////    if (!shutting_down) {
//////        shutting_down = false;
//////        shutdown();
//////    }
//}

static void startup() {
    dirtab_init(clear_persistence, refresh_explicit ? DE_WSTATE_NONE : DE_WSTATE_POLL);
    const char* basedir = dirtab_get_basedir();
    if (chdir(basedir)) {
        report_error("cannot change current directory to %s: %s\n", basedir, strerror(errno));
        exit(FAILED_CHDIR);
    }
    if (persistence) {
        trace(TRACE_INFO, "Cache location: %s\n", dirtab_get_basedir());
    } else {
        trace(TRACE_INFO, "peristence is OFF\n");
    }
    lock_or_unlock(true);
    state_init();
    if (is_traceable(TRACE_FINER) && ! dirtab_is_empty()) {
        trace(TRACE_INFO, "loaded dirtab\n");
        dirtab_visit(print_visitor);
    }
    int thread_num[rp_thread_count];
    if (rp_thread_count > 1) {
        blocking_queue_init(&req_queue);
        trace(TRACE_INFO, "Staring %d threads\n", rp_thread_count);        
        for (int i = 0; i < rp_thread_count; i++) {
            trace(TRACE_FINE, "Starting thread #%d...\n", i);
            thread_num[i] = i;
            pthread_create(&rp_threads[i], NULL, &rp_loop, &thread_num[i]);
        }
    } else {
        trace(TRACE_INFO, "Starting in single-thread mode\n");
    }

    if (refresh) {
        pthread_create(&refresh_thread, NULL, &refresh_loop, NULL);
    }
    if (atexit(exit_function)) {
        report_error("error setting exit function: %s\n", strerror(errno));
        exit(FAILURE_SETTING_EXIT_FUNCTION);
    }
    
    struct sigaction new_sigaction;
    new_sigaction.sa_handler = signal_handler;
    new_sigaction.sa_flags = SA_RESTART;
    sigemptyset(&new_sigaction.sa_mask);
    sigaction_wrapper(SIGHUP, &new_sigaction, NULL);
    sigaction_wrapper(SIGQUIT, &new_sigaction, NULL);
    sigaction_wrapper(SIGINT, &new_sigaction, NULL);    
    
//    new_sigaction.sa_handler = sigpipe_handler;
//    new_sigaction.sa_flags = SA_RESTART;
//    sigemptyset(&new_sigaction.sa_mask);
//    sigaction_wrapper(SIGPIPE, &new_sigaction, NULL);    
}

static void shutdown() {   
    state_set_proceed(false);
    blocking_queue_shutdown(&req_queue);
    trace(TRACE_INFO, "Max. requests queue size: %d\n", blocking_queue_max_size(&req_queue));
    if (statistics) {
        fprintf(stderr, "Max. requests queue size: %d\n", blocking_queue_max_size(&req_queue));
    }
    trace(TRACE_INFO, "Shutting down. Joining threads...\n");
    for (int i = 0; i < rp_thread_count; i++) {
        trace(TRACE_INFO, "Shutting down. Joining thread #%i [%ui]\n", i, rp_threads[i]);
        pthread_join(rp_threads[i], NULL);
    }
    
    if (!dirtab_flush()) {
        report_error("error storing dirtab\n");
    }
    dirtab_free();
    log_close();
    exit(0);
}

static void log_header(int argc, char* argv[]) {
    if (log_flag) {
       log_open("log") ;
       log_print("\n--------------------------------------\nfs_server version %d.%d.%d (%s %s) started on ", 
               FS_SERVER_MAJOR_VERSION, FS_SERVER_MID_VERSION, FS_SERVER_MINOR_VERSION, __DATE__, __TIME__);
       time_t t = time(NULL);
       struct tm *tt = localtime(&t);
       if (tt) {
           log_print("%d/%02d/%02d at %02d:%02d:%02d\n", 
                   tt->tm_year+1900, tt->tm_mon + 1, tt->tm_mday, 
                   tt->tm_hour, tt->tm_min, tt->tm_sec);
       } else {
           log_print("<error getting time: %s>\n", strerror(errno));
       }       
       for (int i = 0; i < argc; i++) {
           log_print("%s ", argv[i]);
       }
       log_print("\n");
    }    
}

int main(int argc, char* argv[]) {
    process_options(argc, argv);
    trace(TRACE_INFO, "Version %d.%d.%d (%s %s)\n", FS_SERVER_MAJOR_VERSION, 
            FS_SERVER_MID_VERSION, FS_SERVER_MINOR_VERSION, __DATE__, __TIME__);
    startup();
    log_header(argc, argv);
    main_loop();    
    shutdown();
    return 0;
}
