package fh.swen.paperless.mapper;

import java.util.Collection;
import java.util.List;

/**
 * Common shape of all mappers between an entity (source) and its DTO (target).
 * The single-object methods are implemented by MapStruct, the list variants are shared here.
 */
public interface BaseMapper<S, T> {

    T mapToTarget(S source);

    S mapToSource(T target);

    default List<T> mapToTarget(Collection<S> sources) {
        return sources.stream().map(this::mapToTarget).toList();
    }

    default List<S> mapToSource(Collection<T> targets) {
        return targets.stream().map(this::mapToSource).toList();
    }
}
