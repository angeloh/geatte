package com.geatte.app.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;

public class DBHelper {
    /**
     * Initialize PMF - we use a context attribute, so other servlets can be
     * share the same instance. This is similar with a shared static field, but
     * avoids dependencies.
     */
    public static PersistenceManagerFactory getPMF(ServletContext ctx) {
	PersistenceManagerFactory pmfFactory = (PersistenceManagerFactory) ctx
	.getAttribute(PersistenceManagerFactory.class.getName());
	if (pmfFactory == null) {
	    pmfFactory = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	    ctx.setAttribute(PersistenceManagerFactory.class.getName(), pmfFactory);
	}
	return pmfFactory;
    }
}