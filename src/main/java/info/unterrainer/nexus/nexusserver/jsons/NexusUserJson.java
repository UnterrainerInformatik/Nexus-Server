package info.unterrainer.nexus.nexusserver.jsons;

import info.unterrainer.commons.serialization.jsons.BasicJson;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class NexusUserJson extends BasicJson {

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
