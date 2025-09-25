package harvestforall.gui.managers

import scalafx.scene.Scene
import scalafx.stage.Stage
import scala.collection.mutable.Map
import harvestforall.gui.scenes.{
  LoadGameScene,
  NewGameScene,
  FarmGameSceneModular
}
import harvestforall.core.GameSaveData
import harvestforall.systems.SaveManager

/** Scene manager for handling different game scenes
  *
  * Manages scene transitions and maintains scene history - basically the
  * traffic controller for all the different screens in the game
  */
class SceneManager(primaryStage: Stage):

  private var currentScene: Option[Scene] = None
  private var sceneHistory: List[String] = List.empty
  private val scenes: Map[String, Scene] = Map.empty

  /** Switch to a new scene - the bread and butter of navigation
    */
  def switchTo(scene: Scene, sceneName: String): Unit =
    currentScene = Some(scene)
    scenes(sceneName) = scene
    sceneHistory =
      sceneName :: sceneHistory.take(
        10
      ) // Keep last 10 scenes - don't want infinite memory usage
    primaryStage.scene = scene
    println(s"[SceneManager] Switched to scene: $sceneName")

  /** Get current scene
    */
  def getCurrentScene: Option[Scene] = currentScene

  /** Get scene by name - useful for reusing scenes instead of recreating
    */
  def getScene(sceneName: String): Option[Scene] = scenes.get(sceneName)

  /** Go back to previous scene
    */
  def goBack(): Boolean =
    if sceneHistory.length > 1 then
      val previousSceneName = sceneHistory(1)
      scenes.get(previousSceneName) match
        case Some(scene) =>
          currentScene = Some(scene)
          primaryStage.scene = scene
          sceneHistory = sceneHistory.tail
          true
        case None => false
    else false

  /** Switch to main menu
    */
  def switchToMainMenu(): Unit =
    // This should be implemented based on my application structure
    // For now, I'll use goBack() or can maintain a refreence to main menu
    if !goBack() then
      println("[SceneManager] Cannot return to main menu - no previous scene")

  /** Switch to Game2D scene
    */
  def switchToGame2D(): Unit =
    scenes.get("Game2D") match
      case Some(scene) =>
        switchTo(scene, "Game2D")
      case None =>
        println("[SceneManager] Game2D scene not found")

  /** Switch to Village scene
    */
  def switchToVillage(): Unit =
    scenes.get("Village") match
      case Some(scene) =>
        switchTo(scene, "Village")
      case None =>
        println("[SceneManager] Village scene not found - creating new one")
      // The village scene will be created by the calling code

  /** Switch to Farm Game scene
    */
  def switchToFarmGameScene(): Unit =
    scenes.get("FarmGame") match
      case Some(scene) =>
        switchTo(scene, "FarmGame")
      case None =>
        println("[SceneManager] FarmGame scene not found - creating new one")

  /** Switch to Farm Game scene with GameState (for loading saves)
    */
  def switchToFarmGameScene(gameState: harvestforall.core.GameState): Unit =
    // Always create a new modular farm game scene for proper state restoration
    val farmGameScene =
      new harvestforall.gui.scenes.FarmGameSceneModular(this, gameState)
    switchTo(farmGameScene.getScene, "FarmGame")

  /** Switch to Load Game scene
    */
  def switchToLoadGame(): Unit =
    val loadGameScene = new LoadGameScene(this)
    switchTo(loadGameScene, "LoadGame")

  /** Switch to New Game scene
    */
  def switchToNewGame(): Unit =
    val newGameScene = new NewGameScene(this)
    switchTo(newGameScene, "NewGame")

  /** Load a saved game and restore state This method is supposed to create a
    * callback that can be used by LoadGameScene to properly restore game state
    * when loading
    */
  def loadGameWithRestore(saveData: GameSaveData): Unit = {
    println(
      s"[SceneManager] Loading game with state restoration: ${saveData.saveName}"
    )

    // Create a new GameState and populate it with save data
    val gameState = new harvestforall.core.GameState()

    // Restore basic game state from save data
    gameState.setCurrency(saveData.gameStats.currency)
    gameState.setSustainabilityRating(saveData.gameStats.sustainabilityRating)
    gameState.currentSaveName = saveData.saveName

    // Store the save data for the next scene to complete restoration
    lastLoadedSave = Some(saveData)

    // Switch to modular farm game scene with the reconstructed game state
    val farmGameScene =
      new harvestforall.gui.scenes.FarmGameSceneModular(this, gameState)
    switchTo(farmGameScene.getScene, "FarmGame")
  }

  // Store the last loaded save for scene restoration
  private var lastLoadedSave: Option[GameSaveData] = None

  /** Get the last loaded save data (for scene restoration) */
  def getLastLoadedSave: Option[GameSaveData] = {
    val save = lastLoadedSave
    lastLoadedSave = None // Clear after retrieval to avoid re-loading
    save
  }

  /** Update current scene (for game loop)
    */
  def update(deltaTime: Long): Unit =
    // Update current scene if it has update logic
    // This would be implemented based on your scene architecture
    ()

  /** Get scene history
    */
  def getSceneHistory: List[String] = sceneHistory

  /** Check if the stage is in fullscreen mode */
  def isFullscreen: Boolean = primaryStage.fullScreen.value

  /** Exit fullscreen mode */
  def exitFullscreen(): Unit =
    if primaryStage.fullScreen.value then primaryStage.fullScreen = false

  /** Toggle fullscreen mode */
  def toggleFullscreen(): Unit =
    primaryStage.fullScreen = !primaryStage.fullScreen.value

end SceneManager
