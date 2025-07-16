package com.axoulotl.alexthequefront;

import com.axoulotl.alexthequefront.entity.enums.Zone;
import com.axoulotl.alexthequefront.entity.in.ConsoleClientDTO;
import com.axoulotl.alexthequefront.entity.out.ConsoleDTO;
import com.axoulotl.alexthequefront.service.ConsoleApiService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
@PageTitle("Consoles")
@Route(value = "consoles", layout = MainLayout.class)
public class ConsoleView extends VerticalLayout {

    private final ConsoleApiService consoleApiService;
    private final Grid<ConsoleClientDTO> grid = new Grid<>(ConsoleClientDTO.class);
    private final Binder<ConsoleDTO> binder = new BeanValidationBinder<>(ConsoleDTO.class);

    // Formulaire d'ajout
    private TextField nameField = new TextField("Name");
    private TextField manufacturerField = new TextField("Manufacturer");
    private ComboBox<Zone> zoneField = new ComboBox<Zone>("Zone");
    private DatePicker launchDateField = new DatePicker("Launch Date");
    private Button addButton = new Button("Add Console");

    public ConsoleView(ConsoleApiService consoleApiService) {
        this.consoleApiService = consoleApiService;
        addClassName("console-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(new H2("Consoles List"), grid, createAddFormLayout());


        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(this::updateList);
        } else {
            System.err.println("Erreur: UI non disponible au démarrage de ConsoleView.");
        }
    }

    private void configureGrid() {
        grid.addClassNames("console-grid");
        grid.setSizeFull();
        grid.setColumns("name", "manufacturer", "zone", "launchDate", "creationDate", "id");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void configureForm() {

        zoneField.setItems(Zone.values());
        zoneField.setItemLabelGenerator(Zone::name);

        binder.bind(nameField, ConsoleDTO::getName, ConsoleDTO::setName);
        binder.bind(manufacturerField, ConsoleDTO::getManufacturer, ConsoleDTO::setManufacturer);
        binder.bind(zoneField,
                consoleDTO -> Zone.getZoneFromInt(consoleDTO.getZone()),
                (consoleDTO, zone) -> consoleDTO.setZone(zone != null ? zone.getZoneValue() : null));

        binder.bind(launchDateField,
                consoleDTO -> consoleDTO.getLaunchDate() != null ? consoleDTO.getLaunchDate().toLocalDate() : null,
                (consoleDTO, localDate) -> consoleDTO.setLaunchDate(localDate != null ? localDate.atStartOfDay() : null));

        addButton.addClickListener(event -> addConsole());
    }

    private HorizontalLayout createAddFormLayout() {
        HorizontalLayout formLayout = new HorizontalLayout(nameField, manufacturerField, zoneField, launchDateField, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formLayout.setSpacing(true);
        return formLayout;
    }

    private void updateList() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            System.err.println("Erreur: UI est null dans updateList. Ne peut pas mettre à jour la grille.");
            return;
        }

        consoleApiService.getAllConsoles()
                .collectList()
                .subscribe(
                        // Succès : Le résultat arrive sur un thread de Reactor
                        consoles -> {
                            ui.access(() -> {
                                System.out.println("Consoles reçues pour la grille: " + consoles);
                                if (consoles != null) {
                                    System.out.println("Nombre de consoles: " + consoles.size());
                                    consoles.forEach(c -> System.out.println(" - " + c.getName() + " (" + c.getId() + ")"));
                                }
                                grid.setItems(consoles);
                                Notification.show("Consoles chargées. Nombre: " + consoles.size(), 1500, Notification.Position.BOTTOM_END);
                            });
                        },
                        error -> {
                            ui.access(() -> {
                                Notification.show("Erreur lors du chargement des consoles: " + error.getMessage(), 3000, Notification.Position.MIDDLE);
                                error.printStackTrace(); // Pour un débogage plus facile
                            });
                        }
                );
    }

    private void addConsole() {

        UI ui = UI.getCurrent();
        if (ui == null) {
            System.err.println("Erreur: UI est null dans addConsole. Ne peut pas ajouter la console.");
            Notification.show("Erreur interne: UI non disponible.", 3000, Notification.Position.MIDDLE);
        }

        ConsoleDTO newConsole = new ConsoleDTO();
        try {
            binder.writeBean(newConsole);
        } catch (Exception e) {
            Notification.show("Erreur de validation: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            return;
        }

        consoleApiService.addConsole(newConsole)
                .subscribe(
                        savedConsole -> {
                            ui.access(() -> {
                                Notification.show("Console ajoutée: " + savedConsole.getName(), 3000, Notification.Position.TOP_END);
                                updateList();
                                binder.readBean(new ConsoleDTO());
                                nameField.focus();
                            });
                        },
                        error -> {
                            ui.access(() -> {
                                Notification.show("Erreur lors de l'ajout de la console: " + error.getMessage(), 5000, Notification.Position.MIDDLE);
                                error.printStackTrace();
                            });
                        }
                );
    }
}