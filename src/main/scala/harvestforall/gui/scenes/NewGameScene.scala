package harvestforall.gui.scenes

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import scalafx.event.ActionEvent
import harvestforall.gui.managers.SceneManager
import harvestforall.gui.utils.FontManager

/** Scene for starting a new game */
class NewGameScene(sceneManager: SceneManager) extends Scene(800, 600) {

  private var playerNameField: TextField = _

  initializeUI()

  private def initializeUI(): Unit = {
    val mainVBox = new VBox {
      spacing = 30
      padding = Insets(50)
      alignment = Pos.Center
    }

    val titleLabel = new Label("Start New Game") {
      font = FontManager.titleFont
      textFill = Color.DarkGreen
    }

    val nameLabel = new Label("Enter Your Farmer Name:") {
      font = FontManager.headerFont
      textFill = Color.DarkBlue
    }

    playerNameField = new TextField {
      text = "Farmer"
      prefWidth = 300
      prefHeight = 40
    }

    val startButton = new Button("Start Farming!") {
      font = FontManager.buttonFont
      prefWidth = 180
      prefHeight = 50
      style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
      onAction = (_: ActionEvent) => handleStartGame()
    }

    val backButton = new Button("Back to Menu") {
      font = FontManager.buttonFont
      prefWidth = 150
      prefHeight = 50
      style = "-fx-background-color: #f44336; -fx-text-fill: white;"
      onAction = (_: ActionEvent) => sceneManager.switchToMainMenu()
    }

    val buttonsHBox = new HBox {
      spacing = 20
      alignment = Pos.Center
      children = List(startButton, backButton)
    }

    mainVBox.children =
      List(titleLabel, nameLabel, playerNameField, buttonsHBox)

    fill = Color.LightGreen
    root = mainVBox
  }

  private def handleStartGame(): Unit = {
    val playerName = playerNameField.text().trim

    if (playerName.nonEmpty) {
      println(s"[NewGameScene] Starting new game - Player: $playerName")
      sceneManager.switchToFarmGameScene()
    }
  }
}
