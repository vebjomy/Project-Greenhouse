

import com.farm.client.controller.DashboardController;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class DashboardView {
  private final BorderPane root = new BorderPane();
  private final DashboardController controller;

  public DashboardView() {
    controller = new DashboardController(this);
    setupUI();
  }

  private void setupUI() {
    Label title = new Label("Farm Dashboard");
    title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

    Button refreshBtn = new Button("Refresh");
    refreshBtn.setOnAction(e -> controller.refreshData());

    HBox top = new HBox(10, title, refreshBtn);
    top.setPadding(new Insets(15));
    root.setTop(top);

    // Основное содержимое
    VBox centerBox = new VBox(10);
    centerBox.setPadding(new Insets(20));

    Label nodeCountLabel = new Label("Nodes: loading...");
    Label lastUpdateLabel = new Label("Last update: -");

    centerBox.getChildren().addAll(nodeCountLabel, lastUpdateLabel);
    root.setCenter(centerBox);

    // Передаём ссылки контроллеру
    controller.setLabels(nodeCountLabel, lastUpdateLabel);
  }

  public BorderPane getRoot() {
    return root;
  }
}
