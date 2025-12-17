package it.unicas.project.template.address.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.util.List;

/**
 * Controller for the Notifications Modal view.
 */
public class NotificationsController {

    @FXML
    private ListView<String> notificationsListView;

    private Stage dialogStage;

    /**
     * Sets the stage of this dialog.
     * @param dialogStage The stage of the modal dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Called by MainApp to pass the list of notification messages.
     * @param notifications The list of formatted notification messages (overdue and holds).
     */
    public void setNotifications(List<String> notifications) {
        ObservableList<String> items = FXCollections.observableArrayList();

        if (notifications != null && !notifications.isEmpty()) {
            items.addAll(notifications);
        } else {
            items.add("You currently have no pending notifications.");
        }

        notificationsListView.setItems(items);
    }

    /**
     * Closes the modal dialog when the Close button is clicked.
     */
    @FXML
    private void handleClose(ActionEvent event) {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}