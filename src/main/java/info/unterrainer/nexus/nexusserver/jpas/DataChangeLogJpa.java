package info.unterrainer.nexus.nexusserver.jpas;

import java.time.LocalDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import info.unterrainer.commons.rdbutils.converters.LocalDateTimeConverter;
import info.unterrainer.commons.rdbutils.entities.BasicJpa;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "DATACHANGELOG")
public class DataChangeLogJpa extends BasicJpa {

	private String changeId;

	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime executedOn;
}
