package vn.edu.iuh.fit.enums;public enum ParticipantType {    CHILDREN("Children"),    ADULTS("Adults"),    ELDERLY("Elderly");    private final String type;    ParticipantType(String type) {        this.type = type;    }    public String getDescription() {        return type;    }    @Override    public String toString() {        return this.name() + " (" + type + ")";    }}