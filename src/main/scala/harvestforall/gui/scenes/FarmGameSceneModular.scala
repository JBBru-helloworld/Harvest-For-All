package harvestforall.gui.scenes

import scalafx.animation.AnimationTimer
import scalafx.scene.Scene
import scalafx.scene.canvas.{Canvas, GraphicsContext}
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.{StackPane, VBox, Priority}
import scalafx.scene.paint.Color
import scalafx.Includes._
import scalafx.geometry.Pos

// Import modular components
import harvestforall.audio.BackgroundMusicManager
import harvestforall.gui.components.{FarmMenuBar, MenuAction}
import harvestforall.input.FarmInputHandler
import harvestforall.systems.AutoSaveManager
import harvestforall.game.proximity.ProximityManager

// Import existing systems
import harvestforall.graphics.{FarmTileManager, FarmPlayer, CropSpriteManager}
import harvestforall.game.ui.{
  DialogueSystem,
  PlayerLifeSystem,
  FarmingInventorySystem
}
import harvestforall.game.systems.{HungerSystem, InteractiveFarmingSystem}
import harvestforall.gui.managers.SceneManager
import harvestforall.core.{GameState, SustainabilityAction}
import harvestforall.systems.SaveManager
import harvestforall.gui.scenes.{VillageScene, MainMenuScene}

import scala.concurrent.ExecutionContext.Implicits.global

/** The main farm scene where all the action happens!
  *
  * I completely refactored this to be way more organised. Instead of having one
  * massive class doing everything, I split it into proper modules that each do
  * their own thing. Much cleaner especially now that my game can run!
  *
  * **Cool OOP stuff I used:**
  *   - Each class has one job (like how InputHandler only handles input)
  *   - This way I can add new features without breaking existing code
  *   - Everything talks through interfaces so I can swap components easily
  *   - Kept dependencies simple and logical
  *
  * **Modern Scala 3 features that make life easier:**
  *   - No more curly braces everywhere at least which is nice!
  *     (indentation-based syntax)
  *   - Extension methods to add functionality to existing classes
  *   - Enums that actually prevent bugs
  *   - Smart dependency injection with given/using
  *   - Case classes for data that doesn't change
  *   - Pattern matching that's way more powerful than switch statements
  */
