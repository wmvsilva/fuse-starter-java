
package org.galatea.starter.entrypoint;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.domain.TradeAgreement;
import org.galatea.starter.entrypoint.messagecontracts.Messages.TradeAgreementMessage;
import org.galatea.starter.service.SettlementService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;


@RequiredArgsConstructor
@Slf4j
@ToString
@EqualsAndHashCode(callSuper = true)
@SpringBootTest
public class SettlementJmsListenerTest extends ASpringTest {

  @Autowired
  protected JmsTemplate jmsTemplate;

  @MockBean
  private SettlementService mockSettlementService;

  @Value("${jms.agreement-queue}")
  protected String queueName;

  @Test
  public void testSettleOneAgreement() {
    TradeAgreementMessage message = getMessage();
    TradeAgreement agreement = getAgreement();

    log.info("Agreement message to put on queue {}", message);
    List<TradeAgreement> agreements = Arrays.asList(agreement);
    log.info("Agreement objects that the service will expect {}", agreements);

    jmsTemplate.convertAndSend(queueName, message.toByteArray());

    // We use verify since the jms listener doesn't actually do anything with the returns from the
    // service
    verify(mockSettlementService, timeout(10000)).spawnMissions(agreements);
  }

  private TradeAgreementMessage getMessage() {
    return TradeAgreementMessage.newBuilder().setInstrument("IBM").setInternalParty("INT-1")
        .setExternalParty("EXT-1").setBuySell("B").setQty(100).build();
  }

  private TradeAgreement getAgreement() {
    return TradeAgreement.builder().id(0l).instrument("IBM").internalParty("INT-1")
        .externalParty("EXT-1").buySell("B").qty(100d).build();
  }
}
