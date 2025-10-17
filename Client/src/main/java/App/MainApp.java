package App;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ui.DashboardView;
import ui.LoginScreenView;
import ui.RegistrationView;
import ui.SplashScreenView;


public class MainApp extends Application {

  private Stage primaryStage;
  private static final double SCENE_WIDTH = 1480;
  private static final double SCENE_HEIGHT = 1000;

  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;
    primaryStage.setTitle("Green House Control");
    // Create a window title

    primaryStage.centerOnScreen();


    Font customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/KaiseiDecol-Regular.ttf"), 10);
    if (customFont == null) {
      System.err.println("error loading font");
    } else {
      System.out.println("Font is loaded: " + customFont.getFamily());
    }


    showSplashScreen(); // Start with the splash screen

    primaryStage.setMinHeight(SCENE_HEIGHT);
    primaryStage.setMinWidth(SCENE_WIDTH);

    primaryStage.show();
  }

  /**
   * views the splash screen.
   */
  public void showSplashScreen() {
    SplashScreenView splash = new SplashScreenView(this);
    Scene scene = new Scene(splash.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Welcome");
  }


  public void showRegistrationScreen() {
   RegistrationView registration = new RegistrationView(this);
    Scene scene = new Scene(registration.getRoot(), 400, 400);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Register");
  }

  /**
   * views the login screen.
   */
  public void showLoginScreen() {
    LoginScreenView login = new LoginScreenView(this);
    Scene scene = new Scene(login.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Login");
  }

  /**
   * views the dashboard screen.
   */
  public void showDashboard() {
    DashboardView dashboard = new DashboardView();
    Scene scene = new Scene(dashboard.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Smart Farm Control"); // view the dashboard
  }





  public static void main(String[] args) {
    launch(args);
  }
}
