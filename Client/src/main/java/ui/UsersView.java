package ui;

import controller.UsersController;
import core.ClientApi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.User;

/**
 * The View (UI) for the user management section. It displays a list of users and buttons to manage
 * them.
 */
public class UsersView {

  private final BorderPane view;
  private final UsersController controller;
  private final TableView<User> userTable = new TableView<>();

  /**
   * This class creates the user management view with a table and buttons.
   */
  public UsersView(ClientApi clientApi) {
    controller = new UsersController(this, clientApi);
    view = createUsersView();
    controller.loadUsers();
  }

  /* creates the user management view layout */
  private BorderPane createUsersView() {
    BorderPane layout = new BorderPane();
    layout.setPadding(new Insets(20));
    Label titleLabel = new Label("User Management");
    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

    // --- Buttons ---
    // add user button
    Button addUserBtn = new Button("Add User");
    addUserBtn.setOnAction(e -> controller.addUser());
    addUserBtn.getStyleClass().add("success-button");
    // edit user button
    Button editUserBtn = new Button("Edit User");
    editUserBtn.setOnAction(e -> controller.editUser());
    editUserBtn.getStyleClass().add("warning-button");
    // delete user button
    Button deleteUserBtn = new Button("Delete User");
    deleteUserBtn.setOnAction(e -> controller.deleteUser());
    deleteUserBtn.getStyleClass().add("danger-button");
    // layout for buttons
    HBox buttonBar = new HBox(10, addUserBtn, editUserBtn, deleteUserBtn);
    buttonBar.setAlignment(Pos.CENTER_LEFT);

    VBox topContainer = new VBox(20, titleLabel, buttonBar);
    layout.setTop(topContainer);

    setupTable();
    layout.setCenter(userTable);

    return layout;
  }

  /* sets up the user table columns */

  private void setupTable() {
    TableColumn<User, Integer> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
    idCol.setPrefWidth(50);

    TableColumn<User, String> usernameCol = new TableColumn<>("Username");
    usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
    usernameCol.setPrefWidth(200);

    TableColumn<User, String> roleCol = new TableColumn<>("Role");
    roleCol.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
    roleCol.setPrefWidth(150);

    userTable.getColumns().addAll(idCol, usernameCol, roleCol);
  }

  /* getters for the user table and main view */

  public TableView<User> getUserTable() {
    return userTable;
  }

  /* returns the main view pane */

  public BorderPane getView() {
    return view;
  }
}
