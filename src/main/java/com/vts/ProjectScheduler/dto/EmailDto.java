package com.vts.ProjectScheduler.dto;

import java.util.ArrayList;
import java.util.List;

public class EmailDto {
    private String email;
    private String DronaEmail;
    private List<Object[]> dakAndSourceAndDueDateList;

    public EmailDto(String email,String DronaEmail) {
        this.email = email;
        this.DronaEmail = DronaEmail;
        this.dakAndSourceAndDueDateList = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }
    
    public String getDronaEmail() {
        return DronaEmail;
    }

    public List<Object[]> getDakAndSourceAndDueDateList() {
        return dakAndSourceAndDueDateList;
    }

    public void addDakAndSourceAndDueDate(String dakNo, String source, String actionDueDate) {
    	dakAndSourceAndDueDateList.add(new Object[]{dakNo, source, actionDueDate});
    }
}
