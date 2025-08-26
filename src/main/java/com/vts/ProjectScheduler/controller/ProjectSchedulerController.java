package com.vts.ProjectScheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vts.ProjectScheduler.DateTimeFormatUtil;
import com.vts.ProjectScheduler.PFMSServeFeignClient;
import com.vts.ProjectScheduler.dto.EmailDto;
import com.vts.ProjectScheduler.dto.MailConfigurationDto;
import com.vts.ProjectScheduler.dto.MeetingMailDto;
import com.vts.ProjectScheduler.dto.PftsEmailDto;
import com.vts.ProjectScheduler.entity.pms.*;
import com.vts.ProjectScheduler.service.ProjectSchedulerService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
public class ProjectSchedulerController {

    private static final Logger logger= LogManager.getLogger(ProjectSchedulerController.class);


    private  SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private  SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd");
    private  SimpleDateFormat rdf=new SimpleDateFormat("dd-MM-yyyy");

    @Autowired
    ProjectSchedulerService service;

    @Autowired
    private Environment env;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    PFMSServeFeignClient PFMSServ;

    private String username;

    private String host;

    private String Port;

    private String password;

    private String Dronausername;

    private String Dronahost;

    private String DronaPort;

    private String Dronapassword;

    @Value("${LabCode}")
    private String LabCode;

    @Value("${server_uri}")
    private String uri;

    MailConfigurationDto dto1=new MailConfigurationDto();
    MailConfigurationDto dto2=new MailConfigurationDto();

    @PostConstruct
    public void init() {
        welcome();
    }
    @GetMapping("/")
    public String welcome() {
        try {
            dto1=service.getMailConfigByTypeOfHost("L");
            Port=dto1.getPort();
            username= dto1.getUsername().toString();
            password=dto1.getPassword();
            host=dto1.getHost();

            dto2=service.getMailConfigByTypeOfHost("D");
            DronaPort=dto2.getPort();
            Dronausername=dto2.getUsername().toString();
            Dronapassword=dto2.getPassword();
            Dronahost=dto2.getHost();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error loading mail configuration.";
        }
        return "";
    }

