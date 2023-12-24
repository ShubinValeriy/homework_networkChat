package tools.logger;

import tools.message.Message;
import tools.message.TypeOfMessage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {
    private final String fileName;
    private final String filePath;
    private final File logFile;

    public Logger(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        logFile = new File(String.format(filePath + "/" + fileName +".txt"));
        if (!logFile.exists()){
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void logMessage(Message message){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile,true))) {
            synchronized (logFile) {
                switch (message.getTypeOfMessage()) {
                    case TEXT_MESSAGE -> bw.write(logFormatText(message.getTextMessage(), message.getTypeOfMessage()));
                    case USER_ADDED -> bw.write(
                            logFormatText(String.format("Пользователь %s присоединился к чату.\n",
                                    message.getTextMessage()), TypeOfMessage.USER_ADDED)
                    );
                    case REMOVED_USER -> bw.write(
                            logFormatText(String.format("Пользователь %s покинул чат.\n",
                                    message.getTextMessage()), TypeOfMessage.REMOVED_USER)
                    );
                    case SERVICE_MESSAGE -> bw.write(logFormatText(message.getTextMessage(), TypeOfMessage.SERVICE_MESSAGE));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String logFormatText(String text, TypeOfMessage typeOfMessage){
        LocalDateTime localDateTime = LocalDateTime.now();
        String typeLog;
        if (typeOfMessage == TypeOfMessage.TEXT_MESSAGE){
            typeLog = "USER_MESSAGE";
        } else {
            typeLog = "SERVICE_MESSAGE";
        }
        return String.format(localDateTime + " - [" + typeLog + "] - " + text);
    }




}
