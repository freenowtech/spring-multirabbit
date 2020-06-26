package springframework.boot.autoconfigure.amqp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SingleListenerTest {

    @Test
    public void loadListenerWithoutMentionToConnectionFactory() {
    }

    @SpringBootApplication
    public static class SingleListenerApp {

        @RabbitListener(containerFactory = "connectionNameA")
        public void onMessage(final String event) {
        }
    }
}
