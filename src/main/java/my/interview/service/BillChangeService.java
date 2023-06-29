package my.interview.service;

import my.interview.model.ChangeMachineState;

public interface BillChangeService {

  ChangeMachineState initMachine(Integer initTokensCount);

  ChangeMachineState getMachineState();

  boolean verifyCentsAmount(int centAmount);
  boolean verifyBillAmount(int billAmount);

  ChangeMachineState topupCoins(Integer coinCents, Integer coinCount);

  ChangeMachineState changeBill(Integer bill);
}