class FarmGameSceneModular(sceneManager: SceneManager, gameState: GameState):

  // Scene configuration
  private val Config = FarmSceneConfig(
    tileSize = 48,
    screenCols = 25,
    screenRows = 15,
    fps = 60
  )

  // Modular components (dependency injection)
  private var audioManager: BackgroundMusicManager = _
  private var menuBar: FarmMenuBar = _
  private var inputHandler: FarmInputHandler = _
  private var autoSaveManager: AutoSaveManager = _
  private var proximityManager: ProximityManager = _

  // Core game systems (kept as is, but could be further modularised)
  private var tileManager: FarmTileManager = _
  private var player: FarmPlayer = _
  private var cropSpriteManager: CropSpriteManager = _
  private var dialogueSystem: DialogueSystem = _
  private var playerLifeSystem: PlayerLifeSystem = _
  private var farmingInventorySystem: FarmingInventorySystem = _
  private var hungerSystem: HungerSystem = _
  private var farmingSystem: InteractiveFarmingSystem = _

  // UI and animation
  private var gameTimer: AnimationTimer = _
  private val canvas = new Canvas(Config.screenWidth, Config.screenHeight)
  private val gc: GraphicsContext = canvas.graphicsContext2D

  /** Set up key event handling for the scene - delegate all input to our
    * handler
    */
  private def setupKeyHandling(): Unit =
    canvas.focusTraversable = true // Make sure canvas can receive focus
    canvas.requestFocus() // Request focus initially

    // I delegate all key events to my abstracted input handler and consume the events
    // The consume() call prevents weird ScalaFX bugs where events get processed multiple times
    canvas.onKeyPressed = (event: KeyEvent) => {
      val keyString = event.code.getName
      inputHandler.handleKeyPressed(keyString)
      event.consume() // Stop the event from bubbling up and causing issues
    }
    canvas.onKeyReleased = (event: KeyEvent) => {
      val keyString = event.code.getName
      inputHandler.handleKeyReleased(keyString)
      event.consume() // Same here - prevent event conflicts
    }

    // These are backup handlers in case the canvas somehow stops listening for keys
    // Sometimes ScalaFX can be annoying about which element is actively receiving input, so this ensures
    // that even if the canvas isn't the "active" element anymore, I still capture key events at the scene level
    scene.onKeyPressed = (event: KeyEvent) => {
      if !canvas.focused.value then
        canvas.requestFocus()
        val keyString = event.code.getName
        inputHandler.handleKeyPressed(keyString)
        event.consume()
    }
    scene.onKeyReleased = (event: KeyEvent) => {
      if !canvas.focused.value then
        val keyString = event.code.getName
        inputHandler.handleKeyReleased(keyString)
        event.consume()
    }

  // Game state
  private var isPaused = false

  // Real-time progression: Days advance every 24 hours, seasons every 5 days!
  private var lastDayAdvanceTime: Long = System.currentTimeMillis()
  private val dayDurationMs: Long =
    24 * 60 * 60 * 1000 // 24 hours in milliseconds
  private var pauseStartTime: Long = 0

  // Track planted crop types for diversity bonus
  private var plantedCropTypes = scala.collection.mutable.Set[String]()

  // Scene layout using composition
  private val scene = createScene()

  /** Initialize all components and systems */
  initialize()

  /** Configuration case class (Scala 3 feature) */
  case class FarmSceneConfig(
      tileSize: Int,
      screenCols: Int,
      screenRows: Int,
      fps: Int
  ):
    val screenWidth: Double = tileSize * screenCols
    val screenHeight: Double = tileSize * screenRows

  /** Create the scene layout using modular components */
  private def createScene(): Scene =
    // Create menu bar with game state context
    given GameState = gameState
    menuBar = new FarmMenuBar()

    val menuBarComponent = menuBar.createMenuBar(handleMenuAction)

    val root = new VBox:
      children = List(menuBarComponent, canvas)
      VBox.setVgrow(canvas, Priority.Always)
      VBox.setVgrow(menuBarComponent, Priority.Never)
      alignment = Pos.TopCenter
      fillWidth = true
      spacing = 0

    /** Make canvas focusable to capture keys */
    canvas.focusTraversable = true

    val newScene = new Scene(root):
      fill = Color.Black

    newScene

  /** Handle menu bar actions using pattern matching */
  private def handleMenuAction(action: MenuAction): Unit =
    action match
      case MenuAction.Pause     => togglePause()
      case MenuAction.Save      => handleSaveGame()
      case MenuAction.MainMenu  => handleBackToMainMenu()
      case MenuAction.LearnMore => handleLearnMore()

  /** Initialize all systems and components */
  private def initialize(): Unit =
    try
      println("Initializing Modular Farm Game Scene...")

      // Initialize core game systems first
      initializeCoreGameSystems()

      // Initialize modular components
      initializeModularComponents()

      // Setup integrations between components
      setupComponentIntegrations()

      // Setup key handling delegation to input handler
      setupKeyHandling()

      // Start systems
      startSystems()

      println("Modular Farm Game Scene initialized successfully")

    catch
      case ex: Exception =>
        println(s"Failed to initialize modular farm game: ${ex.getMessage}")
        ex.printStackTrace()

  /** Initialize core game systems (could be further modularized) */
  private def initializeCoreGameSystems(): Unit =
    // Initialize sprite and tile systems
    cropSpriteManager = new CropSpriteManager()
    cropSpriteManager.initialize()

    tileManager = new FarmTileManager()
    tileManager.initialize()

    player = new FarmPlayer(tileManager)
    player.initialize()

    // Initialize UI systems
    dialogueSystem = new DialogueSystem()
    dialogueSystem.initialize()

    playerLifeSystem = new PlayerLifeSystem()
    playerLifeSystem.initialize()
    playerLifeSystem.setMaxLife(6)
    playerLifeSystem.setCurrentLife(6)

    farmingInventorySystem = new FarmingInventorySystem()
    farmingInventorySystem.initialize(cropSpriteManager)
    farmingInventorySystem.setGameState(gameState)

    given PlayerLifeSystem = playerLifeSystem
    hungerSystem = new HungerSystem()
    farmingInventorySystem.setHungerSystem(hungerSystem)

    farmingSystem =
      new InteractiveFarmingSystem(tileManager, farmingInventorySystem)
    farmingSystem.initialize()

  /** Initialize modular components */
  private def initializeModularComponents(): Unit =
    // Audio management
    audioManager = new BackgroundMusicManager()
    audioManager.playMusic()

    // Input handling
    inputHandler =
      new FarmInputHandler(player, dialogueSystem, farmingInventorySystem)

    // Auto-save management
    autoSaveManager = AutoSaveManager.create(
      gameState,
      player,
      farmingSystem,
      farmingInventorySystem
    )
    // autoSaveManager.addSaveListener(new AutoSaveManager.ConsoleNotificationListener) // TODO: Fix path-dependent types

    // Proximity detection
    proximityManager = ProximityManager.withConsoleLogging(tileManager, player)

  /** Setup component integrations - wire up the action handlers properly */
  private def setupComponentIntegrations(): Unit =
    // Setup input handler action mappings - this is where the magic happens!
    val actionHandlers = Map(
      "pause" -> (() => togglePause()),
      "check_village_entrance" -> (() => checkEnterActions()),
      "check_treasure_chest" -> (() => checkEnterActions()),
      "farming_action" -> (() => handleFarmingAction()),
      "watering_action" -> (() => handleWateringAction()),
      // Debug actions
      "debug_damage" -> (() => playerLifeSystem.takeDamage(1)),
      "debug_heal" -> (() => playerLifeSystem.heal(1)),
      "debug_add_seeds" -> (() => {
        farmingInventorySystem.addItem("wheat_seeds", 5)
        () // Explicitly return Unit
      }),
      "debug_variety_pack" -> (() => addVarietyPack()),
      "debug_add_crops" -> (() => addDebugCrops()),
      "debug_reduce_hunger" -> (() => hungerSystem.debugReduceHunger(30.0)),
      "debug_show_position" -> (() => showPlayerPosition())
    )

    // Actually connect the handlers to the input system
    inputHandler.setActionHandlers(actionHandlers)

  /** Setup input handling for the scene */
  /** Start all systems and timers */
  private def startSystems(): Unit =
    // Initialize real-time tracking - your farming journey begins now!
    lastDayAdvanceTime = System.currentTimeMillis()

    // Check for save restore
    sceneManager.getLastLoadedSave match
      case Some(saveData) =>
        println(s"Restoring game state from save: ${saveData.saveName}")
        SaveManager.getInstance.restoreGameState(
          saveData,
          player,
          farmingSystem,
          farmingInventorySystem
        )
      case None =>
        dialogueSystem.startDialogue(0) // Tutorial dialogue

    // Start main game loop
    gameTimer = AnimationTimer { _ =>
      update()
      render()
    }
    gameTimer.start()

  /** Main update loop */
  private def update(): Unit =
    if !isPaused then
      // Check if a real day has passed - time for your farm to advance!
      val currentTime = System.currentTimeMillis()
      if currentTime - lastDayAdvanceTime >= dayDurationMs then
        gameState.advanceDay()
        lastDayAdvanceTime = currentTime
        println(
          s"🌅 New day! Day ${gameState.currentDay} - Season: ${gameState.currentSeason}"
        )

      // Update core systems
      dialogueSystem.update()
      hungerSystem.update()
      farmingSystem.update()

      // Update player (respecting input context)
      if inputHandler.isMovementAllowed then player.update()

      // Update modular components
      if !farmingInventorySystem.isOpen && !dialogueSystem.isActive then
        proximityManager.updateProximity()

      autoSaveManager.checkAndPerformAutoSave()

      // Update input context
      updateInputContext()

    // Always update menu stats
    menuBar.updateStatus()

  /** Update input handler context based on game state */
  private def updateInputContext(): Unit =
    val context =
      if isPaused then inputHandler.InputContext.Paused
      else if dialogueSystem.isActive then inputHandler.InputContext.Dialogue
      else if farmingInventorySystem.isOpen then
        inputHandler.InputContext.Inventory
      else inputHandler.InputContext.Normal

    // Call the input handler's updateContext method directly
    inputHandler.updateContext()

  /** Main render loop */
  private def render(): Unit =
    // Clear screen
    gc.fill = Color.Black
    gc.fillRect(0, 0, Config.screenWidth, Config.screenHeight)

    try
      // Render game world
      tileManager.draw(gc, farmingSystem, cropSpriteManager)
      player.draw(gc)

      // Render UI components directly (avoiding path-dependent type issues for now)
      renderUIComponents()

    catch
      case ex: Exception =>
        println(s"Render error: ${ex.getMessage}")

  /** Render UI components directly */
  private def renderUIComponents(): Unit =
    // Render player life (hearts) - always visible
    playerLifeSystem.render(gc, Config.screenWidth, Config.screenHeight)

    // Render season indicator (top-right corner)
    renderSeasonIndicator()

    // Render inventory if open
    farmingInventorySystem.render(gc, Config.screenWidth, Config.screenHeight)

    // Render proximity prompts if needed and UI is not busy
    if !farmingInventorySystem.isOpen && !dialogueSystem.isActive then
      if proximityManager.isNearVillage then renderVillagePrompt()
      if proximityManager.isNearTreasureChest then renderTreasureChestPrompt()

    // Render dialogue system (highest priority)
    dialogueSystem.render(gc, Config.screenWidth, Config.screenHeight)

    // Render pause overlay if paused (very high priority)
    if isPaused then renderPauseOverlay()

    // Debug info (optional)
    if !dialogueSystem.isActive then renderDebugInfo()

  /** Render village entrance prompt when player is near a village */
  private def renderVillagePrompt(): Unit =
    gc.fill = Color.color(0, 0, 0, 0.8)
    val promptWidth = 400
    val promptHeight = 60
    val promptX = (Config.screenWidth - promptWidth) / 2
    val promptY = Config.screenHeight - 150

    gc.fillRect(promptX, promptY, promptWidth, promptHeight)
    gc.stroke = Color.White
    gc.lineWidth = 2
    gc.strokeRect(promptX, promptY, promptWidth, promptHeight)

    gc.fill = Color.White
    gc.font = harvestforall.gui.utils.FontManager.buttonFont
    val text = "Press ENTER to visit Village Market"
    gc.fillText(text, promptX + 20, promptY + 35)

  /** Render treasure chest prompt when player is near a treasure chest */
  private def renderTreasureChestPrompt(): Unit =
    gc.fill = Color.color(0.2, 0.1, 0.0, 0.8)
    val promptWidth = 450
    val promptHeight = 60
    val promptX = (Config.screenWidth - promptWidth) / 2
    val promptY = Config.screenHeight - 220

    gc.fillRect(promptX, promptY, promptWidth, promptHeight)
    gc.stroke = Color.Gold
    gc.lineWidth = 2
    gc.strokeRect(promptX, promptY, promptWidth, promptHeight)

    gc.fill = Color.Gold
    gc.font = harvestforall.gui.utils.FontManager.buttonFont
    val text = "Press ENTER to examine Treasure Chest"
    gc.fillText(text, promptX + 20, promptY + 35)

  /** Render pause overlay when game is paused */
  private def renderPauseOverlay(): Unit =
    gc.fill = Color.color(0, 0, 0, 0.7)
    gc.fillRect(0, 0, Config.screenWidth, Config.screenHeight)

    val dialogueWidth = 500
    val dialogueHeight = 300
    val dialogueX = (Config.screenWidth - dialogueWidth) / 2
    val dialogueY = (Config.screenHeight - dialogueHeight) / 2

    gc.fill = Color.color(0, 0, 0, 0.7)
    gc.fillRect(dialogueX, dialogueY, dialogueWidth, dialogueHeight)

    gc.stroke = Color.web("#4CAF50")
    gc.lineWidth = 4
    gc.strokeRect(dialogueX, dialogueY, dialogueWidth, dialogueHeight)

    gc.stroke = Color.web("#66BB6A")
    gc.lineWidth = 2
    gc.strokeRect(
      dialogueX + 5,
      dialogueY + 5,
      dialogueWidth - 10,
      dialogueHeight - 10
    )

    gc.fill = Color.web("#E8F5E8")
    gc.font = harvestforall.gui.utils.FontManager.titleFont
    val title = "GAME PAUSED"
    val centeredX = dialogueX + 100
    gc.fillText(title, centeredX, dialogueY + 60)

    gc.font = harvestforall.gui.utils.FontManager.buttonFont
    gc.fill = Color.web("#81C784")

    gc.fillText(
      "• Click PAUSE again to Resume",
      dialogueX + 50,
      dialogueY + 120
    )
    gc.fillText(
      "• Click MAIN MENU to return to menu",
      dialogueX + 50,
      dialogueY + 150
    )
    gc.fillText(
      "• Click LEARN MORE for game info",
      dialogueX + 50,
      dialogueY + 180
    )

    gc.fill = Color.web("#A5D6A7")
    gc.font = harvestforall.gui.utils.FontManager.smallFont
    gc.fillText(
      "TIP: Save your progress regularly!",
      dialogueX + 50,
      dialogueY + 220
    )
    gc.fillText(
      "Use keyboard shortcuts: P=Pause, I=Inventory, C=Controls",
      dialogueX + 50,
      dialogueY + 240
    )

  private def renderDebugInfo(): Unit =
    gc.fill = Color.White
    gc.font = harvestforall.gui.utils.FontManager.smallFont

    val debugY = Config.screenHeight - 150
    gc.fillText("Controls:", 10, debugY)
    gc.fillText("WASD - Move, E - Plant/Harvest, R - Water", 10, debugY + 15)
    gc.fillText(
      "I - Inventory, T - Tutorial, C - Controls, H - Tips",
      10,
      debugY + 30
    )

    if farmingInventorySystem.isOpen then
      gc.fillText(
        "Use WASD/Arrow keys to navigate inventory, ENTER to eat crop",
        10,
        debugY + 75
      )

  /** Render season indicator in top-right corner */
  private def renderSeasonIndicator(): Unit =
    val season = gameState.currentSeason
    val day = gameState.currentDay

    // Calculate position for top-right corner - make it bigger to show more info
    val indicatorWidth = 250
    val indicatorHeight = 120
    val indicatorX = Config.screenWidth - indicatorWidth - 20
    val indicatorY = 20

    // Semi-transparent background
    gc.fill = Color.color(0, 0, 0, 0.7)
    gc.fillRoundRect(
      indicatorX,
      indicatorY,
      indicatorWidth,
      indicatorHeight,
      10,
      10
    )

    // Border with season-appropriate color
    val seasonColor = season match
      case harvestforall.core.Season.SPRING => Color.web("#4CAF50") // Green
      case harvestforall.core.Season.SUMMER => Color.web("#FF9800") // Orange
      case harvestforall.core.Season.AUTUMN => Color.web("#795548") // Brown
      case harvestforall.core.Season.WINTER => Color.web("#2196F3") // Blue

    gc.stroke = seasonColor
    gc.lineWidth = 2
    gc.strokeRoundRect(
      indicatorX,
      indicatorY,
      indicatorWidth,
      indicatorHeight,
      10,
      10
    )

    // Season text
    gc.fill = Color.White
    gc.font = harvestforall.gui.utils.FontManager.buttonFont
    gc.fillText(s"Season: ${season.toString}", indicatorX + 10, indicatorY + 25)

    // Day counter
    gc.font = harvestforall.gui.utils.FontManager.smallFont
    gc.fillText(s"Day: $day", indicatorX + 10, indicatorY + 45)

    // Time until next day (24 hours)
    val currentTime = System.currentTimeMillis()
    val timeUntilNextDay = dayDurationMs - (currentTime - lastDayAdvanceTime)
    val hoursLeft = (timeUntilNextDay / (60 * 60 * 1000)).toInt.max(0)
    val minutesLeft =
      ((timeUntilNextDay % (60 * 60 * 1000)) / (60 * 1000)).toInt.max(0)
    gc.fillText(
      f"Next day: $hoursLeft%dh $minutesLeft%02dm",
      indicatorX + 10,
      indicatorY + 60
    )

    // Days until next season (every 5 days)
    val daysInCurrentSeason = day % season.durationInDays
    val daysUntilNextSeason =
      if daysInCurrentSeason == 0 then season.durationInDays
      else season.durationInDays - daysInCurrentSeason
    gc.fillText(
      s"Season changes in: $daysUntilNextSeason days",
      indicatorX + 10,
      indicatorY + 75
    )

    // Season effects - calculated live from Season.scala!
    gc.fill = seasonColor
    gc.font = harvestforall.gui.utils.FontManager.smallFont
    val modifier = (season.growthModifier - 1.0) * 100
    val sign = if modifier >= 0 then "+" else ""
    val effectText = f"Growth: $sign$modifier%.0f%%"
    gc.fillText(effectText, indicatorX + 10, indicatorY + 95)

  // Action handlers (simplified and delegated)
  private def togglePause(): Unit =
    val currentTime = System.currentTimeMillis()

    if isPaused then
      // Resuming! Adjust timing so we don't lose time while paused
      val pauseDuration = currentTime - pauseStartTime
      lastDayAdvanceTime += pauseDuration
      isPaused = false
      gameState.unpause() // Sync with GameState pause
      println("🎮 Game resumed - time continues!")
    else
      // Pausing! Remember when so time doesn't jump
      pauseStartTime = currentTime
      isPaused = true
      gameState.pause() // Sync with GameState pause
      println("⏸️ Game paused - time stops!")

  private def checkEnterActions(): Unit =
    if proximityManager.isNearVillage then enterVillage()
    else if proximityManager.isNearTreasureChest then openTreasureChest()

  private def enterVillage(): Unit =
    gameState.setLocation("Village")
    val villageScene =
      new VillageScene(sceneManager, gameState, Some(() => onSceneActivated()))
    sceneManager.switchTo(villageScene.getScene, "Village")

  private def openTreasureChest(): Unit =
    val treasureQuizScene =
      new harvestforall.gui.scenes.TreasureQuizScene(sceneManager, gameState)
    sceneManager.switchTo(treasureQuizScene, "TreasureQuiz")

  private def handleSaveGame(): Unit =
    try
      // Get current save name or prompt for one
      val saveName = gameState.currentSaveName.getOrElse {
        // If no save name exists, prompt user for one
        val dialog = new scalafx.scene.control.TextInputDialog("My Farm"):
          title = "Save Game"
          headerText = "Save Your Farm Progress"
          contentText = "Enter a name for your save file:"

        dialog.showAndWait() match
          case Some(name) =>
            val trimmedName = name.trim
            if trimmedName.nonEmpty then
              gameState.currentSaveName = trimmedName
              trimmedName
            else "" // User cancelled or entered empty name
          case None => "" // User cancelled
      }

      // Only proceed if we have a valid save name
      if saveName.nonEmpty then
        // Use SaveManager to actually save the game
        val saveManager = SaveManager.getInstance
        saveManager.saveGame(
          saveName,
          gameState,
          player,
          farmingSystem,
          farmingInventorySystem
        ) match
          case scala.util.Success(filePath) =>
            val alert = new scalafx.scene.control.Alert(
              scalafx.scene.control.Alert.AlertType.Information
            ):
              title = "Save Game"
              headerText = "Game Saved Successfully!"
              contentText =
                s"Your farm '$saveName' has been saved to:\n$filePath"
            alert.showAndWait()
            println(s"[FarmGameScene] Game saved successfully: $saveName")

          case scala.util.Failure(ex) =>
            val alert = new scalafx.scene.control.Alert(
              scalafx.scene.control.Alert.AlertType.Error
            ):
              title = "Save Error"
              headerText = "Failed to save game"
              contentText = s"Error: ${ex.getMessage}"
            alert.showAndWait()
            println(s"[FarmGameScene] Save failed: ${ex.getMessage}")

    catch
      case ex: Exception =>
        println(s"[FarmGameScene] Error saving game: ${ex.getMessage}")
        val alert = new scalafx.scene.control.Alert(
          scalafx.scene.control.Alert.AlertType.Error
        ):
          title = "Save Error"
          headerText = "Failed to save game"
          contentText = s"Error: ${ex.getMessage}"
        alert.showAndWait()

  private def handleBackToMainMenu(): Unit =
    try
      // Stop the game timer to prevent conflicts
      if gameTimer != null then gameTimer.stop()

      // Stop background music if playing
      if audioManager != null then audioManager.cleanup()

      // Switch to main menu using the scene manager we already have
      val mainMenu = new MainMenuScene(sceneManager, gameState)
      sceneManager.switchTo(mainMenu.getScene, "MainMenu")
      println("Successfully switched to Main Menu")
    catch
      case ex: Exception =>
        println(s"Error switching to main menu: ${ex.getMessage}")
        ex.printStackTrace()

  private def handleLearnMore(): Unit =
    val alert = new scalafx.scene.control.Alert(
      scalafx.scene.control.Alert.AlertType.Information
    ):
      title = "Learn More"
      headerText = "About Harvest for All"
      contentText =
        "This game teaches sustainable farming and resource management!"
    alert.showAndWait()

  // Farming actions (could be moved to a separate FarmingActionHandler)
  private def handleFarmingAction(): Unit =
    val playerWorldX = player.worldX
    val playerWorldY = player.worldY

    if farmingSystem.canFarmHere(playerWorldX, playerWorldY) then
      try
        // Try to harvest first, then plant if nothing to harvest
        if !farmingSystem.harvestCrop(playerWorldX, playerWorldY) then
          // Get selected item from inventory instead of cycling through crops
          farmingInventorySystem.getSelectedItemName match
            case Some(selectedItem) if selectedItem.endsWith("_seeds") =>
              // Extract crop name from seed name (e.g., "wheat_seeds" -> "wheat")
              val cropToPlant = selectedItem.replace("_seeds", "")
              val planted =
                farmingSystem.plantCrop(playerWorldX, playerWorldY, cropToPlant)
              if planted then
                println(s"[FarmGameScene] Successfully planted $cropToPlant")

                // Track crop diversity
                val isNewCropType = !plantedCropTypes.contains(cropToPlant)
                plantedCropTypes.add(cropToPlant)

                // Update sustainability for planting
                gameState.updateSustainability(SustainabilityAction.PlantCrop)

                // Bonus for crop diversity (first time planting a new type)
                if isNewCropType && plantedCropTypes.size >= 3 then
                  gameState.updateSustainability(
                    SustainabilityAction.DiverseCrops
                  )
                  println(
                    s"[Sustainability] Diversity bonus! Growing ${plantedCropTypes.size} different crops."
                  )
              else
                println(
                  s"[FarmGameScene] No ${selectedItem} available or failed to plant"
                )

            case Some(selectedItem) =>
              println(
                s"[FarmGameScene] Selected item '$selectedItem' is not plantable seeds"
              )

            case None =>
              println("[FarmGameScene] No item selected in inventory")
        else
          println("[FarmGameScene] Successfully harvested crop")
          // Update sustainability for harvesting
          gameState.updateSustainability(SustainabilityAction.HarvestCrop)
      catch
        case ex: Exception =>
          println(s"Farming action failed: ${ex.getMessage}")
    else println("Must be on farming tile (brown soil) to plant/harvest!")

  private def handleWateringAction(): Unit =
    val playerWorldX = player.worldX
    val playerWorldY = player.worldY

    if farmingSystem.canFarmHere(playerWorldX, playerWorldY) then
      try
        val watered = farmingSystem.waterCrop(playerWorldX, playerWorldY)
        if watered then
          // Update sustainability for proper watering
          gameState.updateSustainability(SustainabilityAction.WaterCrop)
      catch
        case ex: Exception =>
          println(s"Watering action failed: ${ex.getMessage}")
    else println("Must be on farming tile to water crops!")

  // Debug helpers
  private def addVarietyPack(): Unit =
    List("corn_seeds", "carrot_seeds", "tomato_seeds").foreach { seed =>
      farmingInventorySystem.addItem(seed, 3)
    }

  private def addDebugCrops(): Unit =
    List("wheat", "corn", "carrot").foreach { crop =>
      farmingInventorySystem.addItem(crop, 2)
    }

  private def showPlayerPosition(): Unit =
    val playerCol = (player.worldX / tileManager.tileSize).toInt
    val playerRow = (player.worldY / tileManager.tileSize).toInt
    println(
      s"Player position: world(${player.worldX}, ${player.worldY}) tile($playerCol, $playerRow)"
    )

  // Public interface
  def getScene: Scene = scene
  def cleanup(): Unit =
    if gameTimer != null then gameTimer.stop()
    if audioManager != null then audioManager.cleanup()
  def onSceneActivated(): Unit =
    if farmingInventorySystem != null then
      farmingInventorySystem.syncWithGameState()

end FarmGameSceneModular
