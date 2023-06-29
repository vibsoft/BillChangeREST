package my.interview.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.interview.model.ChangeMachineState;
import my.interview.service.BillChangeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
public class Controller {

  private BillChangeService billChangeService;

  @PostMapping(value = "/api/machine/init", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ChangeMachineState> machineInitPost(
      @RequestParam(value = "coinCount", defaultValue = "100") Integer coinCount) {
    log.info("CONTROLLER - machineInit - machine coin count: {}", coinCount);

    return ResponseEntity.ok(billChangeService.initMachine(coinCount));
  }

  @GetMapping(value = "/api/machine/state", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ChangeMachineState> clusterState() {
    log.info("CONTROLLER - clusterState");

    ChangeMachineState clusterState = billChangeService.getMachineState();
    // log.info("CLUSTER - state: {}", clusterState);

    return ResponseEntity.ok(clusterState);
  }

  @GetMapping(value = "/api/machine/topup_coin", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ChangeMachineState> machineTopupCoins(
      @RequestParam("coinCents") Integer coinCents, @RequestParam("coinCount") Integer coinCount) {
    log.info("CONTROLLER - machineTopupCoins - coinCents: {}, coinCount: {}", coinCents, coinCount);

    billChangeService.verifyCentsAmount(coinCents);

    return ResponseEntity.ok(billChangeService.topupCoins(coinCents, coinCount));
  }

  @GetMapping(value = "/api/machine/bill_to_coints", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ChangeMachineState> changeBill(
      @RequestParam(value = "change_bill") Integer bill) {
    log.info("CONTROLLER - change bill - bill: {}", bill);

    billChangeService.verifyBillAmount(bill);

    return ResponseEntity.ok(billChangeService.changeBill(bill));
  }
}
