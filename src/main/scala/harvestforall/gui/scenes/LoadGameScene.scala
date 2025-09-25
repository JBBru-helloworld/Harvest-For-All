package harvestforall.gui.scenes

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import scalafx.event.ActionEvent
import harvestforall.systems.SaveManager
import harvestforall.core.GameSaveData
import harvestforall.gui.managers.SceneManager
import harvestforall.gui.utils.FontManager

/** Scene for loading saved games Displays list of available saves with metadata
  */
class LoadGameScene(sceneManager: SceneManager) extends Scene(800, 600) {

  // UI Components
  private val saveManager = SaveManager.getInstance
  private var availableSaves: List[GameSaveData] = List.empty

  // Create UI
  initializeUI()

  private def initializeUI(): Unit = {
    // Main container
    val mainVBox = new VBox {
      spacing = 20
      padding = Insets(30)
      alignment = Pos.Center
    }

    // Title
    val titleLabel = new Label("Load Game") {
      font = FontManager.titleFont
      textFill = Color.DarkBlue
    }

    // Refresh and load available saves
    refreshSavesList()

    // Saves list view
    val savesListView = new ListView[GameSaveData] {
      prefWidth = 600
      prefHeight = 350
      cellFactory = (_: ListView[GameSaveData]) => new SaveGameListCell()
    }

    // Update list view with saves
    savesListView.items =
      scalafx.collections.ObservableBuffer(availableSaves: _*)

    // Buttons
    val buttonsHBox = new HBox {
      spacing = 15
      alignment = Pos.Center
    }

    val loadButton = new Button("Load Game") {
      font = FontManager.buttonFont
      prefWidth = 180
      prefHeight = 40
      style =
        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5;"
      onAction = (_: ActionEvent) =>
        handleLoadGame(savesListView.selectionModel().selectedItem())
    }

    val refreshButton = new Button("Refresh") {
      font = FontManager.buttonFont
      prefWidth = 120
      prefHeight = 40
      style =
        "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-border-radius: 5;"
      onAction = (_: ActionEvent) => {
        refreshSavesList()
        savesListView.items =
          scalafx.collections.ObservableBuffer(availableSaves: _*)
      }
    }

    val backButton = new Button("Back to Menu") {
      font = FontManager.buttonFont
      prefWidth = 150
      prefHeight = 40
      style =
        "-fx-background-color: #f44336; -fx-text-fill: white; -fx-border-radius: 5;"
      onAction = (_: ActionEvent) => sceneManager.switchToMainMenu()
    }

    val deleteButton = new Button("Delete Save") {
      font = FontManager.buttonFont
      prefWidth = 130
      prefHeight = 40
      style =
        "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-border-radius: 5;"
      onAction = (_: ActionEvent) =>
        handleDeleteSave(savesListView.selectionModel().selectedItem())
    }

    buttonsHBox.children =
      List(loadButton, refreshButton, deleteButton, backButton)

    // Info label
    val infoLabel = new Label(s"Found ${availableSaves.length} saved games") {
      font = FontManager.labelFont
      textFill = Color.Gray
    }

    // Add all components to main container
    mainVBox.children = List(titleLabel, savesListView, infoLabel, buttonsHBox)

    // Set background
    fill = Color.LightBlue
    root = mainVBox
  }

  /** Refresh the list of available saves */
  private def refreshSavesList(): Unit = {
    availableSaves = saveManager.getAvailableSaves
    println(s"[LoadGameScene] Found ${availableSaves.length} save files")
  }

  /** Handle loading a selected game */
  private def handleLoadGame(selectedSave: GameSaveData): Unit = {
    if (selectedSave != null) {
      val confirmDialog = new Alert(Alert.AlertType.Confirmation) {
        title = "Load Game"
        headerText = s"Load '${selectedSave.saveName}'?"
        contentText =
          s"This will start the game from your saved progress on ${selectedSave.getFormattedDate}."
      }

      confirmDialog.showAndWait() match {
        case Some(ButtonType.OK) =>
          println(s"[LoadGameScene] Loading game: ${selectedSave.saveName}")
          // Load the game with state restoration
          sceneManager.loadGameWithRestore(selectedSave)

        case _ =>
          println("[LoadGameScene] Load cancelled by user")
      }
    } else {
      val alert = new Alert(Alert.AlertType.Warning) {
        title = "No Selection"
        headerText = "Please select a save file"
        contentText = "Choose a saved game from the list to load."
      }
      alert.showAndWait()
    }
  }

  /** Handle deleting a selected save */
  private def handleDeleteSave(selectedSave: GameSaveData): Unit = {
    if (selectedSave != null) {
      val confirmDialog = new Alert(Alert.AlertType.Confirmation) {
        title = "Delete Save"
        headerText = s"Delete '${selectedSave.saveName}'?"
        contentText =
          "This action cannot be undone. Are you sure you want to delete this save file?"
      }

      confirmDialog.showAndWait() match {
        case Some(ButtonType.OK) =>
          saveManager.deleteSave(selectedSave.getFileName) match {
            case scala.util.Success(_) =>
              println(s"[LoadGameScene] Deleted save: ${selectedSave.saveName}")
              refreshSavesList()
              // Refresh the UI
              initializeUI()

            case scala.util.Failure(ex) =>
              val alert = new Alert(Alert.AlertType.Error) {
                title = "Delete Failed"
                headerText = "Could not delete save file"
                contentText = s"Error: ${ex.getMessage}"
              }
              alert.showAndWait()
          }

        case _ =>
          println("[LoadGameScene] Delete cancelled by user")
      }
    } else {
      val alert = new Alert(Alert.AlertType.Warning) {
        title = "No Selection"
        headerText = "Please select a save file"
        contentText = "Choose a saved game from the list to delete."
      }
      alert.showAndWait()
    }
  }
}

/** Custom list cell for displaying save game information */
class SaveGameListCell extends ListCell[GameSaveData] {
  item.onChange { (_, _, newItem) =>
    if (newItem != null) {
      val content = new VBox {
        spacing = 5
        padding = Insets(10)
      }

      val nameLabel = new Label(newItem.saveName) {
        font = FontManager.subHeaderFont
        style = "-fx-font-weight: bold;"
      }

      val dateLabel = new Label(s"Saved: ${newItem.getFormattedDate}") {
        font = FontManager.smallFont
        textFill = Color.Gray
      }

      val statsLabel = new Label(
        f"Currency: $$${newItem.gameStats.currency}%.2f | " +
          s"Sustainability: ${newItem.gameStats.sustainabilityRating.toInt}% | " +
          s"Version: ${newItem.gameVersion}"
      ) {
        font = FontManager.smallFont
        textFill = Color.DarkGray
      }

      content.children = List(nameLabel, dateLabel, statsLabel)
      graphic = content
    } else {
      graphic = null
    }
  }
}
