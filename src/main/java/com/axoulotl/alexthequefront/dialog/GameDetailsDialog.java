package com.axoulotl.alexthequefront.dialog;

import com.axoulotl.alexthequefront.entity.in.GameClientDTO;
import com.axoulotl.alexthequefront.entity.out.GameUpdateDTO;
import com.axoulotl.alexthequefront.service.ConsoleApiService;
import com.axoulotl.alexthequefront.service.GameApiService;
import com.axoulotl.alexthequefront.service.converter.GameTimeConverter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import java.util.function.Consumer;


public class GameDetailsDialog extends Dialog {

    private final GameApiService gameApiService;
    private Integer gameId; // Pour stocker l'ID du jeu en cours
    private GameClientDTO currentGame; // Le DTO complet du jeu

    // Champs du formulaire
    private Checkbox inboxCheckbox = new Checkbox("In Box");
    private Div inboxDisplay = new Div();

    private Span nameField = new Span("Name");
    private Span consoleNameField = new Span("Console");

    private DatePicker startDateField = new DatePicker("Start Date");
    private DatePicker endDateField = new DatePicker("Start Date");
    private TextField gameTime = new TextField("Game time (HH:mm)");

    // Binder pour lier les champs du formulaire au GameDTO (pour la mise à jour)
    private Binder<GameUpdateDTO> binder = new BeanValidationBinder<>(GameUpdateDTO.class);

    // Callback pour rafraîchir la grille parent après une modification
    private Consumer<Void> refreshGridCallback;

    private ProgressBar progressBar = new ProgressBar();

    public GameDetailsDialog(GameApiService gameApiService, ConsoleApiService consoleApiService) {
        this.gameApiService = gameApiService;
        setHeaderTitle("Game Details"); // Titre de la boîte de dialogue
        setWidth("500px"); // Largeur de la boîte de dialogue

        // Configuration des champs
        inboxCheckbox.setValue(false); // Valeur par défaut

        binder.forField(startDateField)
                .asRequired("Game name is required")
                .bind(GameUpdateDTO::getStartDate, GameUpdateDTO::setStartDate);

        binder.forField(endDateField)
                .bind(GameUpdateDTO::getEndDate, GameUpdateDTO::setEndDate);

        binder.forField(gameTime)
                .withNullRepresentation("")
                .withConverter(new GameTimeConverter())
                .bind(GameUpdateDTO::getGameTime, GameUpdateDTO::setGameTime);

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false); // Masquée par défaut



        FormLayout formLayout = new FormLayout();
        formLayout.add(startDateField, endDateField, gameTime);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Ajoute la ProgressBar au layout
        VerticalLayout contentLayout = new VerticalLayout(progressBar, formLayout); // Ajoute la progress bar

