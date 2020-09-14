package info.unterrainer.nexus.nexusserver;

import java.io.IOException;
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
import info.unterrainer.commons.httpserver.daos.JpqlDao;
import info.unterrainer.commons.httpserver.daos.JpqlTransactionManager;
import info.unterrainer.commons.httpserver.enums.Endpoint;
import info.unterrainer.commons.rdbutils.RdbUtils;
import info.unterrainer.commons.rdbutils.exceptions.RdbUtilException;
import info.unterrainer.commons.serialization.JsonMapper;
import info.unterrainer.nexus.nexusserver.crontab.CrontabEmail;
import info.unterrainer.nexus.nexusserver.crontab.CrontabHandler;
import info.unterrainer.nexus.nexusserver.datachangelog.DataChangeLog;
import info.unterrainer.nexus.nexusserver.jpas.CrontabJpa;
import info.unterrainer.nexus.nexusserver.jpas.LogJpa;
import info.unterrainer.nexus.nexusserver.jsons.CrontabJson;
import info.unterrainer.nexus.nexusserver.jsons.LogJson;
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

		JpqlTransactionManager jpqlTransactionManager = new JpqlTransactionManager(emf);
		executorService = new ThreadPoolExecutor(1, 200, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

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

		server.handlerGroupFor(LogJpa.class, LogJson.class, jpqlTransactionManager)
				.path("logs")
				.dao(loggingDao)
				.endpoints(Endpoint.ALL)
				.add();
		server.handlerGroupFor(CrontabJpa.class, CrontabJson.class, jpqlTransactionManager)
				.path("crontabs")
				.dao(crontabDao)
				.endpoints(Endpoint.ALL)
				.add();

		server.start();

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			crontabScheduler.prepareUpdate();
			crontabHandler.load();
			for (BasicCrontabHandler c : crontabHandler.getItems())
				crontabScheduler.addHandler(c, c.getName());
			crontabScheduler.finishUpdate();
		}, 0, 10, TimeUnit.SECONDS);
	}
}