package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class RoomManagementController implements Initializable {

    @FXML private Label lblTotalRooms, lblAvailable, lblOccupied, lblMaintenance;
    @FXML private Label lblGeneralCount, lblPrivateCount, lblICUCount, lblERCount;
    @FXML private FlowPane fpGeneral, fpPrivate, fpICU, fpEmergency;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStats();
        loadRooms();
    }

    // 1. تحميل الإحصائيات العلوية
    private void loadStats() {
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection(); Statement stmt = conn.createStatement()) {

            int total = 0, avail = 0, occ = 0, maint = 0;

            // عد الغرف حسب الحالة
            ResultSet rs = stmt.executeQuery("SELECT status, COUNT(*) FROM rooms GROUP BY status");
            while (rs.next()) {
                String status = rs.getString(1).toLowerCase();
                int count = rs.getInt(2);
                total += count;

                if (status.contains("available")) avail = count;
                else if (status.contains("occupied")) occ = count;
                else if (status.contains("maintenance")) maint = count;
            }

            lblTotalRooms.setText(String.valueOf(total));
            lblAvailable.setText(String.valueOf(avail));
            lblOccupied.setText(String.valueOf(occ));
            lblMaintenance.setText(String.valueOf(maint));

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 2. تحميل الغرف وتوزيعها
    private void loadRooms() {
        // تنظيف القديم
        fpGeneral.getChildren().clear();
        fpPrivate.getChildren().clear();
        fpICU.getChildren().clear();
        fpEmergency.getChildren().clear();

        int genC = 0, privC = 0, icuC = 0, erC = 0;

        DBConnection connect = new DBConnection();
        // بنعمل Left Join عشان نجيب اسم المريض لو الغرفة مشغولة
        String sql = "SELECT r.*, p.name AS patient_name FROM rooms r " +
                "LEFT JOIN patients p ON r.current_patient_id = p.patient_id";

        try (Connection conn = connect.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Room room = new Room(
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getInt("floor_number"),
                        rs.getDouble("price_per_day"),
                        rs.getString("status"),
                        rs.getString("patient_name")
                );

                // إنشاء الكارت
                AnchorPane card = createRoomCard(room);

                // التوزيع حسب النوع
                String type = room.getRoomType().toLowerCase();
                if (type.contains("general")) {
                    fpGeneral.getChildren().add(card);
                    genC++;
                } else if (type.contains("private")) {
                    fpPrivate.getChildren().add(card);
                    privC++;
                } else if (type.contains("icu")) {
                    fpICU.getChildren().add(card);
                    icuC++;
                } else if (type.contains("emergency")) {
                    fpEmergency.getChildren().add(card);
                    erC++;
                }
            }

            // تحديث عناوين الأقسام
            lblGeneralCount.setText("General Rooms (" + genC + ")");
            lblPrivateCount.setText("Private Rooms (" + privC + ")");
            lblICUCount.setText("ICU Rooms (" + icuC + ")");
            lblERCount.setText("Emergency Rooms (" + erC + ")");

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 3. دالة رسم الكارت (تصميم الكارت بالكود)
    private AnchorPane createRoomCard(Room room) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(250, 140);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e5e7eb; -fx-border-width: 1;");
        card.setEffect(new DropShadow(5, Color.rgb(0,0,0,0.05)));

        // تحديد الألوان حسب الحالة
        String statusColor;
        String statusBg;
        if ("occupied".equalsIgnoreCase(room.getStatus())) {
            statusColor = "#ef4444"; // أحمر
            statusBg = "#fee2e2";
            card.setStyle(card.getStyle() + "-fx-border-color: #fecaca;"); // حدود حمراء
        } else if ("available".equalsIgnoreCase(room.getStatus())) {
            statusColor = "#16a34a"; // أخضر
            statusBg = "#dcfce7";
            card.setStyle(card.getStyle() + "-fx-border-color: #bbf7d0;"); // حدود خضراء
        } else {
            statusColor = "#ea580c"; // برتقالي للصيانة
            statusBg = "#ffedd5";
        }

        // رقم الغرفة
        Label lblNum = new Label(room.getRoomNumber());
        lblNum.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblNum.setTextFill(Color.web("#374151"));
        AnchorPane.setTopAnchor(lblNum, 15.0);
        AnchorPane.setLeftAnchor(lblNum, 15.0);

        // أيقونة السرير
        FontIcon icon = new FontIcon("fas-bed");
        icon.setIconSize(18);
        icon.setIconColor(Color.web(statusColor));
        AnchorPane.setTopAnchor(icon, 15.0);
        AnchorPane.setRightAnchor(icon, 15.0);

        // تفاصيل (الدور والسعر)
        VBox details = new VBox(3);
        Label lblFloor = new Label("Floor " + room.getFloor());
        lblFloor.setTextFill(Color.GRAY);
        Label lblPrice = new Label("$" + room.getPrice() + "/day");
        lblPrice.setTextFill(Color.GRAY);
        details.getChildren().addAll(lblFloor, lblPrice);
        AnchorPane.setTopAnchor(details, 45.0);
        AnchorPane.setLeftAnchor(details, 15.0);

        // اسم المريض (لو موجود)
        if (room.getPatientName() != null && !room.getPatientName().isEmpty()) {
            Label lblPatient = new Label(room.getPatientName());
            lblPatient.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            lblPatient.setTextFill(Color.web("#1f2937"));
            details.getChildren().add(lblPatient);
        }

        // حالة الغرفة (Badge)
        Label lblStatus = new Label(room.getStatus());
        lblStatus.setStyle("-fx-background-color: " + statusBg + "; -fx-text-fill: " + statusColor + "; -fx-background-radius: 15;");
        lblStatus.setPadding(new Insets(4, 10, 4, 10));
        lblStatus.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        AnchorPane.setBottomAnchor(lblStatus, 15.0);
        AnchorPane.setLeftAnchor(lblStatus, 15.0);

        card.getChildren().addAll(lblNum, icon, details, lblStatus);
        return card;
    }

    // ضيف المتغيرات دي فوق مع تعريف الكلاس (عشان التحريك)
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void openAddRoomWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddRoom.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // تحريك النافذة
            root.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
            root.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - xOffset); stage.setY(e.getScreenY() - yOffset); });

            stage.showAndWait();

            // تحديث الصفحة بعد الإغلاق عشان الغرفة الجديدة تظهر
            loadStats();
            loadRooms();

        } catch (Exception e) { e.printStackTrace(); }
    }
}