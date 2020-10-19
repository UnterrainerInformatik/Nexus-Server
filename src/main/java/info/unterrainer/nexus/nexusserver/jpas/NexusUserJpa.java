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
@Table(name = "nexusUser")
public class NexusUserJpa extends BasicJpa {

	private String userName;
	private String client;

	private String givenName;
	private String familyName;

	private String email;
	private Boolean emailVerified;

	private String realmRoles;
	private String clientRoles;

	private Boolean isActive;
	private Boolean isBearer;
}
