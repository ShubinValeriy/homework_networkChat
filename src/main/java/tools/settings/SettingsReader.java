package tools.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class SettingsReader {
    private int port;
    private String address;

    public SettingsReader() {
        String filePath;
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("Укажите директорию фала с настройками:");
            filePath = scanner.nextLine();
            File file = new File(filePath);
            if (file.exists()){
                break;
            }
            System.out.println("Файл не найден!");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String s;
            while ((s = br.readLine()) != null){
                if (s.contains("PORT=")){
                     port = Integer.parseInt(s.substring(5));
                }
                if (s.contains("ADDRESS=")){
                    address = s.substring(8);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public int getPort(){
        return port;
    }

    public FullAdress getFullAddress(){
        return new FullAdress(port,address);
    }
}
