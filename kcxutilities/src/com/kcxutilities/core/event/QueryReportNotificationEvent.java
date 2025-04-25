package com.kcxutilities.core.event;

import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import java.util.Collection;

public class QueryReportNotificationEvent extends AbstractEvent {
    private final String message;
    private final String subject;
    private final String query;
    private final Collection<String> headerFieldNames;
    private final Collection<String> dateTypeOfFieldName;
    private final CatalogUnawareMediaModel report;

    public QueryReportNotificationEvent(final String message, final String subject, final String query,
                                        final Collection<String> headerFieldNames, final Collection<String> dateTypeOfFieldName,
                                        final CatalogUnawareMediaModel report) {
        this.message = message;
        this.subject = subject;
        this.query = query;
        this.headerFieldNames = headerFieldNames;
        this.dateTypeOfFieldName = dateTypeOfFieldName;
        this.report = report;
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

    public Collection<String> getHeaderFieldNames() {
        return headerFieldNames;
    }

    public Collection<String> getDateTypeOfFieldName() {
        return dateTypeOfFieldName;
    }

    public CatalogUnawareMediaModel getReport() {
        return report;
    }
}
