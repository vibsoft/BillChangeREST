package my.interview.service;

import lombok.extern.slf4j.Slf4j;
import my.interview.model.ChangeMachineState;
import my.interview.model.ChangeResult;
import my.interview.service.BillToCoin.BillToCoinChangeSimple;
import my.interview.service.BillToCoin.CoinChangeWithMinCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class BillChangeServiceImpl implements BillChangeService {

  @Autowired
  @Qualifier("BillToCoinChangeSimple")
  private CoinChangeWithMinCount coinChangeWithMinCount;

  private Map<Integer, Integer> coinsAvailable;

  @Value("${application.bills}")
  public Integer[] availableBills;

  @Value("${application.coins}")
  public Integer[] availableCoins;

  public boolean verifyBillAmount(Integer billAmount) {
    if (!Stream.of(availableBills).anyMatch(item -> item.equals(billAmount))) {
      throw new IllegalArgumentException(String.format("Illegal bill amount: %s", billAmount));
    }

    return true;
  }

  @Override
  public boolean verifyCentsAmount(Integer centAmount) {
    if (!Stream.of(availableCoins).anyMatch(item -> item.equals(centAmount))) {
      throw new IllegalArgumentException(String.format("Illegal cent amount: %s", centAmount));
    }

    return true;
  }

  public Map<Integer, Integer> getCoinsAvailable() {
    if (coinsAvailable == null) {
      log.error("SERVICE - Bill machine is not initialized");
      throw new IllegalArgumentException("Slot machine cluster is not initialized");
    }

    return coinsAvailable;
  }

  public ChangeMachineState buildChangeMachineState(Map<Integer, Integer> coinsAvailable) {
    return ChangeMachineState.builder().availableCentBalance(coinsAvailable).build();
  }

  @Override
  public ChangeMachineState initMachine(Integer initTokensCount) {
    if (coinsAvailable != null) {
      log.error(
          "SERVICE - Bill machine already initialized, [available coins: {}]", coinsAvailable);
      throw new IllegalArgumentException(
          String.format(
              "SERVICE - Bill machine already initialized, [available coins: %s]", coinsAvailable));
    }

    coinsAvailable = new TreeMap<>();
    for (int i = 0; i < availableCoins.length; i++) {
      coinsAvailable.put(availableCoins[i], initTokensCount);
    }

    return buildChangeMachineState(coinsAvailable);
  }

  @Override
  public ChangeMachineState getMachineState() {
    return buildChangeMachineState(getCoinsAvailable());
  }

  @Override
  public ChangeMachineState topupCoins(Integer coinCents, Integer coinCount) {
    Map<Integer, Integer> coinsAvailable = getCoinsAvailable();
    Integer currentCount = coinsAvailable.getOrDefault(coinCents, 0);

    coinsAvailable.put(coinCents, currentCount + coinCount);
    return buildChangeMachineState(getCoinsAvailable());
  }

  @Override
  public ChangeMachineState changeBill(Integer bill) {
    return doChangeBill(bill * 100);
  }

  private ChangeMachineState doChangeBill(Integer billInCents) {
    ChangeResult coinChangeResult =
        coinChangeWithMinCount.coinChangeWithMinCount(availableCoins, billInCents, coinsAvailable);

    adjustCoinChangeResult(coinChangeResult.getChangeByCoins());

    return ChangeMachineState.builder()
        .changeResult(coinChangeResult)
        .availableCentBalance(coinsAvailable)
        .build();
  }

  protected synchronized void adjustCoinChangeResult(Map<Integer, Integer> coinChangeResult) {
    coinChangeResult.forEach(
        (key, value) -> {
          Integer current = coinsAvailable.getOrDefault(key, 0);

          if (current < value) {
            throw new IllegalArgumentException(
                String.format("Not available %s [cent: %, current: %s]", value, key, current));
          }

          coinsAvailable.put(key, current - value);
        });
  }
}
