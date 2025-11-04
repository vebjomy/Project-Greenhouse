package controller;

import model.User;
import model.UserRegister;
import ui.UsersView;

/**
 * Controller for the UsersView. Delegates business logic to UserRegister.
 */
public class UsersController {
  private final UsersView view;
  private final UserRegister userRegister;

  /**
   * Constructor for UsersController.
   *
   * @param view The UsersView instance to control.
   */
  public UsersController(UsersView view) {
    this.view = view;
    this.userRegister = new UserRegister();
    this.view.getUserTable().setItems(userRegister.getUserList());
  }

  /**
   * Delegates adding a new user to UserRegister.
   */
  public void addUser() {
    userRegister.addUser();
  }

  /**
   * Delegates editing the selected user to UserRegister.
   */
  public void editUser() {
    User selectedUser = view.getUserTable().getSelectionModel().getSelectedItem();
    userRegister.editUser(selectedUser);
  }

  /**
   * Delegates deleting the selected user to UserRegister.
   */
  public void deleteUser() {
    User selectedUser = view.getUserTable().getSelectionModel().getSelectedItem();
    userRegister.deleteUser(selectedUser);
  }
}
