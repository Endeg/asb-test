package some.project;

import com.microsoft.azure.servicebus.ISessionHandler;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SessionHandlerOptions;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TestAsb {

    private final Config config = Config.fromProperties("config.properties");

    public static void main(String[] args) {
        new TestAsb().run();
    }

    private static QueueClient buildQueueClient(
            ConnectionStringBuilder connectionStringBuilder,
            int prefetchCount) {
        try {
            val queueClient = new QueueClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
            queueClient.setPrefetchCount(prefetchCount);
            return queueClient;
        } catch (InterruptedException e) {
            log.warn("Thread is interrupted during Queue client building", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            throw new RuntimeException("Queue client cannot be initialized", e);
        }
    }

    private static void registerSessionHandler(
            SessionHandlerOptions handlerOptions,
            QueueClient queueClient,
            ISessionHandler sessionHandler,
            ExecutorService executor) {
        try {
            queueClient.registerSessionHandler(sessionHandler, handlerOptions, executor);
        } catch (InterruptedException e) {
            log.warn("Thread is interrupted during session handler registration");
            Thread.currentThread().interrupt();
        } catch (ServiceBusException e) {
            throw new RuntimeException("Session handler cannot be registered", e);
        }
    }

    private void run() {
        createListenClients();
        forever();
    }

    private void forever() {
        while (true) {
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<QueueClient> createListenClients() {
        val handlerOptions = new SessionHandlerOptions(
                this.config.getListenMaxConcurrentCalls(),
                true,
                Duration.ofMinutes(5)
        );
        return IntStream.range(0, this.config.getListenFactoriesCount())
                .mapToObj(i -> {
                    val queueClient = buildImportListenClient();
                    registerSessionHandler(handlerOptions, queueClient,
                            new DataSubjectVersionsBulkImportHandler(), ForkJoinPool.commonPool()
                    );
                    return queueClient;
                }).collect(Collectors.toList());
    }

    private QueueClient buildImportListenClient() {
        val connectionStringBuilder = new ConnectionStringBuilder(
                this.config.getListenConnection(),
                this.config.getListenEntityPath()
        );
        return buildQueueClient(connectionStringBuilder, this.config.getListenPrefetchCount());
    }
}
