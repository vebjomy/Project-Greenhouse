

import javafx.application.Application;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.DashboardView;

public class MainApp extends Application {

  @Override
  public void start(Stage stage) {
    DashboardView dashboard = new DashboardView();
    Scene scene = new Scene(dashboard.getRoot(), 1000, 700);

    stage.setTitle("Smart Farm Control");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}