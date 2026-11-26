package fh.swen.paperless.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Shared settings for all MapStruct mappers:
 * <ul>
 *     <li>the generated implementations are Spring beans, so they can be injected into the controllers</li>
 *     <li>a target field that is neither mapped nor explicitly ignored fails the build,
 *     so a new DTO field can't be forgotten silently</li>
 * </ul>
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MapStructConfig {
}