        Button saveButton = new Button("Save", e -> saveGame());
        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        add(createFieldLayout(),contentLayout, formLayout, buttonLayout);
    }

    private VerticalLayout createFieldLayout(){
        HorizontalLayout nameLayout = new HorizontalLayout(new Span("Name : "), nameField);
        HorizontalLayout consoleLayout = new HorizontalLayout(new Span("Console :"), consoleNameField);
        HorizontalLayout inboxLayout = new HorizontalLayout(new Span("In Box :"), inboxDisplay);
        VerticalLayout gameInfo = new VerticalLayout(nameLayout, consoleLayout, inboxLayout);
        gameInfo.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.AUTO);
        gameInfo.setSpacing(true);
        return gameInfo;
    }

    /**
     * Ouvre la boîte de dialogue et charge les détails du jeu.
     * @param gameId L'ID du jeu à afficher/modifier.
     * @param refreshGridCallback Un callback à exécuter après une modification réussie pour rafraîchir la grille.
     */
    public void openDialog(Integer gameId, Consumer<Void> refreshGridCallback) {
        this.gameId = gameId;
        this.refreshGridCallback = refreshGridCallback;

        resetForm();
        progressBar.setVisible(true);

        // Réinitialise le binder et les champs avant de charger les nouvelles données
        binder.setBean(new GameUpdateDTO()); // Vide le binder
        startDateField.clear();
        endDateField.clear();
        gameTime.clear();

        UI ui = UI.getCurrent();

        // Charge les détails du jeu depuis l'API
        gameApiService.getGame(gameId)
                .subscribe(
                        gameClientDTO -> {
                            ui.access(() -> {

                                this.currentGame = gameClientDTO; // Stocke le DTO complet pour récupérer l'objet Console et l'heure
                                // Met à jour les champs du formulaire avec les données du DTO
                                nameField.setText(gameClientDTO.getName() != null ? gameClientDTO.getName() : "");
                                inboxCheckbox.setValue(Boolean.TRUE.equals(gameClientDTO.getInbox()));
                                updateInboxDisplay(gameClientDTO.getInbox());
                                // Sélectionne la console dans la ComboBox
                                if (gameClientDTO.getConsole() != null) {
                                    consoleNameField.setText(gameClientDTO.getConsole().getName());
                                }
                                if(gameClientDTO.getStartDate() != null){
                                    startDateField.setValue(gameClientDTO.getStartDate());
                                }if(gameClientDTO.getEndDate() != null){
                                    startDateField.setValue(gameClientDTO.getEndDate());
                                }
                                if(gameClientDTO.getGameTime() != null){
                                    gameTime.setValue(new GameTimeConverter().convertToPresentation(gameClientDTO.getGameTime(), null));
                                }

                                // Crée un GameDTO pour le binder avec les données existantes pour la modification
                                GameUpdateDTO gameToEdit = new GameUpdateDTO();
                                gameToEdit.setGameTime(gameClientDTO.getGameTime());
                                gameToEdit.setEndDate(gameClientDTO.getEndDate());
                                gameToEdit.setStartDate(gameClientDTO.getStartDate());
                                binder.setBean(gameToEdit); // Lie le DTO pour la modification

                                if (this.getParent().isEmpty()) {
                                    ui.add(this);
                                }

                                this.open(); // Ouvre la boîte de dialogue
                                progressBar.setVisible(false);
                            });
                        },
                        error -> {
                            ui.access(() -> {
                                Notification.show("Error loading game details: " + error.getMessage(), 3000, Notification.Position.MIDDLE);
                                progressBar.setVisible(false);
                                close();
                            });
                        }
                );
    }

    private void saveGame() {
        UI ui = UI.getCurrent();
        if(ui == null){
            System.err.println("Error: UI is not available when saving the game.");
            return;
        }
        if (binder.isValid()) {
            GameUpdateDTO gameToUpdate = binder.getBean();
            // L'ID est déjà dans gameToUpdate.getId() s'il a été mis par openDialog
            // ou tu peux le passer comme ça : gameToUpdate.setId(gameId);

            gameApiService.updateDate(gameToUpdate, gameId)
                    .subscribe(
                            updatedGame -> {
                                ui.access(() -> {
                                    Notification.show("Game saved successfully!", 3000, Notification.Position.MIDDLE);
                                    close(); // Ferme la boîte de dialogue
                                    if (refreshGridCallback != null) {
                                        refreshGridCallback.accept(null); // Rafraîchit la grille parent
                                    }
                                });
                            },
                            error -> {
                                ui.access(() -> {
                                    Notification.show("Error saving game: " + error.getMessage(), 3000, Notification.Position.MIDDLE);
                                });
                            }
                    );
        } else {
            Notification.show("Please correct the errors in the form.", 3000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Met à jour le composant d'affichage de l'état "In Box" avec une icône.
     * @param inbox - la valeur du champ 'inbox' du jeu.
     */
    private void updateInboxDisplay(Boolean inbox) {
        Icon icon;
        if (Boolean.TRUE.equals(inbox)) {
            icon = VaadinIcon.CHECK_CIRCLE.create();
            icon.setColor("green");
            inboxDisplay.setText("Oui"); // Texte pour le lecteur d'écran
        } else {
            icon = VaadinIcon.CLOSE_CIRCLE.create();
            icon.setColor("red");
            inboxDisplay.setText("Non");
        }
        inboxDisplay.removeAll();
        inboxDisplay.add(icon);
    }

    private void resetForm() {
        // Méthode pour vider les champs du formulaire et réinitialiser le binder
        startDateField.clear();
        endDateField.clear();
        gameTime.clear();
        // ... tous les autres champs ...
        binder.readBean(null);
    }
}