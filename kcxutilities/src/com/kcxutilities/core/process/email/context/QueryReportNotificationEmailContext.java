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
import java.util.Collection;
import java.util.List;

/**
 * Context for generating query report notification emails.
 */
public class QueryReportNotificationEmailContext extends AbstractEmailContext<QueryReportNotificationProcessModel>
{
    @Resource(name = "modelService")
    private ModelService modelService;

    @Resource(name = "userService")
    private UserService userService;

    @Override
    public void init(final QueryReportNotificationProcessModel processModel, final EmailPageModel emailPageModel)
    {
        super.init(processModel, emailPageModel);

        if (processModel != null)
        {
            put("subject", processModel.getSubject() != null ? processModel.getSubject() : "No Subject");

            final Collection<String> toAddresses = processModel.getToAddresses();
            if (toAddresses != null && !toAddresses.isEmpty()) {
                put("toEmail", toAddresses.stream().findFirst().orElse("")); // Default to empty string if no email
                put("toEmails", toAddresses);
            } else {
                throw new IllegalStateException("Missing ToEmail in AbstractEmailContext");
            }
        }
    }

    @Override
    protected BaseSiteModel getSite(final QueryReportNotificationProcessModel processModel)
    {
        return processModel != null ? processModel.getSite() : null;
    }

    @Override
    protected CustomerModel getCustomer(final QueryReportNotificationProcessModel processModel)
    {
        return null; // Avoid using the customer as the recipient
    }

    protected List<String> getToEmails(final QueryReportNotificationProcessModel processModel) {
        return processModel != null && processModel.getToAddresses() != null
                ? List.copyOf(processModel.getToAddresses())
                : List.of();
    }

    @Override
    protected LanguageModel getEmailLanguage(final QueryReportNotificationProcessModel processModel)
    {
        return processModel != null ? processModel.getLanguage() : null;
    }
}