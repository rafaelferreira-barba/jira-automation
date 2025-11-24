package org.example;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class SwingUI {

    private static JTable table;
    private static DefaultTableModel model;
    private static String selectedCsvPath = null;
    private static JComboBox<String> commandBox;

    public static void launch() {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Jira Automation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());

            model = new DefaultTableModel();
            table = new JTable(model);
            frame.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JButton btnChooseCsv = new JButton("Escolher CSV");
            JButton btnRun = new JButton("Executar");
            commandBox = new JComboBox<>(new String[]{"Sub-Task", "UserStory"});

            bottom.add(new JLabel("Issue Type:"));
            bottom.add(commandBox);
            bottom.add(btnChooseCsv);
            bottom.add(btnRun);

            frame.add(bottom, BorderLayout.SOUTH);

            btnChooseCsv.addActionListener(evt -> chooseCsvFile(frame));

            btnRun.addActionListener(evt -> runAutomation());

            frame.setVisible(true);
        });
    }

    private static void chooseCsvFile(JFrame frame) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedCsvPath = chooser.getSelectedFile().getAbsolutePath();
            loadCsvToTable(selectedCsvPath);
        }
    }

    private static void loadCsvToTable(String path) {
        try {
            char customDelimiter = ';';
            CSVReader reader = new CSVReaderBuilder(new FileReader(path))
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(customDelimiter)
                            .build())
                    .build();

            List<String[]> rows = reader.readAll();
            String[] header = rows.get(0);

            model.setColumnIdentifiers(header);
            model.setRowCount(0);

            rows.remove(0);
            for (String[] r : rows) {
                model.addRow(r);
            }

            JOptionPane.showMessageDialog(null, "CSV carregado com sucesso!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar CSV: " + e.getMessage());
        }
    }

    private static void runAutomation() {
        if (selectedCsvPath == null) {
            JOptionPane.showMessageDialog(null, "Selecione um CSV antes!");
            return;
        }

        try {
            Properties props = loadProperties("jira.properties");

            String email = props.getProperty("jira.email");
            String token = props.getProperty("jira.token");
            String domain = props.getProperty("jira.domain");
            String projectKey = props.getProperty("jira.project");

            JiraClient client = new JiraClient(domain, email, token, projectKey);

            char customDelimiter = ';';
            CSVReader reader = new CSVReaderBuilder(new FileReader(selectedCsvPath))
                    .withCSVParser(new CSVParserBuilder().withSeparator(customDelimiter).build())
                    .build();

            List<String[]> rows = reader.readAll();
            rows.remove(0);

            String cmd = (String) commandBox.getSelectedItem();

            int count = 0;
            for (String[] row : rows) {

                String summary = row[0];
                String description = row[1];
                String component = row[2];

                if ("UserStory".equalsIgnoreCase(cmd)) {
                    client.createStory(summary, description, component);
                } else {
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
                count++;
            }

            JOptionPane.showMessageDialog(null, count + " tickets criados com sucesso!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao executar automação: " + e.getMessage());
        }
    }

    private static Properties loadProperties(String path) throws Exception {
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(Path.of(path))) {
            props.load(reader);
        }
        return props;
    }
}
