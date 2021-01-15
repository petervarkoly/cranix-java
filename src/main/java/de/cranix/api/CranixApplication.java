/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api;

import de.cranix.api.config.*;
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

		final SchedulerResource schedulerResource = new SchedulerResourceImpl();
		environment.jersey().register(schedulerResource);

		final SystemResource systemResource = new SystemResourceImpl();
		environment.jersey().register(systemResource);

		final AdHocLanResource adHocLanResource = new AdHocLanResource();
		environment.jersey().register(adHocLanResource);

		final SessionsResource sessionsResource = new SessionsResourceImpl();
		environment.jersey().register(sessionsResource);

		final SelfManagementResource selfManagementResource = new SelfManagementResourceImpl();
		environment.jersey().register(selfManagementResource);

		final RoomResource roomResource = new RoomResource();
		environment.jersey().register(roomResource);

		final UserResource usersResource = new UserResourceImpl();
		environment.jersey().register(usersResource);

		final GroupResource groupsResource = new GroupResource();
		environment.jersey().register(groupsResource);

		final DeviceResource devicesResource = new DeviceResource();
		environment.jersey().register(devicesResource);

		final PrinterResource printerResource = new PrinterResource();
		environment.jersey().register(printerResource);

		final CloneToolResource cloneToolResource = new CloneToolResource();
		environment.jersey().register(cloneToolResource);

		final HwconfResource hwconfResource = new HwconfResource();
		environment.jersey().register(hwconfResource);

		final CategoryResource categoryResource = new CategoryResource();
		environment.jersey().register(categoryResource);

		final SoftwareResource softwareResource = new SoftwareResourceImpl();
		environment.jersey().register(softwareResource);

		final EducationResource educationResource = new EducationResource();
		environment.jersey().register(educationResource);

		final InformationResource infoResource = new InformationResource();
		environment.jersey().register(infoResource);

		final SupportResource supportResource = new SupportResourceImpl();
		environment.jersey().register(supportResource);

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