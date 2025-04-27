package com.kcxutilities.core.event;

import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;
import java.util.List;

public class QueryReportNotificationEvent extends AbstractEvent {

    private final List<EmailAttachmentModel> attachmentList;
    private final List<String> toAddresses;
    private final List<String> ccAddressList;
    private final String queryName;
    private final String subject;

    public QueryReportNotificationEvent(List<EmailAttachmentModel> attachmentList, List<String> toAddresses, 
                                       List<String> ccAddressList, String queryName, String subject) {
        this.attachmentList = attachmentList;
        this.toAddresses = toAddresses;
        this.ccAddressList = ccAddressList;
        this.queryName = queryName;
        this.subject = subject;
    }

    public List<EmailAttachmentModel> getAttachmentList() {
        return attachmentList;
    }

    public List<String> getToAddresses() {
        return toAddresses;
    }

    public List<String> getCcAddressList() {
        return ccAddressList;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getSubject() {
        return subject;
    }
}
