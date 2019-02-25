package com.vaadin.flow.component.grid.demo.entity;

public class Customer {
    private int id;
    private String firstName;
    private String LastName;
    private String Country;
    private String State;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public Customer(int id, String firstName, String lastName, String country, String state) {
        this.id = id;
        this.firstName = firstName;
        LastName = lastName;
        Country = country;
        State = state;
    }
}
