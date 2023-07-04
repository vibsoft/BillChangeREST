package my.interview.service.BillToCoin;

import lombok.extern.slf4j.Slf4j;
import my.interview.model.ChangeResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service("BillToCoinChangeSimple")
public class BillToCoinChangeSimple implements CoinChangeWithMinCount {
  private Map<Integer, Integer> coinsMemo;
  private Map<Integer, Integer> availableCoins;

  @Override
  public ChangeResult coinChangeWithMinCount(
      Integer[] coins, int amount, Map<Integer, Integer> availableCoins) {
    this.coinsMemo = new HashMap<>();
    this.availableCoins = availableCoins;

    Integer[] coinsLimits = getCoinsLimits(coins, availableCoins);
    int minCoins = coinChangeWithMinCountSimple(amount, coins, coinsLimits);

    if (minCoins == -1) {
      log.error(
          "BillToCoinChange - Bill '{}' can not be exchanged with available coins: {}",
          amount / 100,
          availableCoins);
      throw new IllegalArgumentException(
          String.format(
              "Bill '%s' can not be exchanged with available coins: %s)",
              amount / 100, availableCoins));
    }

    log.info(
        "BillToCoinChange - Bill '{}' can be exchanged with least amount of coins - {}, coins: {}, used coins: {}",
        amount / 100,
        minCoins,
        coins,
        coinsMemo);

    return ChangeResult.builder()
        .bill(amount / 100)
        .coinsCount(minCoins)
        .changeByCoins(coinsMemo)
        .build();
  }

  private int coinChangeWithMinCountSimple(int amount, Integer[] coins, Integer[] limits) {
    Integer currentAmmount = amount;
    Integer totalCoins = 0;

    for (int i = coins.length - 1; i >= 0; i--) {
      Integer currentLimit = limits[i];

      int currentCoinsNumber = currentAmmount / coins[i];
      int divider = Math.min(currentLimit, currentCoinsNumber);
      totalCoins += divider;

      currentAmmount = currentAmmount - divider * coins[i];

      if (currentCoinsNumber > 0) {
        coinsMemo.put(coins[i], divider);
      }

      if (currentAmmount == 0) {
        return totalCoins;
      }
    }

    return -1;
  }
}
