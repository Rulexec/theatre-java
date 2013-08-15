package by.muna.theatre;

import by.muna.theatre.exceptions.ActorStoppedException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActorsThreadImpl implements ActorsThread {
    private static class ActorMessage {
        private Actor<Object, Object> actor;
        private Object message;
        private boolean sync;

        public ActorMessage(Actor<Object, Object> actor, Object message, boolean sync) {
            this.actor = actor;
            this.message = message;
            this.sync = sync;
        }

        public Actor<Object, Object> getActor() {
            return this.actor;
        }

        public Object getMessage() {
            return this.message;
        }

        public boolean isSync() {
            return this.sync;
        }
    }
    private static class ActorThreadStopped {}
    private static class ActorExceptionCatched {
        private Exception exception;

        public ActorExceptionCatched(Exception exception) {
            this.exception = exception;
        }

        public Exception getException() {
            return this.exception;
        }
    }

    private Thread thread;
    private boolean isStop = false;
    private boolean handleCurrent = true;

    private BlockingQueue<ActorMessage> messages = new LinkedBlockingQueue<ActorMessage>();
    private BlockingQueue<Object> syncResults = new LinkedBlockingQueue<Object>();

    public ActorsThreadImpl() {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ActorsThreadImpl.this.run();
            }
        });
        this.thread.start();
    }

    @Override
    public <T, V> Actor<T, V> createActor(ActorBehavior<T, V> behavior) {
        return new Actor<T, V>(this, behavior);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void send(Actor<T, ?> actor, T message) throws ActorStoppedException {
        if (this.isStop) throw new ActorStoppedException();

        this.messages.add(new ActorMessage((Actor<Object, Object>) actor, message, false));
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T, V> V sendSync(Actor<T, V> actor, T message) throws Exception {
        if (this.isStop) throw new ActorStoppedException();

        this.messages.add(new ActorMessage((Actor<Object, Object>) actor, message, true));

        try {
            Object result = this.syncResults.take();

            if (!(result instanceof ActorExceptionCatched)) {
                return (V) result;
            } else {
                throw ((ActorExceptionCatched) result).getException();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }
    }

    @Override
    public void stop(boolean handleCurrent) {
        this.handleCurrent = handleCurrent;
        this.isStop = true;

        if (!this.handleCurrent) {
            this.thread.interrupt();

            this.syncResults.add(new ActorExceptionCatched(new ActorStoppedException()));
        }

        try {
            this.thread.join();
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void run() {
        try {
            while (!this.isStop || this.handleCurrent) {
                ActorMessage actorMessage = this.messages.take();

                Actor<Object, Object> actor = actorMessage.getActor();
                Object message = actorMessage.getMessage();

                Object result;
                Exception exception = null;

                try {
                    result = actor.onMessage(message);
                } catch (Exception e) {
                    exception = e;

                    result = new ActorExceptionCatched(exception);
                }

                if (actorMessage.isSync()) {
                    this.syncResults.put(result);
                } else {
                    if (exception != null) {
                        // crash, if exception on async send
                        throw new RuntimeException(exception);
                    }
                }
            }
        } catch (InterruptedException e) {}
    }
}
