import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoggerClientImpl implements LoggerClient {
    Map<String, Process> hm;
    PriorityQueue<Process> pq;
    List<CompletableFuture<String>> futures;
    Lock lock;
    ExecutorService[] taskScheduler;

    public LoggerClientImpl() {
        this.hm = new ConcurrentHashMap<>();
        this.pq = new PriorityQueue<>(Comparator.comparing(Process::getStart));
        this.futures = new CopyOnWriteArrayList<>();
        this.lock = new ReentrantLock();
        this.taskScheduler = new ExecutorService[10];
        for (int i = 0; i < taskScheduler.length; i++) {
            taskScheduler[i] = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void start(String processId, long timeStamp) {
        taskScheduler[processId.hashCode()% taskScheduler.length].submit(() -> {
            System.out.println(Thread.currentThread().getId()+ " working on start");
            try {
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Process process = new Process(processId, timeStamp, null);
            pq.add(process);
            hm.put(processId, process);
            System.out.println(Thread.currentThread().getId()+ " completed on start");
        });
    }

    @Override
    public void end(String processId, long timestamp) {
        taskScheduler[processId.hashCode()%taskScheduler.length].submit(() ->{
            System.out.println(Thread.currentThread().getId()+ " working on end");
            lock.lock();
            try {
                hm.get(processId).setEnd(timestamp);
                if (!futures.isEmpty() && pq.peek().getId().equals(processId)) {
                    final var result = futures.remove(0);
                    String s = pollNow();
                    result.complete(s);
                }
            } finally {
                lock.unlock();
            }
            System.out.println(Thread.currentThread().getId()+ " completed on end");
        });
    }

    @Override
    public String poll() {
        lock.lock();
        try {
            if (!pq.isEmpty() && pq.peek().getEnd() != null) {
                return pollNow();
            }
            else {
                // wait
                CompletableFuture<String> result = new CompletableFuture<>();
                futures.add(result);

                try {
                    return result.get(3, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException |TimeoutException e) {
                    e.printStackTrace();
//                    throw new RuntimeException(e);
                }
                return null;
            }
        } finally {
            lock.unlock();
        }

    }

    private String pollNow() {

            // {3} process started at {7} and ended at {15}
            Process process = pq.poll();
            String print = process.getId() + " process started at "+ process.getStart()+" and ended at "
                    +process.getEnd();
            System.out.println(print);
            hm.remove(process.getId());
            return print;
    }
}
