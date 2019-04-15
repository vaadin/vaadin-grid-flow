package com.vaadin.flow.component.grid.demo.data;

import java.util.ArrayList;
import java.util.List;

public class StatesData {
    private List<String> stateList= new ArrayList<>();
    {
        stateList.add("Alabama");
        stateList.add("California");
        stateList.add("Florida");
        stateList.add("Georgia");
        stateList.add("Maryland");
        stateList.add("Michigan");
        stateList.add("Nevada");
        stateList.add("New York");
        stateList.add("Ohio");
        stateList.add("Washington");
    }

    public List<String> getAllStates(){
        return stateList;
    }
}
