package business.representation;

import java.util.Date;
import java.util.List;

import org.activiti.engine.task.Attachment;

import business.models.Lab;

public class RequestRepresentation {

    private String processInstanceId;
    private String requesterId;
    private String requesterName;
    private String status;
    private Date dateCreated; 
    private Lab lab;
    private String assignee;
    private String assigneeName;
    
    private String title;
    private String description;
    private String motivation;
    private boolean statisticsRequest;
    private boolean paReportRequest;
    private boolean materialsRequest;
    private Date returnDate;
    private boolean limitedToCohort;
    private String contactPersonName;

    private List<AttachmentRepresentation> attachments;
    private List<AttachmentRepresentation> agreementAttachments;
    
    // Palga specific
    private boolean requesterValid;
    private boolean requesterAllowed;
    private boolean contactPersonAllowed;
    private boolean requesterLabValid;
    private boolean agreementReached;
    
    public RequestRepresentation() {
        
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public boolean isStatisticsRequest() {
        return statisticsRequest;
    }

    public void setStatisticsRequest(boolean statisticsRequest) {
        this.statisticsRequest = statisticsRequest;
    }

    public boolean isPaReportRequest() {
        return paReportRequest;
    }

    public void setPaReportRequest(boolean paReportRequest) {
        this.paReportRequest = paReportRequest;
    }

    public boolean isMaterialsRequest() {
        return materialsRequest;
    }

    public void setMaterialsRequest(boolean materialsRequest) {
        this.materialsRequest = materialsRequest;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isLimitedToCohort() {
        return limitedToCohort;
    }

    public void setLimitedToCohort(boolean limitedToCohort) {
        this.limitedToCohort = limitedToCohort;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public boolean isRequesterValid() {
        return requesterValid;
    }

    public void setRequesterValid(boolean requesterValid) {
        this.requesterValid = requesterValid;
    }

    public boolean isRequesterAllowed() {
        return requesterAllowed;
    }

    public void setRequesterAllowed(boolean requesterAllowed) {
        this.requesterAllowed = requesterAllowed;
    }

    public boolean isContactPersonAllowed() {
        return contactPersonAllowed;
    }

    public void setContactPersonAllowed(boolean contactPersonAllowed) {
        this.contactPersonAllowed = contactPersonAllowed;
    }

    public boolean isRequesterLabValid() {
        return requesterLabValid;
    }

    public void setRequesterLabValid(boolean requesterLabValid) {
        this.requesterLabValid = requesterLabValid;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public List<AttachmentRepresentation> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentRepresentation> attachments) {
        this.attachments = attachments;
    }

    public List<AttachmentRepresentation> getAgreementAttachments() {
        return agreementAttachments;
    }

    public void setAgreementAttachments(List<AttachmentRepresentation> agreementAttachments) {
        this.agreementAttachments = agreementAttachments;
    }

    public boolean isAgreementReached() {
        return agreementReached;
    }

    public void setAgreementReached(boolean agreementReached) {
        this.agreementReached = agreementReached;
    }
 
}