    @Scheduled(cron = "${DmsTime1}")
    public void reportTodayCurrentTime() throws Exception {
        try {
            myDailyPendingScheduledMailTask("today");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "${DmsTime2}")
    public void reportCurrentTime() throws Exception {
        try {
            myDailyPendingScheduledMailTask("tommorrow");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "${DmsTime3}")
    public void summaryReport() throws Exception {
        try {
            mySummaryDistributedMailTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "${DmsWeekTime}")
    public void weeklyReport() throws Exception {
        try {
            myWeeklyScheduledMailTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron="${PmsTime1}")
    public void pmsMailOne() {
        try {
            sentWeeklyActionMail(0,"D");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron="${PmsTime2}")
    public void pmsMailTwo() {
        try {
            sentWeeklyActionMail(7,"W");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Scheduled(cron="${PmsTime3}")
    public void pmsMailThree() {
        try {
            sendMeetingEmails();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------- DMS MAILS SEND CODE START ------------------------------------------------------------*/


    @Async
    public void myDailyPendingScheduledMailTask(String day) {
        logger.info(new Date() + " Inside CONTROLLER myDailyPendingScheduledMailTask ");
        try {
            long mailTrackingId = 0;
            long mailTrackingInsightsId = 0;
            AtomicInteger mailSendSuccessCount = new AtomicInteger(0);
            long dailyPendingMailSendInitiation = service.getMailInitiatedCount("D");
            if(day!=null && day.equalsIgnoreCase("today")) {
                if (dailyPendingMailSendInitiation == 0) {
                    mailTrackingId = service.insertMailTrackInitiator("D");
                }
            }else {
                mailTrackingId = service.insertMailTrackInitiator("D");
            }
            final long effectivelyFinalMailTrackingId = mailTrackingId;
            List<Object[]> pendingReplyEmpsDetailstoSendMail = service.getDailyPendingReplyEmpData();
            if (mailTrackingId > 0 && pendingReplyEmpsDetailstoSendMail != null && pendingReplyEmpsDetailstoSendMail.size() > 0) {
                mailTrackingInsightsId = service.insertDailyPendingInsights(mailTrackingId);
                if(mailTrackingInsightsId > 0) {
                    Map<Object, EmailDto> empToDataMap = new HashMap<>();
                    for (Object[] obj : pendingReplyEmpsDetailstoSendMail) {
                        Object empId = obj[1];
                        Object dakNo = obj[4];
                        Object source = obj[5];
                        Object dueDate = obj[6];
                        String email = null;
                        String dronalEmail=null;
                        if(obj[3] != null && !obj[3].toString().trim().isEmpty()) {
                            email = obj[3].toString();
                        }
                        if(obj[8] != null && !obj[8].toString().trim().isEmpty()) {
                            dronalEmail = obj[8].toString();
                        }
                        if (empId != null && email != null && !email.isEmpty()) {
                            if (!empToDataMap.containsKey(empId)) {
                                empToDataMap.put(empId, new EmailDto(email,dronalEmail));
                            }
                            if (dakNo != null && !dakNo.toString().isEmpty()) {
                                empToDataMap.get(empId).addDakAndSourceAndDueDate(dakNo.toString(), source.toString(), dueDate.toString());
                            }
                        }
                    }
                    if (!empToDataMap.isEmpty()) {
                        int batchSize = 10;
                        List<Map.Entry<Object, EmailDto>> entries = new ArrayList<>(empToDataMap.entrySet());
                        for (int i = 0; i < entries.size(); i += batchSize) {
                            List<Map.Entry<Object, EmailDto>> batch = entries.subList(i, Math.min(i + batchSize, entries.size()));
                            for (Map.Entry<Object, EmailDto> mailMapData : batch) {
                                Object empId = mailMapData.getKey();
                                EmailDto emailData = mailMapData.getValue();
                                String email = emailData.getEmail();
                                String dronaMail = emailData.getDronaEmail();
                                String dakCount;
                                String word;
                                int size = emailData.getDakAndSourceAndDueDateList().size();

                                if (size > 1) {
                                    dakCount = size + " DAKs";
                                    word = "replies";
                                } else {
                                    dakCount = size + " DAK";
                                    word = "reply";
                                }
                                try {
                                    if (email != null) {
                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                                        String subject = "DMS - Daily Pending Replies Report - " + currentDate;
                                        String message = "<p>Dear Sir/Madam,</p>";
                                        message += "<p></p>";
                                        message += "<p>This email is to inform you that you have " + dakCount + " with actions due " + day + ", awaiting your " + word + " to ensure timely completion.</p>";
                                        message += "<p>This is for your information, please take the action.</p>";
                                        message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 650px; font-size: 16px; border-collapse: collapse;\">";
                                        message += "<thead><tr><th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px\">DAK No</th>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">Source</th></tr></thead><tbody>";
                                        for (Object[] dakAndSource : emailData.getDakAndSourceAndDueDateList()) {
                                            message += "<tr><td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + dakAndSource[0] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + dakAndSource[1] + "</td></tr>";
                                        }
                                        message += "</tbody></table>";
                                        message += "Please <a href=\"" + env.getProperty("Login_link") + "\">Click Here</a> to Go DMS.<br>";
                                        message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                                        message += "<p>Regards,<br>LRDE-DMS Team</p>";
                                        int sendResult1 = sendMessage(email, subject, message);
                                        if (dronaMail != null) {
                                        	sendMessage1(dronaMail, subject, message);
                                        }
                                        Thread.sleep(2000);
                                        if (sendResult1 > 0) {
                                            service.updateParticularEmpMailStatus("D", "S", Long.parseLong(empId.toString()), effectivelyFinalMailTrackingId);
                                            mailSendSuccessCount.incrementAndGet();
                                        } else {
                                            service.updateParticularEmpMailStatus("D", "N", Long.parseLong(empId.toString()), effectivelyFinalMailTrackingId);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(5000); // Sleep for 5 seconds between batches (adjust as necessary)
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    service.updateMailSuccessCount(mailTrackingId, mailSendSuccessCount.get(), "D");
                }
            }else {
                service.updateNoPendingReply(effectivelyFinalMailTrackingId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(new Date() + " Inside CONTROLLER myDailyPendingScheduledMailTask " + e);
        }
    }

    @Async
    public void mySummaryDistributedMailTask() {
        logger.info(new Date() + " Inside CONTROLLER mySummaryDistributedMailTask ");
        try {
            long mailTrackingId = 0;
            long mailTrackingInsightsId = 0;
            AtomicInteger mailSendSuccessCount = new AtomicInteger(0);
            long summaryMailSendInitiation = service.getMailInitiatedCount("S");
            if (summaryMailSendInitiation == 0) {
                mailTrackingId = service.insertMailTrackInitiator("S");
            }
            final long effectivelyFinalMailTrackingId = mailTrackingId;
            List<Object[]> dailyDistributedSummaryToSendMail = service.getSummaryDistributedEmpData();
            if (mailTrackingId > 0 && dailyDistributedSummaryToSendMail != null && !dailyDistributedSummaryToSendMail.isEmpty()) {
                mailTrackingInsightsId = service.insertSummaryDistributedInsights(mailTrackingId);
                if(mailTrackingInsightsId > 0) {
                    Map<Object, EmailDto> empToDataMap = new HashMap<>();
                    for (Object[] obj : dailyDistributedSummaryToSendMail) {
                        Object empId = obj[1];
                        Object dakNo = obj[4];
                        Object source = obj[5];
                        String dueDate = null;
                        if(obj[6] != null && !obj[6].toString().trim().isEmpty()) {
                            SimpleDateFormat rdf=new SimpleDateFormat("dd-MM-yyyy");
                            dueDate = rdf.format(obj[6]);
                        }else {
                            dueDate ="NA";
                        }
                        String email = null;
                        String dronaEmail=null;
                        if(obj[3] != null && !obj[3].toString().trim().isEmpty()) {
                            email = obj[3].toString();
                        }
                        if(obj[7] != null && !obj[7].toString().trim().isEmpty()) {
                            dronaEmail = obj[7].toString();
                        }
                        if (empId != null && email != null && !email.isEmpty()) {
                            if (!empToDataMap.containsKey(empId)) {
                                empToDataMap.put(empId, new EmailDto(email,dronaEmail));
                            }
                            if (dakNo != null && !dakNo.toString().isEmpty()) {
                                empToDataMap.get(empId).addDakAndSourceAndDueDate(dakNo.toString(), source.toString(), dueDate.toString());
                            }
                        }
                    }
                    if (!empToDataMap.isEmpty()) {
                        int batchSize = 10;
                        List<Map.Entry<Object, EmailDto>> entries = new ArrayList<>(empToDataMap.entrySet());
                        for (int i = 0; i < entries.size(); i += batchSize) {
                            List<Map.Entry<Object, EmailDto>> batch = entries.subList(i, Math.min(i + batchSize, entries.size()));
                            for (Map.Entry<Object, EmailDto> mailMapData : batch) {
                                Object empId = mailMapData.getKey();
                                EmailDto emailData = mailMapData.getValue();
                                String email = emailData.getEmail();
                                String dronaMail = emailData.getDronaEmail();
                                String dakCount;
                                String word;
                                int size = emailData.getDakAndSourceAndDueDateList().size();
                                if (size > 1) {
                                    dakCount = size + " DAKs";
                                    word = "replies";
                                } else {
                                    dakCount = size + " DAK";
                                    word = "reply";
                                }
                                try {
                                    if (email != null) {
                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                                        String subject = "DMS - Distributed Summary Report - " + currentDate;
                                        String message = "<p>Dear Sir/Madam,</p>";
                                        message += "<p></p>";
                                        message += "<p>This email is to notify you that you have received " + dakCount + " today. This is for your information, please take the action.</p>";
                                        message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 650px; font-size: 16px; border-collapse: collapse;\">";
                                        message += "<thead><tr><th style=\"text-align: center; width: 400px; border: 1px solid black; padding: 5px; padding-left: 15px\">DAK No</th>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">Source</th>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">Due Date</th></tr></thead><tbody>";
                                        for (Object[] dakAndSource : emailData.getDakAndSourceAndDueDateList()) {
                                            message += "<tr><td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + dakAndSource[0] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + dakAndSource[1] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + dakAndSource[2] + "</td></tr>";
                                        }
                                        message += "</tbody></table>";
                                        message += "Please <a href=\"" + env.getProperty("Login_link") + "\">Click Here</a> to Go DMS.<br>";
                                        message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                                        message += "<p>Regards,<br>LRDE-DMS Team</p>";
                                        int sendResult1 = sendMessage(email, subject, message);
                                        if (dronaMail != null) {
                                        	sendMessage1(dronaMail, subject, message);
                                        }
                                        Thread.sleep(2000);
                                        if (sendResult1 > 0) {
                                            service.updateParticularEmpMailStatus("S", "S", Long.parseLong(empId.toString()), effectivelyFinalMailTrackingId);
                                            mailSendSuccessCount.incrementAndGet();
                                        } else {
                                            service.updateParticularEmpMailStatus("S", "N", Long.parseLong(empId.toString()), effectivelyFinalMailTrackingId);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(5000); // Sleep for 5 seconds between batches (adjust as necessary)
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    service.updateMailSuccessCount(mailTrackingId, mailSendSuccessCount.get(), "S");
                }
            } else {
                service.updateNoPendingReply(effectivelyFinalMailTrackingId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(new Date() + " Inside CONTROLLER mySummaryDistributedMailTask " + e);
        }
    }


    /*----------------------------------------------------------- DMS MAILS SEND CODE END ------------------------------------------------------------*/


    /*----------------------------------------------------------- PMS MAILS SEND CODE START ------------------------------------------------------------*/


    @Async
    public void myWeeklyScheduledMailTask() {
        logger.info(new Date() + " Inside CONTROLLER myWeeklyScheduledMailTask ");
        try {
            long mailTrackingId = 0;
            long mailTrackingInsightsId = 0;
            AtomicInteger mailSendSuccessCount = new AtomicInteger(0);
            long WeeklyMailSendInitiation = service.getMailInitiatedCount("W");
            if (WeeklyMailSendInitiation == 0) {
                mailTrackingId = service.insertMailTrackInitiator("W");
            }
            final long effectivelyFinalMailTrackingId = mailTrackingId;
            List<Object[]> pendingReplyEmpsDetailsToSendMail = service.getWeeklyPendingReplyEmpData();
            if (mailTrackingId > 0 && pendingReplyEmpsDetailsToSendMail != null && !pendingReplyEmpsDetailsToSendMail.isEmpty()) {
                mailTrackingInsightsId = service.insertWeeklyPendingInsights(mailTrackingId);
                if (mailTrackingInsightsId>0) {
                    Map<Object, EmailDto> empToDataMap = new HashMap<>();
                    for (Object[] obj : pendingReplyEmpsDetailsToSendMail) {
                        Object empId = obj[1];
                        Object dakNo = obj[4];
                        Object source = obj[5];
                        String dueDate = null;
                        if(obj[6] != null && !obj[6].toString().trim().isEmpty()) {
                            SimpleDateFormat rdf=new SimpleDateFormat("dd-MM-yyyy");
                            dueDate = rdf.format(obj[6]);
                        }else {
                            dueDate ="--";
                        }
                        String email = null;
                        String dronaEmail=null;
                        if(obj[3] != null && !obj[3].toString().trim().isEmpty()) {
                            email = obj[3].toString();
                        }
                        if(obj[7] != null && !obj[7].toString().trim().isEmpty()) {
                            dronaEmail = obj[7].toString();
                        }
                        if (empId != null && email != null && !email.isEmpty()) {
                            if (!empToDataMap.containsKey(empId)) {
                                empToDataMap.put(empId, new EmailDto(email,dronaEmail));
                            }
                            if (dakNo != null && !dakNo.toString().isEmpty()) {
                                empToDataMap.get(empId).addDakAndSourceAndDueDate(dakNo.toString(), source.toString(),dueDate.toString());
                            }
                        }
                    }
                    if (!empToDataMap.isEmpty()) {
                        int batchSize = 10;
                        List<Map.Entry<Object, EmailDto>> entries = new ArrayList<>(empToDataMap.entrySet());
                        for (int i = 0; i < entries.size(); i += batchSize) {
                            List<Map.Entry<Object, EmailDto>> batch = entries.subList(i, Math.min(i + batchSize, entries.size()));
                            for (Map.Entry<Object, EmailDto> mailMapData : batch) {
                                Object empId = mailMapData.getKey();
                                EmailDto emailData = mailMapData.getValue();
                                String email = emailData.getEmail();
                                String dronaMail = emailData.getDronaEmail();
                                String dakCount;
                                String word;
                                int size = emailData.getDakAndSourceAndDueDateList().size();
                                if (size > 1) {
                                    dakCount = size + " DAKs";
                                    word = "replies";
                                } else {
                                    dakCount = size + " DAK";
                                    word = "reply";
                                }
                                try {
                                    if (email != null) {
                                        String subject = "DMS - Weekly Pending Replies Report from " + DateTimeFormatUtil.getCurrentWeekMonday() + " to " + DateTimeFormatUtil.getCurrentWeekSunday();
                                        String message = "<p>Dear Sir/Madam,</p>";
                                        message += "<p></p>";
                                        message += "<p>This email is to inform you that you have " + dakCount + " within due date (" + DateTimeFormatUtil.getCurrentWeekMonday() + " - " + DateTimeFormatUtil.getCurrentWeekSunday() + "), awaiting your " + word + " to ensure timely completion.</p>";
                                        message += "<p>This is for your information, please take the action.</p>";
                                        message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 650px; font-size: 16px; border-collapse: collapse;\">";
                                        message += "<thead><tr><th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px\">DAK No</th>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">Source</th>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">Due Date</th></tr></thead><tbody>";
                                        for (Object[] dakAndSource : emailData.getDakAndSourceAndDueDateList()) {
                                            message += "<tr><td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + dakAndSource[0] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + dakAndSource[1] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + dakAndSource[2] + "</td></tr>";
                                        }
                                        message += "</tbody></table>";
                                        message += "Please <a href=\"" + env.getProperty("Login_link") + "\">Click Here</a> to Go DMS.<br>";
                                        message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                                        message += "<p>Regards,<br>LRDE-DMS Team</p>";
                                        int sendResult1 = sendMessage(email, subject, message);
                                         if (dronaMail != null) {
                                           sendMessage1(dronaMail, subject, message);
                                         }
                                        Thread.sleep(2000);
                                        if (sendResult1 > 0) {
                                            service.updateParticularEmpMailStatus("W", "S", Long.parseLong(empId.toString()), effectivelyFinalMailTrackingId);
                                            mailSendSuccessCount.incrementAndGet();
                                        } else {
                                            service.updateParticularEmpMailStatus("W", "N", Long.parseLong(empId.toString()), effectivelyFinalMailTrackingId);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    service.updateMailSuccessCount(mailTrackingId, mailSendSuccessCount.get(), "W");
                }
            } else {
                service.updateNoPendingReply(effectivelyFinalMailTrackingId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(new Date() + " Inside CONTROLLER myWeeklyScheduledMailTask " + e);
        }
    }


    @Async
    public void sentWeeklyActionMail(int numberOfDays,String trackingType) {
        try {
            int finalresult=0;
            long mailTrackingId = 0;
            mailTrackingId = service.insertPmsMailTrackInitiator(trackingType,numberOfDays);
            List<Object[]>weeklyActionList = service.weeklyActionList(numberOfDays);
            List<Object[]>weeklySubActionList = new ArrayList<>();
            if(weeklyActionList.size()>0) {
                while(weeklyActionList.size()!=0) {
                    StringBuilder objBuilder = new StringBuilder();
                    String empid = weeklyActionList.get(0)[3].toString();
                    weeklySubActionList = weeklyActionList.stream().filter(e -> e[3].toString().equalsIgnoreCase(empid))
                            .collect(Collectors.toList());
                    String message="Sir/Madam ,<br><p>&emsp;&emsp;This is to inform you that you have some actions to be completed .</p><table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; font-size: 16px; border-collapse:collapse;\" >";
                    message= message+"<tr style=\"font-size:12px;\"><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:5%;\">SN</td><td style=\"border: 1px solid black; padding: 5px;text-align: center; width:30%\">Action No.</td><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:30%;\"> Assignor</td><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:15%\">Progress</td><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:15%\">PDC</td></tr>";
                    int count=0;
                    for (Object[]obj:weeklySubActionList) {
                        if (objBuilder.length() > 0) {
                            objBuilder.append(",");
                        }
                        objBuilder.append(obj[0].toString());
                        message=message+"<tr style=\"font-size:12px;\"><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:5%;\">"+(++count)+"</td><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:30%;font-weight:600\">"+obj[0].toString() +"</td><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:30%;\">"+ obj[1].toString()+"</td><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:15%\">"+obj[4].toString()+"</td><td style=\"border: 1px solid black; padding: 5px;text-align: center;width:15%\">"+obj[5].toString()+",<br>"+LocalDate.parse(obj[5].toString()).getDayOfWeek()  +"</td></tr>";
                    }
                    message=message
                            +"</table><p style=\"font-weight:bold;font-size:13px;\">[Note:This is an autogenerated e-mail.Reply to this will not be attended please.]</p>"
                            +"<p>Regards</p>"
                            +"<p>PMS Team"+"</p>"
                    ;
                    String email=weeklySubActionList.get(0)[2].toString();
                    String dronaemail=weeklySubActionList.get(0)[6].toString();
                    String subject="";
                    if(numberOfDays==0) {
                        subject = "PMS - Actions PDC Today";
                    }
                    if(numberOfDays==7) {
                        subject = "PMS - Actions PDC This Week";
                    }
                    if(email!=null) {
                        sendMessage(email, subject, message);
                    }
                    if(dronaemail!=null) {
                    int dronaMailSentCount=sendMessage1(dronaemail, subject, message);
                    }
                    String actionNos = objBuilder.toString();
                    service.insertPfmsMailTrackingInsights(mailTrackingId,Long.parseLong(empid),actionNos,trackingType,"S",sdf1.format(new Date()));
                    weeklyActionList = weeklyActionList.stream().filter(e -> !e[3].toString().equalsIgnoreCase(empid)).collect(Collectors.toList());
                    finalresult++;
                }
                service.updateEmailTrackingExpectedCount(mailTrackingId,finalresult,sdf1.format(new Date()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Async
    public void sendMeetingEmails() {

        String date = LocalDate.now().plusDays(1).toString();
        try {
            int finalresult=0;
            long mailTrackingId = service.insertPmsMailTrackInitiatorForMeetingMail("M",date,LabCode);
            List<MeetingMailDto> meetingMailDtoData=new ArrayList<>();
            List<String> memberTypes=Arrays.asList("CC","CS","PS","CI","P","I");
            List<Object[]> todayMeetings=service.getTodaysMeetings(date);
            List<Object[]> empAttendance = new ArrayList<>();
            if(!todayMeetings.isEmpty()) {
                for(Object[]obj:todayMeetings) {
                    empAttendance= service.committeeAttendance(obj[0].toString()).stream()
                            .filter(e ->memberTypes.contains(e[3].toString())).collect(Collectors.toList());

                    for(Object[]obj1:empAttendance) {
                        MeetingMailDto m = new MeetingMailDto();
                        m.setEmpid(obj1[0].toString());
                        m.setEmpname(obj1[6].toString());
                        m.setEmail(obj1[8].toString());
                        m.setScheduleid(obj[0].toString());
                        m.setProjectid(obj[1].toString());
                        m.setInitiationId(obj[2].toString());
                        m.setCommitteeShortName(obj[3].toString());
                        m.setCommitteeName(obj[4].toString());
                        m.setMeetingTime(obj[6].toString());
                        m.setMeetingVenue(obj[5].toString());
                        m.setProjectname(obj[8].toString());
                        m.setProjectCode(obj[8].toString());
                        m.setDronaEmail(obj1[13].toString());
                        meetingMailDtoData.add(m);
                    }
                }
                List<MeetingMailDto> meetingMailDtoSubData = new ArrayList<>();
                while(!meetingMailDtoData.isEmpty()) {
                    StringBuilder objBuilder = new StringBuilder();
                    String empId=meetingMailDtoData.get(0).getEmpid();
                    String message="Sir/Madam<br><p>&emsp;&emsp;This is to inform you that you have "+meetingMailDtoSubData.size()+" meetings scheduled tomorrow.</p><table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 650px; font-size: 16px; border-collapse:collapse;\" >";
                    meetingMailDtoSubData = meetingMailDtoData.stream().filter(e -> e.getEmpid().equalsIgnoreCase(empId)).collect(Collectors.toList());
                    for(MeetingMailDto m : meetingMailDtoSubData) {
                        if (!objBuilder.isEmpty()) {
                            objBuilder.append(",");
                        }
                        objBuilder.append(m.getProjectCode()+"-"+m.getCommitteeShortName());
                        message=message+"<tr><th colspan=\"2\" style=\"text-align: left; font-weight: 700; width: 650px;border: 1px solid black; padding: 5px; padding-left: 15px\">Meeting Details </th></tr>"
                                +"<tr>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\"> Project : </td>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\"><b style=\"color:#0D47A1\">" + m.getProjectCode()+ "( "+m.getProjectname()+" )"  + "</b></td></tr>"
                                +"<tr>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\"> Meeting : </td>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\">" + m.getCommitteeShortName()  + "</td></tr>"
                                +"<tr><td style=\"border: 1px solid black; padding: 5px;text-align: left\"> Date :  </td>"
                                +"<td style=\"border: 1px solid black; padding: 5px;text-align: left\">" + LocalDate.now().plusDays(1).toString()+","+LocalDate.parse(LocalDate.now().plusDays(1).toString()).getDayOfWeek()+"</td></tr>"
                                +"<tr>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\"> Time : </td>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\">" + m.getMeetingTime()  + "</td></tr>"
                                +"<tr>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\"> Venue : </td>"
                                + "<td style=\"border: 1px solid black; padding: 5px;text-align: left\">" + m.getMeetingVenue()+ "</td></tr>";
                    }
                    message=message
                            +"</table><p style=\"font-weight:bold;font-size:13px;\">[Note:This is an autogenerated e-mail.Reply to this will not be attended please.]</p>"
                            +"<p>Regards</p>"
                            +"<p>PMS Team"+"</p>";

                    String email = meetingMailDtoSubData.get(0).getEmail();
                    String dronaEmail = meetingMailDtoSubData.get(0).getDronaEmail();
                    String subject = "Tomorrow's Schedule Meetings";
                    if(email!=null) {
                        sendMessage(email,subject,message);
                    }
                     if(dronaEmail!=null) {
                     long dronaMailSent=sendMessage1(dronaEmail,subject,message);
                     }
                    String projectShortNames = objBuilder.toString();
                    service.insertPfmsMailTrackingInsights(mailTrackingId,Long.parseLong(empId),projectShortNames,"M","S",sdf1.format(new Date()));
                    meetingMailDtoData=meetingMailDtoData.stream().filter( e -> !e.getEmpid().equalsIgnoreCase(empId)).collect(Collectors.toList());
                    finalresult++;
                }
                service.updateEmailTrackingExpectedCount(mailTrackingId,finalresult,sdf1.format(new Date()));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*----------------------------------------------------------- PMS MAILS SEND CODE END ------------------------------------------------------------*/



    /*----------------------------------------------------------- PMS PROJECT HEALTH AUTO UPDATE CODE START ------------------------------------------------------------*/

    @Scheduled(cron = "${ProjectHoaTime}")
    public void projectHoaUpdate() {
        logger.info(new Date() + " Inside CONTROLLER projectHoaUpdate ");
        try {
            final String localUri1=uri+"/pfms_serv/tblprojectdata?labcode="+LabCode;
            final String localUri2=uri+"/pfms_serv/pfms-finance-changes?projectCode=A&interval=M";
            final String localUri3=uri+"/pfms_serv/pfms-finance-changes?projectCode=A&interval=W";
            final String localUri4=uri+"/pfms_serv/pfms-finance-changes?projectCode=A&interval=T";
            final String localUri5=uri+"/pfms_serv/labdetails";
//	      	final String CCMDataURI=uri+"/pfms_serv/getCCMViewData";
            String monthlyData=null;
            String weeklyData=null;
            String todayData=null;
            String hoaJsonData=null;
            String labData= null;
            List<CCMView> cCMViewData=null;
            long count= 0L;
            long ibasserveron=0L;
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                headers.set("labcode", LabCode);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response1=restTemplate.exchange(localUri1, HttpMethod.POST, entity, String.class);
                hoaJsonData=response1.getBody();
                ResponseEntity<String> monthlyresponse=restTemplate.exchange(localUri2, HttpMethod.POST, entity, String.class);
                ResponseEntity<String> weeklyresponse=restTemplate.exchange(localUri3, HttpMethod.POST, entity, String.class);
                ResponseEntity<String> todayresponse=restTemplate.exchange(localUri4, HttpMethod.POST, entity, String.class);
                ResponseEntity<String> labdata=restTemplate.exchange(localUri5, HttpMethod.POST, entity, String.class);
                monthlyData=monthlyresponse.getBody();
                weeklyData=weeklyresponse.getBody();
                todayData=todayresponse.getBody();
                labData=labdata.getBody();
                cCMViewData= PFMSServ.getCCMViewData(LabCode);
            }catch(HttpClientErrorException | ResourceAccessException e){
                logger.error(new Date() +" Inside ProjectHoaUpdate.htm pfms_serv Not Responding.htm "+ e);
                e.printStackTrace();
                ibasserveron = 1;
            }catch(Exception e){
                logger.error(new Date() +" Inside ProjectHoaUpdate.htm "+ e);
                e.printStackTrace();
            }
            ObjectMapper mao = new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            List<ProjectHoa> projectDetails1=null;
            List<FinanceChanges> FinanceDetailsMonthly=null;
            List<FinanceChanges> FinanceDetailsWeekly=null;
            List<FinanceChanges> FinanceDetailsToday=null;
            List<IbasLabMaster> LabDetails=null;
            if(hoaJsonData!=null) {
                try {
                    projectDetails1 = mao.readValue(hoaJsonData, mao.getTypeFactory().constructCollectionType(List.class, ProjectHoa.class));
                    LabDetails = mao.readValue(labData, mao.getTypeFactory().constructCollectionType(List.class, IbasLabMaster.class));
                    count = service.projectHoaUpdate(projectDetails1,"admin",LabDetails);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            if(monthlyData!=null) {
                try {
                    FinanceDetailsMonthly = mao.readValue(monthlyData, mao.getTypeFactory().constructCollectionType(List.class, FinanceChanges.class));
                    FinanceDetailsWeekly = mao.readValue(weeklyData, mao.getTypeFactory().constructCollectionType(List.class, FinanceChanges.class));
                    FinanceDetailsToday = mao.readValue(todayData, mao.getTypeFactory().constructCollectionType(List.class, FinanceChanges.class));
                    service.projectFinanceChangesUpdate(FinanceDetailsMonthly,FinanceDetailsWeekly,FinanceDetailsToday,"admin",LabCode);
                } catch (JsonProcessingException e) {
                    logger.error(new Date() +" Inside ProjectHoaUpdate.htm "+ e);
                    e.printStackTrace();
                }
            }
            if(cCMViewData!=null && !cCMViewData.isEmpty())
            {
                Object getClusterId=service.getClusterId(LabCode);
                String ClusterId=getClusterId.toString();
                service.cCMViewDataUpdate(cCMViewData, LabCode, ClusterId, "admin", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(new Date() + " Inside CONTROLLER ProjectHoaUpdate " + e);
        }
    }


    @Scheduled(cron = "${ProjectHealthTime}")
    public void projectHealthUpdate() {
        logger.info(new Date() + " Inside CONTROLLER projectHoaUpdate ");

        try {
            service.projectHealthUpdate(LabCode,"admin");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(new Date() + " Inside CONTROLLER projectHoaUpdate " + e);
        }
    }

    /*----------------------------------------------------------- PMS PROJECT HEALTH AUTO UPDATE CODE END ------------------------------------------------------------*/



    /*----------------------------------------------------------- IBAS MAILS SEND CODE START ------------------------------------------------------------*/


    @Scheduled(cron = "${OrderDetailsMailTime}")
    public void weeklyCommitmentDpMail() {
        logger.info(new Date() + " Inside dailyCommitmentDpExpiredMail");
        try {
            long Status=service.sendEmailAndSmsForSupplyOrderDetails();
        }catch (Exception e){
            e.printStackTrace();
            logger.error(new Date() + " Inside dailyCommitmentDpExpiredMail" + e);
        }
    }


    /*----------------------------------------------------------- IBAS MAILS SEND CODE END ------------------------------------------------------------*/


    /*----------------------------------------------------------- PFTS MAILS SEND CODE START ------------------------------------------------------------*/


    @Scheduled(cron = "${PftsTime1}")
    public void pftsDailySummaryReport() throws Exception {
        try {
            pftsDailySummaryMailTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Async
    public void pftsDailySummaryMailTask() {
        logger.info(new Date() + " Inside CONTROLLER pftsDailySummaryMailTask ");
        try {
            long pftsMailTrackingId = 0;
            long pftsMailTrackingInsightsId = 0;
            AtomicInteger mailSendSuccessCount = new AtomicInteger(0);
            List<Object[]>  pftsDailySummaryList = service.pftsExpectedDailySummaryList();
            long pftsDailySummaryMailSendInitiation = service.getPftsMailInitiatedCount("S");
            if  (pftsDailySummaryMailSendInitiation == 0) {
                pftsMailTrackingId = service.insertPftsMailTrackInitiator("S");
            }
            final long effectivelyFinalMailTrackingId = pftsMailTrackingId;
            if (pftsMailTrackingId > 0 && pftsDailySummaryList != null && !pftsDailySummaryList.isEmpty()) {
                pftsMailTrackingInsightsId = service.insertPftsDailySummaryInsights(pftsMailTrackingId);
                if(pftsMailTrackingInsightsId > 0) {
                    Map<Object, PftsEmailDto> empToDataMap = new HashMap<>();
                    for (Object[] obj : pftsDailySummaryList) {
                        Object empNo = obj[8];
                        Object demandNo = obj[0];
                        Object eventName = obj[5];
                        String forwardeByName = obj[7]!=null ? obj[7].toString(): "";
                        String email = null;
                        String dronaEmail=null;
                        if(obj[10] != null && !obj[10].toString().trim().isEmpty()) {
                            email = obj[10].toString();
                        }
                        if(obj[11] != null && !obj[11].toString().trim().isEmpty()) {
                            dronaEmail = obj[11].toString();
                        }
                        if (empNo != null && email != null && !email.isEmpty()) {
                            if (!empToDataMap.containsKey(empNo)) {
                                empToDataMap.put(empNo, new PftsEmailDto(email,dronaEmail));
                            }
                            if (demandNo != null && !demandNo.toString().isEmpty()) {
                                empToDataMap.get(empNo).addDemandAndEventAndForwardeBy(demandNo.toString(), eventName.toString(), forwardeByName.toString());
                            }
                        }
                    }
                    if (!empToDataMap.isEmpty()) {
                        int batchSize = 10;
                        List<Map.Entry<Object, PftsEmailDto>> entries = new ArrayList<>(empToDataMap.entrySet());
                        for (int i = 0; i < entries.size(); i += batchSize) {
                            List<Map.Entry<Object, PftsEmailDto>> batch = entries.subList(i, Math.min(i + batchSize, entries.size()));
                            for (Map.Entry<Object, PftsEmailDto> mailMapData : batch) {
                                Object empNoUnique = mailMapData.getKey();
                                PftsEmailDto emailData = mailMapData.getValue();
                                String email = emailData.getEmail();
                                String dronaEmail = emailData.getDronaEmail();
                                String demandReceivedCount;
                                int size = emailData.getDemandAndEventAndForwardeByList().size();
                                if (size > 1) {
                                    demandReceivedCount = size + " Files";
                                } else {
                                    demandReceivedCount = size + " File";
                                }
                                try {
                                    if (email != null) {
                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                                        String subject = "PFTS - Daily Summary Report - " + currentDate;
                                        String message = "<p>Dear Sir/Madam,</p>";
                                        message += "<p></p>";
                                        message += "<p>This email is to notify you that you have received " + demandReceivedCount + " today. This is for your information, please take action for received files.</p>";
                                        message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 650px; font-size: 16px; border-collapse: collapse;\">";
                                        message += "<thead>";
                                        message += "<tr>";
                                        message += "<th style=\"text-align: center; width: 400px; border: 1px solid black; padding: 5px; padding-left: 15px\">Demand No</th>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">Current Status</th>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">Forwarded By</th>";
                                        message += "</tr>";
                                        message += "</thead>";
                                        message += "<tbody>";
                                        for (Object[] obj : emailData.getDemandAndEventAndForwardeByList()) {
                                            message += "<tr>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + obj[0] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + obj[1] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + obj[2] + "</td>";
                                            message += "</tr>";
                                        }
                                        message += "</tbody>";
                                        message += "</table>";
                                        message += "Please <a href=\"" + env.getProperty("Pfts_Login_link") + "\">Click Here</a> to Go PFTS.<br>";
                                        message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                                        message += "<p>Regards,<br>LRDE-PFTS Team</p>";
                                        int sendResult1 = sendMessage(email, subject, message);
                                         if (dronaEmail != null) {
                                           sendMessage1(dronaEmail, subject, message);
                                         }
                                        Thread.sleep(2000);
                                        if (sendResult1 > 0) {
                                            service.updateParticularEmpMailStatusInPfts("S", "S", empNoUnique.toString(), effectivelyFinalMailTrackingId);
                                            mailSendSuccessCount.incrementAndGet();
                                        } else {
                                            service.updateParticularEmpMailStatusInPfts("S", "N", empNoUnique.toString(), effectivelyFinalMailTrackingId);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    service.updateMailSuccessCountInPfts(pftsMailTrackingId, mailSendSuccessCount.get(), "S");
                }
            } else {
                service.updatePftsNoPendingReply(effectivelyFinalMailTrackingId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(new Date() + " Inside CONTROLLER pftsDailySummaryMailTask " + e);
        }
    }

    @Scheduled(cron="${pftsDailySendMail}")
    public void pftsDailySendMail() throws Exception{
        logger.info(new Date() + " Inside pftsDailySendMail.htm");
        try {
            long pftsMailTrackingId = 0;
            long pftsMailTrackingInsightsId = 0;
            AtomicInteger mailSendSuccessCount = new AtomicInteger(0);
            List<Object[]> PftsDailySendEmployees=service.pftsDailySendEmployees();
            long pftsDailySummaryMailSendInitiation = service.getPftsMailInitiatedCount("D");
            if(pftsDailySummaryMailSendInitiation == 0) {
                pftsMailTrackingId = service.insertPftsDailyMailTrackInitiator("D");
            }
            final long effectivelyFinalMailTrackingId = pftsMailTrackingId;
            if (pftsMailTrackingId > 0 && PftsDailySendEmployees != null && !PftsDailySendEmployees.isEmpty()) {
                pftsMailTrackingInsightsId = service.insertPftsDailyInsights(pftsMailTrackingId);
                if(pftsMailTrackingInsightsId > 0) {
                    if (!PftsDailySendEmployees.isEmpty()) {
                        int batchSize = 10;
                        List<Object[]> employees = new ArrayList<>(PftsDailySendEmployees);
                        for (int i = 0; i < employees.size(); i += batchSize) {
                            List<Object[]> batch = employees.subList(i, Math.min(i + batchSize, employees.size()));
                            for (Object[] obj : batch) {
                                List<Object[]> PftsDailySendEmployeesDetails = service.pftsDailySendEmployeesDetails(obj[0].toString());
                                int count = 1;
                                String Email = obj[2] != null ? obj[2].toString() : null;
                                String dronaEmail = obj[3] != null ? obj[3].toString() : null;
                                try {
                                    if (Email != null) {
                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                                        String subject = "PFTS - File Received - " + currentDate;
                                        String message = "<p>Dear Sir/Madam,</p>";
                                        message += "<p>This email is to inform you that the following files have been forwarded to you today:</p>";
                                        message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 650px; font-size: 16px; border-collapse: collapse;\">";
                                        message += "<thead>";
                                        message += "<tr>";
                                        message += "<th style=\"text-align: center; width: 200px; border: 1px solid black; padding: 5px; padding-left: 15px\">SN</th>";
                                        message += "<th style=\"text-align: center; width: 400px; border: 1px solid black; padding: 5px; padding-left: 15px\">Demand No</th>";
                                        message += "<th style=\"text-align: center; width: 400px; border: 1px solid black; padding: 5px; padding-left: 15px\">Date</th>";
                                        message += "<th style=\"text-align: center; width: 800px; border: 1px solid black; padding: 5px; padding-left: 15px\">Item For</th>";
                                        message += "<th style=\"text-align: center; width: 400px; border: 1px solid black; padding: 5px; padding-left: 15px\">Status</th>";
                                        message += "<th style=\"text-align: center; width: 800px; border: 1px solid black; padding: 5px; padding-left: 15px\">Forwarded By</th>";
                                        message += "</tr>";
                                        message += "</thead>";
                                        message += "<tbody>";
                                        for (Object[] obj1 : PftsDailySendEmployeesDetails) {
                                            message += "<tr>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + (count++) + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + obj1[0] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + rdf.format(sdf2.parse(obj1[3].toString())) + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + obj1[8] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + obj1[2] + "</td>";
                                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + obj1[9] + ", " + obj1[10] + "</td>";
                                            message += "</tr>";
                                        }
                                        message += "</tbody>";
                                        message += "</table>";
                                        message += "<p>Please <a style=\"color:blue;\" href=\"" + env.getProperty("Pfts_Login_link") + "\">Click Here</a> to Go PFTS.</p>";
                                        message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                                        message += "<p>Regards,<br>LRDE-PFTS Team</p>";
                                        int sendResult1 = sendMessage(Email, subject, message);
                                        if (dronaEmail != null) {
                                        	 sendMessage1(dronaEmail, subject, message);
                                         }
                                        Thread.sleep(2000);
                                        if (sendResult1 > 0) {
                                            service.updateParticularEmpMailStatusInPfts("D", "S", obj[0].toString(), effectivelyFinalMailTrackingId);
                                            mailSendSuccessCount.incrementAndGet(); // Increment success count atomically
                                        } else {
                                            service.updateParticularEmpMailStatusInPfts("D", "N", obj[0].toString(), effectivelyFinalMailTrackingId);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    service.updateMailSuccessCountInPfts(pftsMailTrackingId, mailSendSuccessCount.get(), "D");
                }
            }else {
                service.updatePftsNoPendingReply(effectivelyFinalMailTrackingId);
            }
        }	 catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------- PFTS MAILS SEND CODE END ------------------------------------------------------------*/



    /*----------------------------------------------------------- QMS MAILS SEND CODE START ------------------------------------------------------------*/


    //@Scheduled(cron ="${QmsMailTime}")
    public void sendScheduleMailOfIrf() throws Exception {
         service.sendScheduleMailOfIrf(LabCode);
     }

    /*----------------------------------------------------------- QMS MAILS SEND CODE END ------------------------------------------------------------*/



    /*----------------------------------------------------------- SIS MAILS SEND CODE START ------------------------------------------------------------*/


    @Scheduled(cron ="${sisWeeklyMailTime}")
    public void sendWeeklyPendingItems() throws Exception {
        service.RINCRVPendingList();
    }


    /*----------------------------------------------------------- SIS MAILS SEND CODE END ------------------------------------------------------------*/


    public int sendMessage(String toEmail, String subject, String msg)  {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", Port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
            }
        });

        int mailSendresult = 0;
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(msg);
            message.setContent(msg, "text/html");

            jakarta.mail.Transport.send(message);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mailSendresult++;
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
        return mailSendresult;
    }


    	public int sendMessage1(String toEmail, String subject, String msg)  {
			String typeOfHost = "D";
			MailConfigurationDto mailAuthentication;
			try {
				mailAuthentication = service.getMailConfigByTypeOfHost(typeOfHost);
			} catch (Exception e1) {
				e1.printStackTrace();
				return (Integer) null;
			}
			  if (mailAuthentication == null) {
			  return-3;
			  }else {
				 JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
				 mailSender.setHost(mailAuthentication.getHost().toString());
				 mailSender.setPort(Integer.parseInt(mailAuthentication.getPort().toString()));
				 mailSender.setUsername(mailAuthentication.getUsername().toString());
				 mailSender.setPassword(mailAuthentication.getPassword().toString());

				 Properties properties = System.getProperties();
				 properties.setProperty("mail.smtp.host", mailSender.getHost());
				 properties.put("mail.smtp.starttls.enable", "true");
				 // SSL Port
				 properties.put("mail.smtp.port", mailSender.getPort());
				 // enable authentication
				 properties.put("mail.smtp.auth", "true");
				 // SSL Factory
				 //properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				 //properties.put("mail.smtp.starttls.enable", "true");

                  Session session = Session.getInstance(properties, new Authenticator() {
                      protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                          return new jakarta.mail.PasswordAuthentication(mailSender.getUsername(), mailSender.getPassword());
                      }
                  });
				 int mailSendresult = 0;
				 try {
					 MimeMessage message = new MimeMessage(session);
					 message.setFrom(new InternetAddress(Dronausername));
					 message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
					 message.setSubject(subject);
					 message.setText(msg);
					 message.setContent(msg, "text/html");
					 mailSender.send(message);
					 mailSendresult++;
				 } catch (MessagingException mex) {
					 mex.printStackTrace();
				 }
				 return mailSendresult;
			  }
		}
}
