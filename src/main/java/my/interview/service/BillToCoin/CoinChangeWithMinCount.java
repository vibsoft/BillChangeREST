package my.interview.service.BillToCoin;

import my.interview.model.ChangeResult;

import java.util.Map;

public interface CoinChangeWithMinCount {
  ChangeResult coinChangeWithMinCount(
      Integer[] coins, int amount, Map<Integer, Integer> availableCoins);

  default Integer[] getCoinsLimits(Integer[] coins, Map<Integer, Integer> availableCoins) {
    Integer[] coinsLimit = new Integer[coins.length];
    for (int i = 0; i < coins.length; i++) {
      coinsLimit[i] = availableCoins.getOrDefault(coins[i], 0);
    }

    return coinsLimit;
  }
}
