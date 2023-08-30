/**
 * Name: Haoyu Liu
 * Student id: 1385415
 */

import java.util.LinkedList;
import java.util.Queue;

public class ThreadsPool {

    private final int nThreads; // Number of worker threads.
    private final WorkerThread[] threads; // Array of worker threads.
    private final Queue<Runnable> tasks = new LinkedList<>(); // Queue to hold the tasks.
    private final int maxQueueSize; // Maximum size for the task queue.
    private final RejectedExecutionHandler rejectHandler; // Handler for rejected tasks.
    private volatile boolean isShutdown = false; // Indicator to check if the pool is shut down.

    /**
     * Constructor for the ThreadPool.
     * @param nThreads
     * @param maxQueueSize
     * @param handler
     */
    public ThreadsPool(int nThreads, int maxQueueSize, RejectedExecutionHandler handler) {
        this.nThreads = nThreads;
        this.maxQueueSize = maxQueueSize;
        this.rejectHandler = handler;
        this.threads = new WorkerThread[nThreads];

        // Initialize and start the worker threads.
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    /**
     * Method to submit a task for execution.
     * @param task
     */
    public void execute(Runnable task) {
        synchronized (tasks) {
            if (isShutdown) {
                throw new IllegalStateException("ThreadPool is shutdown and cannot accept new tasks.");
            }

            // If task queue reaches its maximum size, reject the task.
            if (tasks.size() >= maxQueueSize) {
                rejectHandler.rejectedExecution(task, this);
                return;
            }
            tasks.offer(task);
            tasks.notify(); // Notify the worker threads that a new task is available.
        }
    }

    private class WorkerThread extends Thread {
        public void run() {
            Runnable task;

            while (true) {
                synchronized (tasks) {
                    // Wait until there's a task available or the pool is shut down.
                    while (tasks.isEmpty() && !isShutdown) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            System.out.println("Worker Thread Interrupted");
                        }
                    }

                    // If no tasks and pool is shutdown, exit the loop and terminate the thread
                    if (tasks.isEmpty() && isShutdown) {
                        break;
                    }

                    task = tasks.poll();
                }

                // Execute the task.
                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.out.println("Runtime exception in worker thread: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Interface for handling rejected tasks.
     */
    public interface RejectedExecutionHandler {
        void rejectedExecution(Runnable r, ThreadsPool executor);
    }

    /**
     * Method to shut down the thread pool gracefully.
     */
    public void shutdown() {
        synchronized (tasks) {
            isShutdown = true;
            tasks.notifyAll();  // notify all waiting threads so they can check shutdown state
        }
    }
}
