package info.unterrainer.nexus.nexusserver.crontab;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.parser.CronParser;

import info.unterrainer.commons.httpserver.daos.JpqlDao;
import info.unterrainer.nexus.nexusserver.jpas.CrontabJpa;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CrontabHandler {

	private final EntityManagerFactory emf;
	private final JpqlDao<CrontabJpa> crontabRunDao;
	protected final CronParser cronParser;
	protected final CronDescriptor cronDescriptor;
	protected final CrontabEmail emailHandler;

	protected final List<RunCrontabHandler> items = new ArrayList<>();

	public void load() {
		List<CrontabJpa> jpas = crontabRunDao.select().build().getList();
		items.clear();
		for (CrontabJpa jpa : jpas) {
			RunCrontabHandler handler = new RunCrontabHandler("runHandler" + jpa.getId() + "", jpa.getEnabled(),
					jpa.getData(), jpa.getCronDef(), cronParser, cronDescriptor, jpa.getId(), jpa.getType(),
					emailHandler);
			items.add(handler);
		}
	}
}