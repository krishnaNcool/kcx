package com.kcxutilities.core.process.email.context;

import com.kcxutilities.model.QueryReportNotificationProcessModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Context for generating query report notification emails.
 */
public class QueryReportNotificationEmailContext extends AbstractEmailContext<QueryReportNotificationProcessModel> {
    @Resource(name = "modelService")
    private ModelService modelService;
    
    @Resource(name = "userService")
    private UserService userService;
    
    private String message;
    private String subject;
    private String query;
    private List<String> headerFieldNames;
    private String dateTypeOfFieldName;
    private String report;

    @Override
    public void init(final QueryReportNotificationProcessModel processModel, final EmailPageModel emailPageModel) {
        super.init(processModel, emailPageModel);
        
        // Set data from the process model
        this.message = processModel.getMessage();
        this.subject = processModel.getSubject();
//        this.query = processModel.getQuery();
//        this.headerFieldNames = (List<String>) processModel.getHeaderFieldNames();
//        this.dateTypeOfFieldName = processModel.getDateTypeOfFieldName();
//        this.report = processModel.getReport();

    }

    @Override
    protected BaseSiteModel getSite(final QueryReportNotificationProcessModel processModel) {
        return processModel.getSite();
    }

    @Override
    protected CustomerModel getCustomer(final QueryReportNotificationProcessModel processModel) {
        return (CustomerModel) getUserService().getUserForUID("admin");
    }

    @Override
    protected LanguageModel getEmailLanguage(final QueryReportNotificationProcessModel processModel) {
        return processModel.getLanguage();
    }

    public String getMessage() {
        return message;
    }

    public String getSubject() {
        return subject;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getHeaderFieldNames() {
        return headerFieldNames;
    }

    public String getDateTypeOfFieldName() {
        return dateTypeOfFieldName;
    }

    public String getReport() {
        return report;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public UserService getUserService() {
        return userService;
    }
}