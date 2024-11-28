package com.example.sample;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.dialog.Dialog;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route("edit-users")
public class UserEditView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final TextField nameField;
    private final TextField surnameField;
    private final TextField emailField;
    private final TextField phoneNumberField;
    private final TextField homeAddressField;
    private final Dialog userDialog;
    private ObjectId currentUserId;

    @Autowired
    public UserEditView(UserService userService) {
        this.userService = userService;

        nameField = new TextField("Name");
        surnameField = new TextField("Surname");
        emailField = new TextField("Email");
        phoneNumberField = new TextField("Phone Number");
        homeAddressField = new TextField("Home Address");

        Button saveButton = new Button("Save", e -> saveUser());
        Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate(""));

        FormLayout formLayout = new FormLayout(nameField, surnameField, emailField, phoneNumberField, homeAddressField, saveButton, cancelButton);
        userDialog = new Dialog(formLayout);
        add(userDialog);
        userDialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> queryParameters = event.getLocation().getQueryParameters().getParameters();
        String userId = queryParameters.getOrDefault("userId", List.of()).stream().findFirst().orElse(null);

        if (userId != null) {
            try {
                currentUserId = new ObjectId(userId);
                loadUserData(currentUserId);
            } catch (IllegalArgumentException e) {
                Notification.show("Invalid User ID format.");
            }
        } else {
            Notification.show("No User ID provided. Creating a new user.");
            clearForm();
        }
    }

    private void loadUserData(ObjectId userId) {
        userService.getById(userId).ifPresentOrElse(user -> {
            nameField.setValue(user.getName() != null ? user.getName() : "");
            surnameField.setValue(user.getSurname() != null ? user.getSurname() : "");
            emailField.setValue(user.getEmail() != null ? user.getEmail() : "");
            phoneNumberField.setValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            homeAddressField.setValue(user.getHomeAddress() != null ? user.getHomeAddress() : "");
        }, () -> Notification.show("User not found."));
    }

    private void saveUser() {
        if (nameField.isEmpty() || surnameField.isEmpty() || emailField.isEmpty()) {
            Notification.show("Please fill in all required fields.");
            return;
        }

        User user = new User();
        user.setName(nameField.getValue());
        user.setSurname(surnameField.getValue());
        user.setEmail(emailField.getValue());
        user.setPhoneNumber(phoneNumberField.getValue());
        user.setHomeAddress(homeAddressField.getValue());

        if (currentUserId != null) {
            user.setId(currentUserId);
            userService.update(user);
        } else {
            userService.create(user);
        }

        Notification.show("User saved successfully.");
        UI.getCurrent().navigate("");
    }

    private void clearForm() {
        nameField.clear();
        surnameField.clear();
        emailField.clear();
        phoneNumberField.clear();
        homeAddressField.clear();
        currentUserId = null;
    }
}
