package info.unterrainer.nexus.nexusserver.datachangelog;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import info.unterrainer.commons.httpserver.daos.JpqlDao;
import info.unterrainer.commons.httpserver.daos.ParamMap;
import info.unterrainer.commons.rdbutils.Transactions;
import info.unterrainer.nexus.nexusserver.jpas.DataChangeLogJpa;
import info.unterrainer.nexus.nexusserver.jpas.DataChangeLogLockJpa;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataChangeLogBuilder {

	private final EntityManagerFactory emf;
	private final List<ChangeSet> dataChangeSets = new ArrayList<>();
	private final JpqlDao<DataChangeLogJpa> dataChangeSetDao;
	private final JpqlDao<DataChangeLogLockJpa> dataChangeSetLockDao;

	public DataChangeLogBuilder(final EntityManagerFactory emf) {
		this.emf = emf;
		dataChangeSetDao = new JpqlDao<>(emf, DataChangeLogJpa.class);
		dataChangeSetLockDao = new JpqlDao<>(emf, DataChangeLogLockJpa.class);
	}

	/**
	 * Registers a new data-changeset to be run when calling {@link #run()} on this
	 * builder.<br />
	 * Throw a {@link DataChangeLogException} inside of the
	 * {@link Consumer<EntityManager>} if you'd like to abort the whole process. If
	 * you abort none of the changes will be committed since all of the registered
	 * functions are executed within a single transaction.
	 *
	 * @param dataChangeSetFunction the function that will change some data in the
	 *                              database.
	 * @return an instance of this {@link DataChangeLogBuilder} to provide a fluent
	 *         interface.
	 */
	public DataChangeLogBuilder register(final String changeId, final Consumer<EntityManager> dataChangeSetFunction) {
		log.info("ChangeSet [{}] loaded.", changeId);
		dataChangeSets.add(new ChangeSet(changeId, dataChangeSetFunction));
		return this;
	}

	/**
	 * Run all registered data-changeset-functions in order, but only if they have
	 * not yet been executed on this database.
	 */
	public void run() {
		Transactions.withNewTransaction(emf, em -> {
			log.info("Starting execution of changesets.");

			DataChangeLogLockJpa lockJpa = null;
			try {
				// Obtain lock.
				lockJpa = dataChangeSetLockDao.firstOf(em, true);
				if (lockJpa != null)
					throw new DataChangeLogException("Could not obtain database lock.");
				lockJpa = new DataChangeLogLockJpa();
				dataChangeSetLockDao.create(em, lockJpa);
				log.info("Obtained database lock.");

				for (ChangeSet dataChangeSet : dataChangeSets) {
					DataChangeLogJpa changeJpa = dataChangeSetDao.firstOf(em, "o.changeId=:changeId",
							ParamMap.builder().parameter("changeId", dataChangeSet.changeId).build());
					if (changeJpa == null) {
						log.info("Executing changeset [{}].", dataChangeSet.changeId);
						dataChangeSet.function.accept(em);
						changeJpa = DataChangeLogJpa.builder()
								.changeId(dataChangeSet.changeId)
								.executedOn(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime())
								.build();
						dataChangeSetDao.create(changeJpa);
					} else
						log.info("Skipping changeset [{}].", dataChangeSet.changeId);
				}
			} finally {
				if (lockJpa != null) {
					dataChangeSetLockDao.delete(em, lockJpa.getId());
					log.info("Released database lock.");
				}
			}
		});
	}

	@Data
	private class ChangeSet {
		private final String changeId;
		private final Consumer<EntityManager> function;
	}
}
