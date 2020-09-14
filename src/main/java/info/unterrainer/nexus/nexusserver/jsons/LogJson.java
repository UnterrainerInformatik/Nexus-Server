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
public class LogJson extends BasicJson {

	private String message;
}
