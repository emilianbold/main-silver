#ifndef BLOCKING_QUEUE_H
#define	BLOCKING_QUEUE_H

#include "queue.h"
#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct blocking_queue {
    queue q;
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    bool shut_down;
    int size;
    int max_size;
} blocking_queue;

/** Initializes list. A list must be initialized before use */
void blocking_queue_init(blocking_queue *q);

/** gets the amunt of elements in the list */
int  blocking_queue_size(blocking_queue *q);

/** adds element to the list tail */
void blocking_queue_add(blocking_queue *q, void* data);

/** removes and returns element from the list's head */
void* blocking_queue_poll(blocking_queue *q);

/** shuts the queue down - after that blocking_queue_poll returns null on empty queue without waiting */
void blocking_queue_shutdown(blocking_queue *q);

int blocking_queue_max_size(blocking_queue *q);

#ifdef	__cplusplus
}
#endif

#endif	/* BLOCKING_QUEUE_H */
