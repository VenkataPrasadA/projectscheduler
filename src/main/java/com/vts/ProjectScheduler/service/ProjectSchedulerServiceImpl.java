package com.vts.ProjectScheduler.service;

import com.vts.ProjectScheduler.controller.ProjectSchedulerController;
import com.vts.ProjectScheduler.dto.MailConfigurationDto;
import com.vts.ProjectScheduler.entity.dms.DakMailTracking;
import com.vts.ProjectScheduler.entity.dms.DakMailTrackingInsights;
import com.vts.ProjectScheduler.entity.ibas.*;
import com.vts.ProjectScheduler.entity.pms.*;
import com.vts.ProjectScheduler.entity.qms.IrfMailTrack;
import com.vts.ProjectScheduler.entity.qms.IrfMailTrackInsights;
import com.vts.ProjectScheduler.entity.sis.SisMailTrack;
import com.vts.ProjectScheduler.entity.sis.SisMailTrackInsights;
import com.vts.ProjectScheduler.repository.dms.DakMailTrackInsightsRepository;
import com.vts.ProjectScheduler.repository.dms.DakMailTrackRepository;
import com.vts.ProjectScheduler.repository.ibas.*;
import com.vts.ProjectScheduler.repository.pms.*;
import com.vts.ProjectScheduler.repository.qms.IrfMailTrackInsightsRepository;
import com.vts.ProjectScheduler.repository.qms.IrfMailTrackRepository;
import com.vts.ProjectScheduler.repository.sis.SIRRepository;
import com.vts.ProjectScheduler.repository.sis.SisMailTrackInsightsRepository;
import com.vts.ProjectScheduler.repository.sis.SisMailTrackRepository;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProjectSchedulerServiceImpl implements ProjectSchedulerService{

    private  SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private  SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd");
    private  SimpleDateFormat rdf=new SimpleDateFormat("dd-MM-yyyy");

    ReversibleEncryptionAlg rea = new ReversibleEncryptionAlg();

    @Value("${LabCode}")
    private String LabCode;

    @Autowired
    private Environment env;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    @Lazy
    ProjectSchedulerController controller;

    @Autowired
    SubCategoryRepository subCategoryRepository;

    @Autowired
    MailConfigurationRepository mailConfigurationRepository;

    @Autowired
    private DakMailTrackRepository dakMailTrackRepository;

    @Autowired
    private DakMailTrackInsightsRepository dakMailTrackInsightsRepository;

    @Autowired
    private PftsMailTrackRepository pftsMailTrackRepository;

    @Autowired
    private PftsMailTrackInsightsRepository pftsMailTrackInsightsRepository;


    @Autowired
    private IrfMailTrackRepository irfMailTrackRepository;

    @Autowired
    private IrfMailTrackInsightsRepository irfMailTrackInsightsRepository;

    @Autowired
    private ProjectHoaRepository projectHoaRepository;

    @Autowired
    private ProjectHoaChangesRepository projectHoaChangesRepository;

    @Autowired
    private ProjectHealthRepository projectHealthRepository;

    @Autowired
    private CcmViewRepository ccmViewRepository;

    @Autowired
    private PmsMailTrackRepository pmsMailTrackRepository;

    @Autowired
    private PmsMailTrackInsightsRepository pmsMailTrackInsightsRepository;

    @Autowired
    private SupplyOrderMailTrackRepository supplyOrderMailTrackRepository;

    @Autowired
    private SupplyOrderEmployeeMailTrackRepository supplyOrderEmployeeMailTrackRepository;

    @Autowired
    private SIRRepository sirRepository;

    @Autowired
    private SisMailTrackRepository sisMailTrackRepository;

    @Autowired
    private SisMailTrackInsightsRepository sisMailTrackInsightsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public MailConfigurationDto getMailConfigByTypeOfHost(String type) throws Exception {
        List<Object[]> mailPropertiesByTypeOfHost = mailConfigurationRepository.getMailPropertiesByTypeOfHost(type);
        if (mailPropertiesByTypeOfHost != null && !mailPropertiesByTypeOfHost.isEmpty()) {
            Object[] obj = mailPropertiesByTypeOfHost.get(0); // Assuming you only expect one result
            if (obj[2] != null && obj[3] != null && obj[4] != null && obj[5] != null) {
                MailConfigurationDto dto = new MailConfigurationDto();
                dto.setHost(obj[2].toString());
                dto.setPort(obj[3].toString());
                dto.setUsername(obj[4].toString());
                String decryptedPassword = rea.decryptByAesAlg(obj[5].toString());
                dto.setPassword(decryptedPassword);
                return dto;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public long getMailInitiatedCount(String trackingType) throws Exception {
        return dakMailTrackRepository.getMailInitiatedCount(trackingType);
    }


    @Override
    public long insertMailTrackInitiator(String trackingType) throws Exception {
        long rowAddResult = 0;
        DakMailTracking Model  = new DakMailTracking();
        Model.setTrackingType(trackingType);
        if(trackingType!=null && trackingType.equalsIgnoreCase("D")) {
            long dailyPendingCount = dakMailTrackRepository.getDailyExpectedPendingReplyCount();
            Model.setMailExpectedCount(dailyPendingCount);
        }else if(trackingType!=null && trackingType.equalsIgnoreCase("W")) {
            long weeklyPendingCount = 	dakMailTrackRepository.getWeeklyExpectedPendingReplyCount();
            Model.setMailExpectedCount(weeklyPendingCount);

        }else if(trackingType!=null && trackingType.equalsIgnoreCase("S")) {
            long summaryDistributedCount = 	dakMailTrackRepository.getSummaryOfDailyDistributedCount();
            Model.setMailExpectedCount(summaryDistributedCount);
        }
        Model.setMailSentCount(Long.valueOf(0));
        Model.setMailSentStatus("N");
        Model.setCreatedDate(LocalDateTime.now());
        Model.setCreatedTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        rowAddResult = dakMailTrackRepository.save(Model).getMailTrackingId();
        return rowAddResult;
    }


    @Override
    public List<Object[]> getDailyPendingReplyEmpData() throws Exception {
        return dakMailTrackRepository.getDailyPendingReplyEmpData();
    }

    @Override
    public long insertDailyPendingInsights(long mailTrackingId) throws Exception {
        long TrackingInsightsResult = 0;

        List<Object[]> pendingReplyEmpsDetailstoSendMail = dakMailTrackRepository.getDailyPendingReplyEmpData();
        
        if (pendingReplyEmpsDetailstoSendMail != null && pendingReplyEmpsDetailstoSendMail.size() > 0) {
            Map<Integer, Set<String>> empDakNosMap = new HashMap();

            List<DakMailTrackingInsights> insightsList = new ArrayList<>();

            for (Object[] rowData : pendingReplyEmpsDetailstoSendMail) {
                int empId = Integer.parseInt(rowData[1].toString());
                String dakNo = rowData[2].toString();

                if (!empDakNosMap.containsKey(empId)) {
                    empDakNosMap.put(empId, new HashSet<>());
                }
                empDakNosMap.get(empId).add(dakNo);
            }
            for (Map.Entry<Integer, Set<String>> entry : empDakNosMap.entrySet()) {
                int empId = entry.getKey();
                Set<String> dakNosSet = entry.getValue();
                DakMailTrackingInsights Insights = new DakMailTrackingInsights();
                Insights.setMailTrackingId(mailTrackingId);
                Insights.setMailPurpose("D");
                Insights.setMailStatus("N");
                Insights.setCreatedDate(LocalDateTime.now());
                Insights.setEmpId(Long.parseLong(String.valueOf(empId)));
                Insights.setDakNos(String.join(",", dakNosSet));
                insightsList.add(Insights);

            }
            if (!insightsList.isEmpty()) {
                List<DakMailTrackingInsights> savedInsights = dakMailTrackInsightsRepository.saveAll(insightsList);
                if (!savedInsights.isEmpty()) {
                    TrackingInsightsResult = savedInsights.get(savedInsights.size() - 1).getMailTrackingInsightsId();
                }
            }
        }
        return TrackingInsightsResult;
    }

    @Override
    public long updateParticularEmpMailStatus(String mailPurpose,String mailStatus,long empId,long mailTrackingId) throws Exception {
        int result=dakMailTrackInsightsRepository.updateParticularEmpMailStatus(mailPurpose,mailStatus,empId,mailTrackingId);
        return Long.parseLong(String.valueOf(result));
    }

    @Override
    public long updateMailSuccessCount(long mailTrackingId,long mailSendSuccessCount,String trackingType) throws Exception {
        long result=0;
        try {
            Optional<DakMailTracking> model =dakMailTrackRepository.findById(mailTrackingId);

            if(model.isPresent()) {
                DakMailTracking data = model.get();
                data.setMailSentCount(mailSendSuccessCount);
                data.setMailSentStatus("S");
                data.setMailSentDateTime(LocalDateTime.now());
                result=dakMailTrackRepository.save(data).getMailTrackingId();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long updateNoPendingReply(long mailTrackingId)throws Exception{
        long result=0;
        try {
            Optional<DakMailTracking> model =dakMailTrackRepository.findById(mailTrackingId);

            if(model.isPresent()) {
                DakMailTracking data = model.get();
                data.setMailSentCount(Long.valueOf(0));
                data.setMailSentStatus("NA");
                result=dakMailTrackRepository.save(data).getMailTrackingId();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Object[]> getSummaryDistributedEmpData() throws Exception {
        return dakMailTrackRepository.getSummaryDistributedEmpData();
    }

    @Override
    public long insertSummaryDistributedInsights(long mailTrackingId)throws Exception{
        long TrackingInsightsResult = 0;
        List<Object[]> summaryDistributedToSendMail = dakMailTrackRepository.getSummaryDistributedEmpData();
        if (summaryDistributedToSendMail != null && !summaryDistributedToSendMail.isEmpty()) {
            Map<Integer, Set<String>> empDakNosMap = new HashMap();

            List<DakMailTrackingInsights> insightsList = new ArrayList<>();

            for (Object[] rowData : summaryDistributedToSendMail) {
                int empId = Integer.parseInt(rowData[1].toString());
                String dakNo = rowData[2].toString();
                if (!empDakNosMap.containsKey(empId)) {
                    empDakNosMap.put(empId, new HashSet<>());
                }
                empDakNosMap.get(empId).add(dakNo);
            }
            for (Map.Entry<Integer, Set<String>> entry : empDakNosMap.entrySet()) {
                int empId = entry.getKey();
                Set<String> dakNosSet = entry.getValue();
                DakMailTrackingInsights Insights = new DakMailTrackingInsights();
                Insights.setMailTrackingId(mailTrackingId);
                Insights.setMailPurpose("S");
                Insights.setMailStatus("N");
                Insights.setCreatedDate(LocalDateTime.now());
                Insights.setEmpId(Long.parseLong(String.valueOf(empId)));
                Insights.setDakNos(String.join(",", dakNosSet));
                insightsList.add(Insights);

            }
            if (!insightsList.isEmpty()) {
                List<DakMailTrackingInsights> savedInsights = dakMailTrackInsightsRepository.saveAll(insightsList);
                if (!savedInsights.isEmpty()) {
                    TrackingInsightsResult = savedInsights.get(savedInsights.size() - 1).getMailTrackingInsightsId();
                }
            }
        }
        return TrackingInsightsResult;
    }

    @Override
    public long insertWeeklyPendingInsights(long mailTrackingId)throws Exception{
        long TrackingInsightsResult = 0;
        List<Object[]> pendingReplyEmpsDetailsToSendMail = dakMailTrackRepository.getWeeklyPendingReplyEmpData();
        if (pendingReplyEmpsDetailsToSendMail != null && pendingReplyEmpsDetailsToSendMail.size() > 0) {
            Map<Integer, Set<String>> empDakNosMap = new HashMap();
            List<DakMailTrackingInsights> insightsList = new ArrayList<>();
            for (Object[] rowData : pendingReplyEmpsDetailsToSendMail) {
                int empId = Integer.parseInt(rowData[1].toString());
                String dakNo = rowData[2].toString();
                if (!empDakNosMap.containsKey(empId)) {
                    empDakNosMap.put(empId, new HashSet<>());
                }
                empDakNosMap.get(empId).add(dakNo);
            }
            for (Map.Entry<Integer, Set<String>> entry : empDakNosMap.entrySet()) {
                int empId = entry.getKey();
                Set<String> dakNosSet = entry.getValue();
                DakMailTrackingInsights Insights = new DakMailTrackingInsights();
                Insights.setMailTrackingId(mailTrackingId);
                Insights.setMailPurpose("W");
                Insights.setMailStatus("N");
                Insights.setCreatedDate(LocalDateTime.now());
                Insights.setEmpId(Long.parseLong(String.valueOf(empId)));
                Insights.setDakNos(String.join(",", dakNosSet));
                insightsList.add(Insights);

            }
            if (!insightsList.isEmpty()) {
                List<DakMailTrackingInsights> savedInsights = dakMailTrackInsightsRepository.saveAll(insightsList);
                if (!savedInsights.isEmpty()) {
                    TrackingInsightsResult = savedInsights.get(savedInsights.size() - 1).getMailTrackingInsightsId();
                }
            }
        }
        return TrackingInsightsResult;
    }

    @Override
    public List<Object[]> getWeeklyPendingReplyEmpData() throws Exception {
        return dakMailTrackRepository.getWeeklyPendingReplyEmpData();
    }

    @Override
    public long insertPmsMailTrackInitiator(String trackingType,int numberOfDays) throws Exception {
        long rowAddResult = 0;
        PfmsMailTracking track = new PfmsMailTracking();
        track.setTrackingType(trackingType);
        long pfmsMailSendEmployees = pmsMailTrackRepository.pfmsMailSendEmployeesCount(numberOfDays);
        track.setMailExpectedCount(pfmsMailSendEmployees);
        track.setMailSentCount(Long.valueOf(0));
        track.setMailSentStatus("N");
        track.setCreatedDate(LocalDateTime.now());
        track.setCreatedTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        rowAddResult = pmsMailTrackRepository.save(track).getMailTrackingId();
        return rowAddResult;
    }

    @Override
    public List<Object[]> weeklyActionList(int numberOfDays) throws Exception {
        return pmsMailTrackRepository.weeklyActionList(numberOfDays);
    }

    @Override
    public long insertPfmsMailTrackingInsights(long mailTrackingId, long EmpId, String actionNos, String trackingType,String mailStatus, String createdDate) throws Exception{
        long rowAddResult = 0;
        try {
            PfmsMailTrackingInsights track = new PfmsMailTrackingInsights();
            track.setMailTrackingId(mailTrackingId);
            track.setMailPurpose(trackingType);
            track.setEmpId(EmpId);
            track.setActionNos(actionNos);
            track.setMailStatus(mailStatus);
            track.setMailSentDate(LocalDateTime.now());
            track.setCreatedDate(LocalDateTime.now());
            rowAddResult = pmsMailTrackInsightsRepository.save(track).getMailTrackingInsightsId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rowAddResult;
    }

    @Override
    public long updateEmailTrackingExpectedCount(long mailTrackingId, int finalResult,String sentDate) throws Exception {
        int result=pmsMailTrackRepository.updatemailTrackingExpectedCount(mailTrackingId,finalResult,sentDate);
        return Long.parseLong(String.valueOf(result));
    }

    @Override
    public List<Object[]> getTodaysMeetings(String date) throws Exception {
        return pmsMailTrackRepository.getTodaysMeetings(date);
    }

    @Override
    public List<Object[]> committeeAttendance(String committeeScheduleId) throws Exception {
        return pmsMailTrackRepository.committeeAttendance(committeeScheduleId);
    }

    @Override
    public long insertPmsMailTrackInitiatorForMeetingMail(String trackingType, String date,String labCode) throws Exception {
        long rowAddResult = 0;
        PfmsMailTracking track = new PfmsMailTracking();
        track.setTrackingType(trackingType);
        long pfmsMailSendEmployeesCountForMeetingMail = pmsMailTrackRepository.pfmsMailSendEmployeesCountForMeetingMail(date,labCode);
        track.setMailExpectedCount(pfmsMailSendEmployeesCountForMeetingMail);
        track.setMailSentCount(Long.valueOf(0));
        track.setMailSentStatus("N");
        track.setCreatedDate(LocalDateTime.now());
        track.setCreatedTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        rowAddResult = pmsMailTrackRepository.save(track).getMailTrackingId();
        return rowAddResult;
    }

    @Override
    public long projectHoaUpdate(List<ProjectHoa> hoa, String username, List<IbasLabMaster> labDetails) throws Exception{
        long count1 =0 ;
        int count = projectHoaRepository.projectHoaDelete(labDetails.get(0).getLabCode());
        for(ProjectHoa obj : hoa) {
            obj.setCreatedBy(username);
            obj.setCreatedDate(sdf1.format(new Date()));
            obj.setLabCode(labDetails.get(0).getLabCode());
            count1=projectHoaRepository.save(obj).getProjectHoaId();
        }

        return count1;
    }


    @Override
    public long projectFinanceChangesUpdate(List<FinanceChanges> monthly, List<FinanceChanges> weekly, List<FinanceChanges> today, String userId,String labCode) throws Exception {
        List<Object[]> proList=projectHoaRepository.projectList(labCode);
        long result=0;
        for(Object[] obj:proList) {
            try {
                projectHoaChangesRepository.projectHoaChangesDelete(obj[0].toString());
                ProjectHoaChanges changes = new ProjectHoaChanges();
                changes.setProjectId(Long.parseLong(obj[0].toString()));
                changes.setProjectCode(obj[1].toString());
                changes.setMonthlyChanges(Long.valueOf(monthly.stream().filter(c-> c.getProjectCode().toString().equalsIgnoreCase(obj[1].toString())).collect(Collectors.toList()).size()));
                changes.setWeeklyChanges(Long.valueOf(weekly.stream().filter(c-> c.getProjectCode().toString().equalsIgnoreCase(obj[1].toString())).collect(Collectors.toList()).size()));
                changes.setTodayChanges(Long.valueOf(today.stream().filter(c-> c.getProjectCode().toString().equalsIgnoreCase(obj[1].toString())).collect(Collectors.toList()).size()));
                changes.setCreatedBy(userId);
                changes.setCreatedDate(LocalDateTime.now());
                changes.setIsActive(1);

                result= projectHoaChangesRepository.save(changes).getChangesId();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    @Override
    public Object getClusterId(String labCode) throws Exception {
        return projectHoaChangesRepository.getClusterId(labCode);
    }


    @Override
    public long cCMViewDataUpdate(List<CCMView> cCMViewData,String labCode,String clusterId, String userId,String empId) throws Exception
    {
        List<Object[]> proList=projectHoaRepository.projectList(labCode);
        long result=0;
        if(cCMViewData.size()>0)
        {
            ccmViewRepository.cCMDataDelete(labCode);
            for(CCMView ccmdata:cCMViewData)
            {
                PFMSCCMData pfmsccm = PFMSCCMData.builder()

                        .ClusterId(Long.parseLong(clusterId))
                        .LabCode(LabCode)
                        .ProjectId(ccmdata.getProjectId())
                        .ProjectCode(ccmdata.getProjectCode().trim())
                        .BudgetHeadId(ccmdata.getBudgetHeadId())
                        .BudgetHeadDescription(ccmdata.getBudgetHeadDescription())
                        .AllotmentCost(ccmdata.getAllotmentCost())
                        .Expenditure(ccmdata.getExpenditure())
                        .Balance(ccmdata.getBalance())
                        .Q1CashOutGo(ccmdata.getQ1CashOutGo())
                        .Q2CashOutGo(ccmdata.getQ2CashOutGo())
                        .Q3CashOutGo(ccmdata.getQ3CashOutGo())
                        .Q4CashOutGo(ccmdata.getQ4CashOutGo())
                        .Required(ccmdata.getRequired())
                        .CreatedDate(sdf1.format(new Date()))
                        .build();
                result= ccmViewRepository.save(pfmsccm).getCCMDataId();
            }
        }
        return result;
    }


    @Override
    public long projectHealthUpdate(String labCode, String userName) throws Exception {
        List<Object[]> proList=projectHoaRepository.projectList(labCode);
        long result=0;
        if(proList!=null && proList.size()>0) {
            for(Object[] obj:proList) {
                try {
                    projectHealthRepository.projectHealthDelete(obj[0].toString());
                    Object[] data=projectHealthRepository.projectHealthInsertData(obj[0].toString()).get(0);
                    ProjectHealth health=new ProjectHealth();
                    health.setLabCode(data[0].toString());
                    health.setProjectId(Long.parseLong(data[1].toString()));
                    health.setProjectShortName(data[2].toString());
                    health.setPMRCHeld(Long.parseLong(data[3].toString()));
                    health.setPMRCPending( Long.parseLong(data[4].toString())>=0 ?  Long.parseLong(data[4].toString()) : 0 );
                    health.setEBHeld(Long.parseLong(data[5].toString()));
                    health.setEBPending(  Long.parseLong(data[6].toString())>=0 ? Long.parseLong(data[6].toString()) : 0 );
                    health.setMilPending(Long.parseLong(data[7].toString()));
                    health.setMilDelayed(Long.parseLong(data[8].toString()));
                    health.setMilCompleted(Long.parseLong(data[9].toString()));
                    health.setActionPending(Long.parseLong(data[10].toString()));
                    health.setActionForwarded(Long.parseLong(data[11].toString()));
                    health.setActionDelayed(Long.parseLong(data[12].toString()));
                    health.setActionCompleted(Long.parseLong(data[13].toString()));
                    health.setRiskPending(Long.parseLong(data[14].toString()));
                    health.setRiskCompleted(Long.parseLong(data[15].toString()));
                    health.setProjectType(data[20].toString());
                    health.setEndUser(data[21].toString());
                    health.setProjectCode(data[22].toString());
                    health.setPMRCTotal(Long.parseLong(data[23].toString()));
                    health.setEBTotal(Long.parseLong(data[24].toString()));

                    if(data[16]!=null) {
                        health.setExpenditure(Double.parseDouble(data[16].toString()));
                        health.setDipl(Double.parseDouble(data[18].toString()));
                        health.setOutCommitment(Double.parseDouble(data[17].toString()));
                        health.setBalance(Double.parseDouble(data[19].toString()));
                    }else {
                        health.setExpenditure(Double.parseDouble("0.00"));
                        health.setDipl(Double.parseDouble("0.00"));
                        health.setOutCommitment(Double.parseDouble("0.00"));
                        health.setBalance(Double.parseDouble("0.00"));
                    }

                    health.setCreatedBy(userName);
                    health.setCreatedDate(LocalDateTime.now());
                    health.setTodayChanges(Long.parseLong(data[25].toString()));
                    health.setWeeklyChanges(Long.parseLong(data[26].toString()));
                    health.setMonthlyChanges(Long.parseLong(data[27].toString()));
                    health.setPDC(data[28].toString());
                    health.setPMRCTotalToBeHeld(Long.parseLong(data[29].toString()));
                    health.setEBTotalToBeHeld(Long.parseLong(data[30].toString()));
                    health.setSanctionDate(data[31].toString());
                    result=projectHealthRepository.save(health).getProjectHealthId();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    @Override
    public long sendEmailAndSmsForSupplyOrderDetails() throws Exception {
        int status=0;
        try {
            MailConfigurationDto mailDetails = getMailConfigByTypeOfHost("L");
            if (mailDetails != null && mailDetails.getHost() != null && mailDetails.getUsername() != null) {
                SupplyOrderMailTrack mail = new SupplyOrderMailTrack();
                mail.setCreatedDate(LocalDateTime.now());
                mail.setMailDate(LocalDate.now());
                mail.setMailExpectedCount(0);
                mail.setMailSentCount(0);
                mail.setSmsSentCount(0);
                long mailId = supplyOrderMailTrackRepository.save(mail).getMailId();
                if (mailId > 0) {
                    List<Object[]> DirectorList = supplyOrderMailTrackRepository.getLabDirectorDetailsForMail(LabCode);
                    if (DirectorList != null && !DirectorList.isEmpty()) {
                        supplyOrderDetailsSettingToMail(DirectorList.get(0)[0], DirectorList.get(0)[1], DirectorList.get(0)[4], DirectorList.get(0)[3], "D", mail, mailDetails, DirectorList.get(0)[2]); // Director
                    }
                    List<Object[]> EmployeeDetails = supplyOrderMailTrackRepository.getEmployeeDetailsForMail(LabCode);
                    if (EmployeeDetails != null && !EmployeeDetails.isEmpty()) {
                        int batchSize = 8;
                        for (int i = 0; i < EmployeeDetails.size(); i += batchSize) {
                            List<Object[]> batch = EmployeeDetails.subList(i, Math.min(i + batchSize, EmployeeDetails.size()));
                            for (Object[] employeeDetails : batch) {
                                String officerType = "N";
                                if (employeeDetails[16] != null && "Y".equalsIgnoreCase(employeeDetails[16].toString())) {
                                    officerType = "G";
                                } else if (employeeDetails[15] != null && "Y".equalsIgnoreCase(employeeDetails[15].toString())) {
                                    officerType = "H";
                                } else if (employeeDetails[14] != null && "Y".equalsIgnoreCase(employeeDetails[14].toString())) {
                                    officerType = "P";
                                } else if (employeeDetails[13] != null && "Y".equalsIgnoreCase(employeeDetails[13].toString())) {
                                    officerType = "I";
                                }
                                if (employeeDetails[0] != null) {
                                    int smsMailStatus = supplyOrderDetailsSettingToMail(employeeDetails[0], employeeDetails[1], employeeDetails[9], employeeDetails[8], officerType, mail, mailDetails, employeeDetails[2]);
                                    if (smsMailStatus > 0) {
                                        status++;
                                    }
                                }
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        mail.setMailSentDateTime(LocalDateTime.now());
                        long count=supplyOrderMailTrackRepository.save(mail).getMailId();
                    }
                }
            }
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }


    }

    @Override
    public int supplyOrderDetailsSettingToMail(Object empId,Object empNo,Object email,Object mobileNo,String officerType,SupplyOrderMailTrack mail,MailConfigurationDto mailDetails,Object empName) throws Exception
    {
        int sendMailResult =0,supplyOrderCount=0;
        List<Object[]> employeeSupplyOrderDetails=supplyOrderMailTrackRepository.getEmployeeSupplyOrderDetails(officerType,empId.toString());
        if(employeeSupplyOrderDetails!=null && !employeeSupplyOrderDetails.isEmpty())
        {
            supplyOrderCount=employeeSupplyOrderDetails.size();
            mail.setMailExpectedCount(mail.getMailExpectedCount()+1);

            SupplyOrderEmployeeMailTrack empMailDetails=new SupplyOrderEmployeeMailTrack();
            empMailDetails.setMailId(mail.getMailId());
            empMailDetails.setEmpId(Long.parseLong(empId.toString()));
            empMailDetails.setCreatedDate(LocalDateTime.now());
            empMailDetails.setMailStatus("N");
            empMailDetails.setSmsStatus("N");

            supplyOrderEmployeeMailTrackRepository.save(empMailDetails);

            String subject = "PFTS - List of Orders DP Expiring in next 30 days";
            String mailMmessage = "<p>Dear Sir/Madam,</p>";
            mailMmessage += "<p></p>";
            mailMmessage += "<p>Delivery Period will expire in next 30 days for the following order(s)";
            mailMmessage += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 85%; font-size: 16px; border-collapse: collapse;\">";
            mailMmessage += "<thead>";
            mailMmessage += "<tr>";
            mailMmessage += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:5% !important;\">SN</th>";
            mailMmessage += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:15% !important;\">Order No</th>";
            mailMmessage += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Order Date</th>";
            mailMmessage += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">DP Date</th>";
            mailMmessage += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Project Code</th>";
            mailMmessage += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:30% !important;\">Item</th>";
            mailMmessage += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:20% !important;\">Vendor</th>";
            mailMmessage += "</tr>";
            mailMmessage += "</thead>";
            mailMmessage += "<tbody>";
            int serialNo=1;
            StringBuilder objBuilder = new StringBuilder();
            for(Object[] empSoDetails:employeeSupplyOrderDetails)
            {
                if (objBuilder.length() > 0) {
                    objBuilder.append(",");
                }
                objBuilder.append(empSoDetails[2]);
                mailMmessage += "<tr>";
                mailMmessage += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + serialNo++ + "</td>";
                mailMmessage += "<td style=\"border: 1px solid black; padding: 5px; text-align: left; text-wrap: nowrap !important;\">" + empSoDetails[2] + "</td>";

                String soDate="-";
                if(empSoDetails[3]!=null)
                {
                    soDate=rdf.format(sdf2.parse(empSoDetails[3].toString()));
                }

                mailMmessage += "<td style=\"border: 1px solid black; padding: 5px; text-align: left; text-wrap: nowrap !important;\">" + soDate + "</td>";

                String dpDate="-";
                if(empSoDetails[5]!=null)
                {
                    dpDate=rdf.format(sdf2.parse(empSoDetails[5].toString()));
                }
                mailMmessage += "<td style=\"border: 1px solid black; padding: 5px; text-align: left; text-wrap: nowrap !important;\">" + dpDate + "</td>";
                mailMmessage += "<td style=\"border: 1px solid black; padding: 5px; text-align: left; text-wrap: nowrap !important;\">" + empSoDetails[7] + "</td>";
                mailMmessage += "<td style=\"border: 1px solid black; padding: 5px; text-align: left; text-wrap: nowrap !important;\">" + empSoDetails[8] + "</td>";
                mailMmessage += "<td style=\"border: 1px solid black; padding: 5px; text-align: left; text-wrap: nowrap !important;\">" + empSoDetails[10] + "</td>";
                mailMmessage += "</tr>";

            }
            String soNos = objBuilder.toString();
            empMailDetails.setSoNos(soNos);
            mailMmessage += "</tbody>";
            mailMmessage += "</table>";
            String text="";
            if(officerType.equalsIgnoreCase("D"))
            {
                text="information";
            }
            else
            {
                text="action";
            }
            mailMmessage += "<p>This is for your "+text+" please.</p>";
            mailMmessage += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
            mailMmessage += "<p>Thanks & Regards,<br>LRDE-PFTS Team</p>";

            try
            {
                exportExcelFile(empNo.toString(),employeeSupplyOrderDetails,empMailDetails,mail);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            if(email!=null && isValidEmailOrMobileNo(email.toString(),"E"))  // E - mail
            {

                try {
                    sendMailResult = sendMessage(email.toString(),subject, mailMmessage,mailDetails,empMailDetails);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            if(sendMailResult==1)
            {
                mail.setMailSentCount(mail.getMailSentCount()+1);  //setting sent mail count
                empMailDetails.setMailStatus("Y");
            }

            supplyOrderEmployeeMailTrackRepository.save(empMailDetails);
        }
        return sendMailResult;
    }

    @Override
    public List<Object[]>  pftsExpectedDailySummaryList()throws Exception{
        return pftsMailTrackRepository.pftsExpectedDailySummaryList();
    }

    @Override
    public long getPftsMailInitiatedCount(String TrackingType) throws Exception {
        return pftsMailTrackRepository.getPftsMailInitiatedCount(TrackingType);
    }

    @Override
    public long insertPftsMailTrackInitiator(String trackingType) throws Exception {
        long rowAddResult = 0;
        PftsMailTrack track = new PftsMailTrack();
        track.setTrackingType(trackingType);
        if(trackingType!=null && trackingType.equalsIgnoreCase("S")) {
            long expectedTotalNoOfMails = 0;
            List<Object[]> dailyPftsSummaryList = pftsMailTrackRepository.pftsExpectedDailySummaryList();
            if(dailyPftsSummaryList!=null && dailyPftsSummaryList.size()>0) {
                Set<Object> uniqueForwardedTo = new HashSet<>();
                for(Object[] obj: dailyPftsSummaryList) {
                    // Check if obj has more than 6 elements and obj[8] is not null
                    if (obj.length > 8 && obj[8] != null) {
                        uniqueForwardedTo.add(obj[8]);
                    }
                }
                expectedTotalNoOfMails = uniqueForwardedTo.size();
            }
            track.setMailExpectedCount(expectedTotalNoOfMails);
        }
        track.setMailSentCount(Long.valueOf(0));
        track.setMailSentStatus("N");
        track.setCreatedDate(LocalDateTime.now());
        track.setCreatedTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        rowAddResult = pftsMailTrackRepository.save(track).getPftsMailTrackingId();
        return rowAddResult;
    }

    @Override
    public long insertPftsDailySummaryInsights(long pftsMailTrackingId) throws Exception{
        long trackingInsightsResult = 0;
        List<Object[]> dailyPftsSummaryList = pftsMailTrackRepository.pftsExpectedDailySummaryList();
        if(dailyPftsSummaryList!=null && !dailyPftsSummaryList.isEmpty()) {
            Map<String, Set<String>> empDemandNosMap = new HashMap();
            List<PftsMailTrackInsights> insightsList = new ArrayList<>();
            for (Object[] rowData : dailyPftsSummaryList) {

                String forwardeToEmpNo = rowData[8].toString();
                String demandNo = rowData[0].toString();
                if (!empDemandNosMap.containsKey(forwardeToEmpNo)) {
                    empDemandNosMap.put(forwardeToEmpNo, new HashSet<>());
                }
                empDemandNosMap.get(forwardeToEmpNo).add(demandNo);
            }
            for (Map.Entry<String, Set<String>> entry : empDemandNosMap.entrySet()) {
                String employeeNo = entry.getKey();
                Set<String> demandNosSet = entry.getValue();
                PftsMailTrackInsights insights = new PftsMailTrackInsights();
                insights.setPftsMailTrackingId(pftsMailTrackingId);
                insights.setMailPurpose("S");
                insights.setMailStatus("N");
                insights.setCreatedDate(LocalDateTime.now());
                insights.setEmpNo(employeeNo);
                insights.setDemandNo(String.join(",", demandNosSet));

                insightsList.add(insights);
            }
            if (!insightsList.isEmpty()) {
                List<PftsMailTrackInsights> savedInsights = pftsMailTrackInsightsRepository.saveAll(insightsList);
                if (!savedInsights.isEmpty()) {
                    trackingInsightsResult = savedInsights.get(savedInsights.size() - 1).getPftsMailTrackingInsightsId();
                }
            }
        }
        return trackingInsightsResult;
    }

    @Override
    public long updateParticularEmpMailStatusInPfts(String mailPurpose,String mailStatus,String empNo,long pftsMailTrackingId) throws Exception {
        int result=pftsMailTrackInsightsRepository.updateParticularEmpMailStatusInPfts(mailPurpose,mailStatus,empNo,pftsMailTrackingId);
        return Long.parseLong(String.valueOf(result));
    }

    @Override
    public long updateMailSuccessCountInPfts(long pftsMailTrackingId, long mailSendSuccessCount,String trackingType) throws Exception {
        long result=0;
        try {
            Optional<PftsMailTrack> model =pftsMailTrackRepository.findById(pftsMailTrackingId);

            if(model.isPresent()) {
                PftsMailTrack data = model.get();
                data.setMailSentCount(mailSendSuccessCount);
                data.setMailSentStatus("S");
                data.setMailSentDateTime(LocalDateTime.now());
                result=pftsMailTrackRepository.save(data).getPftsMailTrackingId();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long updatePftsNoPendingReply(long effectivelyFinalMailTrackingId) throws Exception {
        long result=0;
        try {
            Optional<PftsMailTrack> model =pftsMailTrackRepository.findById(effectivelyFinalMailTrackingId);
            if(model.isPresent()) {
                PftsMailTrack data = model.get();
                data.setMailSentCount(Long.valueOf(0));
                data.setMailSentStatus("NA");
                result=pftsMailTrackRepository.save(data).getPftsMailTrackingId();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Object[]> pftsDailySendEmployees() throws Exception{

        try {
            return pftsMailTrackRepository.pftsDailySendEmployees();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public long insertPftsDailyMailTrackInitiator(String trackingType) throws Exception {
        long rowAddResult = 0;
        PftsMailTrack track = new PftsMailTrack();
        track.setTrackingType(trackingType);
        List<Object[]> pftsDailySendEmployees = pftsMailTrackRepository.pftsDailySendEmployees();
        track.setMailExpectedCount((long) pftsDailySendEmployees.size());
        track.setMailSentCount(Long.valueOf(0));
        track.setMailSentStatus("N");
        track.setCreatedDate(LocalDateTime.now());
        track.setCreatedTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        rowAddResult = pftsMailTrackRepository.save(track).getPftsMailTrackingId();
        return rowAddResult;
    }


    @Override
    public long insertPftsDailyInsights(long pftsMailTrackingId) throws Exception {
        long TrackingInsightsResult = 0;
        List<Object[]> PftsDailySendEmployees = pftsMailTrackRepository.pftsDailySendEmployees();
        if(PftsDailySendEmployees!=null && PftsDailySendEmployees.size()>0) {
            Map<String, Set<String>> empDemandNosMap = new HashMap();
            for(Object[] obj:PftsDailySendEmployees) {
                List<Object[]> pftsDailySendEmployeesDetails = pftsMailTrackRepository.pftsDailySendEmployeesDetails(obj[0].toString());
                for (Object[] rowData : pftsDailySendEmployeesDetails) {
                    String forwardeToEmpNo = rowData[1].toString();
                    String demandNo = rowData[0].toString();
                    if (!empDemandNosMap.containsKey(forwardeToEmpNo)) {
                        empDemandNosMap.put(forwardeToEmpNo, new HashSet<>());
                    }
                    empDemandNosMap.get(forwardeToEmpNo).add(demandNo);
                }
            }
            for (Map.Entry<String, Set<String>> entry : empDemandNosMap.entrySet()) {
                String employeeNo = entry.getKey();
                Set<String> demandNosSet = entry.getValue();
                PftsMailTrackInsights insights = new PftsMailTrackInsights();
                insights.setPftsMailTrackingId(pftsMailTrackingId);
                insights.setMailPurpose("D");
                insights.setMailStatus("N");
                insights.setCreatedDate(LocalDateTime.now());
                insights.setEmpNo(employeeNo);
                insights.setDemandNo(String.join(",", demandNosSet));

                long result = pftsMailTrackInsightsRepository.save(insights).getPftsMailTrackingInsightsId();
                TrackingInsightsResult = result;
            }
        }
        return TrackingInsightsResult;
    }

    @Override
    public List<Object[]> pftsDailySendEmployeesDetails(String forwardedTo) throws Exception {
        return pftsMailTrackRepository.pftsDailySendEmployeesDetails(forwardedTo);
    }


    @Override
    public int sendScheduleMailOfIrf(String labCode) throws Exception {
        String message = null;
        long count =0;
        long expectCount =0;
        long result=0;
        int msgResult=0;
        List<Object[]> empList = employeeRepository.getEmpDetails(labCode);
        List<Object[]> membersList = irfMailTrackRepository.getMembersList();
        IrfMailTrack irfMailTrack = new IrfMailTrack();
        irfMailTrack.setCreatedDate(LocalDateTime.now());
        irfMailTrack.setCreatedTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));

        long mailTrackId = irfMailTrackRepository.save(irfMailTrack).getMailTrackingId();

        List<String> gdEmp = membersList.stream().filter(data -> data[1].toString().equalsIgnoreCase("D")).map(rowData -> rowData[0].toString()).collect(Collectors.toList());
        List<String> ghEmp = membersList.stream().filter(data -> data[1].toString().equalsIgnoreCase("H")).map(rowData -> rowData[0].toString()).collect(Collectors.toList());
        List<String> oicEmp = membersList.stream().filter(data -> data[1].toString().equalsIgnoreCase("C")).map(rowData -> rowData[0].toString()).collect(Collectors.toList());
        List<String> cwEmp = membersList.stream().filter(data -> data[1].toString().equalsIgnoreCase("U")).map(rowData -> rowData[0].toString()).collect(Collectors.toList());

        List<Object[]> gdList  = empList.stream().filter(data -> gdEmp.contains(data[0].toString())).collect(Collectors.toList());
        List<Object[]> ghList  = empList.stream().filter(data -> ghEmp.contains(data[0].toString())).collect(Collectors.toList());
        List<Object[]> oicList = empList.stream().filter(data -> oicEmp.contains(data[0].toString())).collect(Collectors.toList());
        List<Object[]> cwList  = empList.stream().filter(data -> cwEmp.contains(data[0].toString())).collect(Collectors.toList());


        for(Object[] obj : gdList) {
            List<Object[]> qagdApprovalList = irfMailTrackRepository.getIrfQAGDApprovalList(obj[0].toString(),"11");
            if(qagdApprovalList.size()>0) {
                expectCount++;
                message =	"Dear Sir/Madam,\n\n"+qagdApprovalList.size()+" IR are pending for Approval for the day "+sdf2.format(new Date())+"."+"\n\nImportant Note: This is an automated message. Kindly avoid responding. \n\nRegards, \nLRDE-QMS Team";
                msgResult = controller.sendMessage(obj[2].toString(),"GD-DQA Approval", message);

                IrfMailTrackInsights mailTrackInsights = new IrfMailTrackInsights();
                mailTrackInsights.setMailTrackingId(mailTrackId);
                mailTrackInsights.setEmpId(Long.parseLong(obj[0].toString()));
                mailTrackInsights.setMessage(message);
                mailTrackInsights.setMailPurpose("D");
                if(msgResult>0) {
                    mailTrackInsights.setMailStatus("S");
                }else {
                    mailTrackInsights.setMailStatus("N");
                }

                mailTrackInsights.setMailSentDate(LocalDateTime.now());
                mailTrackInsights.setCreatedDate(LocalDateTime.now());
                result = irfMailTrackInsightsRepository.save(mailTrackInsights).getMailTrackingInsightsId();
                msgResult=0;
                if(result>0) {
                    count++;
                }
            }

        }

        for(Object[] obj : ghList) {
            List<Object[]> qagdApprovalList = irfMailTrackRepository.getIrfQAGDApprovalList(obj[0].toString(),"4");
            if(qagdApprovalList.size()>0) {
                expectCount++;
                message =	"Dear Sir/Madam,\n\n"+qagdApprovalList.size()+" IR are pending for Approval for the day "+sdf2.format(new Date())+"."+"\n\nImportant Note: This is an automated message. Kindly avoid responding. \n\nRegards, \nLRDE-QMS Team";
                msgResult = controller.sendMessage(obj[2].toString(),"GH-QRAG Approval", message);

                IrfMailTrackInsights mailTrackInsights = new IrfMailTrackInsights();
                mailTrackInsights.setMailTrackingId(mailTrackId);
                mailTrackInsights.setEmpId(Long.parseLong(obj[0].toString()));
                mailTrackInsights.setMessage(message);
                mailTrackInsights.setMailPurpose("D");
                if(msgResult>0) {
                    mailTrackInsights.setMailStatus("S");
                }else {
                    mailTrackInsights.setMailStatus("N");
                }

                mailTrackInsights.setMailSentDate(LocalDateTime.now());
                mailTrackInsights.setCreatedDate(LocalDateTime.now());
                result = irfMailTrackInsightsRepository.save(mailTrackInsights).getMailTrackingInsightsId();
                msgResult=0;
                if(result>0) {
                    count++;
                }
            }

        }

        for(Object[] obj : oicList) {
            List<Object[]> qagdApprovalList = irfMailTrackRepository.getIrfQAGDApprovalList(obj[0].toString(),"15");
            if(qagdApprovalList.size()>0) {
                expectCount++;
                message =	"Dear Sir/Madam,\n\n"+qagdApprovalList.size()+" IR are pending for Approval for the day "+sdf2.format(new Date())+"."+"\n\nImportant Note: This is an automated message. Kindly avoid responding. \n\nRegards, \nLRDE-QMS Team";
                msgResult = controller.sendMessage(obj[2].toString(),"OIC-QRAG Approval", message);
                IrfMailTrackInsights mailTrackInsights = new IrfMailTrackInsights();
                mailTrackInsights.setMailTrackingId(mailTrackId);
                mailTrackInsights.setEmpId(Long.parseLong(obj[0].toString()));
                mailTrackInsights.setMessage(message);
                mailTrackInsights.setMailPurpose("D");
                if(msgResult>0) {
                    mailTrackInsights.setMailStatus("S");
                }else {
                    mailTrackInsights.setMailStatus("N");
                }

                mailTrackInsights.setMailSentDate(LocalDateTime.now());
                mailTrackInsights.setCreatedDate(LocalDateTime.now());
                result = irfMailTrackInsightsRepository.save(mailTrackInsights).getMailTrackingInsightsId();
                msgResult=0;
                if(result>0) {
                    count++;
                }
            }
        }

        for(Object[] obj : cwList) {
            List<Object[]> qagdApprovalList = irfMailTrackRepository.getIrfCaseWorkerReqList(obj[0].toString());
            if(qagdApprovalList.size()>0) {
                expectCount++;
                message =	"Dear Sir/Madam,\n\n"+qagdApprovalList.size()+" IR are pending for Approval for the day "+sdf2.format(new Date())+"."+"\n\nImportant Note: This is an automated message. Kindly avoid responding. \n\nRegards, \nLRDE-QMS Team";
                msgResult = controller.sendMessage(obj[2].toString(),"QA-Inspector Pending", message);

                IrfMailTrackInsights mailTrackInsights = new IrfMailTrackInsights();
                mailTrackInsights.setMailTrackingId(mailTrackId);
                mailTrackInsights.setEmpId(Long.parseLong(obj[0].toString()));
                mailTrackInsights.setMessage(message);
                mailTrackInsights.setMailPurpose("D");
                if(msgResult>0) {
                    mailTrackInsights.setMailStatus("S");
                }else {
                    mailTrackInsights.setMailStatus("N");
                }

                mailTrackInsights.setMailSentDate(LocalDateTime.now());
                mailTrackInsights.setCreatedDate(LocalDateTime.now());
                result = irfMailTrackInsightsRepository.save(mailTrackInsights).getMailTrackingInsightsId();
                msgResult=0;
                if(result>0) {
                    count++;
                }
            }
        }

        irfMailTrack.setMailExpectedCount(expectCount);
        irfMailTrack.setMailSentCount(count);
        irfMailTrack.setMailSentDateTime(LocalDateTime.now());
        irfMailTrack.setMailSentStatus("S");

        irfMailTrackRepository.save(irfMailTrack);

        return (int)count;
    }

    @Override
    public int RINCRVPendingList() throws Exception {
        List<Object[]> rinCrvPendingList = sirRepository.getRinCrvPendingList();
        List<Object[]> empList = employeeRepository.getEmpDetails(LabCode);
        List<Object[]> proList = projectHoaRepository.projectList(LabCode);
        List<Object[]> divList = employeeRepository.divList(LabCode);
        List<Object[]> storesOfficerList = sirRepository.storesOfficerList(5);

        List<Object[]> storesEmployeeList = empList.stream()
                .filter(emp -> storesOfficerList.stream()
                        .anyMatch(officer -> Objects.equals((Long) emp[0], (Long) officer[0]))) // EmpId comparison
                .collect(Collectors.toList());

        Map<Long, Set<Object[]>> projectEmployeeMap = new HashMap<>();
        Map<Long, Set<Object[]>> divisonEmployeeMap = new HashMap<>();

        for (Object[] row : rinCrvPendingList) {
            Long projectId = (Long) row[11];
            Long divisionId = (Long) row[10];

            if (projectId == null || divisionId == null) {
                continue;
            }

            Set<Long> projectDirIds = proList.stream()
                    .filter(e -> projectId.equals((Long) e[0]))
                    .map(e -> (Long) e[5])
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Long> divisionHeadIds = divList.stream()
                    .filter(e -> divisionId.equals((Long) e[0]))
                    .map(e -> (Long) e[3])
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Object[]> employeeList = empList.stream()
                    .filter(emp -> projectDirIds.contains((Long) emp[0]))
                    .collect(Collectors.toList());

            if (!employeeList.isEmpty()) {
                projectEmployeeMap.computeIfAbsent(projectId, k -> new HashSet<>()).addAll(employeeList);
            }

            List<Object[]> divmployeeList = empList.stream()
                    .filter(emp -> divisionHeadIds.contains((Long) emp[0]))
                    .collect(Collectors.toList());

            if (!divmployeeList.isEmpty()) {
                divisonEmployeeMap.computeIfAbsent(divisionId, k -> new HashSet<>()).addAll(divmployeeList);
            }
        }

        SisMailTrack mailTrack = new SisMailTrack();
        mailTrack.setMailExpectedCount((long)projectEmployeeMap.size()+(long)divisonEmployeeMap.size()+(long)storesEmployeeList.size());
        mailTrack.setMailSentCount(Long.valueOf(0));
        mailTrack.setTrackingType("W");
        mailTrack.setCreatedDate(LocalDateTime.now());
        mailTrack.setCreatedTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));

        long mailTrackId=sisMailTrackRepository.save(mailTrack).getMailTrackingId();


        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = sdf.format(new Date());
        String subject = "SIS - Pending RIN and CRV List - " + currentDate;

        AtomicInteger mailSendSuccessCount = new AtomicInteger(0);
        projectEmployeeMap.forEach((projectId, employeeList) -> {
            List<Object[]> pendingRows = rinCrvPendingList.stream()
                    .filter(e -> projectId.equals((Long) e[11]))
                    .collect(Collectors.toList());

            employeeList.forEach(emp -> {
                String email = (String) emp[2];
                String dronaEmail = (String) emp[3];
                Long empId = (Long) emp[0];
                if (email != null) {
                    try {
                        String message = "<p>Dear Sir/Madam,</p>";
                        message += "<p>This email is to inform you that the following RIN and CRV Pending List to you :</p>";
                        message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 85%; font-size: 16px; border-collapse: collapse;\">";
                        message += "<thead>";
                        message += "<tr>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:5% !important;\">SN</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:20% !important;\">SIR No & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:25% !important;\">SONo & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Description</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Recieved Qty</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">RIN No & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">CRV No & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Consigner Name</th>";
                        message += "</tr>";
                        message += "</thead>";
                        int count1 = 1;
                        for (Object[] row : pendingRows) {
                            message += "<tr>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + count1++ + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + row[2] + "<br>"+ sdf.format(row[3]) +"</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + row[4] + "<br>"+ sdf.format(row[5])  +"</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" +row[6]  +"</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + row[7] + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + (row[15] != null ? row[15] : "-") + "<br>"+ (row[16] != null ? sdf.format(row[16]) : "-") + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + (row[17] != null ? row[17] : "-") +"<br>"+ (row[18] != null ? sdf.format(row[18]) : "-") + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + row[9] +"</td>";
                            message += "</tr>";
                        }
                        message += "</tbody></table>";
                        message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                        message += "<p>Regards,<br>LRDE-SIS Team</p>";

                        int sendResult = controller.sendMessage(email, subject, message);
                        if(dronaEmail!=null){
                                controller.sendMessage1(dronaEmail, subject, message);
                        }
                        if (sendResult > 0) {
                            SisMailTrackInsights sisMailTrackInsights = new SisMailTrackInsights();
                            sisMailTrackInsights.setMailTrackingId(mailTrackId);
                            sisMailTrackInsights.setMailStatus("S");
                            sisMailTrackInsights.setEmpId(empId);
                            sisMailTrackInsights.setCreatedDate(LocalDateTime.now());
                            sisMailTrackInsights.setMailSentDate(LocalDateTime.now());
                            sisMailTrackInsightsRepository.save(sisMailTrackInsights);
                            mailSendSuccessCount.getAndIncrement();
                        } else {
                            SisMailTrackInsights sisMailTrackInsights = new SisMailTrackInsights();
                            sisMailTrackInsights.setMailTrackingId(mailTrackId);
                            sisMailTrackInsights.setMailStatus("N");
                            sisMailTrackInsights.setEmpId(empId);
                            sisMailTrackInsights.setCreatedDate(LocalDateTime.now());
                            sisMailTrackInsightsRepository.save(sisMailTrackInsights);
                        }

                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        divisonEmployeeMap.forEach((divisionId, employeeList) -> {
            List<Object[]> pendingRows = rinCrvPendingList.stream()
                    .filter(e -> divisionId.equals((Long) e[10]))
                    .collect(Collectors.toList());

            employeeList.forEach(emp -> {
                String email = (String) emp[2];
                String dronaEmail = (String) emp[3];
                Long empId = (Long) emp[0];
                if (email != null) {
                    try {
                        String message = "<p>Dear Sir/Madam,</p>";
                        message += "<p>This email is to inform you that the following RIN and CRV Pending List to you :</p>";
                        message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 85%; font-size: 16px; border-collapse: collapse;\">";
                        message += "<thead>";
                        message += "<tr>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:5% !important;\">SN</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:20% !important;\">SIR No & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:25% !important;\">SONo & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Description</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Recieved Qty</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">RIN No & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">CRV No & Date</th>";
                        message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Consigner Name</th>";
                        message += "</tr>";
                        message += "</thead>";
                        int count1 = 1;
                        for (Object[] row : pendingRows) {
                            message += "<tr>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + count1++ + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + row[2] + "<br>"+ sdf.format(row[3]) +"</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + row[4] + "<br>"+ sdf.format(row[5])  +"</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" +row[6]  +"</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + row[7] + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + (row[15] != null ? row[15] : "-") + "<br>"+ (row[16] != null ? sdf.format(row[16]) : "-") + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + (row[17] != null ? row[17] : "-") +"<br>"+ (row[18] != null ? sdf.format(row[18]) : "-") + "</td>";
                            message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + row[9]+"</td>";
                            message += "</tr>";
                        }
                        message += "</tbody></table>";
                        message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                        message += "<p>Regards,<br>LRDE-SIS Team</p>";

                        int sendResult = controller.sendMessage(email, subject, message);
                        if(dronaEmail!=null){
                            controller.sendMessage1(dronaEmail, subject, message);
                        }
                        if (sendResult > 0) {
                            SisMailTrackInsights sisMailTrackInsights = new SisMailTrackInsights();
                            sisMailTrackInsights.setMailTrackingId(mailTrackId);
                            sisMailTrackInsights.setMailStatus("S");
                            sisMailTrackInsights.setEmpId(empId);
                            sisMailTrackInsights.setCreatedDate(LocalDateTime.now());
                            sisMailTrackInsights.setMailSentDate(LocalDateTime.now());
                            sisMailTrackInsightsRepository.save(sisMailTrackInsights);
                            mailSendSuccessCount.getAndIncrement();
                        } else {
                            SisMailTrackInsights sisMailTrackInsights = new SisMailTrackInsights();
                            sisMailTrackInsights.setMailTrackingId(mailTrackId);
                            sisMailTrackInsights.setMailStatus("N");
                            sisMailTrackInsights.setEmpId(empId);
                            sisMailTrackInsights.setCreatedDate(LocalDateTime.now());
                            sisMailTrackInsightsRepository.save(sisMailTrackInsights);
                        }

                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });


        storesEmployeeList.forEach(emp -> {
            String email = (String) emp[2];
            String dronaEmail = (String) emp[3];
            Long empId = (Long) emp[0];
            if (email != null) {
                try {
                    String message = "<p>Dear Sir/Madam,</p>";
                    message += "<p>This email is to inform you that the following RIN and CRV Pending List to you :</p>";
                    message += "<table style=\"align: left; margin-top: 10px; margin-bottom: 10px; margin-left: 15px; max-width: 85%; font-size: 16px; border-collapse: collapse;\">";
                    message += "<thead>";
                    message += "<tr>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:5% !important;\">SN</th>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:20% !important;\">SIR No & Date</th>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:25% !important;\">SONo & Date</th>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Description</th>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Recieved Qty</th>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">RIN No & Date</th>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">CRV No & Date</th>";
                    message += "<th style=\"text-align: center; width: 500px; border: 1px solid black; padding: 5px; padding-left: 15px;width:10% !important;\">Consigner Name</th>";
                    message += "</tr>";
                    message += "</thead>";
                    int count1 = 1;
                    for (Object[] row : rinCrvPendingList) {
                        message += "<tr>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + count1++ + "</td>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + row[2] + "<br>"+ sdf.format(row[3]) +"</td>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: left\">" + row[4] + "<br>"+ sdf.format(row[5])  +"</td>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" +row[6]  +"</td>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + row[7] + "</td>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + (row[15] != null ? row[15] : "-") + "<br>"+ (row[16] != null ? sdf.format(row[16]) : "-") + "</td>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + (row[17] != null ? row[17] : "-") +"<br>"+ (row[18] != null ? sdf.format(row[18]) : "-") + "</td>";
                        message += "<td style=\"border: 1px solid black; padding: 5px; text-align: center\">" + row[9]+"</td>";
                        message += "</tr>";
                    }
                    message += "</tbody></table>";
                    message += "<p>Important Note: This is an automated message. Kindly avoid responding.</p>";
                    message += "<p>Regards,<br>LRDE-SIS Team</p>";

                    int sendResult = controller.sendMessage(email, subject, message);
                    if(dronaEmail!=null){
                        controller.sendMessage1(dronaEmail, subject, message);
                    }
                    if (sendResult > 0) {
                        SisMailTrackInsights sisMailTrackInsights = new SisMailTrackInsights();
                        sisMailTrackInsights.setMailTrackingId(mailTrackId);
                        sisMailTrackInsights.setMailStatus("S");
                        sisMailTrackInsights.setEmpId(empId);
                        sisMailTrackInsights.setCreatedDate(LocalDateTime.now());
                        sisMailTrackInsights.setMailSentDate(LocalDateTime.now());
                        sisMailTrackInsightsRepository.save(sisMailTrackInsights);
                        mailSendSuccessCount.getAndIncrement();
                    } else {
                        SisMailTrackInsights sisMailTrackInsights = new SisMailTrackInsights();
                        sisMailTrackInsights.setMailTrackingId(mailTrackId);
                        sisMailTrackInsights.setMailStatus("N");
                        sisMailTrackInsights.setEmpId(empId);
                        sisMailTrackInsights.setCreatedDate(LocalDateTime.now());
                        sisMailTrackInsightsRepository.save(sisMailTrackInsights);
                    }

                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Optional<SisMailTrack> updateMailTrack=sisMailTrackRepository.findById(mailTrackId);
        if(updateMailTrack.isPresent()) {
         SisMailTrack data = updateMailTrack.get();
         data.setMailSentCount((long)mailSendSuccessCount.get());
         data.setMailSentDateTime(LocalDateTime.now());
         sisMailTrackRepository.save(data);
        }
        projectEmployeeMap.keySet().forEach(System.out::println);

        return 1;
    }



    public int sendMessage(String toEmail, String subject, String msg,MailConfigurationDto mailDetails,SupplyOrderEmployeeMailTrack empMailDetails) throws Exception  {
        int status=0;
        try
        {
            if (mailDetails != null) {
                Properties properties = System.getProperties();
                properties.setProperty("mail.smtp.host", mailDetails.getHost());
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.port", mailDetails.getPort());
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                Session session = Session.getDefaultInstance(properties, new Authenticator() {
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(mailDetails.getUsername(), mailDetails.getPassword());
                    }
                });

                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(mailDetails.getUsername()));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                    message.setSubject(subject);

                    // Create the message body part
                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(msg);
                    messageBodyPart.setContent(msg, "text/html");

                    // Create a multipart message
                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageBodyPart);

                    messageBodyPart = new MimeBodyPart();
                    String filename = empMailDetails.getFilePath();

                    DataSource source = new FileDataSource(env.getProperty("SupplyOrderExcelFilePath")+filename);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName("Supply Order List.xlsx");
                    multipart.addBodyPart(messageBodyPart);
                    message.setContent(multipart);
                    Transport.send(message);
                    status = 1; // mail sent successfully
                } catch (MessagingException e) {
                    e.printStackTrace();
                    status = 0;
                }
            } else {
                status = 0;
            }
            return status;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    public int exportExcelFile(String empNo,List<Object[]> list,SupplyOrderEmployeeMailTrack empMailDetails,SupplyOrderMailTrack mail) throws Exception  {
        int status=0;
        try
        {
            int rowNo=0;
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("List Of Order(s)");
            sheet.setColumnWidth(0, 1000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 3400);
            sheet.setColumnWidth(3, 3400);
            sheet.setColumnWidth(4, 3400);
            sheet.setColumnWidth(5, 10000);
            sheet.setColumnWidth(6, 10000);

            XSSFFont font = ((XSSFWorkbook) workbook).createFont();
            font.setFontName("Times New Roman");
            font.setFontHeightInPoints((short) 10);
            font.setBold(true);


            CellStyle t_header_style = workbook.createCellStyle();
            t_header_style.setLocked(true);
            t_header_style.setFont(font);
            t_header_style.setWrapText(true);
            t_header_style.setAlignment(HorizontalAlignment.CENTER);
            t_header_style.setVerticalAlignment(VerticalAlignment.CENTER);
            CellStyle t_body_style = workbook.createCellStyle();
            t_body_style.setWrapText(true);

            CellStyle styleRight = workbook.createCellStyle();
            styleRight.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle styleLeft = workbook.createCellStyle();
            styleLeft.setAlignment(HorizontalAlignment.LEFT);

            CellStyle SubHeading = workbook.createCellStyle();
            SubHeading.setFont(font);
            SubHeading.setWrapText(true);
            SubHeading.setAlignment(HorizontalAlignment.LEFT);

            CellStyle styleCenter = workbook.createCellStyle();
            styleCenter.setAlignment(HorizontalAlignment.CENTER);

            CellStyle file_header_Style = workbook.createCellStyle();
            file_header_Style.setLocked(true);
            file_header_Style.setFont(font);
            file_header_Style.setWrapText(true);
            file_header_Style.setAlignment(HorizontalAlignment.CENTER);
            file_header_Style.setVerticalAlignment(VerticalAlignment.CENTER);

            String Title="List of Order(s) DP Expiring in next 30 days";

            Row file_header_row = sheet.createRow(rowNo++);
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,6));
            Cell cell= file_header_row.createCell(0);
            cell.setCellValue(Title);
            file_header_row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            cell.setCellStyle(file_header_Style);


            Row t_header_row = sheet.createRow(rowNo++);
            cell= t_header_row.createCell(0);
            cell.setCellValue("SN");
            cell.setCellStyle(t_header_style);

            cell= t_header_row.createCell(1);
            cell.setCellValue("Order No");
            cell.setCellStyle(t_header_style);

            cell= t_header_row.createCell(2);
            cell.setCellValue("Order Date");
            cell.setCellStyle(t_header_style);

            cell= t_header_row.createCell(3);
            cell.setCellValue("DP Date");
            cell.setCellStyle(t_header_style);

            cell= t_header_row.createCell(4);
            cell.setCellValue("Project Code");
            cell.setCellStyle(t_header_style);

            cell= t_header_row.createCell(5);
            cell.setCellValue("Item");
            cell.setCellStyle(t_header_style);

            cell= t_header_row.createCell(6);
            cell.setCellValue("Vendor");
            cell.setCellStyle(t_header_style);

            int sn=1;
            if(list!=null && !list.isEmpty()) {
                for(Object[] obj: list)
                {
                    Row t_body_row = sheet.createRow(rowNo++);
                    cell= t_body_row.createCell(0);
                    cell.setCellValue(sn++);
                    cell.setCellStyle(t_body_style);
                    cell.setCellStyle(styleCenter);

                    cell= t_body_row.createCell(1);
                    cell.setCellValue(obj[2].toString());
                    cell.setCellStyle(t_body_style);
                    cell.setCellStyle(styleLeft);

                    cell= t_body_row.createCell(2);
                    if(obj[3]!=null)
                    {
                        cell.setCellValue(rdf.format(sdf2.parse(obj[3].toString())));
                    }
                    else
                    {
                        cell.setCellValue("--");
                    }
                    cell.setCellStyle(t_body_style);
                    cell.setCellStyle(styleCenter);

                    cell= t_body_row.createCell(3);
                    if(obj[5]!=null)
                    {
                        cell.setCellValue(rdf.format(sdf2.parse(obj[5].toString())));
                    }
                    else
                    {
                        cell.setCellValue("--");
                    }
                    cell.setCellStyle(t_body_style);
                    cell.setCellStyle(styleCenter);

                    cell= t_body_row.createCell(4);
                    cell.setCellValue(obj[7].toString());
                    cell.setCellStyle(t_body_style);
                    cell.setCellStyle(styleLeft);

                    cell= t_body_row.createCell(5);
                    cell.setCellValue(obj[8].toString());
                    cell.setCellStyle(t_body_style);
                    cell.setCellStyle(styleLeft);

                    cell= t_body_row.createCell(6);
                    cell.setCellValue(obj[10].toString());
                    cell.setCellStyle(t_body_style);
                    cell.setCellStyle(styleLeft);

                }

                try {
                    String fileName="SupplyOrderDetails/"+empNo+"-("+mail.getMailDate()+")-"+mail.getMailId()+".xlsx";
                    Path path = Paths.get(env.getProperty("SupplyOrderExcelFilePath")+fileName);

                    if (Files.notExists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }

                    try (FileOutputStream fileOut = new FileOutputStream(path.toFile())) {
                        workbook.write(fileOut);
                    }
                    workbook.close();
                    empMailDetails.setFilePath(fileName);
                    supplyOrderEmployeeMailTrackRepository.save(empMailDetails);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return status;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isValidEmailOrMobileNo(String data, String Type) {
        if (data == null || Type == null) {
            return false;
        }
        data = data.trim();
        String patternForAll = null;
        if (Type.equalsIgnoreCase("E")) {
            patternForAll = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        } else if (Type.equalsIgnoreCase("M")) {
            patternForAll = "^[0-9]{10}$";
        }

        if (patternForAll == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(patternForAll);
        Matcher matcher = pattern.matcher(data);

        return matcher.matches();
    }

}
