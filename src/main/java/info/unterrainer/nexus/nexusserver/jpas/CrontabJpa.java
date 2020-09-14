package info.unterrainer.nexus.nexusserver.jpas;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import info.unterrainer.commons.rdbutils.entities.BasicJpa;
import info.unterrainer.nexus.nexusserver.enums.CrontabType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "crontab")
public class CrontabJpa extends BasicJpa {

	private String name;
	private Boolean enabled;
	private String data;

	@Enumerated(EnumType.STRING)
	private CrontabType type;

	private String cronDef;
	private String className;
}
