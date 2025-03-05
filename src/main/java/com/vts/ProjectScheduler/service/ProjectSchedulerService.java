package com.vts.ProjectScheduler.service;

import com.vts.ProjectScheduler.dto.MailConfigurationDto;
import com.vts.ProjectScheduler.entity.ibas.Role;
import com.vts.ProjectScheduler.entity.ibas.SupplyOrderMailTrack;
import com.vts.ProjectScheduler.entity.pms.CCMView;
import com.vts.ProjectScheduler.entity.pms.FinanceChanges;
import com.vts.ProjectScheduler.entity.pms.IbasLabMaster;
import com.vts.ProjectScheduler.entity.pms.ProjectHoa;

import java.util.List;

public interface ProjectSchedulerService {

    public MailConfigurationDto getMailConfigByTypeOfHost(String type) throws Exception;

    public long getMailInitiatedCount(String trackingType) throws Exception;

    public long insertMailTrackInitiator(String trackingType) throws Exception;

    public List<Object[]> getDailyPendingReplyEmpData() throws Exception;

    public long insertDailyPendingInsights(long mailTrackingId) throws Exception;

    public long updateParticularEmpMailStatus(String mailPurpose,String mailStatus,long empId,long mailTrackingId) throws Exception;

    public long updateMailSuccessCount(long mailTrackingId,long mailSendSuccessCount,String trackingType) throws Exception;

    public long updateNoPendingReply(long mailTrackingId)throws Exception;

    public List<Object[]> getSummaryDistributedEmpData() throws Exception;

    public long insertSummaryDistributedInsights(long mailTrackingId)throws Exception;

    public long insertWeeklyPendingInsights(long mailTrackingId)throws Exception;

    public List<Object[]> getWeeklyPendingReplyEmpData() throws Exception;

    public long insertPmsMailTrackInitiator(String trackingType,int numberOfDays) throws Exception;

    public List<Object[]> weeklyActionList(int numberOfDays) throws Exception;

    public long insertPfmsMailTrackingInsights(long mailTrackingId, long empId, String actionNos, String trackingType,String mailStatus, String createdDate) throws Exception;

    public long updateEmailTrackingExpectedCount(long mailTrackingId, int finalResult,String sentDate) throws Exception;

    public List<Object[]> getTodaysMeetings(String date) throws Exception;

    public List<Object[]> committeeAttendance(String committeeScheduleId) throws Exception;

    public long insertPmsMailTrackInitiatorForMeetingMail(String trackingType, String date,String labCode) throws Exception;

    public long projectHoaUpdate(List<ProjectHoa> hoa, String username, List<IbasLabMaster> labDetails) throws Exception;

    public long projectFinanceChangesUpdate(List<FinanceChanges> monthly, List<FinanceChanges> weekly, List<FinanceChanges> today, String userId, String labCode) throws Exception;

    public Object getClusterId(String labCode) throws Exception;

    public long cCMViewDataUpdate(List<CCMView> cCMViewData, String labCode, String clusterId, String userId, String empId) throws Exception;

    public long projectHealthUpdate(String labCode, String userName) throws Exception;

    public long sendEmailAndSmsForSupplyOrderDetails() throws Exception;

    public int supplyOrderDetailsSettingToMail(Object empId, Object empNo, Object email, Object mobileNo, String officerType, SupplyOrderMailTrack mail, MailConfigurationDto mailDetails, Object empName) throws Exception;

    public List<Object[]>  pftsExpectedDailySummaryList()throws Exception;

    public long getPftsMailInitiatedCount(String trackingType) throws Exception;

    public long insertPftsMailTrackInitiator(String trackingType) throws Exception;

    public long insertPftsDailySummaryInsights(long pftsMailTrackingId) throws Exception;

    public long updateParticularEmpMailStatusInPfts(String mailPurpose,String mailStatus,String empNo,long pftsMailTrackingId) throws Exception;

    public long updateMailSuccessCountInPfts(long pftsMailTrackingId, long mailSendSucessCount,String trackingType) throws Exception;

    public long updatePftsNoPendingReply(long effectivelyFinalMailTrackingId) throws Exception;

    public List<Object[]> pftsDailySendEmployees() throws Exception;

    public long insertPftsDailyMailTrackInitiator(String trackingType) throws Exception;

    public long insertPftsDailyInsights(long pftsMailTrackingId) throws Exception;

    public List<Object[]> pftsDailySendEmployeesDetails(String forwardedTo) throws Exception;

    public int sendScheduleMailOfIrf(String labcode) throws Exception;


    public int RINCRVPendingList() throws Exception;
}
