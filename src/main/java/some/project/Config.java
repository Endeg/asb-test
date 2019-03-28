package some.project;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {
    private final String listenConnection;
    private final String listenEntityPath;
    private final int listenFactoriesCount;
    private final int listenPrefetchCount;
    private final int listenMaxConcurrentCalls;

    public static Config fromProperties(String fileName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
             Reader isr = new InputStreamReader(is)
        ) {
            val properties = new Properties();
            properties.load(isr);
            return new Config(
                    properties.getProperty("listenConnection"),
                    properties.getProperty("listenEntityPath"),
                    Integer.valueOf(properties.getProperty("listenFactoriesCount")),
                    Integer.valueOf(properties.getProperty("listenPrefetchCount")),
                    Integer.valueOf(properties.getProperty("listenMaxConcurrentCalls"))
            );
        } catch (IOException e) {
            throw new RuntimeException("Well...", e);
        }
    }
}
