package info.unterrainer.nexus.nexusserver.crontab;

import java.time.ZonedDateTime;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.parser.CronParser;

import info.unterrainer.commons.crontabscheduler.BasicCrontabHandler;
import info.unterrainer.nexus.nexusserver.enums.CrontabType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RunCrontabHandler extends BasicCrontabHandler {

	private final Long id;
	private final CrontabType runType;
	private final CrontabEmail emailCrontabHandler;

	public RunCrontabHandler(final String name, final Boolean enabled, final String data, final String cronDef,
			final CronParser parser, final CronDescriptor descriptor, final Long id, final CrontabType runType,
			final CrontabEmail emailCrontabHandler) {
		super(name, enabled, data, cronDef, parser, descriptor);
		this.id = id;
		this.runType = runType;
		this.emailCrontabHandler = emailCrontabHandler;
	}

	@Override
	public void handle(final ZonedDateTime started) {
		switch (runType) {
		case EMAIL:
			emailCrontabHandler.send(started, data);
			break;
		default:
			break;
		}
	}
}
