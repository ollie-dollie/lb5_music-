import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Main {

    public static void main(String[] args) {

        String urlString = "https://cdn.uwupad.me/109782.mp3";
        String filePath = "C:\\Users\\user\\Music\\song.mp3";

        Thread downloadThread = new Thread(() -> {

            System.out.println("Начинается скачивание...");
            System.out.println("Путь сохранения: " + filePath);

            try {
                long durationMs = downloadAudioFile(filePath, urlString);

                System.out.printf(
                        "Скачивание успешно завершено за %d мс%n",
                        durationMs
                );

            } catch (IOException ex) {
                System.err.println("Ошибка при скачивании: " + ex.getMessage());
            }
        });

        downloadThread.start();

        try {
            downloadThread.join();
        } catch (InterruptedException ex) {
            System.err.println("Скачивание было прервано");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Скачивает аудиофайл по указанной ссылке
     * и сохраняет его по заданному пути.
     *
     * @param filePath путь сохранения файла
     * @param urlString ссылка на аудиофайл
     * @return время скачивания в миллисекундах
     * @throws IOException если произошла ошибка при скачивании
     */
    public static long downloadAudioFile(String filePath, String urlString)
            throws IOException {

        long startTime = System.currentTimeMillis();

        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();

        // создаем папку, если ее нет
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Не удалось создать папку: " + parentDir);
            }
        }

        // устанавливаем соединение
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);

        // скачиваем файл и автоматически закрываем потоки
        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fout = new FileOutputStream(targetFile)) {

            byte[] data = new byte[8192];
            int count;

            while ((count = in.read(data)) != -1) {
                fout.write(data, 0, count);
            }
        }

        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }
}