package my.interview.service;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BillToCoinChange {
  private Map<Integer, Integer> coinsMemo;
  private Map<Integer, Integer> availableCoins;

  public Map<Integer, Integer> coinChange(Integer[] coins, int amount, Map<Integer, Integer> availableCoins) {
    this.coinsMemo = new HashMap<>();
    this.availableCoins = availableCoins;

    int minCoins = coinChangeRecHelper(coins, amount, amount, 0, new HashMap<Integer, Integer>());

    if (minCoins == -1) {
      log.error(
          "BillToCoinChange - Bill {} can not be exchanged with available coins: {}", amount / 100);
      throw new IllegalArgumentException(
          String.format("Bill %s can not be exchanged with available coins: %s)", amount / 100));
    }

    log.info(
        "BillToCoinChange - Bill {} can be exchanged with {} coins, coins: {}",
        amount / 100,
        minCoins,
        coinsMemo);

    return coinsMemo;
  }

  protected int coinChangeRecHelper(
      Integer[] coins,
      int amount,
      int currAmount,
      int currNumCoins,
      Map<Integer, Integer> coinQty) {

    log.info(
        "coinChangeRecHelper:  coins:{}, amount: {}, currAmount: {}, currNumCoins: {},  coinQty: {}",
        coins,
        amount,
        currAmount,
        currNumCoins,
        coinQty);
    if (currAmount < 0) return -1;

    if (currAmount == 0) {
      coinsMemo = coinQty;
      return currNumCoins;
    }

    int minCoins = Integer.MAX_VALUE;
    for (int currCoin : coins) {
      Map<Integer, Integer> coinQtyCopy = new HashMap<>(coinQty);
      coinQtyCopy.putIfAbsent(currCoin, 0);
      coinQtyCopy.put(currCoin, coinQtyCopy.get(currCoin) + 1);
      int numCoinsTmp =
          coinChangeRecHelper(coins, amount, currAmount - currCoin, currNumCoins + 1, coinQtyCopy);
      if (numCoinsTmp != -1) {
        minCoins = Math.min(minCoins, numCoinsTmp);
      }
    }

    if (minCoins == Integer.MAX_VALUE) {
      minCoins = -1;
    }

    return minCoins;
  }
}
