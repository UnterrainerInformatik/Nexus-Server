package info.unterrainer.nexus.nexusserver;

import java.util.Optional;

import org.simplejavamail.api.mailer.config.TransportStrategy;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class NexusServerConfiguration {

	private NexusServerConfiguration() {
	}

	private String smtpServer;
	private int smtpPort;
	private boolean smtpAuth;
	private String smtpUser;
	private String smtpPassword;
	private TransportStrategy smtpTransport;

	public static NexusServerConfiguration read() {
		return read(null);
	}

	public static NexusServerConfiguration read(final String prefix) {
		String p = "";
		if (prefix != null)
			p = prefix;
		NexusServerConfiguration config = new NexusServerConfiguration();

		config.smtpServer = Optional.ofNullable(System.getenv(p + "NEXUS_SMTP_SERVER")).orElse("smtp.gmail.com");
		config.smtpPort = Integer.parseInt(Optional.ofNullable(System.getenv(p + "NEXUS_SMTP_PORT")).orElse("465"));
		config.smtpAuth = Boolean
				.parseBoolean(Optional.ofNullable(System.getenv(p + "NEXUS_SMTP_AUTH")).orElse("true"));
		config.smtpUser = Optional.ofNullable(System.getenv(p + "NEXUS_SMTP_USER")).orElse("");
		config.smtpPassword = Optional.ofNullable(System.getenv(p + "NEXUS_SMTP_PASSWORD")).orElse("");
		config.smtpTransport = TransportStrategy
				.valueOf(Optional.ofNullable(System.getenv(p + "NEXUS_SMTP_TRANSPORT")).orElse("SMTPS"));

		return config;
	}
}
