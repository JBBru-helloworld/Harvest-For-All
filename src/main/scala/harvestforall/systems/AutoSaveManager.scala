package harvestforall.systems

import harvestforall.systems.SaveManager
import harvestforall.core.GameState
import harvestforall.graphics.FarmPlayer
import harvestforall.game.systems.InteractiveFarmingSystem
import harvestforall.game.ui.FarmingInventorySystem
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}

// Save notification trait
trait SaveNotificationListener:
  def onSaveStarted(): Unit = {}
  def onSaveCompleted(saveName: String): Unit = {}
  def onSaveFailed(error: String): Unit = {}

/** Manages automatic saving functionality for the farm game
  *
  * This was so difficult to implement because I wanted to ensure that the game
  * state is saved correctly without interrupting gameplay. I hate when games
  * freeze during saves! Also moved it to its own file and class to keep
  * organized.
  */
class AutoSaveManager(
    gameState: GameState,
    player: FarmPlayer,
    farmingSystem: InteractiveFarmingSystem,
    farmingInventorySystem: FarmingInventorySystem
)(using ExecutionContext):

  // Auto-save settings - you can tweak these if you want
  case class AutoSaveConfig(
      intervalMinutes: Int =
        5, // Save every 5 mins - not too often to be annoying
      maxAutoSaves: Int = 3, // Keep only 3 auto-saves to save disk space
      enableNotifications: Boolean =
        true, // Show save messages (kinda satisfying tbh)
      autoSaveName: String = "AutoSave" // Prefix for auto saves
  )

  // Save notification trait
  trait SaveNotificationListener:
    def onSaveStarted(): Unit = {}
    def onSaveCompleted(saveName: String): Unit = {}
    def onSaveFailed(error: String): Unit = {}

  private val config = AutoSaveConfig()
  private var lastSaveTime: Long = System.currentTimeMillis()
  private var saveListeners: List[SaveNotificationListener] = List.empty
  private val saveManager = SaveManager.getInstance

  /** Add a save notification listener */
  def addSaveListener(listener: SaveNotificationListener): Unit =
    saveListeners = listener :: saveListeners

  /** Remove a save notification listener */
  def removeSaveListener(listener: SaveNotificationListener): Unit =
    saveListeners = saveListeners.filterNot(_ == listener)

  /** Check if auto-save should be performed and do it if necessary */
  def checkAndPerformAutoSave(): Unit =
    val currentTime = System.currentTimeMillis()
    val intervalMs = config.intervalMinutes * 60 * 1000L

    if currentTime - lastSaveTime >= intervalMs then
      performAutoSaveAsync() // Time to save!
      lastSaveTime = currentTime

  /** Perform auto-save asynchronously */
  private def performAutoSaveAsync(): Future[Unit] =
    // Tell everyone we're starting to save
    saveListeners.foreach(_.onSaveStarted())

    val saveFuture = Future {
      saveManager.saveGame(
        config.autoSaveName,
        gameState,
        player,
        farmingSystem,
        farmingInventorySystem
      )
    }

    saveFuture.onComplete {
      case Success(savePath) =>
        saveListeners.foreach(_.onSaveCompleted(config.autoSaveName))
        if config.enableNotifications then
          println(s"💾 Auto-saved game to: $savePath") // Success!

      case Failure(exception) =>
        val errorMsg = s"Auto-save failed: ${exception.getMessage}"
        saveListeners.foreach(_.onSaveFailed(errorMsg))
        println(s"❌ $errorMsg") // Oops, something went wrong
    }

    saveFuture.map(_ => ())

  /** Perform manual save with custom name */
  def performManualSave(saveName: String): Future[String] =
    saveListeners.foreach(_.onSaveStarted())

    Future {
      saveManager.saveGame(
        saveName,
        gameState,
        player,
        farmingSystem,
        farmingInventorySystem
      ) match {
        case Success(filePath) =>
          saveListeners.foreach(_.onSaveCompleted(saveName))
          filePath
        case Failure(ex) =>
          val errorMsg = s"Save failed: ${ex.getMessage}"
          saveListeners.foreach(_.onSaveFailed(errorMsg))
          throw ex
      }
    }

  /** Reset the auto-save timer */
  def resetAutoSaveTimer(): Unit =
    lastSaveTime = System.currentTimeMillis()

  /** Get time until next auto-save in minutes */
  def getMinutesUntilNextAutoSave: Double =
    val elapsed = (System.currentTimeMillis() - lastSaveTime) / 1000.0 / 60.0
    math.max(0.0, config.intervalMinutes - elapsed)

  /** Check if auto-save is due */
  def isAutoSaveDue: Boolean =
    getMinutesUntilNextAutoSave <= 0

  /** Get auto-save configuration */
  def getConfig: AutoSaveConfig = config

// Companion object for factory methods
object AutoSaveManager:
  /** Create an auto-save manager with default configuration */
  def create(
      gameState: GameState,
      player: FarmPlayer,
      farmingSystem: InteractiveFarmingSystem,
      farmingInventorySystem: FarmingInventorySystem
  )(using ExecutionContext): AutoSaveManager =
    new AutoSaveManager(
      gameState,
      player,
      farmingSystem,
      farmingInventorySystem
    )

  /** Simple notification listener for console output */
  class ConsoleNotificationListener extends SaveNotificationListener:
    override def onSaveStarted(): Unit =
      println("💾 Starting auto-save...")

    override def onSaveCompleted(saveName: String): Unit =
      println(s"✅ Auto-save completed: $saveName")

    override def onSaveFailed(error: String): Unit =
      println(s"❌ Auto-save failed: $error")
