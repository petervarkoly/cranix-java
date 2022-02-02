/* (c) 2021 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api;

import de.cranix.api.config.*;
import de.cranix.api.auth.CrxAuthorizer;
import de.cranix.api.auth.CrxTokenAuthenticator;
import de.cranix.api.health.TemplateHealthCheck;
import de.cranix.api.resources.*;
import de.cranix.dao.Session;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
//import io.federecio.dropwizard.swagger.SwaggerBundle;
//import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.io.File;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import static de.cranix.helper.CranixConstants.cranixMdmConfig;

public class CranixApplication extends Application<ServerConfiguration> {

	public static void main(String[] args) throws Exception {
		new CranixApplication().run(args);
	}

	@Override
	public String getName() {
		return "CRANIX API";
	}

	/* @Override
	public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
		bootstrap.addBundle(new SwaggerBundle<ServerConfiguration>() {
		       @Override
		       protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ServerConfiguration configuration) {
			       return configuration.swaggerBundleConfiguration;
		       }
	       });
	}*/

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

		final AdHocLanResource adHocLanResource = new AdHocLanResource();
		environment.jersey().register(adHocLanResource);

		final CategoryResource categoryResource = new CategoryResource();
		environment.jersey().register(categoryResource);

		final CloneToolResource cloneToolResource = new CloneToolResource();
		environment.jersey().register(cloneToolResource);

		final DeviceResource devicesResource = new DeviceResource();
		environment.jersey().register(devicesResource);

		final EducationResource educationResource = new EducationResource();
		environment.jersey().register(educationResource);

		final GroupResource groupsResource = new GroupResource();
		environment.jersey().register(groupsResource);

		final HwconfResource hwconfResource = new HwconfResource();
		environment.jersey().register(hwconfResource);

		final InformationResource infoResource = new InformationResource();
		environment.jersey().register(infoResource);

		final PrinterResource printerResource = new PrinterResource();
		environment.jersey().register(printerResource);

		final RoomResource roomResource = new RoomResource();
		environment.jersey().register(roomResource);

		final SchedulerResource schedulerResource = new SchedulerResource();
		environment.jersey().register(schedulerResource);

		final SelfManagementResource selfManagementResource = new SelfManagementResource();
		environment.jersey().register(selfManagementResource);

		final SessionsResource sessionsResource = new SessionsResource();
		environment.jersey().register(sessionsResource);

		final SoftwareResource softwareResource = new SoftwareResource();
		environment.jersey().register(softwareResource);

		final SupportResource supportResource = new SupportResource();
		environment.jersey().register(supportResource);

		final SystemResource systemResource = new SystemResource();
		environment.jersey().register(systemResource);

		final UserResource usersResource = new UserResource();
		environment.jersey().register(usersResource);

		final ObjectResource objectResource = new ObjectResource();
		environment.jersey().register(objectResource);

		//Start mdm api only if it is configured.
		File mdm_config = new File(cranixMdmConfig);
		if( mdm_config.exists() ) {
			final MdmResource mdmResource = new MdmResource();
			environment.jersey().register(mdmResource);
		}

		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		environment.healthChecks().register("template", healthCheck);

	}
}
