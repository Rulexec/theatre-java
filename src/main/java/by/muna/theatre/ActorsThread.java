package by.muna.theatre;

import by.muna.theatre.exceptions.ActorStoppedException;

public interface ActorsThread {
    <T, V> Actor<T, V> createActor(ActorBehavior<T, V> behavior);

    <T> void send(Actor<T, ?> actor, T message) throws ActorStoppedException;
    <T, V> V sendSync(Actor<T, V> actor, T message) throws Exception;

    void stop(boolean handleCurrent);
}
