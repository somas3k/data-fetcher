import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static void main(String[] args) throws InterruptedException {
        int counter = 0;

        while (true) {
            getData();
            Thread.sleep(1000*60*3);
            if (++counter % 20 == 0) {
                mergeFiles();
            }
        }
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss", Locale.forLanguageTag("pl"));

    private static void mergeFiles() {
        File folder = new File("./hive");

        List<File> listOfFiles = Arrays.asList(Objects.requireNonNull(folder.listFiles()));

        listOfFiles.sort(Comparator.comparing(File::getName));
        listOfFiles = listOfFiles.subList(0, listOfFiles.size() - 2);

        LocalDateTime startDate =
                LocalDateTime.ofInstant(getInstantFromFileName(listOfFiles.get(0).getAbsoluteFile().getName()), ZoneId.systemDefault());
        LocalDateTime endDate =
                LocalDateTime.ofInstant(getInstantFromFileName(listOfFiles.get(listOfFiles.size() - 1).getAbsoluteFile().getName()), ZoneId.systemDefault());

        String fileName = startDate.format(FORMATTER) + " " + endDate.format(FORMATTER) + ".json";
        try (PrintWriter writer = new PrintWriter("./hive/merged/" + fileName)) {
            writer.write("{");
            int i = 1;
            for (File f : listOfFiles) {
                Scanner sc = new Scanner(f);
                String line = String.format("\"%s\":%s", f.getName(), sc.nextLine());
                if (i++ != listOfFiles.size()) {
                    line = line + ",";
                }
                writer.write(line);
            }
            writer.write("}");
            listOfFiles.forEach(File::delete);
        } catch (IOException ignored) {

        }
    }

    private static Instant getInstantFromFileName(String fileName) {
        return Instant.ofEpochMilli(Long.parseLong(fileName));
    }

    private static void getData() {
        HttpGet request = new HttpGet("https://hive.frontend.fleetbird.eu/api/prod/v1.06/map/cars/?lat1=50.033295&lon1=19.874814&lat2=50.095005&lon2=20.012733");

        long timestamp = System.currentTimeMillis();

        try (CloseableHttpResponse response = httpClient.execute(request);
             BufferedWriter bfToLogFile = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream("./hive/" + timestamp, true)))
        ) {
            JsonReader jsonReader = Json.createReader(response.getEntity().getContent());
            JsonWriter writer = Json.createWriter(bfToLogFile);
            writer.write(jsonReader.read());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
