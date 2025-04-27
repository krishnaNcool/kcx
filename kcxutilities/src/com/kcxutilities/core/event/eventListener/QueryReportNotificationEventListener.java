package com.kcxutilities.core.event.eventListener;

import com.kcxutilities.core.event.QueryReportNotificationEvent;
import com.kcxutilities.model.QueryReportNotificationProcessModel;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.processengine.BusinessProcessService;

import javax.annotation.Resource;

public class QueryReportNotificationEventListener extends AbstractEventListener<QueryReportNotificationEvent> {

    @Resource(name = "modelService")
    private ModelService modelService;
    
    @Resource(name = "businessProcessService")
    private BusinessProcessService businessProcessService;
    
    @Override
    protected void onEvent(final QueryReportNotificationEvent event) {
        try {
            // Create a new process model directly
            final QueryReportNotificationProcessModel processModel = modelService.create(QueryReportNotificationProcessModel.class);
            processModel.setCode("queryReportNotificationEmailProcess-" + System.currentTimeMillis());
            processModel.setProcessDefinitionName("queryReportNotificationEmailProcess");
            
            // Set necessary data on the process model
            processModel.setAttachments(event.getAttachmentList());
            processModel.setToAddresses(event.getToAddresses());
            processModel.setCcAddresses(event.getCcAddressList());
            processModel.setMessage(event.getQueryName());
            processModel.setSubject(event.getSubject());
            
            // Save the process model
            modelService.save(processModel);
            
            // Start the process
            businessProcessService.startProcess(processModel);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error creating process: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected ModelService getModelService() {
        return modelService;
    }

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }
}
