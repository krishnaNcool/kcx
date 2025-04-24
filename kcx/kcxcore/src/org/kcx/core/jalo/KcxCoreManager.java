/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.kcx.core.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.kcx.core.constants.KcxCoreConstants;
import org.kcx.core.setup.CoreSystemSetup;


/**
 * Do not use, please use {@link CoreSystemSetup} instead.
 * 
 */
public class KcxCoreManager extends GeneratedKcxCoreManager
{
	public static final KcxCoreManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (KcxCoreManager) em.getExtension(KcxCoreConstants.EXTENSIONNAME);
	}
}
