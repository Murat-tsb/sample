package com.example.sample;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Route("")
public class UserTableView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> userGrid;

    @Autowired
    public UserTableView(UserService userService) {
        this.userService = userService;
        this.userGrid = new Grid<>(User.class);


        userGrid.addColumn(User::getName).setHeader("Name").setSortable(true);
        userGrid.addColumn(User::getSurname).setHeader("Surname").setSortable(true);
        userGrid.addColumn(User::getEmail).setHeader("Email").setSortable(true);
        userGrid.addColumn(User::getPhoneNumber).setHeader("Phone Number").setSortable(true);
        userGrid.addColumn(User::getHomeAddress).setHeader("Home Address").setSortable(true);


        userGrid.addComponentColumn(this::createDeleteButton).setHeader("Actions");

        userGrid.addItemClickListener(event -> {
            User selectedUser = event.getItem();
            openEditUserDialog(selectedUser);
        });


        refreshGrid();

        Button createButton = new Button("Create New User", e -> UI.getCurrent().navigate("edit-users"));
        Button exportExcelButton = new Button("Download Excel", e -> downloadExcelFile());

        HorizontalLayout buttonsLayout = new HorizontalLayout(createButton, exportExcelButton);
        add(buttonsLayout, userGrid);
    }

    private void refreshGrid() {
        userGrid.setItems(userService.findAll());
    }

    private Button createDeleteButton(User user) {
        Button deleteButton = new Button("Delete", click -> {
            userService.delete(user);
            Notification.show("User deleted: " + user.getName());
            refreshGrid(); // Refresh the grid after deletion
        });
        deleteButton.getStyle().set("color", "red");
        return deleteButton;
    }

    private void downloadExcelFile() {
        try {
            List<User> users = userService.findAll();
            byte[] excelContent = UserExcel.createExcelFile(users);

            StreamResource resource = new StreamResource("users.xlsx", () -> new ByteArrayInputStream(excelContent));
            Anchor downloadLink = new Anchor(resource, "Download Users Excel File");
            downloadLink.getElement().setAttribute("download", true);


            add(downloadLink);
            Notification.show("Click the link to download the file.");
        } catch (IOException e) {
            Notification.show("Error generating Excel file: " + e.getMessage());
        }
    }

    private void openEditUserDialog(User selectedUser) {
        if (selectedUser != null) {
            String userId = selectedUser.getId().toHexString(); // Convert ObjectId to String
            UI.getCurrent().navigate("edit-users", QueryParameters.simple(Map.of("userId", userId)));
        } else {
            Notification.show("No user selected for editing.");
        }
    }
}
