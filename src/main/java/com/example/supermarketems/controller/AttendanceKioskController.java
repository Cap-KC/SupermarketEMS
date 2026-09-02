package com.example.supermarketems.controller;

import com.example.supermarketems.dao.AttendanceDAO;
import com.example.supermarketems.dao.EmployeeDAO;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.util.SceneNavigator;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AttendanceKioskController {

    @FXML
    private Label lblClock;

    @FXML
    private Label lblDate;

    @FXML
    private TextField txtScannerInput;

    @FXML
    private TextField txtManualId;

    @FXML
    private VBox cardFeedback;

    @FXML
    private Label lblFeedbackStatus;

    @FXML
    private Label lblFeedbackName;

    @FXML
    private Label lblFeedbackDetails;

    // Webcam & Mode UI Elements
    @FXML
    private ImageView imgWebcamPreview;

    @FXML
    private Label lblScannerStatus;

    @FXML
    private Button btnNfcMode;

    @FXML
    private Button btnQrMode;

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    private Timeline clockTimeline;

    // Webcam Scanning Fields
    private Webcam webcam;
    private ScheduledExecutorService webcamExecutor;
    private volatile boolean isScanning = false;
    private String activeMode = "NFC"; // Default active mode

    @FXML
    public void initialize() {
        startRealTimeClock();

        // Start background webcam capture thread
        startWebcamScanner();

        // Ensure the hidden scanner input field always retains focus for USB readers
        Platform.runLater(() -> txtScannerInput.requestFocus());
    }

    /**
     * Initializes webcam hardware and polls camera frames for QR decoding
     */
    private void startWebcamScanner() {
        webcamExecutor = Executors.newSingleThreadScheduledExecutor();

        webcamExecutor.execute(() -> {
            try {
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    Platform.runLater(() -> {
                        if (lblScannerStatus != null) {
                            lblScannerStatus.setText("No camera hardware detected.");
                        }
                    });
                    return;
                }

                if (!webcam.isOpen()) {
                    webcam.open();
                }

                // Schedule frame polling (~20 FPS)
                webcamExecutor.scheduleAtFixedRate(() -> {
                    if (webcam != null && webcam.isOpen()) {
                        BufferedImage bufferedImage = webcam.getImage();
                        if (bufferedImage != null) {

                            // Render frame onto UI thread
                            if (imgWebcamPreview != null) {
                                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                                Platform.runLater(() -> imgWebcamPreview.setImage(fxImage));
                            }

                            // Scan frame for QR code if not currently on delay/cooldown
                            if (!isScanning) {
                                decodeQRCode(bufferedImage);
                            }
                        }
                    }
                }, 0, 50, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                System.err.println("Webcam initialization error: " + e.getMessage());
                Platform.runLater(() -> {
                    if (lblScannerStatus != null) {
                        lblScannerStatus.setText("Camera Error: " + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Decodes QR code from BufferedImage frame using ZXing
     */
    private void decodeQRCode(BufferedImage bufferedImage) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);

            if (result != null && result.getText() != null) {
                isScanning = true; // Lock scanner during feedback processing
                String qrData = result.getText().trim();

                Platform.runLater(() -> {
                    if (lblScannerStatus != null) {
                        lblScannerStatus.setText("QR Code Scanned: " + qrData);
                    }
                    processAttendanceScan(qrData, "QR");

                    // 3-second cooldown delay before next scan
                    PauseTransition cd = new PauseTransition(Duration.seconds(3));
                    cd.setOnFinished(e -> {
                        isScanning = false;
                        if (lblScannerStatus != null) {
                            lblScannerStatus.setText("Point QR Code at camera...");
                        }
                    });
                    cd.play();
                });
            }
        } catch (NotFoundException e) {
            // Normal behavior - frame does not contain a valid QR code
        } catch (Exception e) {
            System.err.println("QR Decoding Error: " + e.getMessage());
        }
    }

    /**
     * Mode selection button handlers
     */
    @FXML
    private void handleSelectNfcMode() {
        activeMode = "NFC";
        if (btnNfcMode != null) {
            btnNfcMode.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        }
        if (btnQrMode != null) {
            btnQrMode.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        }
        txtScannerInput.requestFocus();
    }

    @FXML
    private void handleSelectQrMode() {
        activeMode = "QR";
        if (btnQrMode != null) {
            btnQrMode.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        }
        if (btnNfcMode != null) {
            btnNfcMode.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        }
        txtScannerInput.requestFocus();
    }

    /**
     * Handles inputs sent from hardware scanners (USB NFC/QR readers ending with ENTER)
     */
    @FXML
    private void handleScannerSubmit() {
        String scannedData = txtScannerInput.getText().trim();
        txtScannerInput.clear();

        if (!scannedData.isEmpty()) {
            String method = determineScanMethod(scannedData);
            if ("NFC/QR".equals(method)) {
                method = activeMode;
            }
            processAttendanceScan(scannedData, method);
        }
    }

    /**
     * Handles manual ID submission via the text box
     */
    @FXML
    private void handleManualSubmit() {
        String employeeId = txtManualId.getText().trim();
        txtManualId.clear();

        if (!employeeId.isEmpty()) {
            processAttendanceScan(employeeId, "MANUAL");
        }
    }

    /**
     * Main logic to verify employee ID and toggle Clock In / Clock Out
     */
    private void processAttendanceScan(String identifier, String method) {
        // Fetch employee record by ID or Email/Card mapping
        Employee employee = employeeDAO.getEmployeeById(identifier);

        if (employee == null) {
            showFeedback(false, "EMPLOYEE NOT FOUND", "ID: " + identifier, "Please check your ID and try again.");
            return;
        }

        // Check current clock-in status from DB
        boolean isCurrentlyClockedIn = attendanceDAO.isEmployeeClockedIn(employee.getEmployeeId());

        boolean success;
        String actionStatus;

        if (isCurrentlyClockedIn) {
            success = attendanceDAO.clockOut(employee.getEmployeeId());
            actionStatus = "CLOCKED OUT SUCCESSFULLY";
        } else {
            success = attendanceDAO.clockIn(employee.getEmployeeId(), method);
            actionStatus = "CLOCKED IN SUCCESSFULLY";
        }

        if (success) {
            String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            showFeedback(true, actionStatus, employee.getName(), "Time: " + timeStr + " | Method: " + method);
        } else {
            showFeedback(false, "TRANSACTION FAILED", employee.getName(), "Database update failed. Try again.");
        }
    }

    /**
     * Renders feedback card and auto-resets after 3 seconds
     */
    private void showFeedback(boolean isSuccess, String status, String name, String details) {
        cardFeedback.setStyle(isSuccess
                ? "-fx-background-color: #1e293b; -fx-border-color: #22c55e; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20 40; -fx-min-width: 500;"
                : "-fx-background-color: #1e293b; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20 40; -fx-min-width: 500;");

        lblFeedbackStatus.setStyle(isSuccess
                ? "-fx-text-fill: #22c55e; -fx-font-size: 20px; -fx-font-weight: bold;"
                : "-fx-text-fill: #ef4444; -fx-font-size: 20px; -fx-font-weight: bold;");

        lblFeedbackStatus.setText(status);
        lblFeedbackName.setText(name);
        lblFeedbackDetails.setText(details);

        cardFeedback.setVisible(true);
        cardFeedback.setManaged(true);

        // Auto-reset feedback panel back to standby after 3 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            cardFeedback.setVisible(false);
            cardFeedback.setManaged(false);
            txtScannerInput.requestFocus();
        });
        delay.play();
    }

    /**
     * Redirects key focus back to the hidden input box if an employee scans without clicking
     */
    @FXML
    private void handleGlobalKeyPress(KeyEvent event) {
        if (!txtManualId.isFocused()) {
            txtScannerInput.requestFocus();
        }
    }

    /**
     * Live digital clock ticker
     */
    private void startRealTimeClock() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            lblClock.setText(now.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
            lblDate.setText(now.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")));
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private String determineScanMethod(String data) {
        if (data.startsWith("NFC-") || data.matches("\\d{10}")) {
            return "NFC";
        } else if (data.startsWith("QR-") || data.contains(":")) {
            return "QR";
        }
        return "NFC/QR";
    }

    /**
     * Clean shutdown for clock timeline and webcam resources
     */
    public void stopWebcam() {
        if (webcamExecutor != null && !webcamExecutor.isShutdown()) {
            webcamExecutor.shutdownNow();
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    @FXML
    private void handleExitKiosk(ActionEvent event) {
        stopWebcam();
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        SceneNavigator.switchScene(event, "/com/example/supermarketems/login-view.fxml", "Supermarket EMS - Login", null);
    }
}