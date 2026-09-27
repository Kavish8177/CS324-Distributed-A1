package fj.usp.cs324.distrilab.client;

import fj.usp.cs324.distrilab.bootstrap.BootstrapServer;
import fj.usp.cs324.distrilab.common.*;
import fj.usp.cs324.distrilab.remote.BootstrapService;
import fj.usp.cs324.distrilab.remote.WorkerService;
import fj.usp.cs324.distrilab.worker.RmiLookup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClientGui extends JFrame {

    // =========================================================
    // COLORS
    // =========================================================
    private static final Color BG =
            new Color(10, 10, 12);

    private static final Color PANEL =
            new Color(24, 24, 27);

    private static final Color FIELD =
            new Color(15, 15, 18);

    private static final Color DISABLED_FIELD =
            new Color(19, 19, 22);

    private static final Color BORDER =
            new Color(55, 55, 62);

    private static final Color TEXT =
            new Color(245, 245, 245);

    private static final Color MUTED =
            new Color(170, 185, 215);

    private static final Color GREEN =
            new Color(83, 230, 164);

    private static final Color BUTTON_LIGHT =
            new Color(225, 225, 230);

    private static final Color BLACK =
            new Color(8, 8, 10);

    // =========================================================
    // FONTS
    // =========================================================
    private static final Font TITLE_FONT =
            new Font("SansSerif", Font.BOLD, 28);

    private static final Font SECTION_FONT =
            new Font("SansSerif", Font.BOLD, 19);

    private static final Font NORMAL_FONT =
            new Font("SansSerif", Font.PLAIN, 15);

    private static final Font BUTTON_FONT =
            new Font("SansSerif", Font.BOLD, 14);

    private static final Font MONO_FONT =
            new Font("Monospaced", Font.PLAIN, 13);

    private static final Font MONO_BOLD =
            new Font("Monospaced", Font.BOLD, 13);

    // =========================================================
    // RMI
    // =========================================================
    private final BootstrapService bootstrap;

    // =========================================================
    // COMPONENTS
    // =========================================================
    private final JComboBox<JobType> typeBox =
            new JComboBox<>(JobType.values());

    private final JTextArea inputArea =
            new JTextArea();

    private final JTextField startField =
            new JTextField("1");

    private final JTextField endField =
            new JTextField("1000");

    private final JTextArea outputArea =
            new JTextArea();

    private final JButton submitButton =
            new JButton("SUBMIT JOB");

    private final JButton csvButton =
            new JButton("LOAD CSV");

    private final JButton clearButton =
            new JButton("CLEAR OUTPUT");

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public ClientGui(
            String bootstrapHost,
            int bootstrapPort
    ) throws Exception {

        super("DistriLab | Distributed Computing Client");

        bootstrap =
                (BootstrapService)
                        LocateRegistry
                                .getRegistry(
                                        bootstrapHost,
                                        bootstrapPort
                                )
                                .lookup(
                                        BootstrapServer.BINDING
                                );

        createUi();
    }

    // =========================================================
    // CREATE UI
    // =========================================================
    private void createUi() {

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        getContentPane().setBackground(BG);

        setLayout(
                new BorderLayout()
        );

        // -----------------------------------------------------
        // MAIN CONTAINER
        // -----------------------------------------------------
        JPanel main =
                new JPanel(
                        new BorderLayout(
                                0,
                                14
                        )
                );

        main.setBackground(BG);

        main.setBorder(
                new EmptyBorder(
                        22,
                        35,
                        22,
                        35
                )
        );

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------
        main.add(
                createHeader(),
                BorderLayout.NORTH
        );

        // -----------------------------------------------------
        // CONTENT
        // -----------------------------------------------------
        JPanel content =
                new JPanel(
                        new BorderLayout(
                                0,
                                14
                        )
                );

        content.setBackground(BG);

        // -----------------------------------------------------
        // TOP TWO PANELS
        // -----------------------------------------------------
        JPanel topBlocks =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                20,
                                0
                        )
                );

        topBlocks.setBackground(BG);

        /*
         * Compact top section.
         * Results will receive the remaining space.
         */
        topBlocks.setPreferredSize(
                new Dimension(
                        1200,
                        315
                )
        );

        topBlocks.setMinimumSize(
                new Dimension(
                        700,
                        275
                )
        );

        topBlocks.add(
                createJobPanel()
        );

        topBlocks.add(
                createInputPanel()
        );

        content.add(
                topBlocks,
                BorderLayout.NORTH
        );

        // -----------------------------------------------------
        // RESULTS FILLS REMAINING SPACE
        // -----------------------------------------------------
        content.add(
                createResultsPanel(),
                BorderLayout.CENTER
        );

        main.add(
                content,
                BorderLayout.CENTER
        );

        add(
                main,
                BorderLayout.CENTER
        );

        // -----------------------------------------------------
        // RESPONSIVE WINDOW
        // -----------------------------------------------------
        setMinimumSize(
                new Dimension(
                        1000,
                        650
                )
        );

        setSize(
                1350,
                800
        );

        setLocationRelativeTo(null);

        // =====================================================
        // ACTIONS
        // =====================================================
        typeBox.addActionListener(
                e -> updateInputState()
        );

        csvButton.addActionListener(
                e -> loadCsv()
        );

        submitButton.addActionListener(
                e -> submitJob()
        );

        clearButton.addActionListener(
                e -> outputArea.setText("")
        );

        updateInputState();
    }

    // =========================================================
    // HEADER
    // =========================================================
    private JPanel createHeader() {

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(BG);

        header.setBorder(
                new EmptyBorder(
                        0,
                        0,
                        8,
                        0
                )
        );

        // -----------------------------------------------------
        // LEFT
        // -----------------------------------------------------
        JPanel left =
                new JPanel();

        left.setLayout(
                new BoxLayout(
                        left,
                        BoxLayout.Y_AXIS
                )
        );

        left.setBackground(BG);

        JLabel title =
                new JLabel(
                        "DISTRILAB"
                );

        title.setForeground(TEXT);
        title.setFont(TITLE_FONT);

        JLabel subtitle =
                new JLabel(
                        "DISTRIBUTED COMPUTING CLIENT"
                );

        subtitle.setForeground(MUTED);
        subtitle.setFont(MONO_FONT);

        left.add(title);

        left.add(
                Box.createVerticalStrut(5)
        );

        left.add(subtitle);

        // -----------------------------------------------------
        // RIGHT
        // -----------------------------------------------------
        JPanel status =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                5
                        )
                );

        status.setBackground(BG);

        JLabel dot =
                new JLabel("●");

        dot.setForeground(GREEN);

        dot.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        17
                )
        );

        JLabel online =
                new JLabel(
                        "SYSTEM ONLINE"
                );

        online.setForeground(GREEN);
        online.setFont(MONO_BOLD);

        status.add(dot);
        status.add(online);

        header.add(
                left,
                BorderLayout.WEST
        );

        header.add(
                status,
                BorderLayout.EAST
        );

        return header;
    }

    // =========================================================
    // JOB CONFIGURATION
    // =========================================================
    private JPanel createJobPanel() {

        JPanel panel =
                createBlock();

        panel.setLayout(
                new BorderLayout()
        );

        JPanel inner =
                new JPanel();

        inner.setBackground(PANEL);

        inner.setLayout(
                new BoxLayout(
                        inner,
                        BoxLayout.Y_AXIS
                )
        );

        inner.setBorder(
                new EmptyBorder(
                        16,
                        22,
                        14,
                        22
                )
        );

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------
        JLabel heading =
                createSectionLabel(
                        "JOB CONFIGURATION"
                );

        heading.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(heading);

        inner.add(
                Box.createVerticalStrut(12)
        );

        // -----------------------------------------------------
        // JOB TYPE
        // -----------------------------------------------------
        JLabel typeLabel =
                createSmallLabel(
                        "JOB TYPE"
                );

        typeLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(typeLabel);

        inner.add(
                Box.createVerticalStrut(5)
        );

        styleComboBox(typeBox);

        typeBox.setPreferredSize(
                new Dimension(
                        500,
                        40
                )
        );

        typeBox.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40
                )
        );

        typeBox.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(typeBox);

        inner.add(
                Box.createVerticalStrut(10)
        );

        // -----------------------------------------------------
        // RANGE START
        // -----------------------------------------------------
        JLabel startLabel =
                createSmallLabel(
                        "RANGE START"
                );

        startLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(startLabel);

        inner.add(
                Box.createVerticalStrut(5)
        );

        styleTextField(startField);

        startField.setPreferredSize(
                new Dimension(
                        500,
                        40
                )
        );

        startField.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40
                )
        );

        startField.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(startField);

        inner.add(
                Box.createVerticalStrut(10)
        );

        // -----------------------------------------------------
        // RANGE END
        // -----------------------------------------------------
        JLabel endLabel =
                createSmallLabel(
                        "RANGE END"
                );

        endLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(endLabel);

        inner.add(
                Box.createVerticalStrut(5)
        );

        styleTextField(endField);

        endField.setPreferredSize(
                new Dimension(
                        500,
                        40
                )
        );

        endField.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40
                )
        );

        endField.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(endField);

        inner.add(
                Box.createVerticalStrut(10)
        );

        // -----------------------------------------------------
        // HELP TEXT
        // -----------------------------------------------------
        JLabel info =
                new JLabel(
                        "<html>"
                                + "PRIMESUM uses range fields.<br>"
                                + "MAX and PRIMECOUNT use input data."
                                + "</html>"
                );

        info.setForeground(MUTED);

        info.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        info.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        inner.add(info);

        panel.add(
                inner,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // INPUT DATA PANEL
    // =========================================================
    private JPanel createInputPanel() {

        JPanel panel =
                createBlock();

        panel.setLayout(
                new BorderLayout(
                        0,
                        10
                )
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                BORDER,
                                1
                        ),
                        new EmptyBorder(
                                16,
                                22,
                                14,
                                22
                        )
                )
        );

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------
        JLabel heading =
                createSectionLabel(
                        "INPUT DATA"
                );

        panel.add(
                heading,
                BorderLayout.NORTH
        );

        // -----------------------------------------------------
        // INPUT AREA
        // -----------------------------------------------------
        inputArea.setBackground(FIELD);
        inputArea.setForeground(TEXT);
        inputArea.setCaretColor(TEXT);

        inputArea.setFont(
                NORMAL_FONT
        );

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        inputArea.setMargin(
                new Insets(
                        12,
                        12,
                        12,
                        12
                )
        );

        inputArea.setToolTipText(
                "For MAX or PRIMECOUNT, enter numbers separated by commas or spaces"
        );

        JScrollPane inputScroll =
                new JScrollPane(
                        inputArea
                );

        inputScroll.setBorder(
                new LineBorder(
                        BORDER,
                        1
                )
        );

        inputScroll
                .getViewport()
                .setBackground(FIELD);

        panel.add(
                inputScroll,
                BorderLayout.CENTER
        );

        // -----------------------------------------------------
        // BUTTONS
        // -----------------------------------------------------
        JPanel buttons =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                14,
                                0
                        )
                );

        buttons.setBackground(PANEL);

        buttons.setPreferredSize(
                new Dimension(
                        500,
                        45
                )
        );

        styleDarkButton(csvButton);
        styleLightButton(submitButton);

        buttons.add(csvButton);
        buttons.add(submitButton);

        panel.add(
                buttons,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // RESULTS PANEL
    // =========================================================
    private JPanel createResultsPanel() {

        JPanel panel =
                createBlock();

        panel.setLayout(
                new BorderLayout(
                        0,
                        10
                )
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                BORDER,
                                1
                        ),
                        new EmptyBorder(
                                14,
                                22,
                                14,
                                22
                        )
                )
        );

        /*
         * No fixed preferred height here.
         *
         * Because this panel is in BorderLayout.CENTER,
         * Swing gives it all remaining vertical space.
         */
        panel.setMinimumSize(
                new Dimension(
                        600,
                        170
                )
        );

        // -----------------------------------------------------
        // RESULTS HEADER
        // -----------------------------------------------------
        JPanel resultHeader =
                new JPanel(
                        new BorderLayout()
                );

        resultHeader.setBackground(PANEL);

        JLabel heading =
                createSectionLabel(
                        "RESULTS"
                );

        JLabel concurrent =
                new JLabel(
                        "CONCURRENT JOB OUTPUT"
                );

        concurrent.setForeground(MUTED);

        concurrent.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        11
                )
        );

        resultHeader.add(
                heading,
                BorderLayout.WEST
        );

        resultHeader.add(
                concurrent,
                BorderLayout.EAST
        );

        panel.add(
                resultHeader,
                BorderLayout.NORTH
        );

        // -----------------------------------------------------
        // OUTPUT AREA
        // -----------------------------------------------------
        outputArea.setEditable(false);

        outputArea.setBackground(FIELD);
        outputArea.setForeground(TEXT);
        outputArea.setCaretColor(TEXT);

        outputArea.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        15
                )
        );

        outputArea.setLineWrap(false);

        outputArea.setMargin(
                new Insets(
                        12,
                        12,
                        12,
                        12
                )
        );

        JScrollPane outputScroll =
                new JScrollPane(
                        outputArea
                );

        outputScroll.setBorder(
                new LineBorder(
                        BORDER,
                        1
                )
        );

        outputScroll
                .getViewport()
                .setBackground(FIELD);

        /*
         * Minimum only.
         *
         * DO NOT set a fixed preferred height.
         * This is what allows the results area
         * to resize with the window.
         */
        outputScroll.setMinimumSize(
                new Dimension(
                        500,
                        110
                )
        );

        panel.add(
                outputScroll,
                BorderLayout.CENTER
        );

        // -----------------------------------------------------
        // CLEAR OUTPUT
        // -----------------------------------------------------
        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                0,
                                0
                        )
                );

        bottom.setBackground(PANEL);

        styleDarkButton(
                clearButton
        );

        clearButton.setPreferredSize(
                new Dimension(
                        165,
                        40
                )
        );

        bottom.add(
                clearButton
        );

        panel.add(
                bottom,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // GENERIC PANEL
    // =========================================================
    private JPanel createBlock() {

        JPanel panel =
                new JPanel();

        panel.setBackground(PANEL);

        panel.setBorder(
                new LineBorder(
                        BORDER,
                        1
                )
        );

        return panel;
    }

    // =========================================================
    // SECTION LABEL
    // =========================================================
    private JLabel createSectionLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(TEXT);
        label.setFont(SECTION_FONT);

        return label;
    }

    // =========================================================
    // SMALL LABEL
    // =========================================================
    private JLabel createSmallLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(MUTED);
        label.setFont(MONO_FONT);

        return label;
    }

    // =========================================================
    // TEXT FIELD STYLE
    // =========================================================
    private void styleTextField(
            JTextField field
    ) {

        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);

        field.setFont(
                NORMAL_FONT
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                BORDER,
                                1
                        ),
                        new EmptyBorder(
                                6,
                                10,
                                6,
                                10
                        )
                )
        );
    }

    // =========================================================
    // COMBO BOX STYLE
    // =========================================================
    private void styleComboBox(
            JComboBox<JobType> combo
    ) {

        combo.setBackground(FIELD);
        combo.setForeground(TEXT);

        combo.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        15
                )
        );

        combo.setFocusable(false);

        combo.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus
                    ) {

                        JLabel label =
                                (JLabel)
                                        super.getListCellRendererComponent(
                                                list,
                                                value,
                                                index,
                                                isSelected,
                                                cellHasFocus
                                        );

                        label.setBackground(
                                isSelected
                                        ? new Color(
                                                45,
                                                45,
                                                50
                                        )
                                        : FIELD
                        );

                        label.setForeground(
                                TEXT
                        );

                        label.setBorder(
                                new EmptyBorder(
                                        6,
                                        8,
                                        6,
                                        8
                                )
                        );

                        return label;
                    }
                }
        );
    }

    // =========================================================
    // DARK BUTTON
    // =========================================================
    private void styleDarkButton(
            JButton button
    ) {

        button.setBackground(PANEL);
        button.setForeground(TEXT);

        button.setFont(
                BUTTON_FONT
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new LineBorder(
                        BORDER,
                        1
                )
        );
    }

    // =========================================================
    // LIGHT BUTTON
    // =========================================================
    private void styleLightButton(
            JButton button
    ) {

        button.setBackground(
                BUTTON_LIGHT
        );

        button.setForeground(
                BLACK
        );

        button.setFont(
                BUTTON_FONT
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new LineBorder(
                        BUTTON_LIGHT,
                        1
                )
        );
    }

    // =========================================================
    // UPDATE INPUT STATE
    // =========================================================
    private void updateInputState() {

        boolean rangeJob =
                typeBox.getSelectedItem()
                        == JobType.PRIMESUM;

        // PRIMESUM
        startField.setEnabled(
                rangeJob
        );

        endField.setEnabled(
                rangeJob
        );

        // MAX / PRIMECOUNT
        inputArea.setEnabled(
                !rangeJob
        );

        if (rangeJob) {

            startField.setBackground(
                    FIELD
            );

            endField.setBackground(
                    FIELD
            );

            startField.setForeground(
                    TEXT
            );

            endField.setForeground(
                    TEXT
            );

            inputArea.setBackground(
                    DISABLED_FIELD
            );

            inputArea.setForeground(
                    new Color(
                            105,
                            105,
                            115
                    )
            );

        } else {

            startField.setBackground(
                    DISABLED_FIELD
            );

            endField.setBackground(
                    DISABLED_FIELD
            );

            inputArea.setBackground(
                    FIELD
            );

            inputArea.setForeground(
                    TEXT
            );
        }
    }

    // =========================================================
    // LOAD CSV
    // =========================================================
    private void loadCsv() {

        JFileChooser chooser =
                new JFileChooser();

        if (
                chooser.showOpenDialog(this)
                        != JFileChooser.APPROVE_OPTION
        ) {

            return;
        }

        try {

            inputArea.setText(
                    Files.readString(
                            chooser
                                    .getSelectedFile()
                                    .toPath()
                    )
            );

        } catch (IOException e) {

            showError(
                    "Could not read CSV: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SUBMIT JOB
    // =========================================================
    private void submitJob() {

        try {

            JobRequest request =
                    createRequest();

            append(
                    ">> SUBMITTED   "
                            + request.type()
                            + "   JOB: "
                            + shortId(request)
                            + "\n"
            );

            new SwingWorker<JobResult, Void>() {

                @Override
                protected JobResult doInBackground()
                        throws Exception {

                    WorkerService coordinator =
                            findCoordinator();

                    return coordinator.submitJob(
                            request
                    );
                }

                @Override
                protected void done() {

                    try {

                        JobResult result =
                                get();

                        append(
                                "<< COMPLETED   "
                                        + shortId(request)
                                        + "   |   RESULT: "
                                        + result.value()
                                        + "\n"
                        );

                        append(
                                "   "
                                        + result.explanation()
                                        + "\n\n"
                        );

                    } catch (
                            InterruptedException e
                    ) {

                        Thread.currentThread()
                                .interrupt();

                    } catch (
                            ExecutionException e
                    ) {

                        append(
                                "!! FAILED      "
                                        + shortId(request)
                                        + "   |   "
                                        + rootMessage(
                                                e.getCause()
                                        )
                                        + "\n\n"
                        );
                    }
                }
            }.execute();

        } catch (Exception e) {

            showError(
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // CREATE REQUEST
    // =========================================================
    private JobRequest createRequest() {

        JobType type =
                (JobType)
                        typeBox.getSelectedItem();

        if (type == JobType.PRIMESUM) {

            long start =
                    Long.parseLong(
                            startField
                                    .getText()
                                    .trim()
                    );

            long end =
                    Long.parseLong(
                            endField
                                    .getText()
                                    .trim()
                    );

            if (start > end) {

                throw new IllegalArgumentException(
                        "Range start must be less than or equal to range end."
                );
            }

            return JobRequest.primeSum(
                    start,
                    end
            );
        }

        List<Long> numbers =
                parseNumbers(
                        inputArea.getText()
                );

        if (type == JobType.MAX) {

            return JobRequest.max(
                    numbers
            );
        }

        return JobRequest.primeCount(
                numbers
        );
    }

    // =========================================================
    // PARSE NUMBERS
    // =========================================================
    private static List<Long> parseNumbers(
            String text
    ) {

        String cleaned =
                text.trim();

        if (cleaned.isEmpty()) {

            throw new IllegalArgumentException(
                    "Enter or load some numbers first."
            );
        }

        String[] tokens =
                cleaned.split(
                        "[,;\\s]+"
                );

        List<Long> numbers =
                new ArrayList<>();

        for (String token : tokens) {

            if (!token.isBlank()) {

                numbers.add(
                        Long.parseLong(
                                token.trim()
                        )
                );
            }
        }

        return numbers;
    }

    // =========================================================
    // FIND COORDINATOR
    // =========================================================
    private WorkerService findCoordinator()
            throws Exception {

        List<WorkerInfo> workers =
                bootstrap.getActiveWorkers();

        if (workers.isEmpty()) {

            throw new IllegalStateException(
                    "No workers are active."
            );
        }

        int coordinatorId = -1;

        // -----------------------------------------------------
        // ASK WORKERS FOR CURRENT COORDINATOR
        // -----------------------------------------------------
        for (
                WorkerInfo worker :
                        workers
        ) {

            try {

                int reported =
                        RmiLookup
                                .worker(worker)
                                .getCoordinatorId();

                if (reported >= 0) {

                    coordinatorId =
                            reported;

                    break;
                }

            } catch (Exception ignored) {
            }
        }

        // -----------------------------------------------------
        // NO COORDINATOR -> START ELECTION
        // -----------------------------------------------------
        if (coordinatorId < 0) {

            WorkerService first =
                    RmiLookup.worker(
                            workers.get(0)
                    );

            first.startElection();

            /*
             * Wait briefly for the election
             * to propagate through the workers.
             */
            for (
                    int attempt = 0;
                    attempt < 20;
                    attempt++
            ) {

                Thread.sleep(100);

                coordinatorId =
                        first.getCoordinatorId();

                if (coordinatorId >= 0) {
                    break;
                }
            }
        }

        if (coordinatorId < 0) {

            throw new IllegalStateException(
                    "Coordinator election did not complete."
            );
        }

        final int selectedId =
                coordinatorId;

        WorkerInfo coordinator =
                workers
                        .stream()
                        .filter(
                                worker ->
                                        worker.id()
                                                == selectedId
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Coordinator "
                                                        + selectedId
                                                        + " is not in the active worker list."
                                        )
                        );

        return RmiLookup.worker(
                coordinator
        );
    }

    // =========================================================
    // APPEND OUTPUT
    // =========================================================
    private void append(
            String text
    ) {

        outputArea.append(text);

        outputArea.setCaretPosition(
                outputArea
                        .getDocument()
                        .getLength()
        );
    }

    // =========================================================
    // ERROR MESSAGE
    // =========================================================
    private void showError(
            String message
    ) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "DistriLab Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================================================
    // SHORT JOB ID
    // =========================================================
    private static String shortId(
            JobRequest request
    ) {

        return request
                .jobId()
                .toString()
                .substring(
                        0,
                        8
                );
    }

    // =========================================================
    // ROOT ERROR MESSAGE
    // =========================================================
    private static String rootMessage(
            Throwable throwable
    ) {

        Throwable current =
                throwable;

        while (
                current.getCause()
                        != null
        ) {

            current =
                    current.getCause();
        }

        return current.getMessage()
                == null
                ? current.toString()
                : current.getMessage();
    }

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(
            String[] args
    ) {

        String host =
                args.length > 0
                        ? args[0]
                        : "localhost";

        int port =
                args.length > 1
                        ? Integer.parseInt(
                                args[1]
                        )
                        : 1099;

        SwingUtilities.invokeLater(
                () -> {

                    try {

                        ClientGui gui =
                                new ClientGui(
                                        host,
                                        port
                                );

                        /*
                         * Normal resizable window.
                         *
                         * User can maximize it manually.
                         * Results panel expands automatically.
                         */
                        gui.setVisible(true);

                    } catch (Exception e) {

                        JOptionPane.showMessageDialog(
                                null,
                                "Could not connect to Bootstrap Node: "
                                        + e.getMessage(),
                                "Startup Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
        );
    }
}