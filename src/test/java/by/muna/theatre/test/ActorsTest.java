package by.muna.theatre.test;

import by.muna.theatre.Actor;
import by.muna.theatre.ActorBehavior;
import by.muna.theatre.ActorsThread;
import by.muna.theatre.ActorsThreadImpl;
import by.muna.theatre.exceptions.ActorStoppedException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ActorsTest {
    @Test
    public void actorsTest() throws Exception {
        ActorsThread actors = new ActorsThreadImpl();

        final CountDownLatch countdown = new CountDownLatch(1);

        Actor<Integer, Integer> actor = actors.createActor(new ActorBehavior<Integer, Integer>() {
            @Override
            public Integer onMessage(Integer message) {
                if (message == 7) {
                    countdown.countDown();

                    return null;
                } else {
                    return message * 2;
                }
            }
        });

        try { actor.send(7); } catch (ActorStoppedException e) {}

        int result;
        try {
            result = actor.sendSync(12);
        } catch (ActorStoppedException e) {
            throw new RuntimeException("Actor stopped");
        }

        Assert.assertEquals(24, result);

        if (!countdown.await(500, TimeUnit.MILLISECONDS)) {
            Assert.fail("Async message not delivered.");
        }

        actors.stop(false);

        boolean catched = false;
        try {
            actor.send(42);
        } catch (ActorStoppedException e) {
            catched = true;
        }

        Assert.assertTrue("ActorStoppedException catched after .stop()", catched);

        catched = false;

        try {
            actor.sendSilent(77);
        } catch (RuntimeException e) {
            catched = true;

            Assert.assertEquals(ActorStoppedException.class, e.getCause().getClass());
        }

        Assert.assertTrue(".sendSilent throws RuntimeException", catched);

        catched = false;

        try {
            actor.sendSyncSilent(88);
        } catch (RuntimeException e) {
            catched = true;

            Assert.assertEquals(ActorStoppedException.class, e.getCause().getClass());
        }

        Assert.assertTrue(".sendSyncSilent throws RuntimeException", catched);
    }

    @Test
    public void exceptionFromOnMessage() {
        ActorsThread actorsThread = new ActorsThreadImpl();

        Actor<Integer, Integer> actor = actorsThread.createActor(new ActorBehavior<Integer, Integer>() {
            @Override
            public Integer onMessage(Integer message) throws Exception {
                throw new Exception("This should be catched!");
            }
        });

        boolean catched = false;

        try {
            actor.sendSync(42);
        } catch (Exception e) {
            Assert.assertEquals("This should be catched!", e.getMessage());

            catched = true;
        }

        Assert.assertTrue("Not catched", catched);
    }
}
