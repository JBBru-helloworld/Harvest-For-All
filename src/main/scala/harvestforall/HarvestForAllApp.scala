package harvestforall

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.image.Image
import scalafx.scene.input.KeyCombination
import scalafx.stage.WindowEvent
import scalafx.Includes._
import harvestforall.gui.scenes.MainMenuScene
import harvestforall.gui.managers.{SceneManager, AssetManager}
import harvestforall.systems.{GameLoop, SaveSystem}
import harvestforall.core.GameState

/** Main application class for Harvest for All - SDG 2 Agricultural Management
  * System
  *
  * ATTRIBUTION:
  *   - Tile Graphics: Blue Boy Adventure Graphics Pack by RyiSnow (Educational
  *     Use)
  *   - Plant Sprites: 20.02a - Farming Crops #1 3.0 (Purchased asset)
  *   - Font: Upheaval TrueType Font by Brian Kent (Freeware) Website:
  *     https://www.dafont.com/upheaval.font | Email: kentpw@norwich.net
  *   - Music: YouTube:
  *     https://www.youtube.com/watch?v=D5L36JDKxR0&list=PLdsGes2mFh92eHpOZVJQgoubb6rF0CcvU&index=1
  *   - Artist: Pix: https://www.youtube.com/@Pixverses
  *   - (Educational Use: NO COPYRIGHT)
  *   - Framework: ScalaFX 21.0.0-R32, Scala 3
  *
  * Advanced OOP Features Demonstrated:
  *   - Inheritance hierarchies in agriculture and resource management systems
  *   - Polymorphism in plant types and resource managers
  *   - Trait composition for shared behaviors like Upgradeable, Monitorable
  *   - Abstract classes for extensible game systems
  *   - Comprehensive GUI with pixel art aesthetic
  *
  * All features can be seen in my report and video of the gameplay.
  *
  * @author
  *   Joshua Bonham (23020050)
  * @version 1.0
  */
object HarvestForAllApp extends JFXApp3:

  // Application metadata
  private val APP_TITLE = "Harvest for All - UN Goal 2 Agricultural Management"
  private val APP_VERSION = "1.0.0"
  private val WINDOW_WIDTH = 1200
  private val WINDOW_HEIGHT = 800

  // Core game systems using Option[T] for safe initialization
  private var gameState: Option[GameState] = None
  private var sceneManager: Option[SceneManager] = None
  private var gameLoop: Option[GameLoop] = None
  private var saveSystem: Option[SaveSystem] = None

  override def start(): Unit =
    try
      println(s"Starting $APP_TITLE v$APP_VERSION")

      // Setup primary stage first
      setupPrimaryStage()

      // Initialize core systems (after stage is set up)
      initializeSystems()

      // Load initial scene
      loadMainMenu()

      // Start game loop
      gameLoop.foreach(_.start())

      println("Application started successfully")

    catch
      case ex: Exception =>
        println(s"Failed to start application: ${ex.getMessage}")
        ex.printStackTrace()
        showErrorDialog(
          "Startup Error",
          s"Failed to start application: ${ex.getMessage}"
        )
        sys.exit(1)

  /** Setup the primary stage with proper configuration
    */
  private def setupPrimaryStage(): Unit =
    stage = new PrimaryStage():
      title = APP_TITLE
      width = WINDOW_WIDTH
      height = WINDOW_HEIGHT
      resizable = true
      fullScreenExitHint = "Press ESC to exit fullscreen"

      // Set application icon using ScalaFX syntax
      try
        val iconStream = getClass.getResourceAsStream("/assets/ui/app-icon.png")
        if iconStream != null then icons += new Image(iconStream)
        else
          // Use default icon from AssetManager (if available)
          println("Could not find app icon resource, using default")
      catch
        case _: Exception =>
          println("Could not load application icon")

      // Handle window close event
      onCloseRequest = (event: WindowEvent) => handleApplicationExit(event)

  /** Initialize all core game systems
    */
  private def initializeSystems(): Unit =
    // Initialize asset manager first (other systems depend on it)
    AssetManager.loadEssentialAssets()

    // Initialize game state
    gameState = Some(new GameState())

    // Initialize scene manager (stage is now available)
    sceneManager = Some(new SceneManager(stage))

    // Initialize save system
    saveSystem = gameState.map(gs => new SaveSystem(gs))

    // Initialize game loop
    for {
      gs <- gameState
      sm <- sceneManager
    } yield gameLoop = Some(new GameLoop(gs, sm))

  /** Load the main menu scene
    */
  private def loadMainMenu(): Unit =
    for {
      sm <- sceneManager
      gs <- gameState
    } yield {
      val mainMenuScene = new MainMenuScene(sm, gs)
      sm.switchTo(mainMenuScene.getScene, "MainMenu")
    }

  /** Handle application exit with proper cleanup
    */
  private def handleApplicationExit(event: WindowEvent): Unit =
    // Show confirmation dialog
    val alert = new Alert(Alert.AlertType.Confirmation):
      title = "Exit Application"
      headerText = "Are you sure you want to exit?"
      contentText = "Any unsaved progress will be lost."

    val result = alert.showAndWait()

    if result.contains(ButtonType.OK) then
      // Perform cleanup
      cleanup()
      println("Application exited cleanly")
    else
      // Cancel the close event
      event.consume()

  /** Cleanup resources before application exit
    */
  private def cleanup(): Unit =
    try
      // Stop game loop
      gameLoop.foreach(_.stop())

      // Save game state
      for {
        ss <- saveSystem
        gs <- gameState
      } yield ss.autoSave()

      // Cleanup asset manager
      AssetManager.cleanup()

    catch
      case ex: Exception =>
        println(s"Error during cleanup: ${ex.getMessage}")

  /** Show error dialog to user
    */
  private def showErrorDialog(dialogTitle: String, message: String): Unit =
    val alert = new Alert(Alert.AlertType.Error):
      title = dialogTitle
      headerText = "An error occurred"
      contentText = message

    alert.showAndWait()

  /** Toggle fullscreen mode
    */
  def toggleFullscreen(): Unit =
    stage.fullScreen = !stage.fullScreen.value

  /** Set fullscreen mode
    */
  def setFullscreen(enabled: Boolean): Unit =
    stage.fullScreen = enabled

  /** Check if currently in fullscreen mode
    */
  def isFullscreen: Boolean = stage.fullScreen.value

  /** Helper methods for safe access to initialized systems
    */
  def getGameState: Option[GameState] = gameState
  def getSceneManager: Option[SceneManager] = sceneManager
  def getGameLoop: Option[GameLoop] = gameLoop
  def getSaveSystem: Option[SaveSystem] = saveSystem

end HarvestForAllApp
