package info.unterrainer.nexus.nexusserver.datachangelog;

import javax.persistence.EntityManagerFactory;

public class DataChangeLog {

	public static DataChangeLogBuilder builder(final EntityManagerFactory emf) {
		return new DataChangeLogBuilder(emf);
	}
}
