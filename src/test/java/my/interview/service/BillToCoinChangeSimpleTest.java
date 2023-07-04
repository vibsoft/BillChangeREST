package my.interview.service;

import my.interview.model.ChangeResult;
import my.interview.service.BillToCoin.BillToCoinChangeDP;
import my.interview.service.BillToCoin.BillToCoinChangeSimple;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BillToCoinChangeSimpleTest {
  Map<Integer, Integer> availableCoins = new HashMap<>();
  //  {{
  //    put(1, 100);
  //    put(5, 100);
  //  }};//(1, 100, 5, 100, 10, 100, 25, 100);
  public Integer[] coins = new Integer[] {1, 5, 10, 25};

  @Test
  public void testBillExchangeWithLeastAmount() {

    BillToCoinChangeSimple billToCoins = new BillToCoinChangeSimple();
    int amountInCents = 100; // ! dollar bill
    Map<Integer, Integer> availableCoins = new HashMap<>();
    availableCoins.put(1, 100);
    availableCoins.put(5, 100);
    availableCoins.put(10, 100);
    availableCoins.put(25, 100);

    Integer[] coins = new Integer[] {1, 5, 10, 25};
    ChangeResult result = billToCoins.coinChangeWithMinCount(coins, amountInCents, availableCoins);
    assertEquals(result.getCoinsCount(), 4);

    coins = new Integer[] {1, 5, 10};
    result = billToCoins.coinChangeWithMinCount(coins, amountInCents, availableCoins);
    assertEquals(result.getCoinsCount(), 10);

    coins = new Integer[] {1, 5};
    result = billToCoins.coinChangeWithMinCount(coins, amountInCents, availableCoins);
    assertEquals(result.getCoinsCount(), 20);
  }

  @Test
  public void testBillExchangeWithMostAmount() {
    BillToCoinChangeDP billToCoins = new BillToCoinChangeDP();
    int amountInCents = 100; // ! dollar bill

    Integer[] coins = new Integer[] {1, 5, 10, 25};
    ChangeResult result = billToCoins.coinChangeWithMaxCount(coins, amountInCents, availableCoins);
    assertEquals(result.getCoinsCount(), 100);

    coins = new Integer[] {5, 10, 25};
    result = billToCoins.coinChangeWithMaxCount(coins, amountInCents, availableCoins);
    assertEquals(result.getCoinsCount(), 20);

    coins = new Integer[] {10, 25};
    result = billToCoins.coinChangeWithMaxCount(coins, amountInCents, availableCoins);
    assertEquals(result.getCoinsCount(), 10);
  }
}
