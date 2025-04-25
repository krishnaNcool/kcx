package com.kcxutilities.core.event.eventListener;

import com.kcxutilities.core.event.QueryReportNotificationEvent;
import com.kcxutilities.model.QueryReportNotificationProcessModel;
import de.hybris.platform.acceleratorservices.site.AbstractAcceleratorSiteEventListener;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.processengine.BusinessProcessService;

import javax.annotation.Resource;

public class QueryReportNotificationEventListener extends AbstractAcceleratorSiteEventListener<QueryReportNotificationEvent> {

    @Resource(name = "modelService")
    private ModelService modelService;
    
    @Resource(name = "businessProcessService")
    private BusinessProcessService businessProcessService;
    protected void onSiteEvent(final QueryReportNotificationEvent event) {
        // Create a new process for handling the query report notification
        final QueryReportNotificationProcessModel processModel = (QueryReportNotificationProcessModel) getBusinessProcessService().createProcess(
                "queryReportNotificationEmailProcess-" + System.currentTimeMillis(),
                "queryReportNotificationEmailProcess");

        // Set necessary data on the process model
        processModel.setMessage(event.getMessage());
        processModel.setSubject(event.getSubject());
        processModel.setQuery(event.getQuery());
        processModel.setHeaderFieldNames(event.getHeaderFieldNames());
        processModel.setDateTypeOfFieldName(event.getDateTypeOfFieldName());
        processModel.setReport(event.getReport());

        // Save and start the process
        getModelService().save(processModel);
        getBusinessProcessService().startProcess(processModel);
    }

    protected ModelService getModelService() {
        return modelService;
    }

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    @Override
    protected SiteChannel getSiteChannelForEvent(final QueryReportNotificationEvent event) {
        // Assuming you have a way to determine the site channel from the event
        // This might involve accessing a site model or similar
        // For now, returning a default or null if not applicable
        return null; // Replace with actual logic if needed
    }
}
