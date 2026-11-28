package com.scentstation.simulator.ui;

import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.*;
import com.scentstation.simulator.service.DeviceSimulatorService;
import com.scentstation.simulator.service.DispenseCommandService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SimulatorController {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private TextField brokerUrlField;
    @FXML private TextField machineIdField;
    @FXML private TextField deviceIdentityField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private Button connectButton;
    @FXML private Button disconnectButton;
    @FXML private Label connectionBadgeLabel;
    @FXML private Label pumpStateLabel;
    @FXML private Label activeCommandLabel;
    @FXML private Label lastResultLabel;
    @FXML private Label sprayAnimationLabel;
    @FXML private Circle sprayPulseCircle;

    @FXML private ComboBox<SimulationScenario> scenarioComboBox;

    @FXML private TableView<SlotState> slotTableView;
    @FXML private TableColumn<SlotState, String> colSlotId;
    @FXML private TableColumn<SlotState, String> colFragrance;
    @FXML private TableColumn<SlotState, String> colRemaining;
    @FXML private TableColumn<SlotState, String> colStatus;
    @FXML private TableColumn<SlotState, SlotState> colLevelBar;

    @FXML private TextField newSlotIdField;
    @FXML private TextField newFragranceField;
    @FXML private TextField newRemainingField;
    @FXML private TextField newCapacityField;

    @FXML private ComboBox<String> demoSlotComboBox;
    @FXML private TextField demoVolumeField;
    @FXML private TextArea activityLogArea;

    private final ObservableList<SlotState> slotItems = FXCollections.observableArrayList();
    private SimulatorConfig config;
    private DeviceSimulatorService simulatorService;

    public void initializeWithConfig(SimulatorConfig config) {
        this.config = config;

        brokerUrlField.setText(config.getBrokerUrl());
        machineIdField.setText(config.getMachineId());
        deviceIdentityField.setText(config.getDeviceIdentity());
        usernameField.setText(config.getMqttUsername());
        passwordField.setText(config.getMqttPassword());

        scenarioComboBox.setItems(FXCollections.observableArrayList(SimulationScenario.values()));
        scenarioComboBox.setValue(SimulationScenario.NORMAL);
        scenarioComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SimulationScenario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " — " + item.getDisplayName());
            }
        });
        scenarioComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SimulationScenario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " — " + item.getDisplayName());
            }
        });
        scenarioComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && simulatorService != null) {
                simulatorService.setScenario(newVal);
                if (newVal == SimulationScenario.DEVICE_ERROR) {
                    updatePumpBadge(PumpState.ERROR, null);
                } else if (newVal == SimulationScenario.OFFLINE) {
                    updatePumpBadge(PumpState.OFFLINE, null);
                } else {
                    updatePumpBadge(PumpState.IDLE, null);
                }
            }
        });

        setupSlotTable();

        this.simulatorService = new DeviceSimulatorService(
                config,
                this::appendLog,
                this::onConnectionChanged,
                new DispenseCommandService.Listener() {
                    @Override
                    public void onCommandReceived(DispenseCommand command) {
                        Platform.runLater(() -> activeCommandLabel.setText(
                                command.commandId() + " | Slot " + command.slotId() + " | " + command.volumeMl() + " ml"
                        ));
                    }

                    @Override
                    public void onPumpStateChanged(PumpState state, String activeSlotId, long durationMs) {
                        Platform.runLater(() -> {
                            updatePumpBadge(state, activeSlotId);
                            if (state == PumpState.DISPENSING) {
                                playSprayAnimation(activeSlotId, durationMs);
                            }
                        });
                    }

                    @Override
                    public void onCommandCompleted(ProcessedCommand record) {
                        Platform.runLater(() -> {
                            String err = record.errorCode() != null ? " (" + record.errorCode() + ")" : "";
                            lastResultLabel.setText(record.commandId() + " -> " + record.result() + err);
                            refreshSlots();
                        });
                    }

                    @Override
                    public void onSlotsUpdated() {
                        Platform.runLater(() -> refreshSlots());
                    }
                }
        );

        refreshSlots();
        updateConnectionBadge(false, "Disconnected");
        updatePumpBadge(PumpState.IDLE, null);
        appendLog("[INIT] Simulator ready for machineId=" + config.getMachineId()
                + ", deviceIdentity=" + config.getDeviceIdentity());
    }

    private void setupSlotTable() {
        slotTableView.setItems(slotItems);
        colSlotId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSlotId()));
        colFragrance.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFragranceName()));
        colRemaining.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRemainingMl().toPlainString() + " / " + c.getValue().getCapacityMl().toPlainString() + " ml"
        ));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colLevelBar.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colLevelBar.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar(0);
            private final Label pctLabel = new Label("0%");
            private final VBox box = new VBox(2, bar, pctLabel);

            {
                bar.setMaxWidth(Double.MAX_VALUE);
                pctLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
            }

            @Override
            protected void updateItem(SlotState slot, boolean empty) {
                super.updateItem(slot, empty);
                if (empty || slot == null) {
                    setGraphic(null);
                } else {
                    double ratio = slot.getFillRatio();
                    bar.setProgress(ratio);
                    pctLabel.setText(Math.round(ratio * 100) + "%");
                    if ("EMPTY".equals(slot.getStatus())) {
                        bar.setStyle("-fx-accent: #dc2626;");
                    } else if ("LOW".equals(slot.getStatus())) {
                        bar.setStyle("-fx-accent: #f59e0b;");
                    } else {
                        bar.setStyle("-fx-accent: #16a34a;");
                    }
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    public void handleConnect() {
        String prevMachineId = config.getMachineId();
        config.setBrokerUrl(brokerUrlField.getText().trim());
        config.setMachineId(machineIdField.getText().trim());
        config.setDeviceIdentity(deviceIdentityField.getText().trim());
        config.setMqttUsername(usernameField.getText().trim());
        config.setMqttPassword(passwordField.getText());

        if (!config.getMachineId().equals(prevMachineId)) {
            simulatorService.reloadDeviceStorageIfNeeded();
            refreshSlots();
        }

        connectButton.setDisable(true);
        connectionBadgeLabel.setText("CONNECTING...");
        simulatorService.connectAsync();
    }

    @FXML
    public void handleDisconnect() {
        disconnectButton.setDisable(true);
        simulatorService.disconnectAsync();
    }

    @FXML
    public void handleSaveSlot() {
        try {
            String slotId = newSlotIdField.getText().trim();
            String fragrance = newFragranceField.getText().trim();
            BigDecimal rem = new BigDecimal(newRemainingField.getText().trim());
            BigDecimal cap = new BigDecimal(newCapacityField.getText().trim());
            if (slotId.isEmpty() || fragrance.isEmpty() || cap.compareTo(BigDecimal.ZERO) <= 0 || rem.compareTo(BigDecimal.ZERO) < 0) {
                appendLog("[WARN] Invalid slot input values.");
                return;
            }
            if (rem.compareTo(cap) > 0) {
                rem = cap;
            }
            simulatorService.addOrUpdateSlot(slotId, fragrance, rem, cap);
            newSlotIdField.clear();
            newFragranceField.clear();
            newRemainingField.clear();
            newCapacityField.clear();
        } catch (Exception e) {
            appendLog("[WARN] Invalid slot input: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefillAll() {
        simulatorService.refillAllSlots();
    }

    @FXML
    public void handleDeleteSelectedSlot() {
        SlotState selected = slotTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            simulatorService.deleteSlot(selected.getSlotId());
        }
    }

    @FXML
    public void handleDemoDispense() {
        String slotId = demoSlotComboBox.getValue();
        if (slotId == null || slotId.isBlank()) {
            appendLog("[WARN] Please select a slot for demo dispense.");
            return;
        }
        try {
            BigDecimal vol = new BigDecimal(demoVolumeField.getText().trim());
            if (vol.compareTo(BigDecimal.ZERO) <= 0) {
                appendLog("[WARN] Demo volume must be > 0.");
                return;
            }
            simulatorService.triggerLocalTestDispense(slotId, vol);
        } catch (Exception e) {
            appendLog("[WARN] Invalid demo volume: " + e.getMessage());
        }
    }

    @FXML
    public void handleClearLog() {
        activityLogArea.clear();
    }

    private void refreshSlots() {
        List<SlotState> slots = simulatorService.getSlots();
        slotItems.setAll(slots);
        slotTableView.refresh();

        String currentDemo = demoSlotComboBox.getValue();
        ObservableList<String> slotIds = FXCollections.observableArrayList(
                slots.stream().map(SlotState::getSlotId).toList()
        );
        demoSlotComboBox.setItems(slotIds);
        if (currentDemo != null && slotIds.contains(currentDemo)) {
            demoSlotComboBox.setValue(currentDemo);
        } else if (!slotIds.isEmpty()) {
            demoSlotComboBox.setValue(slotIds.get(0));
        }
    }

    private void onConnectionChanged(boolean connected, String reason) {
        Platform.runLater(() -> updateConnectionBadge(connected, reason));
    }

    private void updateConnectionBadge(boolean connected, String reason) {
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
        brokerUrlField.setDisable(connected);
        machineIdField.setDisable(connected);
        deviceIdentityField.setDisable(connected);
        usernameField.setDisable(connected);
        passwordField.setDisable(connected);

        if (connected) {
            connectionBadgeLabel.setText("ONLINE (" + config.getMachineId() + ")");
            connectionBadgeLabel.getStyleClass().removeAll("badge-offline", "badge-error");
            if (!connectionBadgeLabel.getStyleClass().contains("badge-online")) {
                connectionBadgeLabel.getStyleClass().add("badge-online");
            }
        } else {
            connectionBadgeLabel.setText("OFFLINE" + (reason != null && !reason.isBlank() ? " — " + reason : ""));
            connectionBadgeLabel.getStyleClass().removeAll("badge-online");
            if (!connectionBadgeLabel.getStyleClass().contains("badge-offline")) {
                connectionBadgeLabel.getStyleClass().add("badge-offline");
            }
        }
    }

    private void updatePumpBadge(PumpState state, String slotId) {
        String suffix = (slotId != null && !slotId.isBlank()) ? " [" + slotId + "]" : "";
        pumpStateLabel.setText(state.name() + suffix);
        pumpStateLabel.getStyleClass().removeAll("pump-idle", "pump-dispensing", "pump-error");
        switch (state) {
            case DISPENSING -> pumpStateLabel.getStyleClass().add("pump-dispensing");
            case ERROR, OFFLINE -> pumpStateLabel.getStyleClass().add("pump-error");
            default -> pumpStateLabel.getStyleClass().add("pump-idle");
        }
    }

    private void playSprayAnimation(String slotId, long durationMs) {
        long animMs = Math.max(400L, durationMs);
        sprayAnimationLabel.setText("SPRAYING MIST -> " + slotId + " ...");
        sprayPulseCircle.setVisible(true);

        ScaleTransition scale = new ScaleTransition(Duration.millis(animMs / 2.0), sprayPulseCircle);
        scale.setFromX(0.6);
        scale.setFromY(0.6);
        scale.setToX(1.6);
        scale.setToY(1.6);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();

        FadeTransition fade = new FadeTransition(Duration.millis(animMs), sprayPulseCircle);
        fade.setFromValue(0.95);
        fade.setToValue(0.2);
        fade.play();

        PauseTransition pause = new PauseTransition(Duration.millis(animMs));
        pause.setOnFinished(e -> {
            sprayPulseCircle.setVisible(false);
            sprayAnimationLabel.setText("Nozzle Ready (Idle)");
        });
        pause.play();
    }

    private void appendLog(String line) {
        String stamped = "[" + LocalTime.now().format(TIME_FMT) + "] " + line + "\n";
        Platform.runLater(() -> {
            if (activityLogArea != null) {
                activityLogArea.appendText(stamped);
            }
        });
    }

    public void shutdown() {
        if (simulatorService != null) {
            simulatorService.shutdown();
        }
    }
}
