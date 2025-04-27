package com.kcxutilities.core.job;

import com.kcxutilities.core.service.DefaultCronJobNotificationService;
import com.kcxutilities.model.CustomQueryNotificationCronJobModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Job implementation for the CustomQueryNotificationCronJob
 */
public class CustomQueryNotificationJob extends AbstractJobPerformable<CustomQueryNotificationCronJobModel> {

    private static final Logger LOG = Logger.getLogger(CustomQueryNotificationJob.class);

    private DefaultCronJobNotificationService cronJobNotificationService;

    @Override
    public PerformResult perform(final CustomQueryNotificationCronJobModel cronJobModel) {
        LOG.info("Starting CustomQueryNotificationJob execution");

        try {
            cronJobNotificationService.generateCustomQueryReport(cronJobModel);
            LOG.info("CustomQueryNotificationJob executed successfully");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        } catch (Exception e) {
            LOG.error("Error executing CustomQueryNotificationJob: " + e.getMessage(), e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
    }

    @Required
    public void setCronJobNotificationService(DefaultCronJobNotificationService cronJobNotificationService) {
        this.cronJobNotificationService = cronJobNotificationService;
    }
}
