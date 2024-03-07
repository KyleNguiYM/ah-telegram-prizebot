package tutorial;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class pBot {

    public static void main(String[] args) throws TelegramApiException {

        String proxyHost = "149.102.231.173";
        int proxyPort = 80;

        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setProxyHost(proxyHost);
        botOptions.setProxyPort(proxyPort);
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

        DefaultBotSession defaultBotSession = new DefaultBotSession();
        defaultBotSession.setOptions(botOptions);
        try{
            TelegramBotsApi botsApi = new TelegramBotsApi(defaultBotSession.getClass());
            Bot bot = new Bot();                  //We moved this line out of the register method, to access it later
            botsApi.registerBot(bot);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
        //TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

    }
}
