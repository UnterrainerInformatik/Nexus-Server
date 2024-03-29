package info.unterrainer.nexus.nexusserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import info.unterrainer.commons.crontabscheduler.BasicCrontabHandler;
import info.unterrainer.commons.crontabscheduler.CrontabScheduler;
import info.unterrainer.commons.httpserver.HttpServer;
import info.unterrainer.commons.httpserver.accessmanager.RoleBuilder;
import info.unterrainer.commons.httpserver.daos.JpqlDao;
import info.unterrainer.commons.httpserver.enums.Endpoint;
import info.unterrainer.commons.rdbutils.RdbUtils;
import info.unterrainer.commons.rdbutils.exceptions.RdbUtilException;
import info.unterrainer.commons.serialization.JsonMapper;
import info.unterrainer.nexus.nexusserver.crontab.CrontabEmail;
import info.unterrainer.nexus.nexusserver.crontab.CrontabHandler;
import info.unterrainer.nexus.nexusserver.datachangelog.DataChangeLog;
import info.unterrainer.nexus.nexusserver.jpas.CrontabJpa;
import info.unterrainer.nexus.nexusserver.jpas.LogJpa;
import info.unterrainer.nexus.nexusserver.jpas.NexusUserJpa;
import info.unterrainer.nexus.nexusserver.jpas.PreferencesJpa;
import info.unterrainer.nexus.nexusserver.jsons.CrontabJson;
import info.unterrainer.nexus.nexusserver.jsons.LogJson;
import info.unterrainer.nexus.nexusserver.jsons.NexusUserJson;
import info.unterrainer.nexus.nexusserver.jsons.PreferencesJson;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

@Slf4j
public class NexusServer {

	private final JsonMapper jsonMapper = JsonMapper.create();
	private MapperFactory orikaFactory = new DefaultMapperFactory.Builder().build();
	private EntityManagerFactory emf;
	private NexusServerConfiguration configuration;
	private ThreadPoolExecutor executorService;

	private CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
	private CronDescriptor cronDescriptor = CronDescriptor.instance(Locale.GERMANY);
	private Mailer mailer;

	private CrontabScheduler crontabScheduler;
	private CrontabHandler crontabHandler;

	private JpqlDao<LogJpa> loggingDao = new JpqlDao<>(emf, LogJpa.class);
	private JpqlDao<CrontabJpa> crontabDao;

	public static void main(final String[] args) {
		try {
			new NexusServer().run();
		} catch (Exception e) {
			log.error("uncaught exception", e);
		}
	}

	public void run() throws IOException, RdbUtilException {
		configuration = NexusServerConfiguration.read();
		emf = RdbUtils.createAutoclosingEntityManagerFactory(NexusServer.class, "nexusserver");

		executorService = new ThreadPoolExecutor(200, 200, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		executorService.allowCoreThreadTimeOut(true);

		MailerRegularBuilderImpl mailerBuilder = MailerBuilder.withSMTPServer(configuration.smtpServer(),
				configuration.smtpPort());
		if (configuration.smtpAuth())
			mailerBuilder.withSMTPServerUsername(configuration.smtpUser())
					.withSMTPServerPassword(configuration.smtpPassword());
		mailer = mailerBuilder.withTransportStrategy(configuration.smtpTransport()).buildMailer();

		loggingDao = new JpqlDao<>(emf, LogJpa.class);
		crontabDao = new JpqlDao<>(emf, CrontabJpa.class);

		CrontabEmail crontabEmail = new CrontabEmail(mailer);

		crontabScheduler = CrontabScheduler.builder().period(500).timeUnit(TimeUnit.MILLISECONDS).build();
		crontabHandler = new CrontabHandler(emf, crontabDao, cronParser, cronDescriptor, crontabEmail);

		DataChangeLog.builder(emf).run();

		HttpServer server = HttpServer.builder()
				.applicationName("nexus-server")
				.jsonMapper(jsonMapper)
				.orikaFactory(orikaFactory)
				.executorService(executorService)
				.appVersionFqns(new String[] { "at.elitezettl.server.eliteserver.Information" })
				.build();

		server.handlerGroupFor(NexusUserJpa.class, NexusUserJson.class, new JpqlDao<>(emf, NexusUserJpa.class))
				.path("users")
				.endpoints(Endpoint.ALL)
				.addRoleFor(Endpoint.ALL, RoleBuilder.authenticated())
				.getListInterceptor()
				.query("o.userName = :userName[string]")
				.build()
				.getListInterceptor()
				.query("o.clientRoles LIKE :clientRole[string] AND o.realmRoles LIKE :realmRole[string]")
				.build()
				.add();
		server.handlerGroupFor(PreferencesJpa.class, PreferencesJson.class, new JpqlDao<>(emf, PreferencesJpa.class))
				.path("preferences")
				.endpoints(Endpoint.ALL)
				.addRoleFor(Endpoint.ALL, RoleBuilder.authenticated())
				.getListInterceptor()
				.join("JOIN " + NexusUserJpa.class.getSimpleName() + " u ON o.userId = u.id")
				.query("u.userName = :userName[string]")
				.build()
				.add();
		server.handlerGroupFor(LogJpa.class, LogJson.class, loggingDao).path("logs").endpoints(Endpoint.ALL).add();
		server.handlerGroupFor(CrontabJpa.class, CrontabJson.class, crontabDao)
				.path("crontabs")
				.endpoints(Endpoint.ALL)
				.add();

		server.start();

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			crontabHandler.load();
			List<BasicCrontabHandler> handlers = new ArrayList<>(crontabHandler.getItems());
			crontabScheduler.setHandlers(handlers);
		}, 0, 10, TimeUnit.SECONDS);
	}
}