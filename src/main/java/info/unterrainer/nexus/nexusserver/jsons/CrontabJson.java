package info.unterrainer.nexus.nexusserver.jsons;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import info.unterrainer.commons.serialization.jsons.BasicJson;
import info.unterrainer.nexus.nexusserver.enums.CrontabType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class CrontabJson extends BasicJson {

	private String name;
	private Boolean enabled;
	private String data;

	@Enumerated(EnumType.STRING)
	private CrontabType type;

	private String cronDef;
	private String className;
}
