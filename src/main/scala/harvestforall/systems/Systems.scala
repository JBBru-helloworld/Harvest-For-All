package harvestforall.systems

import harvestforall.core.GameState
import harvestforall.utilities.Logger

/** Game loop system for managing the game's update cycle
  *
  * Handles real-time updates and ensures smooth gameplay
  */
class GameLoop(
    gameState: GameState,
    sceneManager: harvestforall.gui.managers.SceneManager
):

  private var isRunning: Boolean = false
  private var gameThread: Option[Thread] = None
  private val TARGET_FPS = 60
  private val FRAME_TIME = 1000 / TARGET_FPS // milliseconds per frame

  /** Start the game loop
    */
  def start(): Unit =
    if !isRunning then
      isRunning = true
      gameThread = Some(new Thread(gameLoopRunnable))
      gameThread.foreach(_.start())
      Logger.info("Game loop started")

  /** Stop the game loop
    */
  def stop(): Unit =
    isRunning = false
    gameThread.foreach(_.join())
    gameThread = None
    Logger.info("Game loop stopped")

  /** Game loop runnable
    */
  private def gameLoopRunnable: Runnable = () => {
    var lastTime = System.currentTimeMillis()

    while isRunning do
      val currentTime = System.currentTimeMillis()
      val deltaTime = currentTime - lastTime

      if deltaTime >= FRAME_TIME then
        // Update game state if not paused
        if !gameState.isPaused then
          update(deltaTime)
          // Update scene manager if needed
          // sceneManager.update(deltaTime)

        lastTime = currentTime

      // Sleep to prevent busy waiting
      Thread.sleep(Math.max(1, FRAME_TIME - deltaTime).toInt)
  }

  /** Update game systems
    */
  private def update(deltaTime: Long): Unit =
    // Update game systems here
    // This would typically update physics, AI, animations, etc.

    // Log periodically
    val currentTime = System.currentTimeMillis()
    if currentTime % 5000 < FRAME_TIME then
      Logger.debug(s"Game loop running - Day ${gameState.currentDay}")

end GameLoop

/** Save system for persisting game state
  *
  * Handles saving and loading game progress
  */
class SaveSystem(gameState: GameState):

  private var lastAutoSaveTime: Long = System.currentTimeMillis()
  private val AUTO_SAVE_INTERVAL = 5 * 60 * 1000 // 5 minutes

  /** Save game state to file
    */
  def saveGame(fileName: String): Unit =
    try
      // In a real implementation, this would serialize the game state
      // Example: Files.write(Paths.get(fileName), gameState.serialize())
      Logger.info(s"Game saved to $fileName")
    catch
      case ex: Exception =>
        Logger.error(s"Failed to save game: ${ex.getMessage}")

  /** Load game state from file
    */
  def loadGame(fileName: String): Boolean =
    try
      // In a real implementation, this would deserialize the game state
      // Example: gameState.deserialize(Files.readString(Paths.get(fileName)))
      Logger.info(s"Game loaded from $fileName")
      true
    catch
      case ex: Exception =>
        Logger.error(s"Failed to load game: ${ex.getMessage}")
        false

  /** Auto-save game if enough time has passed
    */
  def autoSave(): Unit =
    val currentTime = System.currentTimeMillis()
    if currentTime - lastAutoSaveTime >= AUTO_SAVE_INTERVAL then
      saveGame("autosave.dat")
      lastAutoSaveTime = currentTime

end SaveSystem
