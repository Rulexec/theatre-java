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
    public void actorsTest() throws InterruptedException {
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
    }
}
