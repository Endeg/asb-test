package some.project;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSession;
import com.microsoft.azure.servicebus.ISessionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class DataSubjectVersionsBulkImportHandler implements ISessionHandler {
    @Override
    public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage message) {
        return CompletableFuture.runAsync(() -> log.info("Processing session/message {}/{}", session.getSessionId(), message.getMessageId()));
    }

    @Override
    public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session) {
        return CompletableFuture.runAsync(() -> log.info("Session {} closed", session.getSessionId()));
    }

    @Override
    public void notifyException(Throwable exception, ExceptionPhase phase) {
        log.error("Something wrong with message handler in phase = {}", phase, exception);
    }
}
