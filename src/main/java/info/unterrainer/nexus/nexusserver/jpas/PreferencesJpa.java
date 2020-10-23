package info.unterrainer.nexus.nexusserver.jpas;

import javax.persistence.Entity;
import javax.persistence.Table;

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
@Table(name = "preferences")
public class PreferencesJpa extends BasicJpa {

	private Long userId;
	private String languageKey;
	private Boolean darkTheme;
}
