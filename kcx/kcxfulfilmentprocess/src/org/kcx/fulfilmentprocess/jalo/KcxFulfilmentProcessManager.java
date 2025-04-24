/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.kcx.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.kcx.fulfilmentprocess.constants.KcxFulfilmentProcessConstants;

public class KcxFulfilmentProcessManager extends GeneratedKcxFulfilmentProcessManager
{
	public static final KcxFulfilmentProcessManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (KcxFulfilmentProcessManager) em.getExtension(KcxFulfilmentProcessConstants.EXTENSIONNAME);
	}
	
}
