import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Main {

    private static final String MUSIC_URL =
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";

    public static void main(String[] args) {

        // папка загрузки
        Path savePath = Paths.get(
                System.getProperty("user.home"), "Downloads", "soundhelix_song1.mp3");

        System.out.println("Загрузка начата. Файл будет сохранён: " + savePath);

        // скачивание выполняется в отдельном потоке
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            try {
                fetchMp3(MUSIC_URL, savePath);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

        try {
            task.join(); // ждём завершения
            System.out.println("\nГотово! Файл сохранён.");
        } catch (CompletionException e) {
            System.err.println("\nОшибка при скачивании: " + e.getCause().getMessage());
        }
    }

    // скачиваем файл по ссылке и сохраняем по указанному пути.
    public static void fetchMp3(String link, Path target) throws IOException {

        // создаём папку, если её нет
        Files.createDirectories(target.getParent());

        HttpURLConnection conn = (HttpURLConnection) new URL(link).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);

        // проверяем, что сервер ответил успешно
        int code = conn.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IOException("Сервер вернул код " + code);
        }

        long totalSize = conn.getContentLengthLong(); // размер файла в байтах

        try (InputStream in = conn.getInputStream();
             OutputStream out = Files.newOutputStream(target)) {

            byte[] buffer = new byte[16 * 1024];
            long downloaded = 0;
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;

                if (totalSize > 0) {
                    int percent = (int) (downloaded * 100 / totalSize);
                    System.out.print("\rСкачано: " + percent + "%");
                }
            }
        } finally {
            conn.disconnect();
        }
    }
}
