import java.util.LinkedList;
import java.util.Queue;

public class ThreadsPool {

    private final int nThreads;
    private final WorkerThread[] threads;
    private final Queue<Runnable> tasks = new LinkedList<>();

    public ThreadsPool(int nThreads) {
        this.nThreads = nThreads;
        this.threads = new WorkerThread[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    public void execute(Runnable task) {
        synchronized (tasks) {
            tasks.offer(task);
            tasks.notify();
        }
    }

    private class WorkerThread extends Thread {
        public void run() {
            Runnable task;

            while (true) {
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            System.out.println("Worker Thread Interrupted");
                        }
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

    public static void main(String[] args) {
        ThreadsPool threadPool = new ThreadsPool(5);  // 5 threads

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                System.out.println("Task " + finalI + " executed by " + Thread.currentThread().getName());
            });
        }
    }
}
