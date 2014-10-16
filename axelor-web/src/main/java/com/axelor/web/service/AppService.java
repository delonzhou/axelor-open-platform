/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.web.service;

import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.axelor.web.AppSessionListener;
import com.axelor.web.internal.AppInfo;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/app")
public class AppService extends AbstractService {

	@GET
	@Path("info")
	public String info() {
		return AppInfo.asJson();
	}

	@GET
	@Path("sysinfo")
	public Properties getSystemInfo() {
		final Properties info = new Properties();
		final Runtime runtime = Runtime.getRuntime();

		int mb = 1024;

		info.setProperty("osName", System.getProperty("os.name"));
		info.setProperty("osArch", System.getProperty("os.arch"));
		info.setProperty("osVersion", System.getProperty("os.version"));

		info.setProperty("javaRuntime", System.getProperty("java.runtime.name"));
		info.setProperty("javaVersion", System.getProperty("java.runtime.version"));

		info.setProperty("memTotal", (runtime.totalMemory() / mb) + " Kb");
		info.setProperty("memMax", (runtime.maxMemory() / mb) + " Kb");
		info.setProperty("memUsed", ((runtime.totalMemory() - runtime.freeMemory()) / mb) + " Kb");
		info.setProperty("memFree", (runtime.freeMemory() / mb) + " Kb");

		info.setProperty("sessionCount", "" + AppSessionListener.getActiveSessions().size());

		return info;
	}
}
