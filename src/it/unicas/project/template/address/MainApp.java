package it.unicas.project.template.address;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.view.LoginDialogController;
import it.unicas.project.template.address.view.UserLandingController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;
    private User loggedUser;

    // Listas para el usuario
    private ObservableList<String> userLoans = FXCollections.observableArrayList();
    private ObservableList<String> userReservations = FXCollections.observableArrayList();

    public ObservableList<String> getUserLoans() {
        return userLoans;
    }

    public ObservableList<String> getUserReservations() {
        return userReservations;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Library Management App");
        this.primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));

        boolean loggedIn = showLoginDialog();
        if (loggedIn) {
            // Dependiendo del rol, abrir la vista correspondiente
            if (loggedUser.getIdRole() == 1) {
                showAdminLanding();
            } else {
                showUserLandingView();
            }
            primaryStage.show();
        } else {
            primaryStage.close();
        }
    }

    private boolean showLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/LoginDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            LoginDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isLoginSuccessful()) {
                String username = controller.getUsername();
                loggedUser = UserDAOMySQLImpl.getInstance().getByUsername(username);

                // Inicializamos listas de préstamos y reservas del usuario
                loadUserLoans();
                loadUserReservations();

                return true;
            } else {
                return false;
            }
        } catch (IOException | DAOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadUserLoans() {
        // Ejemplo de datos de prueba
        userLoans.clear();
        userLoans.addAll("Préstamo 1", "Préstamo 2", "Préstamo 3");
        // Aquí puedes reemplazar por datos reales desde la BD
    }

    private void loadUserReservations() {
        // Ejemplo de datos de prueba
        userReservations.clear();
        userReservations.addAll("Reserva A", "Reserva B");
        // Aquí puedes reemplazar por datos reales desde la BD
    }

    private void showUserLandingView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserLandingView.fxml"));
            BorderPane userPane = loader.load();

            Scene scene = new Scene(userPane);
            primaryStage.setScene(scene);

            UserLandingController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAdminLanding() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AdminLandingView.fxml"));
            BorderPane adminPane = loader.load();

            Scene scene = new Scene(adminPane);
            primaryStage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
