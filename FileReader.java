package edu.virginia.sde.hw1;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import java.util.Iterator;

public class FileReader {
    private String file_path;
    private ArrayList<State> state_list;
    private ArrayList<String> found_states;
    private boolean valid_scan;

    private XSSFWorkbook workbook;
    public FileReader(String csv_path) {
        this.file_path = csv_path;
        this.state_list = new ArrayList<>();
        this.found_states = new ArrayList<>();
        this.valid_scan = false;
    }

    public void readCSV() {
        try (Scanner sc = new Scanner(new File(this.file_path))){ // Source: https://www.javatpoint.com/how-to-read-csv-file-in-java

            String header = sc.nextLine();
            String[] header_values = (header).split(",");
            int state_column_index = -1;
            int pop_column_index = -1;
            for (int i = 0; i<header_values.length; i++) {
                String column_name = header_values[i].strip().toLowerCase();
                if (column_name.equals("state")) {
                    state_column_index = i;
                }
                else if (column_name.equals("population")) {
                    pop_column_index = i;
                }
            }

            if (state_column_index < 0 || pop_column_index < 0) {
                throw new RuntimeException("Error: state and/or population column not found in " + this.file_path);
            }

            while (sc.hasNextLine())
            {
                String next_line = sc.nextLine();
                String[] row_values = (next_line).split(",");
                if (row_values.length < 2) {
                    throw new ArrayIndexOutOfBoundsException("Error: CSV row does not have at least two columns. Row: " + next_line);
                }
                String current_state = row_values[state_column_index];
                try { // Source: https://kevinsguides.com/guides/code/java/javaintro/ch13-trycatch
                        int current_pop = Integer.valueOf(row_values[pop_column_index].strip());
                        // Check if the state already exists and is listed
                        if (this.found_states.contains(current_state)) {
                            // Replace state population if it's already been scanned
                            handle_state_duplicates(current_state, current_pop);
                        }
                        else {
                            if (validateRow(current_state, current_pop)) {
                                State state_obj = new State(current_state, current_pop);
                                this.state_list.add(state_obj);
                                this.found_states.add(current_state);
                            }
                        }
                }
                catch (NumberFormatException e) {
                    if (!row_values[1].contains("Population")) {
                        System.out.println("Incorrect number formatted: '" + row_values[pop_column_index] + "'. Skipping line");
                    }
                }
            }
            sc.close();  //closes the scanner
        }
        catch (FileNotFoundException e){
            System.out.println("Error - no file provided or incorrect file path given");
        }
        if (!this.state_list.isEmpty()) {
            this.valid_scan = true;
        }
    }

    public void handle_state_duplicates(String current_state, int current_pop) {
        for (State s : this.state_list) {
            if (s.getStateName().equals(current_state)) {
                if (current_pop >= 0) {
                    s.setPopulation(current_pop);
                }
                break;
            }
        }
    }
    public void openXLSXWorkbook() {
        String filename = this.file_path;
        try {
            FileInputStream inputStream = new FileInputStream(new File(filename));
            this.workbook = new XSSFWorkbook(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error: File " + filename + " not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void readXLSX() {
        openXLSXWorkbook();
        Sheet worksheet = this.workbook.getSheetAt(0);
        Iterator<Row> rowIterator = worksheet.rowIterator();
        if (!rowIterator.hasNext()) {
            throw new RuntimeException("Empty Spreadsheet!");
        }
        Row header_row = rowIterator.next();
        Iterator<Cell> cellIterator = header_row.cellIterator();
        int state_column_index = -1;
        int pop_column_index = -1;
        int current_index = 0;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            String cell_value = cell.getStringCellValue().strip().toLowerCase();
            if (cell_value.equals("state")) {
                state_column_index = current_index;
            }
            else if (cell_value.equals("population")) {
                pop_column_index = current_index;
            }
            current_index += 1;
        }
        if (state_column_index < 0 || pop_column_index < 0) {
            throw new RuntimeException("Error: state and/or population column missing from .xlsx file");
        }
        // Read remaining rows using the appropriate columns
        while (rowIterator.hasNext()) {
            Row current_row = rowIterator.next();

            try {
                String state = current_row.getCell(state_column_index).getStringCellValue();
                int population = (int) current_row.getCell(pop_column_index).getNumericCellValue();
                if (validateRow(state, population)) {
                    if (found_states.contains(state)) {
                        handle_state_duplicates(state, population);
                    }
                    else {
                        State s = new State(state, population);
                        this.state_list.add(s);
                        this.found_states.add(state);
                    }
                }
            }
            catch(NumberFormatException e) {
                System.out.println("Invalid population: " + current_row.getCell(pop_column_index).getStringCellValue());
            }
        }
        if (!this.state_list.isEmpty()) {
            this.valid_scan = true;
        }
    }

    public boolean validateRow(String state, int population) {
        if (!state.isBlank() && population >= 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean getValidScan() {
        return this.valid_scan;
    }

    public ArrayList<State> getState_list() {
        return this.state_list;
    }
}
