package com.kcxutilities.core.event;

import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;

public class QueryReportNotificationEvent extends AbstractEvent {

    private final BaseStoreModel baseStoreModel;
    private final BaseSiteModel baseSiteModel;
    private final CurrencyModel currencyModel;
    private final LanguageModel languageModel;
    private final List<EmailAttachmentModel> attachmentList;
    private final List<String> toAddresses;
    private final List<String> ccAddressList;
    private final String queryName;
    private final String subject;

    public QueryReportNotificationEvent(BaseStoreModel baseStoreModel, BaseSiteModel baseSiteModel, CurrencyModel currencyModel, LanguageModel languageModel, List<EmailAttachmentModel> attachmentList, List<String> toAddresses, List<String> ccAddressList, String queryName, String subject) {
        this.baseStoreModel = baseStoreModel;
        this.baseSiteModel = baseSiteModel;
        this.currencyModel = currencyModel;
        this.languageModel = languageModel;
        this.attachmentList = attachmentList;
        this.toAddresses = toAddresses;
        this.ccAddressList = ccAddressList;
        this.queryName = queryName;
        this.subject = subject;
    }

    public BaseStoreModel getBaseStoreModel() {
        return baseStoreModel;
    }

    public BaseSiteModel getBaseSiteModel() {
        return baseSiteModel;
    }

    public CurrencyModel getCurrencyModel() {
        return currencyModel;
    }

    public LanguageModel getLanguageModel() {
        return languageModel;
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