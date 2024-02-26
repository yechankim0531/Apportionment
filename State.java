package edu.virginia.sde.hw1;

public class State {
    private String stateName;
    private int population;
    private double divide;
    private double floor;
    private double remainder;

    private double priority;

    public State(String name, int pop) {
        this.stateName = name;
        this.population = pop;
        this.divide = 0.0;
        this.floor = 0.0;
        this.remainder = 0.0;
        this.priority = 0.0;
    }

    public void setPriority(double new_priority) {
        this.priority = new_priority;
    }

    public double getPriority() {
        return this.priority;
    }
    public double getDivide() {
        return this.divide;
    }

    public String getStateName() {
        return this.stateName;
    }
    public int getPopulation() {
        return this.population;
    }

    public void setPopulation(int new_pop) {
        this.population = new_pop;
    }

    public void setApportionValues(double divide) {
        this.divide = divide;
        this.floor = Math.floor(divide); // https://www.programiz.com/java-programming/library/math/floor
        this.remainder = this.divide - this.floor;
    }
    public double getFloor() {
        return this.floor;
    }

    public void incrementFloor() {
        this.floor += 1;
    }

    public double getRemainder() {
        return this.remainder;
    }
}
