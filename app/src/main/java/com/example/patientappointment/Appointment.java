package com.example.patientappointment;

public class Appointment {
    private String id;
    private String userId;
    private String doctor;
    private String specialization;
    private String date;
    private String time;

    // Required empty constructor
    public Appointment() {}

    // Ensure there are 6 String parameters here
    public Appointment(String id, String userId, String doctor, String specialization, String date, String time) {
        this.id = id;
        this.userId = userId;
        this.doctor = doctor;
        this.specialization = specialization;
        this.date = date;
        this.time = time;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDoctor() { return doctor; }
    public String getSpecialization() { return specialization; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}