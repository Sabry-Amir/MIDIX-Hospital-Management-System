package com.example.hodpital;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colDepartment;
    @FXML private TableColumn<User, String> colActions;
    @FXML private TextField searchField;

    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadData();
        setupSearch();
        applyTableStyles(); // هنا بيتم تطبيق الستايل الجديد
    }

    // ============================================
    //  دالة الستايل (المعدلة لتوسيط الكلام)
    // ============================================
    private void applyTableStyles() {
        String css = "data:text/css," +
                ".table-view { -fx-background-color: white; -fx-border-color: transparent; } " +
                ".table-view .column-header-background { -fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0; } " +
                ".table-view .column-header { -fx-background-color: transparent; -fx-padding: 10px; } " +

                // توسيط عناوين الأعمدة
                ".table-view .column-header .label { -fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-alignment: CENTER; } " +

                ".table-row-cell { -fx-background-color: white; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 0 0 1 0; -fx-padding: 5px 0; } " +
                ".table-row-cell:hover { -fx-background-color: #F9FAFB; } " +

                // توسيط البيانات داخل الخلايا
                ".table-cell { -fx-text-fill: #374151; -fx-font-size: 13px; -fx-alignment: CENTER; -fx-border-color: transparent; } " +

                ".table-view .scroll-bar:horizontal .track, .table-view .scroll-bar:vertical .track { -fx-background-color: transparent; } " +
                ".table-view .scroll-bar:horizontal .thumb, .table-view .scroll-bar:vertical .thumb { -fx-background-color: #E5E7EB; -fx-background-radius: 5em; }";

        usersTable.getStylesheets().add(css);
    }

    private void setupColumns() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.setPrefWidth(90); lbl.setPrefHeight(25); lbl.setAlignment(Pos.CENTER);
                    String baseStyle = "-fx-font-weight: bold; -fx-background-radius: 15; -fx-font-size: 11px;";
                    switch (item.toLowerCase()) {
                        case "admin": lbl.setStyle(baseStyle + "-fx-background-color: #f3e8ff; -fx-text-fill: #9333ea;"); break;
                        case "doctor": lbl.setStyle(baseStyle + "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb;"); break;
                        case "nurse": lbl.setStyle(baseStyle + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;"); break;
                        case "receptionist": lbl.setStyle(baseStyle + "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563;"); break;
                        default: lbl.setStyle(baseStyle + "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;"); break;
                    }
                    setGraphic(lbl); setText(null);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            {
                FontIcon editIcon = new FontIcon("fas-pen"); editIcon.setIconSize(13); editIcon.setIconColor(Color.web("#3b82f6"));
                editBtn.setGraphic(editIcon); editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editBtn.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));

                FontIcon deleteIcon = new FontIcon("fas-trash"); deleteIcon.setIconSize(13); deleteIcon.setIconColor(Color.web("#ef4444"));
                deleteBtn.setGraphic(deleteIcon); deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn) {{ setAlignment(Pos.CENTER); }});
            }
        });
    }

    private void loadData() {
        usersList.clear();
        DBConnection connectNow = new DBConnection();
        try (Connection connectDB = connectNow.getConnection()) {
            if (connectDB != null) {
                String sql = "SELECT * FROM users";
                Statement statement = connectDB.createStatement();
                ResultSet result = statement.executeQuery(sql);
                while (result.next()) {
                    usersList.add(new User(
                            result.getString("username"), result.getString("full_name"),
                            result.getString("role"), result.getString("email"),
                            result.getString("department"), result.getString("password")
                    ));
                }
                usersTable.setItems(usersList);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<User> filteredData = new FilteredList<>(usersList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (user.getFullName().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (user.getRole() != null && user.getRole().toLowerCase().contains(lowerCaseFilter)) return true;
                else return user.getDepartment() != null && user.getDepartment().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);
    }

    @FXML
    public void openAddUserWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            makeDraggable(root, stage);
            stage.showAndWait();
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddUser.fxml"));
            Parent root = loader.load();
            AddUserController controller = loader.getController();
            controller.setUpdateData(user);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            makeDraggable(root, stage);
            stage.showAndWait();
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete user: " + user.getUsername() + "?");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-border-color: #4f46e5; -fx-border-width: 2px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: white;");
        makeDraggable(dialogPane, (Stage) dialogPane.getScene().getWindow());

        if (alert.showAndWait().get() == ButtonType.OK) {
            DBConnection connectNow = new DBConnection();
            try (Connection connectDB = connectNow.getConnection()) {
                String sql = "DELETE FROM users WHERE username = ?";
                PreparedStatement statement = connectDB.prepareStatement(sql);
                statement.setString(1, user.getUsername());
                if (statement.executeUpdate() > 0) loadData();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void makeDraggable(Parent root, Stage stage) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}