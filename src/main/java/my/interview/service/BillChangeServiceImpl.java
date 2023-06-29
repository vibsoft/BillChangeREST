package my.interview.service;

import lombok.extern.slf4j.Slf4j;
import my.interview.model.ChangeMachineState;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class BillChangeServiceImpl implements BillChangeService {

  public static final Integer[] AVAILABLE_BILLS = new Integer[] {1, 2, 5, 10, 25, 50, 100};

  public static final Integer[] AVAILABLE_CENTS = new Integer[] {1, 5, 10, 25};
  private Map<Integer, Integer> coinsAvailable;

  public boolean verifyBillAmount(int billAmount) {
    if (!Stream.of(AVAILABLE_BILLS).anyMatch(item -> item.equals(billAmount))) {
      throw new IllegalArgumentException(String.format("Illegal bill amount: %s", billAmount));
    }

    return true;
  }

  @Override
  public boolean verifyCentsAmount(int centAmount) {
    if (!Stream.of(AVAILABLE_CENTS).anyMatch(item -> item.equals(centAmount))) {
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
    for (int i = 0; i < AVAILABLE_CENTS.length; i++) {
      coinsAvailable.put(AVAILABLE_CENTS[i], initTokensCount);
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
    doChangeBill(bill * 100);

    return buildChangeMachineState(getCoinsAvailable());
  }

  private ChangeMachineState doChangeBill(Integer billInCents) {
    BillToCoinChange exchanger = new BillToCoinChange();

    log.info("doChangeBill: {} ", billInCents);
    Integer[] sortedArr = AVAILABLE_CENTS.clone();
    Arrays.sort(sortedArr, Collections.reverseOrder());

    Map<Integer, Integer> coinChangeResult =
        exchanger.coinChange(sortedArr, billInCents, coinsAvailable);

    adjustCoinChangeResult(coinChangeResult);

    return ChangeMachineState.builder()
        .changeByCent(coinChangeResult)
        .availableCentBalance(coinsAvailable)
        .build();
  }

  protected synchronized void adjustCoinChangeResult(Map<Integer, Integer> coinChangeResult) {
    coinChangeResult.forEach(
        (key, value) -> {
          Integer current = coinsAvailable.getOrDefault(key, 0);
          coinsAvailable.put(key, current - value);
        });
  }
}
