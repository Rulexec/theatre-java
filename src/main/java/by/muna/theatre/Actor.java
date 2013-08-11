package by.muna.theatre;

import by.muna.theatre.exceptions.ActorStoppedException;

public class Actor<T, V> {
    private ActorsThread thread;
    private ActorBehavior<T, V> behavior;

    public Actor(ActorsThread thread, ActorBehavior<T, V> behavior) {
        this.thread = thread;
        this.behavior = behavior;
    }

    public void send(T message) throws ActorStoppedException {
        this.thread.send(this, message);
    }
    public V sendSync(T message) throws ActorStoppedException {
        return this.thread.sendSync(this, message);
    }

    public V onMessage(T message) {
        return this.behavior.onMessage(message);
    }
}