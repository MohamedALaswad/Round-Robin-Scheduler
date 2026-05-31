/*
Mohammed Abdulla Alaswad 202310618
Hussain Jameel Moosa Omran 202308610
Supervised by: Dr. Qasem Turki Obaidat
 */
import java.util.*;
class Process {
    String id;
    int at;  // Arrival Time
    int bt;  // Burst Time
    int pr;  // Priority
    int rem; // Remaining Time
    int ct;  // Completion Time
    int tat; // Turnaround Time
    int wt;  // Waiting Time
    int rt = -1; // Response Time

    public Process(String id, int at, int bt, int pr) {
        this.id = id;
        this.at = at;
        this.bt = bt;
        this.pr = pr;
        this.rem = bt;
    }
}

public class PriorityRR {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Priority Scheduling with Round Robin ");

        int q;
        while (true) {
            System.out.print("Enter Quantum Time (q): ");
            try {
                q = Integer.parseInt(sc.next().trim());
                if (q > 0) break;
                System.out.println("Error!!\n Quantum must be positive!");
            } catch (NumberFormatException e) {
                System.out.println("Error!!\n Invalid input!");
            }
        }

        List<Process> processes = new ArrayList<>();
        System.out.println("Enter Process Details (ID \tArrival Time\t Burst Time\t Priority):");
        System.out.println("Example: p1 0 5 1 (Enter 0 0 0 0 to finish)");

        while (true) {
            try {
                String id = sc.next().trim();
                int at = sc.nextInt();
                int bt = sc.nextInt();
                int pr = sc.nextInt();

                if (id.equals("0") && at == 0 && bt == 0 && pr == 0) break;
                if (at < 0 || bt <= 0 || pr < 0) {
                    System.out.println("  Error!!\n Invalid values, try again.");
                    continue;
                }
                processes.add(new Process(id, at, bt, pr));
            } catch (Exception e) {
                System.out.println("  Error!! Check your input format.");
                sc.nextLine();
            }
        }

        if (processes.isEmpty()) return;

        processes.sort(Comparator.comparingInt(p -> p.at));

        int n = processes.size();
        TreeMap<Integer, ArrayDeque<Process>> priorityQueues = new TreeMap<>();
        boolean[] added = new boolean[n];
        List<String> gLabels = new ArrayList<>();
        List<Integer> gTimes = new ArrayList<>();

        int curT = 0;
        int comp = 0;

        while (comp < n) {
            for (int i = 0; i < n; i++) {
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

            for (int i = 0; i < n; i++) {
                if (!added[i] && processes.get(i).at <= curT) {
                    priorityQueues.computeIfAbsent(processes.get(i).pr, k -> new ArrayDeque<>()).addLast(processes.get(i));
                    added[i] = true;
                }
            }

            if (current.rem == 0) {
                current.ct = curT;
                current.tat = current.ct - current.at;
                current.wt = current.tat - current.bt;
                comp++;
            } else {
                priorityQueues.computeIfAbsent(current.pr, k -> new ArrayDeque<>()).addLast(current);
            }
        }

        gTimes.add(curT);

        System.out.println("\n \t Gantt Chart \t");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gLabels.size(); i++) {
            sb.append(gTimes.get(i)).append(" | ").append(gLabels.get(i)).append(" | ");
        }
        sb.append(curT);
        System.out.println(sb);

        System.out.println("\nID\tAT\tBT\tPR\tWT\tTAT\tRT");
        double sumWT = 0, sumTAT = 0, sumRT = 0;

        for (Process p : processes) {
            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\t%d\n", p.id, p.at, p.bt, p.pr, p.wt, p.tat, p.rt);
            sumWT += p.wt; sumTAT += p.tat; sumRT += p.rt;
        }
        System.out.printf("\nAvg WaitingT: %.2f | Avg  Turnaround Time: %.2f | Avg Response Time: %.2f\n", sumWT/n, sumTAT/n, sumRT/n);
        sc.close();
    }
}