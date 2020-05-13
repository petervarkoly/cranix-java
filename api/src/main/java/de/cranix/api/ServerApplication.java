/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.cranix.api;

import de.cranix.api.auth.CrxAuthorizer;

import de.cranix.api.auth.CrxTokenAuthenticator;
import de.cranix.api.health.TemplateHealthCheck;
import de.cranix.api.resourceimpl.*;
import de.cranix.api.resources.*;
import de.cranix.dao.Session;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import java.io.File;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import de.extis.xpluginlib.PluginHandler;

//import de.cranix.dao.controller.GetJPAInf;
//import java.io.IOException;
//import java.net.ServerSocket;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class ServerApplication extends Application<ServerConfiguration> {

	public static void main(String[] args) throws Exception {
		new ServerApplication().run(args);
	}

	@Override
	public String getName() {
		return "CRANIX API";
	}

	@Override
	public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
		bootstrap.addBundle(new SwaggerBundle<ServerConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ServerConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	public void run(ServerConfiguration configuration, Environment environment) {


		@SuppressWarnings("rawtypes")
		AuthFilter tokenAuthorizer = new OAuthCredentialAuthFilter.Builder<Session>()
		.setAuthenticator(new CrxTokenAuthenticator())
		.setAuthorizer(new CrxAuthorizer())
		.setPrefix("Bearer")
		.buildAuthFilter();

		environment.jersey().register(new AuthDynamicFeature(tokenAuthorizer));
		environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Session.class));
		environment.jersey().register(RolesAllowedDynamicFeature.class);

		environment.jersey().register(MultiPartFeature.class);

		final SchedulerResource schedulerResource = new SchedulerResourceImpl();
		environment.jersey().register(schedulerResource);

		final SystemResource systemResource = new SystemResourceImpl();
		environment.jersey().register(systemResource);

		//TODO check if allowed
		final AdHocLanResource adHocLanResource = new AdHocLanResourceImpl();
		environment.jersey().register(adHocLanResource);

		final SessionsResource sessionsResource = new SessionsResourceImpl();
		environment.jersey().register(sessionsResource);

		final SelfManagementResource selfManagementResource = new SelfManagementResourceImpl();
		environment.jersey().register(selfManagementResource);

		final RoomResource roomsResource = new RoomRescourceImpl();
		environment.jersey().register(roomsResource);

		final UserResource usersResource = new UserResourceImpl();
		environment.jersey().register(usersResource);

		final GroupResource groupsResource = new GroupResourceImpl();
		environment.jersey().register(groupsResource);

		final DeviceResource devicesResource = new DeviceResourceImpl();
		environment.jersey().register(devicesResource);

		final PrinterResource printerResource = new PrinterResourceImpl();
		environment.jersey().register(printerResource);

		final CloneToolResource cloneToolResource = new CloneToolResourceImpl();
		environment.jersey().register(cloneToolResource);

		final HwconfResource hwconfResource = new HwconfResourceImpl();
		environment.jersey().register(hwconfResource);

		final CategoryResource categoryResource = new CategoryResourceImpl();
		environment.jersey().register(categoryResource);

		final SoftwareResource softwareResource = new SoftwareResourceImpl();
		environment.jersey().register(softwareResource);

		final EducationResource educationResource = new EducationResourceImpl();
		environment.jersey().register(educationResource);

		final InformationResource infoResource = new InformationResourceImpl();
		environment.jersey().register(infoResource);

		if( new File("/srv/www/ossweb/index.html").exists() ) {
			final ImporterResource importerResource = new ImporterResourceImpl();
			environment.jersey().register(importerResource);
			PluginHandler.registerPlugins(environment);
		}

		final SupportResource supportResource = new SupportResourceImpl();
		environment.jersey().register(supportResource);

		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		environment.healthChecks().register("template", healthCheck);

	}
}
