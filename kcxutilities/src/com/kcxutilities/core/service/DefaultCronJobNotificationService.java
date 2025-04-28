package com.kcxutilities.core.service;

import com.kcxutilities.constants.KcxutilitiesConstants;
import com.kcxutilities.core.dao.DefaultCronJobNotificationDao;
import com.kcxutilities.core.event.QueryReportNotificationEvent;
import com.kcxutilities.model.CustomQueryNotificationCronJobModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class DefaultCronJobNotificationService {

    @Resource(name = "modelService")
    private ModelService modelService;

    @Resource(name = "mediaService")
    private MediaService mediaService;

    @Resource(name = "eventService")
    private EventService eventService;

    @Resource(name = "baseSiteService")
    private BaseSiteService baseSiteService;

    @Resource(name = "commonI18NService")
    private CommonI18NService commonI18NService;

    @Resource(name = "baseStoreService")
    private BaseStoreService baseStoreService;

    @Resource(name = "cronJobNotificationDao")
    private DefaultCronJobNotificationDao cronJobNotificationDao;

    private static final Logger LOG = Logger.getLogger(DefaultCronJobNotificationService.class.getName());

    public void generateCustomQueryReport(CustomQueryNotificationCronJobModel cronJobModel) {
        List<List<Object>> resultList = cronJobNotificationDao.getQueryResultList(
                cronJobModel.getCustomQuery(),
                cronJobModel.getParameters(),
                cronJobModel.getFieldClassType()
        );

        Map<Integer, Object[]> data = new HashMap<>();
        data.put(0, new ArrayList<>(cronJobModel.getFieldNames()).toArray());

        int count = 1;
        for (List<Object> row : resultList) {
            data.put(count++, row.toArray());
        }

        try {
            Date currentDate = new Date();
            EmailAttachmentModel attachment = getCustomExcelMedia(
                    data,
                    cronJobModel.getSubject() + currentDate.toString(),
                    "xlsx",
                    cronJobModel
            );
            triggerQueryCronJobEmailProcess(attachment, cronJobModel);
        } catch (Exception e) {
            LOG.error("Error while creating attachment & sending email " + e.getMessage());
        }
    }

    public void triggerQueryCronJobEmailProcess(EmailAttachmentModel attachment, CustomQueryNotificationCronJobModel cronjob) {

        List<EmailAttachmentModel> attachmentList = Collections.singletonList(attachment);
        List<String> ccAddressList = new ArrayList<>(cronjob.getCcAddresses());
        List<String> toAddresses = new ArrayList<>(cronjob.getToAddresses());
        BaseSiteModel baseSiteModel = baseSiteService.getBaseSiteForUID("kcx");
        BaseStoreModel baseStoreModel = baseStoreService.getBaseStoreForUid("kcx");
        CurrencyModel currencyModel = commonI18NService.getCurrentCurrency();
        LanguageModel languageModel = commonI18NService.getCurrentLanguage();
        String queryName = cronjob.getSubject();

        eventService.publishEvent(new QueryReportNotificationEvent(
                        baseStoreModel,
                        baseSiteModel,
                        currencyModel,
                        languageModel,
                        attachmentList,
                        toAddresses,
                        ccAddressList,
                        queryName,
                        cronjob.getSubject()
                )
        );
    }

    public EmailAttachmentModel getCustomExcelMedia(
            Map<Integer, Object[]> data,
            String fileName,
            String fileExtension,
            CustomQueryNotificationCronJobModel cronJobModel
    ) {
        ByteArrayOutputStream outputStream = generateReport(data);
        EmailAttachmentModel attachment = modelService.create(EmailAttachmentModel.class);

        attachment.setCode(fileName + "_" + System.currentTimeMillis());
        attachment.setAltText(fileName + "." + fileExtension);
        attachment.setRealFileName(fileName + "." + fileExtension);
        attachment.setFolder(mediaService.getFolder(KcxutilitiesConstants.FOLDER_EMAIL_ATTACHMENTS));

        modelService.save(attachment);
        mediaService.setStreamForMedia(attachment, new ByteArrayInputStream(outputStream.toByteArray()));
        modelService.refresh(attachment);

        return attachment;
    }

    public ByteArrayOutputStream generateReport(Map<Integer, Object[]> data) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("transactions");

        XSSFCellStyle headerStyle = getHeaderStyle(workbook);
        XSSFCellStyle dateStyle = getDateStyle(workbook);
        XSSFCellStyle simpleStyle = getSimpleStyle(workbook);

        for (Map.Entry<Integer, Object[]> entry : data.entrySet()) {
            try {
                Row row = sheet.createRow(entry.getKey());
                Object[] objArr = entry.getValue();
                int columnCount = 0;

                for (Object field : objArr) {
                    Cell cell = row.createCell(columnCount++);

                    if (entry.getKey() == 0) {
                        cell.setCellStyle(headerStyle);
                        cell.setCellValue((String) field);
                    } else if (field instanceof Double) {
                        cell.setCellStyle(simpleStyle);
                        cell.setCellValue((Double) field);
                    } else if (field instanceof Integer) {
                        cell.setCellStyle(simpleStyle);
                        cell.setCellValue((Integer) field);
                    } else if (field instanceof Long) {
                        cell.setCellStyle(simpleStyle);
                        cell.setCellValue((Long) field);
                    } else if (field instanceof Date) {
                        cell.setCellStyle(dateStyle);
                        cell.setCellValue((Date) field);
                    } else {
                        cell.setCellStyle(simpleStyle);
                        cell.setCellValue(String.valueOf(field));
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while creating sheet for key " + e.getMessage());
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            LOG.error("Error writing Excel file", e);
        }

        return outputStream;
    }

    protected XSSFCellStyle getHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        setBorderToCell(headerStyle);
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    protected XSSFCellStyle getSimpleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle simpleStyle = workbook.createCellStyle();
        setBorderToCell(simpleStyle);
        return simpleStyle;
    }

    protected static XSSFCellStyle getDateStyle(XSSFWorkbook workbook) {
        XSSFCellStyle dateStyle = workbook.createCellStyle();
        setBorderToCell(dateStyle);
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm:ss"));
        return dateStyle;
    }

    protected static void setBorderToCell(XSSFCellStyle cellStyle) {
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
    }

    // Getters and setters
    public void setCommonI18NService(CommonI18NService commonI18NService) {
        this.commonI18NService = commonI18NService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }
}
