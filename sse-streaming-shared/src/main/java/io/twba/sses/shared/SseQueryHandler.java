package io.twba.sses.shared;

import org.reactivestreams.Publisher;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface SseQueryHandler<T> {

    Publisher<StreamedCloudEvent> execute(T t);

    default String handles(){
        Class<?> clazz = this.getClass();
        ParameterizedType parameterizedType = (ParameterizedType)clazz.getGenericInterfaces()[0];
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        return typeArguments[0].getTypeName();
    }
}
