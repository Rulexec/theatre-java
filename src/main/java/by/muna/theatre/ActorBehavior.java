package by.muna.theatre;

public interface ActorBehavior<T, V> {
    V onMessage(T message);
}
