package com.axoulotl.alexthequefront;

import com.axoulotl.alexthequefront.dialog.GameDetailsDialog;
import com.axoulotl.alexthequefront.entity.enums.Status;
import com.axoulotl.alexthequefront.entity.in.ConsoleClientDTO;
import com.axoulotl.alexthequefront.entity.in.GameClientDTO;
import com.axoulotl.alexthequefront.entity.out.GameDTO;
import com.axoulotl.alexthequefront.entity.out.SearchDTO;
import com.axoulotl.alexthequefront.service.ConsoleApiService;
import com.axoulotl.alexthequefront.service.GameApiService;
import com.axoulotl.alexthequefront.service.converter.ConsoleDtoToIdConverter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.util.Arrays;
import java.util.List;

@SpringComponent
@UIScope
@PageTitle("Games")
@Route(value = "games", layout = MainLayout.class)
public class GameView extends VerticalLayout {

    private final GameApiService gameApiService;
    private final ConsoleApiService consoleApiService; // Pour la combobox des consoles
    private final Grid<GameClientDTO> grid = new Grid<>(GameClientDTO.class);
    private final Binder<GameDTO> binder = new BeanValidationBinder<>(GameDTO.class);
    private final Binder<SearchDTO> binderSearch = new BeanValidationBinder<>(SearchDTO.class);

    private List<ConsoleClientDTO> loadedConsoles;

    private GameDetailsDialog gameDetailsDialog;

    // Formulaire recherche
    private TextField nameFieldSearch = new TextField("Name");
    private DatePicker datePickerSeatch = new DatePicker();
    private ComboBox<ConsoleClientDTO> consoleComboBoxSearch = new ComboBox<>("Console");
    private ComboBox<Status> statusComboBoxSearch = new ComboBox<>("Status");
    private Button searchButton = new Button("Search");

    // Formulaire d'ajout de jeu
    private TextField nameField = new TextField("Game Name");
    private ComboBox<ConsoleClientDTO> consoleComboBox = new ComboBox<>("Console");
    private Checkbox inboxCheckbox = new Checkbox("In Box");
    private Button addButton = new Button("Add Game");

    // Element de pagination
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 0;
    private int totalGames = 0;

    private Button previousPageButton = new Button("Previous page", VaadinIcon.ARROW_CIRCLE_LEFT.create());
    private Button nextPageButton = new Button("Next page", VaadinIcon.ARROW_CIRCLE_RIGHT.create());
    private Span paginationInfoLabel = new Span();
    private ComboBox<Integer> nbItemPerPageCombo = new ComboBox<>("Nb item per pages");

    public GameView(GameApiService gameApiService, ConsoleApiService consoleApiService) {
        this.gameApiService = gameApiService;
        this.consoleApiService = consoleApiService;
        addClassName("game-view");
        setSizeFull();

        this.gameDetailsDialog = new GameDetailsDialog(gameApiService, consoleApiService);

        configureGrid();
        configureForm();
        configureSearchForm();
        configureNbItemCombox();
        configureConsoleComboBox(consoleComboBox);
        configureConsoleComboBox(consoleComboBoxSearch);

        loadConsolesIntoComboBox(consoleComboBox, consoleComboBoxSearch);

        add(new H2("Games List"), nbItemPerPageCombo, createSearchLayout(), grid, createPaginationLayout(), createAddFormLayout());

    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        updateList(currentPage, pageSize);
    }

    private void configureNbItemCombox(){
        List<Integer> nbItem = Arrays.asList(10, 20, 30, 40);
        nbItemPerPageCombo.setItems(nbItem);
        nbItemPerPageCombo.setValue(pageSize);

        nbItemPerPageCombo.addValueChangeListener(event -> {
            if(event.getValue() != null){
                pageSize = event.getValue();
                currentPage = 0;
                updateList(currentPage, pageSize);
            }
        });
    }

