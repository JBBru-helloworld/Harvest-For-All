package harvestforall.gui.scenes

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{
  VBox,
  HBox,
  Background,
  BackgroundFill,
  CornerRadii,
  Border,
  BorderStroke,
  BorderWidths,
  BorderStrokeStyle
}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.{Font, FontWeight}
import scalafx.scene.paint.{Color, LinearGradient, CycleMethod, Stop}
import scalafx.scene.effect.{DropShadow, BlurType}
import scalafx.scene.input.{KeyEvent, KeyCode}
import scalafx.Includes._
import scalafx.event.ActionEvent
import harvestforall.gui.managers.SceneManager
import harvestforall.core.GameState
import harvestforall.gui.utils.FontManager
import harvestforall.gui.scenes.FarmGameSceneModular

/** Main menu scene for the application
  *
  * Provides navigation to different game modes and controls
  */
class MainMenuScene(sceneManager: SceneManager, gameState: GameState):

  private val SCENE_WIDTH = 1200
  private val SCENE_HEIGHT = 800

  /** Create the main menu scene
    */
  def getScene: Scene =
    val scene = new Scene(SCENE_WIDTH, SCENE_HEIGHT):
      fill = Color.web("#1B4F20") // Darker forest green
      root = createMainMenuLayout()

      // Add key event handling for fullscreen functionality
      onKeyPressed = (event: KeyEvent) => handleKeyPress(event)

    scene

  /** Handle key press events for fullscreen functionality
    */
  private def handleKeyPress(event: KeyEvent): Unit =
    event.code match
      case KeyCode.Escape =>
        // If in fullscreen, exit fullscreen mode
        if sceneManager.isFullscreen then sceneManager.exitFullscreen()
      // If not in fullscreen, ESC doesn't do anything in main menu

      case KeyCode.F11 =>
        // Toggle fullscreen mode
        sceneManager.toggleFullscreen()

      case _ => // Ignore other keys
  /** Create the main menu layout
    */
  private def createMainMenuLayout(): VBox =
    val mainLayout = new VBox():
      spacing = 30
      alignment = Pos.Center
      padding = Insets(50)
      // Pixel art inspired gradient background
      background = Background(
        Array(
          BackgroundFill(
            LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = CycleMethod.NoCycle,
              stops = List(
                Stop(0, Color.web("#2E7D32")), // Dark green top
                Stop(0.5, Color.web("#1B5E20")), // Darker green middle
                Stop(1, Color.web("#0D4F14")) // Very dark green bottom
              )
            ),
            CornerRadii.Empty,
            Insets.Empty
          )
        )
      )

    // Title with pixel art styling
    val titleLabel = new Label("Harvest for All"):
      font = FontManager.titleFont
      textFill = Color.web("#E8F5E8")
      effect = new DropShadow {
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 3
        offsetY = 3
        radius = 4
        spread = 0.8
      }

    val subtitleLabel = new Label("SDG 2 - Agricultural Management System"):
      font = FontManager.subtitleFont
      textFill = Color.web("#81C784")
      effect = new DropShadow {
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 2
        offsetY = 2
        radius = 3
        spread = 0.6
      }

    // Menu buttons with enhanced container
    val buttonContainer = new VBox():
      spacing = 20
      alignment = Pos.Center
      padding = Insets(20)
      background = Background(
        Array(
          BackgroundFill(
            Color.web("#000000", 0.2), // Semi-transparent black
            CornerRadii(10),
            Insets.Empty
          )
        )
      )
      border = Border(
        BorderStroke(
          Color.web("#4CAF50"),
          BorderStrokeStyle.Solid,
          CornerRadii(10),
          BorderWidths(2)
        )
      )

    val newGameButton =
      createMenuButton("Start New Farm", () => start2DGame())
    val loadGameButton = createMenuButton("Load Game", () => loadGame())
    val controlsButton = createMenuButton("Controls", () => showControls())
    val aboutButton = createMenuButton("About", () => showAbout())
    val exitButton = createMenuButton("Exit", () => exitApplication())

    buttonContainer.children.addAll(
      newGameButton,
      loadGameButton,
      controlsButton,
      aboutButton,
      exitButton
    )

    // Game stats (if available)
    val statsContainer = createStatsContainer()

    mainLayout.children.addAll(
      titleLabel,
      subtitleLabel,
      buttonContainer,
      statsContainer
    )

    mainLayout

  /** Create a styled menu button with pixel art theme
    */
  private def createMenuButton(text: String, action: () => Unit): Button =
    val button = new Button(text):
      prefWidth = 220
      prefHeight = 55
      font = FontManager.buttonFont
      textFill = Color.White
      // Default button style
      background = Background(
        Array(
          BackgroundFill(
            LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = CycleMethod.NoCycle,
              stops = List(
                Stop(0, Color.web("#4CAF50")),
                Stop(1, Color.web("#388E3C"))
              )
            ),
            CornerRadii(0), // Square corners for pixel art feel
            Insets.Empty
          )
        )
      )
      border = Border(
        BorderStroke(
          Color.web("#2E7D32"),
          BorderStrokeStyle.Solid,
          CornerRadii(0),
          BorderWidths(3)
        )
      )
      effect = new DropShadow {
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 3
        offsetY = 3
        radius = 5
        spread = 0.7
      }
      onAction = (event: ActionEvent) => action()

    // Hover effects
    button.onMouseEntered = (event) =>
      button.background = Background(
        Array(
          BackgroundFill(
            LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = CycleMethod.NoCycle,
              stops = List(
                Stop(0, Color.web("#66BB6A")),
                Stop(1, Color.web("#4CAF50"))
              )
            ),
            CornerRadii(0),
            Insets.Empty
          )
        )
      )
      button.border = Border(
        BorderStroke(
          Color.web("#4CAF50"),
          BorderStrokeStyle.Solid,
          CornerRadii(0),
          BorderWidths(3)
        )
      )
      button.effect = new DropShadow {
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 4
        offsetY = 4
        radius = 8
        spread = 0.9
      }

    button.onMouseExited = (event) =>
      button.background = Background(
        Array(
          BackgroundFill(
            LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = CycleMethod.NoCycle,
              stops = List(
                Stop(0, Color.web("#4CAF50")),
                Stop(1, Color.web("#388E3C"))
              )
            ),
            CornerRadii(0),
            Insets.Empty
          )
        )
      )
      button.border = Border(
        BorderStroke(
          Color.web("#2E7D32"),
          BorderStrokeStyle.Solid,
          CornerRadii(0),
          BorderWidths(3)
        )
      )
      button.effect = new DropShadow {
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 3
        offsetY = 3
        radius = 5
        spread = 0.7
      }

    button

  /** Create stats container with pixel art styling
    */
  private def createStatsContainer(): VBox =
    val statsContainer = new VBox():
      spacing = 12
      alignment = Pos.Center
      padding = Insets(25)
      background = Background(
        Array(
          BackgroundFill(
            Color.web("#000000", 0.4), // Semi-transparent black
            CornerRadii(8),
            Insets.Empty
          )
        )
      )
      border = Border(
        BorderStroke(
          Color.web("#4CAF50"),
          BorderStrokeStyle.Solid,
          CornerRadii(8),
          BorderWidths(2)
        )
      )
      effect = new DropShadow {
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 3
        offsetY = 3
        radius = 6
        spread = 0.8
      }

    val statsLabel = new Label("Game Statistics"):
      font = FontManager.subHeaderFont
      textFill = Color.web("#E8F5E8")
      effect = new DropShadow {
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 2
        offsetY = 2
        radius = 3
        spread = 0.7
      }

    val stats = getLatestGameStats()
    val playerLevelLabel = new Label(s"Player Level: ${stats("playerLevel")}")
    val totalProductionLabel = new Label(
      s"Total Production: ${stats("totalFoodSecurity")}"
    )
    val sustainabilityLabel = new Label(
      s"Sustainability: ${stats("globalSustainabilityRating")}%"
    )

    List(playerLevelLabel, totalProductionLabel, sustainabilityLabel).foreach {
      label =>
        label.font = FontManager.labelFont
        label.textFill = Color.web("#B8E6B8")
        label.effect = new DropShadow {
          blurType = BlurType.Gaussian
          color = Color.Black
          offsetX = 1
          offsetY = 1
          radius = 2
          spread = 0.6
        }
    }

    statsContainer.children.addAll(
      statsLabel,
      playerLevelLabel,
      totalProductionLabel,
      sustainabilityLabel
    )

    statsContainer

  /** Start 2D farming game
    */
  private def start2DGame(): Unit =
    gameState.resetGame()
    val farmGameScene = new FarmGameSceneModular(sceneManager, gameState)
    sceneManager.switchTo(farmGameScene.getScene, "FarmGame")

  /** Load existing game
    */
  private def loadGame(): Unit =
    sceneManager.switchToLoadGame()

  /** Show controls
    */
  private def showControls(): Unit =
    val controlsScene = new ControlsScene(sceneManager, gameState)
    sceneManager.switchTo(controlsScene.getScene, "Controls")

  /** Show about dialog
    */
  private def showAbout(): Unit =
    val aboutScene = new AboutScene(sceneManager, gameState)
    sceneManager.switchTo(aboutScene.getScene, "About")

  /** Exit application
    */
  private def exitApplication(): Unit =
    System.exit(0)

  /** Get the latest game statistics from the most recent save file
    */
  private def getLatestGameStats(): Map[String, Any] =
    try
      // Try to load the most recent AutoSave first, then check other saves
      val saveManager = harvestforall.systems.SaveManager.getInstance

      // Check if AutoSave exists using SaveManager (uses correct path)
      if saveManager.saveExists("AutoSave") then
        saveManager.loadGame("AutoSave.json") match
          case scala.util.Success(saveData) =>
            Map(
              "playerLevel" -> 1, // You can enhance this based on your game logic
              "totalFoodSecurity" -> saveData.gameStats.totalCropsHarvested,
              "globalSustainabilityRating" -> saveData.gameStats.sustainabilityRating.toInt
            )
          case scala.util.Failure(_) =>
            // Fallback to default stats if save loading fails
            gameState.getGameStats
      else
        // Fallback to default stats if no save exists
        gameState.getGameStats
    catch
      case _: Exception =>
        // If there's any error loading saves, use default stats
        gameState.getGameStats

end MainMenuScene
