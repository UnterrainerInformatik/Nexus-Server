package info.unterrainer.nexus.nexusserver.datachangelog;

public class DataChangeLogException extends RuntimeException {

	private static final long serialVersionUID = -728330495830182079L;

	public DataChangeLogException(final String message) {
		super(message);
	}

	public DataChangeLogException(final Throwable cause) {
		super(cause);
	}

	public DataChangeLogException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
