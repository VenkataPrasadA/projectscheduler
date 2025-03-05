package com.vts.ProjectScheduler.dto;

	import java.util.ArrayList;
	import java.util.List;

	public class PftsEmailDto {
	    private String email;
	    private String DronaEmail;
	    private List<Object[]> demandAndEventAndForwardeByList;

	    public PftsEmailDto(String email,String DronaEmail) {
	        this.email = email;
	        this.DronaEmail = DronaEmail;
	        this.demandAndEventAndForwardeByList = new ArrayList<>();
	    }

	    public String getEmail() {
	        return email;
	    }
	    
	    public String getDronaEmail() {
	        return DronaEmail;
	    }

	    public List<Object[]> getDemandAndEventAndForwardeByList() {
	        return demandAndEventAndForwardeByList;
	    }

	    public void addDemandAndEventAndForwardeBy(String demandNo, String eventName, String forwardeByName) {
	    	demandAndEventAndForwardeByList.add(new Object[]{demandNo, eventName, forwardeByName});
	    }
	}

