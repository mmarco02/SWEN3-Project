package fh.swen.paperless.mapper;

import java.util.Collection;
import java.util.List;

public interface Mapper<S, T>{
    T mapToTarget(S source);

    S mapToSource(T target);

    default List<T> mapToTarget(Collection<S> sources) {
        return sources.stream().map(this::mapToTarget).toList();
    }

    default List<S> mapToSource(Collection<T> targets) {
        return targets.stream().map(this::mapToSource).toList();
    }
}
