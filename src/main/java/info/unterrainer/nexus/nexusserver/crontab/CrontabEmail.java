package info.unterrainer.nexus.nexusserver.crontab;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;

import lombok.Data;
import lombok.experimental.Accessors;

public class CrontabEmail {

	@Data
	@Accessors(fluent = true)
	public class Text {

		private String text = "";
		private String html = "";

		public Text heading(final String t, final Object... args) {
			String s = String.format(t, args);
			text += "--- " + s + " ---";
			html += "<h2>" + s + "</h2>";
			return this;
		}

		public Text addLine(final String t, final Object... args) {
			add(t, args).newLine();
			return this;
		}

		public Text addParagraph(final String t, final Object... args) {
			add(t, args).paragraph();
			return this;
		}

		public Text add(final String t, final Object... args) {
			String s = String.format(t, args);
			text += s;
			html += s;
			return this;
		}

		public Text newLine() {
			text += "\n";
			html += "<br>";
			return this;
		}

		public Text paragraph() {
			text += "\n\n";
			html += "<p>";
			return this;
		}
	}

	private Mailer mailer;

	public CrontabEmail(final Mailer mailer) {
		this.mailer = mailer;
	}

	public void send(final ZonedDateTime started, final String recipient) {
		Text t = new Text();
		t.heading("Testmail").paragraph();

		DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");

		t.addLine("This is a test-mail.").addLine("It was sent on: %s", LocalDateTime.now().format(f));

		Email email = EmailBuilder.startingBlank()
				.from("Elite-Server", "elite.zettl@gmail.com")
				.to(recipient)
				.withSubject("Testmail from Nexus-Server")
				.withPlainText(t.text)
				.withHTMLText(t.html)
				.buildEmail();
		mailer.sendMail(email);
	}
}
