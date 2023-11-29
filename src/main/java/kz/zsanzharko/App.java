package kz.zsanzharko;


import kz.zsanzharko.utils.DatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class App {
    private static final String PROPERTY_FILE_NAME = "application.properties";
    public static void main(String[] args) throws TelegramApiException, IOException, ClassNotFoundException {
        new App().run();
    }

    public void run() throws TelegramApiException, IOException, ClassNotFoundException {
        Properties properties = initAppProp();
        initDatasource(properties);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        String username = properties.getProperty("bot.username");
        String token = properties.getProperty("bot.token");
        botsApi.registerBot(new FinanceReportBot(token, username));
    }

    private static Properties initAppProp() throws IOException {
        Properties properties = new Properties();
        InputStream fis = App.class.getResourceAsStream("/" + PROPERTY_FILE_NAME);
        log.info("Properties is loading");
        properties.load(fis);
        log.info("Properties loaded");
        return properties;
    }

    private static void initDatasource(Properties properties) throws ClassNotFoundException {
        final String url = properties.getProperty("datasource.url");
        final String username = properties.getProperty("datasource.username");
        final String password = properties.getProperty("datasource.password");
        final String driverName = properties.getProperty("datasource.driver-class-name");
        log.info("First initialize connector");
        DatabaseConnector.getInstance(url, username, password, driverName);
        log.info("Connector is initialized");
    }
}