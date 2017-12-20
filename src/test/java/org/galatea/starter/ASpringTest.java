
package org.galatea.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.galatea.starter.domain.FXRateResponse;
import org.galatea.starter.domain.TradeAgreement;
import org.galatea.starter.utils.deserializers.FXRateResponseDeserializer;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import junitparams.JUnitParamsRunner;
import junitparams.mappers.DataMapper;
import junitparams.mappers.IdentityMapper;

@Slf4j
@ActiveProfiles("dev")
public abstract class ASpringTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  protected ApplicationContext applicationContext;

  /**
   * Pipe delimited mapper used for parameterized unit tests run by JUnitParamsRunner.class
   *
   */
  public static class JsonTestFileMapper extends IdentityMapper {

    public static final String DELIM = "\\|";

    @Override
    public Object[] map(Reader reader) {
      Object[] lines = super.map(reader);
      return Arrays.stream(lines).map(objLine -> (String) objLine).filter(line -> !line.trim().isEmpty())
          .map(line -> line.split(DELIM)).collect(Collectors.toList()).toArray();
    }

  }

  public static String readData(final String fileName) throws IOException {
    return IOUtils.toString(ASpringTest.class.getClassLoader().getResourceAsStream(fileName))
        .trim();
  }

  /**
   * The ActiveMQ broker isn't automatically shutdown after each test, so this step ensures we are
   * shutting it down. Otherwise, you may have old mocks injected into the listeners when you run
   * future tests. Subclasses that override this method should make sure to do a super call
   *
   */
  @After
  public void cleanup() {
    try {
      // We can't get a direct handle to active mq, so let's go through the endpoint registry. This
      // will also ensure that the listener containers are shutdown.
      JmsListenerEndpointRegistry bean =
          applicationContext.getBean(JmsListenerEndpointRegistry.class);

      if (bean.isRunning()) {
        log.info("jms registry is running so let's destroy it");
        bean.destroy();
      } else {
        log.info("jms registry is not running so nothing to do");
      }
    } catch (NoSuchBeanDefinitionException nbd) {
      log.info("No need to shutdown jms listener registry since the bean doesn't exist");
    } catch (Exception err) {
      log.info("Could not determine whether or not to shutdown jms listener"
          + " registry since we came across an exception", err);
    }


  }


}
