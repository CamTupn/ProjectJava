package com.scentstation.scheduler;

import com.scentstation.entity.AlertType;
import com.scentstation.entity.MachineSlot;
import com.scentstation.repository.MachineSlotRepository;
import com.scentstation.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockAlertJob {

    private final MachineSlotRepository machineSlotRepository;
    private final AlertService alertService;

    // TODO: Đối chiếu ngưỡng LOW_STOCK (ml) trong Issue #53
    @Value("${monitoring.stock.low-threshold-ml:50}")
    private int lowStockThresholdMl;

    @Scheduled(fixedDelayString = "${monitoring.stock.check-interval-ms:60000}")
    @Transactional
    public void checkSlotStockLevels() {
        List<MachineSlot> slots = machineSlotRepository.findAll();

        for (MachineSlot slot : slots) {
            BigDecimal currentMl = slot.getCurrentVolumeMl();
            if (currentMl == null) continue;

            if (currentMl.compareTo(BigDecimal.ZERO) == 0) {
                alertService.createIfAbsent(
                        slot.getMachine(),
                        slot,
                        AlertType.EMPTY_SLOT,
                        "Slot " + slot.getSlotNumber() + " đã hết nước hoa (0 ml)"
                );
            } else if (currentMl.compareTo(BigDecimal.valueOf(lowStockThresholdMl)) <= 0) {
                alertService.createIfAbsent(
                        slot.getMachine(),
                        slot,
                        AlertType.LOW_STOCK,
                        "Slot " + slot.getSlotNumber() + " sắp hết nước hoa (còn " + currentMl.stripTrailingZeros().toPlainString() + " ml)"
                );
            }
        }
    }
}