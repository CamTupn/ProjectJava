package com.scentstation.simulator.persistence;

import com.scentstation.simulator.model.CommandStatus;
import com.scentstation.simulator.model.DispenseCommand;
import com.scentstation.simulator.model.ProcessedCommand;
import com.scentstation.simulator.model.SlotState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProcessedCommandRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistDataAcrossRestartAndMarkInProgressAsUncertain() {
        Path dbFile = tempDir.resolve("simulator-test.db");

        // Instance 1: creates slots, completes cmd-1, leaves cmd-2 IN_PROGRESS (simulating crash/restart)
        SqliteDatabaseManager db1 = new SqliteDatabaseManager(dbFile);
        SlotStateRepository slotRepo1 = new SlotStateRepository(db1);
        ProcessedCommandRepository cmdRepo1 = new ProcessedCommandRepository(db1, slotRepo1);

        SlotState slot = new SlotState(
                "SLOT-1", "machine-1", "Bleu",
                new BigDecimal("100.00"), new BigDecimal("100.00"), "READY", Instant.now()
        );
        slotRepo1.save(slot);

        DispenseCommand cmd1 = new DispenseCommand(
                "cmd-1", "machine-1", "dev-1", "DISPENSE", "SLOT-1", "ord-1",
                new BigDecimal("2.50"), 500, Instant.now().plusSeconds(60)
        );
        assertInstanceOf(ProcessedCommandRepository.ClaimOutcome.Acquired.class, cmdRepo1.claimOrFetch(cmd1));
        slot.setRemainingMl(new BigDecimal("97.50"));
        cmdRepo1.completeCommandAndDeductSlot("cmd-1", "SUCCESS", null, new BigDecimal("2.50"), slot);

        DispenseCommand cmd2 = new DispenseCommand(
                "cmd-2", "machine-1", "dev-1", "DISPENSE", "SLOT-1", "ord-2",
                new BigDecimal("1.50"), 500, Instant.now().plusSeconds(60)
        );
        assertInstanceOf(ProcessedCommandRepository.ClaimOutcome.Acquired.class, cmdRepo1.claimOrFetch(cmd2));

        // Instance 2: simulates restart opening the same SQLite file
        SqliteDatabaseManager db2 = new SqliteDatabaseManager(dbFile);
        SlotStateRepository slotRepo2 = new SlotStateRepository(db2);
        ProcessedCommandRepository cmdRepo2 = new ProcessedCommandRepository(db2, slotRepo2);

        int reconciled = cmdRepo2.markInterruptedCommandsUncertain();
        assertEquals(1, reconciled);

        // Slot balance persisted across restart
        SlotState reloadedSlot = slotRepo2.findByMachineIdAndSlotId("machine-1", "SLOT-1").orElseThrow();
        assertEquals(0, new BigDecimal("97.50").compareTo(reloadedSlot.getRemainingMl()));

        // cmd-1 remains DONE
        ProcessedCommand reloadedCmd1 = cmdRepo2.findByCommandId("cmd-1").orElseThrow();
        assertEquals(CommandStatus.DONE, reloadedCmd1.status());
        assertEquals("SUCCESS", reloadedCmd1.result());
        assertEquals(0, new BigDecimal("2.50").compareTo(reloadedCmd1.volumeDispensedMl()));

        // cmd-2 became UNCERTAIN with volumeDispensedMl == null and cannot be re-acquired
        Optional<ProcessedCommand> reloadedCmd2Opt = cmdRepo2.findByCommandId("cmd-2");
        assertTrue(reloadedCmd2Opt.isPresent());
        ProcessedCommand reloadedCmd2 = reloadedCmd2Opt.get();
        assertEquals(CommandStatus.UNCERTAIN, reloadedCmd2.status());
        assertNull(reloadedCmd2.volumeDispensedMl());

        ProcessedCommandRepository.ClaimOutcome retryAfterRestart = cmdRepo2.claimOrFetch(cmd2);
        assertInstanceOf(ProcessedCommandRepository.ClaimOutcome.Duplicate.class, retryAfterRestart);
    }

    @Test
    void shouldDetectPayloadConflictWhenSameCommandIdHasDifferentPayload() {
        Path dbFile = tempDir.resolve("conflict-test.db");
        SqliteDatabaseManager db = new SqliteDatabaseManager(dbFile);
        SlotStateRepository slotRepo = new SlotStateRepository(db);
        ProcessedCommandRepository cmdRepo = new ProcessedCommandRepository(db, slotRepo);

        DispenseCommand original = new DispenseCommand(
                "cmd-dup-1", "machine-1", "dev-1", "DISPENSE", "SLOT-1", "ord-1",
                new BigDecimal("2.0"), 500, Instant.now().plusSeconds(60)
        );
        DispenseCommand tampered = new DispenseCommand(
                "cmd-dup-1", "machine-1", "dev-1", "DISPENSE", "SLOT-1", "ord-1",
                new BigDecimal("5.0"), 500, Instant.now().plusSeconds(60)
        );

        assertInstanceOf(ProcessedCommandRepository.ClaimOutcome.Acquired.class, cmdRepo.claimOrFetch(original));
        assertInstanceOf(ProcessedCommandRepository.ClaimOutcome.PayloadConflict.class, cmdRepo.claimOrFetch(tampered));
    }
}