    private void configureGrid() {
        grid.addClassNames("game-grid");
        grid.setSizeFull();

        grid.removeAllColumns();

        // Colonne pour l'ID
        grid.addColumn(GameClientDTO::getId)
                .setHeader("Id")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne pour le nom du jeu
        grid.addColumn(GameClientDTO::getName)
                .setHeader("Name")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne pour le nom de la console
        grid.addColumn(game -> game.getConsole() != null ? game.getConsole().getName() : "N/A")
                .setHeader("Console Name")
                .setSortable(true)
                .setAutoWidth(true);

        // << CORRECTION ICI : Colonne "In Box" avec des icônes >>
        grid.addColumn(new ComponentRenderer<>(game -> {
                    // Crée une icône basée sur la valeur de 'inbox'
                    Icon icon;
                    if (Boolean.TRUE.equals(game.getInbox())) { // Utilise Boolean.TRUE.equals pour gérer les nulls
                        icon = VaadinIcon.CHECK_CIRCLE.create(); // Icône de coche
                        icon.setColor("green"); // Couleur verte
                    } else {
                        icon = VaadinIcon.CLOSE_CIRCLE.create(); // Icône de croix
                        icon.setColor("red"); // Couleur rouge
                    }
                    return icon;
                }))
                .setHeader("In Box")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(game -> {
                    String statusText = game.getStatus() != null ? game.getStatus().name().replace("_", " ") : "N/A"; // Convertit l'enum en texte lisible
                    String colorClass; // Classe CSS pour la couleur du badge

                    switch (game.getStatus()) {
                        case TO_START:
                            colorClass = "badge-blue"; // Ou "primary" si tu veux des couleurs lumo par défaut
                            break;
                        case IN_PROGRESS:
                            colorClass = "badge-success"; // Vert
                            break;
                        case DONE:
                            colorClass = "badge-success"; // Vert
                            break;
                        case STOPPED:
                            colorClass = "badge-error"; // Rouge
                            break;
                        case COMPLETE:
                            colorClass = "badge-success"; // Vert
                            break;
                        default:
                            colorClass = "badge-contrast"; // Couleur par défaut (gris)
                            break;
                    }

                    Span statusSpan = new Span(statusText); // Utilise un Span pour le badge
                    statusSpan.getElement().getThemeList().add("badge"); // Applique le style de badge Lumo par défaut
                    statusSpan.getElement().getThemeList().add(colorClass); // Ajoute ta classe de couleur personnalisée
                    statusSpan.getElement().getThemeList().add("small"); // Optionnel : pour un badge plus petit

                    return statusSpan;
        })).setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(game -> {
            Button detailsButton = new Button(new Icon(VaadinIcon.INFO_CIRCLE)); // Icône pour le détail
            detailsButton.addClickListener(e -> {
                // Ouvre la boîte de dialogue avec les détails du jeu
                // Important : Passe une référence à la GameView pour pouvoir rafraîchir la grille
                gameDetailsDialog.openDialog(game.getId(), v -> updateList(currentPage, pageSize));
            });
            return detailsButton;
        })).setHeader("Actions").setAutoWidth(true);
    }

    /**
     *
     * @return
     */
    private Component createSearchLayout() {
        HorizontalLayout searchLayout = new HorizontalLayout(nameFieldSearch, datePickerSeatch, consoleComboBoxSearch, statusComboBoxSearch, searchButton);
        searchLayout.setSpacing(true);
        searchLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        return searchLayout;
    }


    private void configureSearchForm() {
        binderSearch.forField(nameFieldSearch).bind(SearchDTO::getName, SearchDTO::setName);
        binderSearch.forField(datePickerSeatch).bind(SearchDTO::getStartedDate, SearchDTO::setStartedDate);
        statusComboBoxSearch.setLabel("Status");
        statusComboBoxSearch.setItemLabelGenerator(Status::name);
        statusComboBoxSearch.setItems(Status.values());

        searchButton.addClickListener(buttonClickEvent -> searchGame());
    }


    private void searchGame() {
    }

    private void configureForm() {
        binder.forField(nameField).asRequired().bind(GameDTO::getName, GameDTO::setName);
        binder.bind(inboxCheckbox, GameDTO::getInbox, GameDTO::setInbox);
        consoleComboBox.setItemLabelGenerator(ConsoleClientDTO::getName);

        addButton.addClickListener(event -> addGame());
    }

    private HorizontalLayout createAddFormLayout() {
        HorizontalLayout formLayout = new HorizontalLayout(nameField, consoleComboBox, inboxCheckbox, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formLayout.setSpacing(true);
        return formLayout;
    }

    private void loadConsolesIntoComboBox(ComboBox<ConsoleClientDTO> comboBox,
                                          ComboBox<ConsoleClientDTO> comboBoxSearch) {
        UI ui = UI.getCurrent();
        if (ui == null) {
            System.err.println("Error: UI is null when trying to load consoles. Cannot update the UI.");
            return;
        }
        consoleApiService.getAllConsoles()
                .collectList() // Collecte tous les Mono<ConsoleDTO> en List<ConsoleDTO>
                .subscribe(
                        consoles -> {
                            ui.access(() -> {
                                this.loadedConsoles = consoles;
                                comboBox.setItems(consoles); // Remplis la ComboBox
                                Notification.show("Liste des consoles chargée.", 1500, Notification.Position.BOTTOM_END);
                                binder.forField(comboBox)
                                        .withConverter(new ConsoleDtoToIdConverter(this.loadedConsoles)) // <-- IMPORTANT : PASSER LA LISTE
                                        .bind(GameDTO::getConsole, GameDTO::setConsole);
                                comboBoxSearch.setItems(consoles);
                                binderSearch.forField(comboBoxSearch)
                                        .withConverter(new ConsoleDtoToIdConverter(this.loadedConsoles))
                                        .bind(SearchDTO::getConsoleId, SearchDTO::setConsoleId);
                            });
                        },
                        // Erreur : Problème lors de la récupération des consoles
                        error -> {
                            ui.access(() -> {
                                Notification.show("Erreur lors du chargement des consoles: " + error.getMessage(), 5000, Notification.Position.MIDDLE);
                                error.printStackTrace();
                            });
                        }
                );
    }

    private void configureConsoleComboBox(ComboBox<ConsoleClientDTO> consolesCombo) {
        consolesCombo.setItemLabelGenerator(console -> {
            if (console == null) {
                return "";
            }
            return console.getName() + " (" + console.getZone() + ")";
        });

        consolesCombo.setPlaceholder("Select a console");

    }

    private HorizontalLayout createPaginationLayout(){

        previousPageButton.addClickListener(buttonClickEvent -> {
           if(currentPage > 0){
               currentPage--;
               updateList(currentPage, pageSize);
           }
            Notification.show("Button Value = " + currentPage, 1500, Notification.Position.MIDDLE);
        });

        nextPageButton.addClickListener(buttonClickEvent -> {
            if(currentPage < totalPages - 1){
                currentPage++;
                updateList(currentPage, pageSize);
            }
            Notification.show("Button Value = " + currentPage, 1500, Notification.Position.MIDDLE);
        });

        updatePaginationButtonStates();
        HorizontalLayout paginationLayout = new HorizontalLayout(previousPageButton, paginationInfoLabel, nextPageButton);
        paginationLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        paginationLayout.setSpacing(true);
        return paginationLayout;
    }

    private void updatePaginationButtonStates() {
        previousPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(totalPages > 0 && currentPage < totalPages - 1);
        paginationInfoLabel.setText(String.format("Page %d/%d (Total %d games)",
                currentPage + 1, totalPages, totalGames));

    }

    private void updateList(int page, int size) {
        UI ui = UI.getCurrent();
        if(ui == null){
            return;
        }
        gameApiService.getAllGames(page, size)
                // .publishOn(Schedulers.from(getUI().orElseThrow().getUIExecutor())) // <-- SUPPRIMÉE
                .subscribe(
                        paginatedGamesDTO -> {
                            ui.access(() -> {
                                grid.setItems(paginatedGamesDTO.getGames());
                                currentPage = paginatedGamesDTO.getCurrentPage();
                                totalPages = paginatedGamesDTO.getTotalPages();
                                totalGames = paginatedGamesDTO.getNbGames().intValue();
                                updatePaginationButtonStates();
                                Notification.show("Game loaded for page " + (currentPage + 1), 1500, Notification.Position.BOTTOM_END);
                            });
                        },
                        error ->  {
                            ui.access(() -> {
                               Notification.show("Error while fetching games " + error.getMessage());
                               error.printStackTrace();
                            });
                        }
                );
    }

    private void addGame() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            System.err.println("Erreur: UI est null dans addConsole. Ne peut pas ajouter la console.");
            Notification.show("Erreur interne: UI non disponible.", 3000, Notification.Position.MIDDLE);
            return;
        }
        GameDTO newGame = new GameDTO();
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
                            ui.access(() -> {
                                Notification.show("Game added: " + savedGame.getName(), 3000, Notification.Position.MIDDLE).open();
                                // grid.setItems(new ArrayList<>()); // Cette ligne n'est pas nécessaire si updateList() est appelé
                                currentPage = 0;
                                updateList(currentPage, pageSize);
                                binder.readBean(new GameDTO()); // Réinitialise le formulaire

                            });
                            },
                        error -> {
                            ui.access(() -> {
                                Notification.show("Error adding game: " + error.getMessage(), 3000, Notification.Position.MIDDLE).open();
                                error.printStackTrace();
                            });
                        }
                );
    }
}