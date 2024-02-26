package edu.virginia.sde.hw1;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Collections;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Error: did not enter any arguments. Please specify a csv file path and, optionally, the number of representatives to apportion.");
        }

        if (args.length > 3) {
            throw new IllegalArgumentException("Error: entered more than the expected amount of arguments. Just specify the .csv file path and (optionally) the amount of representatives and --hamilton tag");
        }
        String file_path = args[0].strip();
        String scanning_method = "None";
        if (file_path.endsWith(".xlsx")) {
            scanning_method = ".xlsx";
        }
        else if (file_path.endsWith(".csv")) {
            scanning_method = ".csv";
        }
        int reps = 435;
        String algorithm = "huntington";
        boolean rep_specified = false;
        boolean algo_specified = false;
        if (args.length > 1) {
            for (int i=1; i<args.length; i++) {
                if (args[i].equals("--hamilton")) {
                    if (algo_specified) {
                        throw new IllegalArgumentException("Error: already specified a --hamilton tag");
                    }
                    else {
                        algorithm = "hamilton";
                        algo_specified = true;
                    }
                }
                else {
                    if (rep_specified) {
                        throw new IllegalArgumentException("Error: already specified a number of reps");
                    }
                    try {
                        int new_reps = Integer.valueOf(args[i].strip());
                        if (new_reps > 0) {
                            reps = new_reps;
                            rep_specified = true;
                        }
                        else {
                            throw new NumberFormatException("Error: improper negative/zero integer specified");
                        }
                    }
                    catch (NumberFormatException e) {
                        throw new NumberFormatException("Error: improper argument. A positive integer (for # of reps) or the --hamilton tag was expected, but either you already provided it or you provided something different entirely.");
                    }

                }
            }
        }

        FileReader file_reader = new FileReader(file_path);
        if (scanning_method.equals("None")) {
            throw new IllegalArgumentException("Error: did not specify a .xlsx or .csv file to be scanned.");
        }
        else if (scanning_method.equals(".xlsx")) {
            file_reader.readXLSX();
        }
        else {
            file_reader.readCSV();
        }
        ArrayList<State> state_list = file_reader.getState_list();
        if (!file_reader.getValidScan()) {
            throw new IllegalArgumentException("Error - .CSV file is empty/does not exist. Program could not scan any values");
        }
        else {
            if (algorithm.equals("hamilton")) {
                System.out.println("Executing Hamilton Algorithm with " + reps + " representatives to apportion...");
                ArrayList<Integer> populations = new ArrayList<>();
                for (State i : state_list) {
                    populations.add(i.getPopulation());
                }
                hamilton_method(populations, state_list, reps);
            }
            else {
                System.out.println("Executing Huntington Algorithm with " + reps + " representatives to apportion...");
                huntington_method(state_list, reps);
            }
            printResult(state_list);
        }
    }

    public static void hamilton_method(ArrayList<Integer> populations, ArrayList<State> state_list, int num_reps) {
        int total_pop = 0;
        for (Integer i : populations) {
            total_pop += i;
        }
        double average_pop = (double) total_pop / num_reps;
        int taken_reps = 0;
        for (State s : state_list) {
            int state_pop = s.getPopulation();
            double divide = state_pop / average_pop;
            s.setApportionValues(divide);
            taken_reps += (int) s.getFloor();
        }
        int remaining_reps = num_reps - taken_reps;
        state_list.sort((o1, o2) // Source: https://www.geeksforgeeks.org/how-to-sort-an-arraylist-of-objects-by-property-in-java/
                -> Double.compare(o2.getRemainder(), o1.getRemainder())); // Source: https://www.scaler.com/topics/double-compare-java/
        for (int i = 0; i < remaining_reps; i++) {
            State s = state_list.get(i);
            s.incrementFloor();
        }
        state_list.sort((o1, o2) -> o1.getStateName().compareTo(o2.getStateName())); // Source: https://www.geeksforgeeks.org/how-to-sort-an-arraylist-of-objects-by-property-in-java/
    }

    public static void huntington_method(ArrayList<State> state_list, int num_reps) {
        int used_reps = 0;
        for (State s : state_list) {
            s.incrementFloor();
            s.setPriority(calculate_priority(s));
            used_reps++;
        }
        if (used_reps > num_reps) {
            throw new RuntimeException("Error: too many states for the requested amount of representatives. Each state must have at least one representative and that is impossible with only " + num_reps + " desired representatives.");
        }
        while (used_reps < num_reps) {
            State current_state = get_max_priority(state_list);
            current_state.incrementFloor();
            current_state.setPriority(calculate_priority(current_state));
            used_reps++;
        }
    }

    public static State get_max_priority(ArrayList<State> state_list) {
        return Collections.max(state_list, Comparator.comparing(s -> s.getPriority())); // Source: https://zengcode.medium.com/getting-object-with-min-max-value-property-from-list-of-objects-in-java-8-7b1d839537f
    }
    public static double calculate_priority(State s) {
        int num_reps = (int) s.getFloor();
        double priority = (double) s.getPopulation() / (Math.sqrt((num_reps * (num_reps + 1)))); // https://www.geeksforgeeks.org/java-sqrt-method-examples/
        return priority;
    }
    public static void printResult(ArrayList<State> state_list){
        for(State state : state_list){
            int num_reps = (int)state.getFloor();
            System.out.println(state.getStateName() + " - " + num_reps);
        }
    }

}
