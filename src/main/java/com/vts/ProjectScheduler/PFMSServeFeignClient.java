package com.vts.ProjectScheduler;

import java.util.List;

import com.vts.ProjectScheduler.entity.pms.CCMView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.vts.ProjectScheduler.entity.pms.CCMView;

@FeignClient(name = "PFMSServeFeignClient", url = "${server_uri}"+"/pfms_serv")
public interface PFMSServeFeignClient {
	
	
	@PostMapping("/getCCMViewData")
    public List<CCMView> getCCMViewData(@RequestHeader(name = "labcode")String LabCode);
    
	
}