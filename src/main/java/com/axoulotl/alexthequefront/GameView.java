package com.axoulotl.alexthequefront;

import com.axoulotl.alexthequefront.entity.in.ConsoleClientDTO;
import com.axoulotl.alexthequefront.entity.in.GameClientDTO;
import com.axoulotl.alexthequefront.service.ConsoleApiService;
import com.axoulotl.alexthequefront.service.GameApiService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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
@PageTitle("Games")
@Route(value = "games", layout = MainLayout.class)
public class GameView extends VerticalLayout {

    private final GameApiService gameApiService;
    private final ConsoleApiService consoleApiService; // Pour la combobox des consoles
    private final Grid<GameClientDTO> grid = new Grid<>(GameClientDTO.class);
    private final Binder<GameClientDTO> binder = new BeanValidationBinder<>(GameClientDTO.class);

    // Formulaire d'ajout de jeu
    private TextField nameField = new TextField("Game Name");
    private ComboBox<ConsoleClientDTO> consoleComboBox = new ComboBox<>("Console");
    private Checkbox inboxCheckbox = new Checkbox("In Box");
    private Button addButton = new Button("Add Game");

    public GameView(GameApiService gameApiService, ConsoleApiService consoleApiService) {
        this.gameApiService = gameApiService;
        this.consoleApiService = consoleApiService;
        addClassName("game-view");
        setSizeFull();

        configureGrid();
        configureForm();
        loadConsolesForComboBox(); // Charge les consoles pour la combobox

        add(new H2("Games List"), grid, createAddFormLayout());
        updateList(); // Charge les données initiales
    }

    private void configureGrid() {
        grid.addClassNames("game-grid");
        grid.setSizeFull();
        // Afficher le nom de la console et non l'objet complet
        grid.addColumn(game -> game.getConsole() != null ? game.getConsole().getName() : "N/A")
                .setHeader("Console Name").setSortable(true);
        grid.setColumns("name", "inbox", "creationDate", "id"); // Ajoutez d'autres colonnes si désiré
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void configureForm() {
        binder.bind(nameField, GameClientDTO::getName, GameClientDTO::setName);
        binder.bind(inboxCheckbox, GameClientDTO::getInbox, GameClientDTO::setInbox);
        binder.bind(consoleComboBox, GameClientDTO::getConsole, GameClientDTO::setConsole);

        // Configure la ComboBox pour afficher le nom de la console
        consoleComboBox.setItemLabelGenerator(ConsoleClientDTO::getName);

        addButton.addClickListener(event -> addGame());
    }

    private HorizontalLayout createAddFormLayout() {
        HorizontalLayout formLayout = new HorizontalLayout(nameField, consoleComboBox, inboxCheckbox, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formLayout.setSpacing(true);
        return formLayout;
    }

    private void loadConsolesForComboBox() {
        consoleApiService.getAllConsoles()
                .collectList() // Collecte tous les éléments dans une liste
                // .publishOn(Schedulers.from(getUI().orElseThrow().getUIExecutor())) // <-- SUPPRIMÉE
                .subscribe(
                        consoles -> {
                            consoleComboBox.setItems(consoles);
                        },
                        error -> Notification.show("Error loading consoles for combo box: " + error.getMessage(), 3000, Notification.Position.MIDDLE).open()
                );
    }

    private void updateList() {
        gameApiService.getAllGames()
                .collectList() // <-- NOUVEAU : Collecte tous les éléments du Flux dans une List
                // .publishOn(Schedulers.from(getUI().orElseThrow().getUIExecutor())) // <-- SUPPRIMÉE
                .subscribe(
                        games -> grid.setItems(games), // <-- CORRECTION : Utilise setItems avec la List
                        error -> Notification.show("Error fetching games: " + error.getMessage(), 3000, Notification.Position.MIDDLE).open()
                );
    }

    private void addGame() {
        GameClientDTO newGame = new GameClientDTO();
        try {
            binder.writeBean(newGame);
        } catch (Exception e) {
            Notification.show("Validation error: " + e.getMessage(), 3000, Notification.Position.MIDDLE).open();
            return;
        }

        gameApiService.addGame(newGame)
                // .publishOn(Schedulers.from(getUI().orElseThrow().getUIExecutor())) // <-- SUPPRIMÉE
                .subscribe(
                        savedGame -> {
                            Notification.show("Game added: " + savedGame.getName(), 3000, Notification.Position.MIDDLE).open();
                            // grid.setItems(new ArrayList<>()); // Cette ligne n'est pas nécessaire si updateList() est appelé
                            updateList(); // Recharge toutes les games
                            binder.readBean(null); // Réinitialise le formulaire
                        },
                        error -> Notification.show("Error adding game: " + error.getMessage(), 3000, Notification.Position.MIDDLE).open()
                );
    }
}