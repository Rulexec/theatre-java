package by.muna.theatre;

import by.muna.theatre.exceptions.ActorStoppedException;

public interface ActorBehavior<T, V> {
    V onMessage(T message) throws Exception;
}
