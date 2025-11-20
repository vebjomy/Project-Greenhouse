package app;

/**
 * Serves as a starting point for the shaded jar. In order to create a shaded jar with JavaFX, a
 * main class which does <i>not</i> extend Application is needed.
 */
public class ShadeMain {

  /**
   * The main method which launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    MainApp.main(args);
  }
}
