package org.example;


import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try {
            String command = "";
            String csvPath = "";

            while (true) {
                System.out.println("Digite 'u ou t <caminho_do_csv>' e pressione Enter:");
                String line = scanner.nextLine();
                String[] parts = line.trim().split("\\s+", 2);
                if (parts.length == 2 && ("u".equalsIgnoreCase(parts[0]) || "t".equalsIgnoreCase(parts[0]))) {
                    command = parts[0];
                    csvPath = parts[1];
                    break;
                } else {
                    System.out.println("Comando inválido. Formato esperado: u <arquivo.csv>");
                }
            }
            Properties props = loadProperties("jira.properties");

            String email = props.getProperty("jira.email");
            String token = props.getProperty("jira.token");
            String domain = props.getProperty("jira.domain");
            String projectKey = props.getProperty("jira.project");

            if (email == null || token == null || domain == null || projectKey == null) {
                System.err.println("Erro: jira.properties está incompleto.");
                return;
            }

            JiraClient client = new JiraClient(domain, email, token, projectKey);
            List<String[]> rows = readCsv(csvPath);

            System.out.println("Iniciando criação...");
            for (String[] row : rows) {

                String summary = row[0];
                String description = row[1];
                String component = row[2];


                if ("u".equalsIgnoreCase(command)) {
                    client.createStory(summary, description, component);
                } else if ("t".equalsIgnoreCase(command)) {
                    String parentKey = row[3];
                    String label = row[4];
                    String sp = row[5];
                    client.createSubTask(
                            parentKey,
                            summary,
                            description,
                            component,
                            label,
                            Integer.parseInt(sp)
                    );
                }
            }

            System.out.println("Todas foram criadas com sucesso!");
        } catch (Exception e) {
            System.out.println("erro: " + e.getMessage());
        } finally {
            String input = "";
            while (!"q".equalsIgnoreCase(input)) {
                System.out.println("Digite 'q' para sair...");
                input = scanner.nextLine();
            }
            scanner.close();
        }
    }

    private static Properties loadProperties(String path) throws Exception {
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(Path.of(path))) {
            props.load(reader);
        }
        return props;
    }

    private static List<String[]> readCsv(String path) throws Exception {
        char customDelimiter = ';';
        try (
                var reader = new CSVReaderBuilder(new FileReader(path))
                        .withCSVParser(new CSVParserBuilder()
                                .withSeparator(customDelimiter)
                                .build())
                        .build();

                ) {
            List<String[]> all = reader.readAll();
            all.remove(0);
            return all;
        }
    }
}
