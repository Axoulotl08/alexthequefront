package com.axoulotl.alexthequefront;

import com.axoulotl.alexthequefront.entity.in.ConsoleClientDTO;
import com.axoulotl.alexthequefront.entity.out.ConsoleDTO;
import com.axoulotl.alexthequefront.service.ConsoleApiService;
import com.vaadin.flow.component.UI; // Importe UI
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification; // Importe Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
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
    private IntegerField zoneField = new IntegerField("Zone (1-3)");
    private DatePicker launchDateField = new DatePicker("Launch Date");
    private Button addButton = new Button("Add Console");

    public ConsoleView(ConsoleApiService consoleApiService) {
        this.consoleApiService = consoleApiService;
        addClassName("console-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(new H2("Consoles List"), grid, createAddFormLayout());

        // --- MISE À JOUR IMPORTANTE ICI ---
        // L'UI est disponible dans le constructeur de la vue car elle est appelée sur le thread UI
        UI ui = UI.getCurrent();
        if (ui != null) {
            // Utilise ui.access() pour le premier chargement aussi, par sécurité,
            // bien que dans le constructeur c'est souvent déjà le thread UI.
            // C'est une bonne pratique pour tout code qui pourrait être appelé de manière différée ou asynchrone.
            ui.access(this::updateList);
        } else {
            System.err.println("Erreur: UI non disponible au démarrage de ConsoleView.");
            // Gérer l'erreur, peut-être afficher une notification via un mécanisme de fallback
        }
    }

    private void configureGrid() {
        grid.addClassNames("console-grid");
        grid.setSizeFull();
        grid.setColumns("name", "manufacturer", "zone", "launchDate", "creationDate", "id");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void configureForm() {
        binder.bind(nameField, ConsoleDTO::getName, ConsoleDTO::setName);
        binder.bind(manufacturerField, ConsoleDTO::getManufacturer, ConsoleDTO::setManufacturer);
        binder.bind(zoneField, ConsoleDTO::getZone, ConsoleDTO::setZone);

        binder.bind(launchDateField,
                consoleDTO -> consoleDTO.getLaunchDate() != null ? consoleDTO.getLaunchDate().toLocalDate() : null,
                (consoleDTO, localDate) -> consoleDTO.setLaunchDate(localDate != null ? localDate.atStartOfDay() : null));

        // Le click listener est déjà sur le thread UI, donc UI.getCurrent() sera valide ici
        addButton.addClickListener(event -> addConsole());
    }

    private HorizontalLayout createAddFormLayout() {
        HorizontalLayout formLayout = new HorizontalLayout(nameField, manufacturerField, zoneField, launchDateField, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formLayout.setSpacing(true);
        return formLayout;
    }

    private void updateList() {
        // --- MISE À JOUR IMPORTANTE ICI ---
        // Capture l'UI ICI, avant l'appel asynchrone
        UI ui = UI.getCurrent();
        if (ui == null) {
            System.err.println("Erreur: UI est null dans updateList. Ne peut pas mettre à jour la grille.");
            return; // Sortir si l'UI n'est pas disponible
        }

        consoleApiService.getAllConsoles()
                .collectList()
                .subscribe(
                        // Succès : Le résultat arrive sur un thread de Reactor
                        consoles -> {
                            // Exécute la mise à jour de la grille sur le thread UI de Vaadin
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
                        // Erreur : Le résultat de l'erreur arrive sur un thread de Reactor
                        error -> {
                            // Exécute l'affichage de la notification d'erreur sur le thread UI de Vaadin
                            ui.access(() -> {
                                Notification.show("Erreur lors du chargement des consoles: " + error.getMessage(), 3000, Notification.Position.MIDDLE);
                                error.printStackTrace(); // Pour un débogage plus facile
                            });
                        }
                );
    }

    private void addConsole() {
        // --- MISE À JOUR IMPORTANTE ICI ---
        // Capture l'UI ICI, avant l'appel asynchrone
        UI ui = UI.getCurrent();
        if (ui == null) {
            System.err.println("Erreur: UI est null dans addConsole. Ne peut pas ajouter la console.");
            Notification.show("Erreur interne: UI non disponible.", 3000, Notification.Position.MIDDLE);
            return; // Sortir si l'UI n'est pas disponible
        }

        ConsoleDTO newConsole = new ConsoleDTO();
        try {
            binder.writeBean(newConsole);
        } catch (Exception e) {
            // Les erreurs de validation sont souvent affichées directement sur les champs du formulaire,
            // ou tu peux utiliser une notification ici si tu le souhaites.
            Notification.show("Erreur de validation: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            return;
        }

        consoleApiService.addConsole(newConsole)
                .subscribe(
                        // Succès : Le résultat arrive sur un thread de Reactor
                        savedConsole -> {
                            // !!! TOUTES les interactions UI DOIVENT être dans ui.access() !!!
                            ui.access(() -> {
                                Notification.show("Console ajoutée: " + savedConsole.getName(), 3000, Notification.Position.TOP_END);
                                updateList(); // Recharge toutes les consoles (qui contient déjà un ui.access())
                                binder.readBean(new ConsoleDTO()); // Réinitialise le formulaire avec une nouvelle instance vide
                                nameField.focus(); // Met le focus sur le premier champ pour une meilleure UX
                            });
                        },
                        // Erreur : Le résultat de l'erreur arrive sur un thread de Reactor
                        error -> {
                            // !!! TOUTES les interactions UI DOIVENT être dans ui.access() !!!
                            ui.access(() -> {
                                Notification.show("Erreur lors de l'ajout de la console: " + error.getMessage(), 5000, Notification.Position.MIDDLE);
                                error.printStackTrace(); // Pour un débogage plus facile
                            });
                        }
                );
    }
}