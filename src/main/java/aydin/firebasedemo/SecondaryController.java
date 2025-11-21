package aydin.firebasedemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import static aydin.firebasedemo.DemoApp.fauth;
import static aydin.firebasedemo.DemoApp.fstore;

public class SecondaryController {

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private void registerButtonClicked() {
        registerUser();
    }

    @FXML
    private void loginButtonClicked() {
        if (loginUser()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("User logged in successfully");
            alert.showAndWait();
            try {
                DemoApp.setRoot("primary");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Invalid.");
            alert.showAndWait();
        }
   }

    private boolean loginUser() {
        try {
            QuerySnapshot result = fstore.collection("Users")
                    .whereEqualTo("email", usernameTextField.getText())
                    .get()
                    .get();

            if (result.isEmpty()) {
                return false;
            }

            DocumentSnapshot doc = result.getDocuments().get(0);
            String storedPw = doc.getString("password");
            String uid = doc.getId();

            return passwordTextField.getText().equals(storedPw);


        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }


    private boolean registerUser() {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(usernameTextField.getText())
                .setEmailVerified(false)
                .setPassword(passwordTextField.getText())
                .setDisabled(false);

        UserRecord userRecord;
        try {
            userRecord = fauth.createUser(request);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("User registered successfully");
            alert.showAndWait();
            DemoApp.setRoot("primary");
            DocumentReference docRef = DemoApp.fstore.collection("Users").document(UUID.randomUUID().toString());

            Map<String, Object> data = new HashMap<>();
            data.put("email", usernameTextField.getText());
            data.put("password", passwordTextField.getText());
            //asynchronously write data
            ApiFuture<WriteResult> result = docRef.set(data);
            return true;

        } catch (FirebaseAuthException ex) {
            // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error creating a new user in the firebase");
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    private void switchToPrimary() throws IOException {
        DemoApp.setRoot("primary");
    }
}
