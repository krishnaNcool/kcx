package com.kcxutilities.core.event.eventListener;

import com.kcxutilities.core.event.QueryReportNotificationEvent;
import com.kcxutilities.model.QueryReportNotificationProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

public class QueryReportNotificationEventListener extends AbstractEventListener<QueryReportNotificationEvent> {

    private ModelService modelService; // provide getter and setter
    private BusinessProcessService businessProcessService; // provide getter and setter

    @Override
    protected void onEvent(final QueryReportNotificationEvent event) {
        final QueryReportNotificationProcessModel processModel =
                (QueryReportNotificationProcessModel) getBusinessProcessService()
                        .createProcess("queryReportNotificationEmailProcess-" + System.currentTimeMillis(), "queryReportNotificationEmailProcess");

        // Set the required fields from event to process model
        processModel.setAttachments(event.getAttachmentList());
        processModel.setToAddresses(event.getToAddresses());
        processModel.setCcAddresses(event.getCcAddressList());
        processModel.setMessage(event.getQueryName());
        processModel.setSubject(event.getQueryName());

        // Inherited fields from StoreFrontCustomerProcess
        processModel.setStore(event.getBaseStoreModel());
        processModel.setSite(event.getBaseSiteModel());
        processModel.setCurrency(event.getCurrencyModel());
        processModel.setLanguage(event.getLanguageModel());

        getModelService().save(processModel);
        getBusinessProcessService().startProcess(processModel);
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }
}
