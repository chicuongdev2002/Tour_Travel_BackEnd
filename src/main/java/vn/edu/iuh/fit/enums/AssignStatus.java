package vn.edu.iuh.fit.enums;public enum AssignStatus {    TODO("Todo"),    ACCEPT("Accept"),    REJECT("Reject");    private final String description;    AssignStatus(String description) {        this.description = description;    }    public String getDescription() {        return description;    }    @Override    public String toString() {        return this.name() + " (" + description + ")";    }}