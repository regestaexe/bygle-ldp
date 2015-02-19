package org.bygle.listeners;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;
import org.bygle.endpoint.managing.EndPointManager;
import org.bygle.utils.BygleSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class BygleListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(BygleListener.class);

	public void contextDestroyed(ServletContextEvent arg0) {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		Driver d = null;
		while (drivers.hasMoreElements()) {
			try {
				d = drivers.nextElement();
				DriverManager.deregisterDriver(d);
				logger.warn(String.format("Driver %s deregistered", d));
			} catch (SQLException ex) {
				logger.warn(String.format("Error deregistering driver %s", d), ex);
			}
		}
	}

	public void contextInitialized(ServletContextEvent arg0) {
		try {
			if (BygleSystemUtils.getStringProperty("hibernate.hbm2ddl.auto").equals("create")) {
				BygleSystemUtils.getPropertiesConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");
				BygleSystemUtils.getPropertiesConfiguration().save();
			} else if (BygleSystemUtils.getStringProperty("hibernate.hbm2ddl.auto").equals("drop")) {
				BygleSystemUtils.getPropertiesConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");
				BygleSystemUtils.getPropertiesConfiguration().save();
				WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(arg0.getServletContext());
				EndPointManager endPointManager = (EndPointManager) springContext.getBean("endPointManager");
				endPointManager.dropEndpoint();
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
