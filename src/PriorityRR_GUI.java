/*
Mohammed Abdulla Alaswad 202310618
Hussain Jameel Moosa Omran 202308610
Supervised by: Dr. Qasem Turki Obaidat
 */
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PriorityRR_GUI extends JFrame {
    private JTable inputTable, outputTable;
    private DefaultTableModel inputModel, outputModel;
    private JTextField qField;
    private JTextArea ganttArea;
    private JLabel avgLabel;
    public PriorityRR_GUI() {
        setTitle("Priority Scheduling with Round Robin - Professional GUI");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        topPanel.add(new JLabel("Quantum (q):"));
        qField = new JTextField("2", 5);
        qField.setHorizontalAlignment(JTextField.CENTER);
        topPanel.add(qField);
        JButton addBtn = new JButton("Add Process");
        JButton clearBtn = new JButton("Clear All");
        topPanel.add(addBtn);
        topPanel.add(clearBtn);
        add(topPanel, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        String[] inCols = {"Process ID", "Arrival Time", "Burst Time", "Priority"};
        inputModel = new DefaultTableModel(inCols, 0);
        inputTable = new JTable(inputModel);
        styleTable(inputTable);
        inputModel.addRow(new Object[]{"p1", "0", "5", "2"});
        inputModel.addRow(new Object[]{"p2", "1", "4", "1"});
        JPanel inWrapper = new JPanel(new BorderLayout());
        inWrapper.add(new JLabel("Input Data:", JLabel.CENTER), BorderLayout.NORTH);
        inWrapper.add(new JScrollPane(inputTable), BorderLayout.CENTER);
        centerPanel.add(inWrapper);
        String[] outCols = {"ID", "AT", "BT", "PR", "WT", "TAT", "RT"};
        outputModel = new DefaultTableModel(outCols, 0);
        outputTable = new JTable(outputModel);
        styleTable(outputTable);
        JPanel outWrapper = new JPanel(new BorderLayout());
        outWrapper.add(new JLabel("Results Analysis:", JLabel.CENTER), BorderLayout.NORTH);
        outWrapper.add(new JScrollPane(outputTable), BorderLayout.CENTER);
        centerPanel.add(outWrapper);
        add(centerPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        ganttArea = new JTextArea("Gantt Chart will appear here");
        ganttArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        ganttArea.setEditable(false);
        ganttArea.setRows(2);
        JScrollPane ganttScroll = new JScrollPane(ganttArea);
        ganttScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        ganttScroll.setBorder(BorderFactory.createTitledBorder("Gantt Chart Flow"));

        avgLabel = new JLabel("Averages: N/A", JLabel.CENTER);
        avgLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));

        JButton calcBtn = new JButton("Run Simulation");
        calcBtn.setPreferredSize(new Dimension(200, 40));
        calcBtn.setBackground(new Color(30, 30, 30));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setFocusPainted(false);

        bottomPanel.add(ganttScroll, BorderLayout.NORTH);
        bottomPanel.add(avgLabel, BorderLayout.CENTER);
        bottomPanel.add(calcBtn, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> inputModel.addRow(new Object[]{"p" + (inputModel.getRowCount() + 1), "0", "0", "0"}));
        clearBtn.addActionListener(e -> { inputModel.setRowCount(0); outputModel.setRowCount(0); ganttArea.setText(""); });
        calcBtn.addActionListener(e -> calculateLogic());
    }
    private void styleTable(JTable table) {
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    private void calculateLogic() {
        try {
            int q = Integer.parseInt(qField.getText().trim());
            List<Process> processes = new ArrayList<>();
            for (int i = 0; i < inputModel.getRowCount(); i++) {
                String id = inputModel.getValueAt(i, 0).toString();
                int at = Integer.parseInt(inputModel.getValueAt(i, 1).toString());
                int bt = Integer.parseInt(inputModel.getValueAt(i, 2).toString());
                int pr = Integer.parseInt(inputModel.getValueAt(i, 3).toString());
                processes.add(new Process(id, at, bt, pr));
            }

            if (processes.isEmpty()) return;

            processes.sort(Comparator.comparingInt(p -> p.at));

            TreeMap<Integer, ArrayDeque<Process>> priorityQueues = new TreeMap<>();
            boolean[] added = new boolean[processes.size()];
            List<String> gLabels = new ArrayList<>();
            List<Integer> gTimes = new ArrayList<>();
            int curT = 0, completed = 0;

            while (completed < processes.size()) {
                for (int i = 0; i < processes.size(); i++) {
                    if (!added[i] && processes.get(i).at <= curT) {
                        priorityQueues.computeIfAbsent(processes.get(i).pr, k -> new ArrayDeque<>()).addLast(processes.get(i));
                        added[i] = true;
                    }
                }

                if (priorityQueues.isEmpty()) {
                    if (gLabels.isEmpty() || !gLabels.get(gLabels.size() - 1).equals("idle")) {
                        gTimes.add(curT);
                        gLabels.add("idle");
                    }
                    curT++;
                    continue;
                }

                int highestPr = priorityQueues.firstKey();
                Process current = priorityQueues.get(highestPr).removeFirst();
                if (priorityQueues.get(highestPr).isEmpty()) priorityQueues.remove(highestPr);

                if (current.rt == -1) current.rt = curT - current.at;
                gTimes.add(curT);
                gLabels.add(current.id);

                int runTime = Math.min(q, current.rem);
                current.rem -= runTime;
                curT += runTime;

                for (int i = 0; i < processes.size(); i++) {
                    if (!added[i] && processes.get(i).at <= curT) {
                        priorityQueues.computeIfAbsent(processes.get(i).pr, k -> new ArrayDeque<>()).addLast(processes.get(i));
                        added[i] = true;
                    }
                }

                if (current.rem == 0) {
                    current.ct = curT;
                    current.tat = current.ct - current.at;
                    current.wt = current.tat - current.bt;
                    completed++;
                } else {
                    priorityQueues.computeIfAbsent(current.pr, k -> new ArrayDeque<>()).addLast(current);
                }
            }
            gTimes.add(curT);
            updateResultsInUI(processes, gLabels, gTimes, curT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    private void updateResultsInUI(List<Process> processes, List<String> gLabels, List<Integer> gTimes, int totalTime) {
        outputModel.setRowCount(0);
        double sWT = 0, sTAT = 0;
        for (Process p : processes) {
            outputModel.addRow(new Object[]{p.id, p.at, p.bt, p.pr, p.wt, p.tat, p.rt});
            sWT += p.wt; sTAT += p.tat;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gLabels.size(); i++) {
            sb.append(gTimes.get(i)).append(" | ").append(gLabels.get(i)).append(" | ");
        }
        sb.append(totalTime);
        ganttArea.setText(sb.toString());
        avgLabel.setText(String.format("Avg Waiting Time: %.2f  |  Avg Turnaround Time: %.2f", sWT/processes.size(), sTAT/processes.size()));
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PriorityRR_GUI().setVisible(true));
    }
}