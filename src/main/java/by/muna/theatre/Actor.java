package by.muna.theatre;

import by.muna.theatre.exceptions.ActorStoppedException;

public class Actor<T, V> implements ActorBehavior<T, V> {
    private ActorsThread thread;
    private ActorBehavior<T, V> behavior;

    private boolean stopped = false;
    private boolean handleCurrent = true;

    public Actor(ActorsThread thread, ActorBehavior<T, V> behavior) {
        this.thread = thread;
        this.behavior = behavior;
    }

    public void send(T message) throws ActorStoppedException {
        if (this.stopped) throw new ActorStoppedException();

        this.thread.send(this, message);
    }
    public V sendSync(T message) throws Exception {
        if (this.stopped) throw new ActorStoppedException();

        return this.thread.sendSync(this, message);
    }

    public void sendSilent(T message) {
        try {
            this.thread.send(this, message);
        } catch (ActorStoppedException e) {
            throw new RuntimeException(e);
        }
    }
    public V sendSyncSilent(T message) throws Exception {
        try {
            return this.thread.sendSync(this, message);
        } catch (ActorStoppedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        this.stopped = true;
    }
    public void stop(boolean handleCurrent) {
        this.stopped = true;
        this.handleCurrent = handleCurrent;
    }

    @Override
    public V onMessage(T message) throws Exception {
        if (this.stopped && !this.handleCurrent) throw new ActorStoppedException();

        try {
            return this.behavior.onMessage(message);
        } catch (ActorStoppedException e) {
            this.stopped = true;
            this.handleCurrent = false;

            throw e;
        }
    }
}
