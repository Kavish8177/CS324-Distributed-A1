package fj.usp.cs324.distrilab.client;

import fj.usp.cs324.distrilab.bootstrap.BootstrapServer;
import fj.usp.cs324.distrilab.common.*;
import fj.usp.cs324.distrilab.remote.BootstrapService;
import fj.usp.cs324.distrilab.remote.WorkerService;
import fj.usp.cs324.distrilab.worker.RmiLookup;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClientGui extends JFrame {
    private final BootstrapService bootstrap;
    private final JComboBox<JobType> typeBox = new JComboBox<>(JobType.values());
    private final JTextArea inputArea = new JTextArea(8, 45);
    private final JTextField startField = new JTextField("1", 10);
    private final JTextField endField = new JTextField("1000", 10);
    private final JTextArea outputArea = new JTextArea(12, 45);
    private final JButton submitButton = new JButton("Submit job");

    public ClientGui(String bootstrapHost, int bootstrapPort) throws Exception {
        super("DistriLab Client");
        bootstrap = (BootstrapService) LocateRegistry.getRegistry(bootstrapHost, bootstrapPort)
                .lookup(BootstrapServer.BINDING);
        createUi();
    }

    private void createUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Job type:"));
        top.add(typeBox);
        top.add(new JLabel("Range start:"));
        top.add(startField);
        top.add(new JLabel("Range end:"));
        top.add(endField);

        inputArea.setLineWrap(true);
        inputArea.setToolTipText("For MAX or PRIMECOUNT, enter numbers separated by commas or spaces");
        outputArea.setEditable(false);
        JButton csvButton = new JButton("Load CSV");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(csvButton);
        buttons.add(submitButton);
        JButton clearButton = new JButton("Clear output");
        buttons.add(clearButton);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.add(new JLabel("Manual input / loaded CSV values:"));
        centre.add(new JScrollPane(inputArea));
        centre.add(buttons);
        centre.add(new JLabel("Results (jobs may run concurrently):"));
        centre.add(new JScrollPane(outputArea));

        add(top, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(new JLabel("MAX and PRIMECOUNT use the values box; PRIMESUM uses the range fields."),
                BorderLayout.SOUTH);

        typeBox.addActionListener(e -> updateInputState());
        csvButton.addActionListener(e -> loadCsv());
        submitButton.addActionListener(e -> submitJob());
        clearButton.addActionListener(e -> outputArea.setText(""));
        updateInputState();
        pack();
        setLocationRelativeTo(null);
    }

    private void updateInputState() {
        boolean rangeJob = typeBox.getSelectedItem() == JobType.PRIMESUM;
        startField.setEnabled(rangeJob);
        endField.setEnabled(rangeJob);
        inputArea.setEnabled(!rangeJob);
    }

    private void loadCsv() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            inputArea.setText(Files.readString(chooser.getSelectedFile().toPath()));
        } catch (IOException e) {
            showError("Could not read CSV: " + e.getMessage());
        }
    }

    private void submitJob() {
        try {
            JobRequest request = createRequest();
            append("Submitting " + request.type() + " job " + shortId(request) + "...\n");
            new SwingWorker<JobResult, Void>() {
                @Override
                protected JobResult doInBackground() throws Exception {
                    WorkerService coordinator = findCoordinator();
                    return coordinator.submitJob(request);
                }

                @Override
                protected void done() {
                    try {
                        JobResult result = get();
                        append("Completed " + shortId(request) + ": " + result.value()
                                + " (" + result.explanation() + ")\n");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        append("FAILED " + shortId(request) + ": "
                                + rootMessage(e.getCause()) + "\n");
                    }
                }
            }.execute();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private JobRequest createRequest() {
        JobType type = (JobType) typeBox.getSelectedItem();
        if (type == JobType.PRIMESUM) {
            long start = Long.parseLong(startField.getText().trim());
            long end = Long.parseLong(endField.getText().trim());
            return JobRequest.primeSum(start, end);
        }
        List<Long> numbers = parseNumbers(inputArea.getText());
        return type == JobType.MAX ? JobRequest.max(numbers) : JobRequest.primeCount(numbers);
    }

    private static List<Long> parseNumbers(String text) {
        String cleaned = text.trim();
        if (cleaned.isEmpty()) throw new IllegalArgumentException("Enter or load some numbers first");
        String[] tokens = cleaned.split("[,;\\s]+");
        List<Long> numbers = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isBlank()) numbers.add(Long.parseLong(token.trim()));
        }
        return numbers;
    }

    private WorkerService findCoordinator() throws Exception {
        List<WorkerInfo> workers = bootstrap.getActiveWorkers();
        if (workers.isEmpty()) throw new IllegalStateException("No workers are active");

        int coordinatorId = -1;
        for (WorkerInfo worker : workers) {
            try {
                int reported = RmiLookup.worker(worker).getCoordinatorId();
                if (reported >= 0) {
                    coordinatorId = reported;
                    break;
                }
            } catch (Exception ignored) { }
        }

        if (coordinatorId < 0) {
            RmiLookup.worker(workers.get(0)).startElection();
            coordinatorId = RmiLookup.worker(workers.get(0)).getCoordinatorId();
        }
        final int selectedId = coordinatorId;
        WorkerInfo coordinator = workers.stream().filter(w -> w.id() == selectedId).findFirst()
                .orElseThrow(() -> new IllegalStateException("Coordinator " + selectedId
                        + " is not in the active worker list"));
        return RmiLookup.worker(coordinator);
    }

    private void append(String text) {
        outputArea.append(text);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "DistriLab error", JOptionPane.ERROR_MESSAGE);
    }

    private static String shortId(JobRequest request) {
        return request.jobId().toString().substring(0, 8);
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.toString() : current.getMessage();
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
        SwingUtilities.invokeLater(() -> {
            try {
                new ClientGui(host, port).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Could not connect to Bootstrap Node: "
                        + e.getMessage(), "Startup error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
