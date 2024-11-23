package de.cranix.dao;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class FilterObject {
    private boolean showPrivate = true;
    private boolean showIndividual = true;
    private List<Room> rooms = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    public FilterObject() {}
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
        }
    }
    public List<Room> getRooms() {return rooms;}

    public void setRooms(List<Room> rooms) {this.rooms = rooms;}

    public List<Group> getGroups() {return groups;}

    public void setGroups(List<Group> groups) {this.groups = groups;}

    public boolean isShowPrivate() {return showPrivate;}

    public void setShowPrivate(boolean showPrivate) {this.showPrivate = showPrivate;}

    public boolean isShowIndividual() {return showIndividual;}

    public void setShowIndividual(boolean showIndividual) {this.showIndividual = showIndividual;}
}
