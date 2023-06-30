package my.interview.service;

import lombok.extern.slf4j.Slf4j;
import my.interview.model.ChangeResult;

import java.util.*;

@Slf4j
public class BillToCoinChange {
  private Map<Integer, Integer> coinsMemo;
  private Map<Integer, Integer> availableCoins;

  private Integer[] getCoinsLimits(Integer[] coins, Map<Integer, Integer> availableCoins) {
    Integer[] coinsLimit = new Integer[coins.length];
    for (int i = 0; i < coins.length; i++) {
      coinsLimit[i] = availableCoins.getOrDefault(coins[i], 0);
    }

    return coinsLimit;
  }

  public ChangeResult coinChangeWithMinCount(
      Integer[] coins, int amount, Map<Integer, Integer> availableCoins) {
    this.coinsMemo = new HashMap<>();
    this.availableCoins = availableCoins;

    int minCoins = coinChangeWithMinCountDP(amount, coins);

    // Integer[] coinsLimits = getCoinsLimits(coins, availableCoins);
    // int changeDynamics = dynamicChange(amount, coins, coinsLimits);
    // log.info("changeCoins: {}", changeDynamics);
    // int minCoins = coinChangeRecursive(coins, amount, amount, 0, new HashMap<Integer,
    // Integer>());

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

  public ChangeResult coinChangeWithMaxCount(
      Integer[] coins, int amount, Map<Integer, Integer> availableCoins) {
    this.coinsMemo = new HashMap<>();
    this.availableCoins = availableCoins;

    int maxCoins = coinChangeWithMaxCountDP(amount, coins);

    if (maxCoins == -1) {
      log.error(
          "BillToCoinChange - Bill '{}' can not be exchanged with least coins: {}", amount / 100);
      throw new IllegalArgumentException(
          String.format("Bill '%s' can not be exchanged with available coins: %s)", amount / 100));
    }

    log.info(
        "BillToCoinChange - Bill '{}' can be exchanged with most amount of coins - {}, coins: {}, used coins: {}",
        amount / 100,
        maxCoins,
        coins,
        coinsMemo);

    return ChangeResult.builder()
        .bill(amount / 100)
        .coinsCount(maxCoins)
        .changeByCoins(coinsMemo)
        .build();
  }

  private int coinChangeWithMinCountDP(int amount, Integer[] coins) {
    int[] prevCoin = new int[amount + 1];
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, 1, dp.length, amount + 1);

    for (int i = 1; i <= amount; ++i)
      for (final int coin : coins) {
        if (coin <= i) {
          dp[i] = Math.min(dp[i], dp[i - coin] + 1);
          // log.info("for i:{}', coin:{} - dp: {}", i ,coin, dp);
          prevCoin[i] = coin;
        }
      }

    coinsMemo = getCoinsForChangeMin(amount, prevCoin);

    return dp[amount] == amount + 1 ? -1 : dp[amount];
  }

  private int coinChangeWithMaxCountDP(int amount, Integer[] coins) {
    int[] prevCoin = new int[amount + 1];
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, 1, dp.length, -100);

    for (int i = 1; i <= amount; ++i)
      for (final int coin : coins) {
        if (coin <= i) {
          dp[i] = Math.max(dp[i], dp[i - coin] + 1);
          //log.info("for i:{}', coin:{} - dp: {}", i, coin, dp);
          prevCoin[i] = coin;
        }
      }

    //coinsMemo = getCoinsForChange(amount, prevCoin);

    return dp[amount] == -100 ? -1 : dp[amount];
  }

  private Map<Integer, Integer> getCoinsForChangeMin(int amount, int[] prevCoin) {
    List<Integer> coinsAdded = new ArrayList<>();
    for (int i = amount; i >= 1; ) {
      coinsAdded.add(prevCoin[i]);
      int j = i;
      i = amount - prevCoin[i];
      amount = amount - prevCoin[j];
    }

    Map<Integer, Integer> usedCoins = new HashMap<>();
    for (Integer coin : coinsAdded) {
      Integer currentCount = usedCoins.getOrDefault(coin, 0);
      usedCoins.put(coin, currentCount + 1);
    }
    //log.info("usedCoins: {}", usedCoins);

    return usedCoins;
  }

  // too slow
  private int coinChangeRecursive(
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
          coinChangeRecursive(coins, amount, currAmount - currCoin, currNumCoins + 1, coinQtyCopy);
      if (numCoinsTmp != -1) {
        minCoins = Math.min(minCoins, numCoinsTmp);
      }
    }

    if (minCoins == Integer.MAX_VALUE) {
      minCoins = -1;
    }

    return minCoins;
  }

  // TODO check limits and return
  public int dynamicChange(int amount, Integer[] coins, Integer[] limits) {
    int[] change;
    int[][] coinsUsed = new int[amount + 1][coins.length];
    for (int i = 0; i <= amount; ++i) {
      coinsUsed[i] = new int[coins.length];
    }

    int[] minCoins = new int[amount + 1];
    Arrays.fill(minCoins, Integer.MAX_VALUE - 1);

    Integer[] limitsCopy = Arrays.copyOf(limits, limits.length);

    for (int i = 0; i < coins.length; ++i) {
      while (limitsCopy[i] > 0) {
        for (int j = amount; j >= 0; --j) {
          int currAmount = j + coins[i];
          if (currAmount <= amount) {
            if (minCoins[currAmount] > minCoins[j] + 1) {
              minCoins[currAmount] = minCoins[j] + 1;

              System.arraycopy(coinsUsed[j], 0, coinsUsed[currAmount], 0, coins.length);
              coinsUsed[currAmount][i] += 1;
            }
          }
        }

        limitsCopy[i] -= 1;
      }
    }

    // log.info("coinsUsed : {}", coinsUsed);
    // log.info("minCoins : {}", minCoins);

    if (minCoins[amount] == Integer.MAX_VALUE - 1) {
      // change = null;
      return -1;
    }

    // return coinsUsed[amount];
    return minCoins[amount];
  }
}
