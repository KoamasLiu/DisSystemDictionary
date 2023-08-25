import java.util.LinkedList;
import java.util.Queue;

public class ThreadsPool {

    private final int nThreads;
    private final WorkerThread[] threads;
    private final Queue<Runnable> tasks = new LinkedList<>();
    private final int maxQueueSize;
    private final RejectedExecutionHandler rejectHandler;
    private volatile boolean isShutdown = false;

    public ThreadsPool(int nThreads, int maxQueueSize, RejectedExecutionHandler handler) {
        this.nThreads = nThreads;
        this.maxQueueSize = maxQueueSize;
        this.rejectHandler = handler;
        this.threads = new WorkerThread[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    public void execute(Runnable task) {
        synchronized (tasks) {
            if (isShutdown) {
                throw new IllegalStateException("ThreadPool is shutdown and cannot accept new tasks.");
            }
            if (tasks.size() >= maxQueueSize) {
                rejectHandler.rejectedExecution(task, this);
                return;
            }
            tasks.offer(task);
            tasks.notify();
        }
    }

    private class WorkerThread extends Thread {
        public void run() {
            Runnable task;

            while (true) {
                synchronized (tasks) {
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

                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.out.println("Runtime exception in worker thread: " + e.getMessage());
                }
            }
        }
    }

    public interface RejectedExecutionHandler {
        void rejectedExecution(Runnable r, ThreadsPool executor);
    }

    public void shutdown() {
        synchronized (tasks) {
            isShutdown = true;
            tasks.notifyAll();  // notify all waiting threads so they can check shutdown state
        }
    }

}
