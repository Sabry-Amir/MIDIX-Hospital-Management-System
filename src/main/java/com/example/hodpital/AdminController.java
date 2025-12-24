package com.example.hodpital;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // ============================================
    //  تعريف عناصر الواجهة (FXML)
    // ============================================

    @FXML private AnchorPane rightSideContainer;
    @FXML private StackPane mainContentArea;
    @FXML private BorderPane sidebar;
    @FXML private Label lblPageTitle;
    @FXML private Label lblAdminName;

    // --- أزرار القائمة الجانبية ---
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnPatients;
    @FXML private Button btnAppointments;
    @FXML private Button btnMedicalRecords;
    @FXML private Button btnRooms;
    @FXML private Button btnBilling;
    @FXML private Button btnInventory;

    // --- عناصر إحصائيات الداشبورد (الأرقام) ---
    @FXML private Label patients_Number;
    @FXML private Label Admitted_Patients_Numbre;
    @FXML private Label Todays_Appointments_Numbre;
    @FXML private Label lblOccupiedRooms;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblLowStock;

    // --- كونتينر القوائم (الداشبورد المطور) ---
    @FXML private VBox pnlRecentAppointments;
    @FXML private VBox pnlRecentPatients;

    // متغيرات مساعدة
    private Button currentActiveButton;
    private boolean isSidebarOpen = true;

    // ============================================
    //  دالة التشغيل (Initialize)
    // ============================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (lblAdminName != null) lblAdminName.setText(UserSession.getUserName());

        // تفعيل تأثير الهوفر للأزرار
        addHoverEffect(btnDashboard);
        addHoverEffect(btnUsers);
        addHoverEffect(btnPatients);
        addHoverEffect(btnAppointments);
        addHoverEffect(btnMedicalRecords);
        addHoverEffect(btnRooms);
        addHoverEffect(btnBilling);
        addHoverEffect(btnInventory);

        // تحميل الصفحة الرئيسية (الداشبورد)
        if (mainContentArea != null) {
            loadPage("Dashboard");
            if (lblPageTitle != null) lblPageTitle.setText("Dashboard");
            setActive(btnDashboard);
        }

        // تحميل بيانات الداشبورد لو العناصر موجودة
        if (patients_Number != null) {
            loadDashboardData();
            loadRecentData();
        }
    }

    // ============================================
    //  نظام التنقل (Navigation Handlers)
    // ============================================

    @FXML public void handleDashboardBtn(ActionEvent event) {
        setActive(btnDashboard);
        loadPage("Dashboard");
        if (lblPageTitle != null) lblPageTitle.setText("Dashboard");
        loadDashboardData();
        loadRecentData();
    }

    @FXML public void handleUserManagementBtn(ActionEvent event) {
        setActive(btnUsers);
        loadPage("UserManagement");
        if (lblPageTitle != null) lblPageTitle.setText("User Management");
    }

    @FXML public void handlePatientsBtn(ActionEvent event) {
        setActive(btnPatients);
        loadPage("Patients");
        if (lblPageTitle != null) lblPageTitle.setText("Patients");
    }

    @FXML public void handleAppointmentsBtn(ActionEvent event) {
        setActive(btnAppointments);
        loadPage("Appointments");
        if (lblPageTitle != null) lblPageTitle.setText("Appointments");
    }

    @FXML public void handleMedicalRecordsBtn(ActionEvent event) {
        setActive(btnMedicalRecords);
        loadPage("MedicalRecords");
        if (lblPageTitle != null) lblPageTitle.setText("Medical Records");
    }

    @FXML public void handleRoomsBtn(ActionEvent event) {
        setActive(btnRooms);
        loadPage("RoomManagement");
        if (lblPageTitle != null) lblPageTitle.setText("Room Management");
    }

    @FXML public void handleBillingBtn(ActionEvent event) {
        setActive(btnBilling);
        loadPage("Billing");
        if (lblPageTitle != null) lblPageTitle.setText("Billing");
    }

    @FXML public void handleInventoryBtn(ActionEvent event) {
        setActive(btnInventory);
        loadPage("Inventory");
        if (lblPageTitle != null) lblPageTitle.setText("Inventory");
    }

    // ============================================
    //  دوال التحكم في الأزرار (Active State)
    // ============================================

    private void setActive(Button activeButton) {
        this.currentActiveButton = activeButton;

        // تصفير ستايل كل الأزرار
        resetButtonStyle(btnDashboard);
        resetButtonStyle(btnUsers);
        resetButtonStyle(btnPatients);
        resetButtonStyle(btnAppointments);
        resetButtonStyle(btnMedicalRecords);
        resetButtonStyle(btnRooms);
        resetButtonStyle(btnBilling);
        resetButtonStyle(btnInventory);

        // تفعيل الزرار المختار
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #4B65BD; -fx-text-fill: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);");
        }
    }

    private void resetButtonStyle(Button btn) {
        if (btn != null) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        }
    }

    private void addHoverEffect(Button btn) {
        if (btn == null) return;
        btn.setOnMouseEntered(e -> {
            if (btn != currentActiveButton) {
                btn.setStyle("-fx-background-color: #35528A; -fx-text-fill: white; -fx-background-radius: 10;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != currentActiveButton) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            }
        });
    }

    // ============================================
    //  دوال البيانات (Dashboard Logic)
    // ============================================

    private void loadDashboardData() {
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection(); Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM patients");
            if (rs.next() && patients_Number != null) patients_Number.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM patients WHERE status = 'admitted'");
            if (rs.next() && Admitted_Patients_Numbre != null) Admitted_Patients_Numbre.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM appointments WHERE appointment_date = CAST(GETDATE() AS DATE)");
            if (rs.next() && Todays_Appointments_Numbre != null) Todays_Appointments_Numbre.setText(String.valueOf(rs.getInt(1)));

            int occupied = 0, totalRooms = 0;
            rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms WHERE status = 'occupied'");
            if(rs.next()) occupied = rs.getInt(1);
            rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if(rs.next()) totalRooms = rs.getInt(1);
            if (lblOccupiedRooms != null) lblOccupiedRooms.setText(occupied + "/" + totalRooms);

            rs = stmt.executeQuery("SELECT SUM(paid_amount) FROM billing");
            if (rs.next() && lblTotalRevenue != null) lblTotalRevenue.setText("$" + String.format("%.1f", rs.getDouble(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM inventory WHERE quantity_in_stock <= low_stock_threshold");
            if (rs.next() && lblLowStock != null) lblLowStock.setText(String.valueOf(rs.getInt(1)));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadRecentData() {
        DBConnection connect = new DBConnection();

        // قائمة المواعيد
        if (pnlRecentAppointments != null) {
            pnlRecentAppointments.getChildren().clear();
            String sqlAppt = "SELECT TOP 5 p.name AS patient_name, a.reason, a.appointment_date, a.appointment_time FROM appointments a JOIN patients p ON a.patient_id = p.patient_id ORDER BY a.appointment_date DESC, a.appointment_time DESC";
            try (Connection conn = connect.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlAppt)) {
                while (rs.next()) {
                    pnlRecentAppointments.getChildren().add(createAppointmentItem(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                    pnlRecentAppointments.getChildren().add(new Separator());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // قائمة المرضى
        if (pnlRecentPatients != null) {
            pnlRecentPatients.getChildren().clear();
            String sqlPat = "SELECT TOP 5 name, phone, status FROM patients ORDER BY patient_id DESC";
            try (Connection conn = connect.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlPat)) {
                while (rs.next()) {
                    pnlRecentPatients.getChildren().add(createPatientItem(rs.getString(1), rs.getString(2), rs.getString(3)));
                    pnlRecentPatients.getChildren().add(new Separator());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // --- رسم كروت القوائم ---
    private HBox createAppointmentItem(String name, String reason, String date, String time) {
        HBox hbox = new HBox(10); hbox.setAlignment(Pos.CENTER_LEFT); hbox.setPadding(new Insets(10));
        VBox vLeft = new VBox(5);
        Label lblN = new Label(name); lblN.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label lblR = new Label(reason); lblR.setFont(Font.font("Arial", 12)); lblR.setTextFill(Color.GRAY);
        vLeft.getChildren().addAll(lblN, lblR);
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        VBox vRight = new VBox(5); vRight.setAlignment(Pos.CENTER_RIGHT);
        Label lblD = new Label(date); lblD.setFont(Font.font("Arial", 11)); lblD.setTextFill(Color.GRAY);
        Label lblT = new Label(time); lblT.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        vRight.getChildren().addAll(lblD, lblT);
        hbox.getChildren().addAll(vLeft, s, vRight);
        return hbox;
    }

    private HBox createPatientItem(String name, String phone, String status) {
        HBox hbox = new HBox(10); hbox.setAlignment(Pos.CENTER_LEFT); hbox.setPadding(new Insets(10));
        VBox vLeft = new VBox(5);
        Label lblN = new Label(name); lblN.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label lblP = new Label(phone); lblP.setFont(Font.font("Arial", 12)); lblP.setTextFill(Color.GRAY);
        vLeft.getChildren().addAll(lblN, lblP);
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label lblS = new Label(status); lblS.setPadding(new Insets(3, 10, 3, 10)); lblS.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        if ("admitted".equalsIgnoreCase(status)) lblS.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 15;");
        else lblS.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 15;");
        hbox.getChildren().addAll(vLeft, s, lblS);
        return hbox;
    }

    // ============================================
    //  دوال مساعدة (Helpers)
    // ============================================

    private void loadPage(String pageName) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(pageName + ".fxml"));
            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- تعديل دالة الـ Toggle لمنع خروج الشاشة لليمين ---
    @FXML
    public void toggleSidebar(ActionEvent event) {
        if (sidebar == null || rightSideContainer == null) return;

        double sidebarWidth = sidebar.getWidth();
        TranslateTransition st = new TranslateTransition(Duration.seconds(0.4), sidebar);

        if (isSidebarOpen) {
            // إخفاء المنيو: تتحرك لليسار بقيمة عرضها بالسالب
            st.setToX(-sidebarWidth);
            // تعديل الـ Anchor للجزء اليمين ليملأ الفراغ فوراً
            AnchorPane.setLeftAnchor(rightSideContainer, 0.0);
            isSidebarOpen = false;
        } else {
            // إظهار المنيو: ترجع للصفر
            st.setToX(0);
            // تعديل الـ Anchor للجزء اليمين ليترك مساحة للمنيو
            AnchorPane.setLeftAnchor(rightSideContainer, sidebarWidth);
            isSidebarOpen = true;
        }
        st.play();
    }

    @FXML public void LogoutAction(ActionEvent e) {
        try {
            Parent r = FXMLLoader.load(getClass().getResource("Login.fxml"));
            ((Stage)((Node)e.getSource()).getScene().getWindow()).setScene(new Scene(r));
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}