package harvestforall.input

import scalafx.scene.input.{KeyCode, KeyEvent}
import harvestforall.graphics.FarmPlayer
import harvestforall.game.ui.{DialogueSystem, FarmingInventorySystem}

/** Handles all keyboard input for the farm game scene
  *
  * I wanted to organize all the input stuff so it wouldn't be a big mess. Shows
  * off some patterns like:
  *   - Command Pattern (each key does a specific thing)
  *   - Strategy Pattern (different ways to handle input based on what's
  *     happening)
  */
class FarmInputHandler(
    player: FarmPlayer,
    dialogueSystem: DialogueSystem,
    farmingInventorySystem: FarmingInventorySystem
):

  // Which mode we're in (normal farming, talking, looking at inventory, etc)
  enum InputContext:
    case Normal, Dialogue, Inventory, Paused

  // Every key press is basically a command - neat pattern I like the simplicity actaully!
  trait InputCommand:
    def execute(): Unit

  // Current mode we're in
  private var currentContext: InputContext = InputContext.Normal

  // Functions that do stuff when keys are pressed (dependency injection ftw)
  private var actionHandlers: Map[String, () => Unit] = Map.empty

  /** Set action handlers for various game actions */
  def setActionHandlers(handlers: Map[String, () => Unit]): Unit =
    actionHandlers = handlers

  /** Set the current input context */
  def setContext(context: InputContext): Unit =
    currentContext = context

  /** Handle key pressed event */
  def handleKeyPressed(key: String): Unit =
    currentContext match
      case InputContext.Dialogue  => handleDialogueInput(key)
      case InputContext.Inventory => handleInventoryInput(key)
      case InputContext.Paused    => handlePausedInput(key)
      case InputContext.Normal    => handleNormalInput(key)

  /** Handle key released event */
  def handleKeyReleased(key: String): Unit =
    // Always handle movement releases to prevent stuck keys
    handleMovementRelease(key)

  /** Handle input during dialogue */
  private def handleDialogueInput(key: String): Unit =
    key match
      case "ENTER" | "Enter" | "Return" | "SPACE" | "Space" | " " =>
        dialogueSystem.handleInput(key)
        checkDialogueState()
      case _ =>
        // Still allow movement while talking
        handleMovementInput(key)

  /** Handle input when inventory is open */
  private def handleInventoryInput(key: String): Unit =
    farmingInventorySystem.handleInput(key)
    checkInventoryState()

  /** Handle input when game is paused */
  private def handlePausedInput(key: String): Unit =
    key match
      case "P" | "SPACE" | "Space" => actionHandlers.get("pause").foreach(_())
      case "ESCAPE"                => actionHandlers.get("pause").foreach(_())
      case _ => // Ignore everything else when paused - gotta respect the pause less guarr!
  /** Handle input during normal gameplay */
  private def handleNormalInput(key: String): Unit =
    key match
      // System stuff
      case "ENTER" | "Enter" | "Return" => handleEnterAction()
      case "P" | "SPACE" | "Space" => actionHandlers.get("pause").foreach(_())
      case "I" =>
        farmingInventorySystem.toggleInventory()
        updateContext()

      // Quick dialogue access - press these for help!
      case "T" => startDialogue("tutorial")
      case "C" => startDialogue("controls")
      case "H" => startDialogue("help")

      // Debug actions (these are just for testing - I might remove later if I remember ahaha)
      case "1" => actionHandlers.get("debug_damage").foreach(_())
      case "2" => actionHandlers.get("debug_heal").foreach(_())
      case "3" => actionHandlers.get("debug_add_seeds").foreach(_())
      case "4" => actionHandlers.get("debug_variety_pack").foreach(_())
      case "5" => actionHandlers.get("debug_add_crops").foreach(_())
      case "6" => actionHandlers.get("debug_reduce_hunger").foreach(_())
      case "9" => actionHandlers.get("debug_show_position").foreach(_())

      // The important farming stuff! E for "everything" (plant/harvest), R for "rain" (watering)
      case "E" => actionHandlers.get("farming_action").foreach(_())
      case "R" => actionHandlers.get("watering_action").foreach(_())

      // Movement (WASD)
      case _ => handleMovementInput(key)

  /** Handle ENTER key action based on game state */
  private def handleEnterAction(): Unit =
    // Check for special interactions first - village entrance, treasure etc
    actionHandlers.get("check_village_entrance").foreach(_())
    actionHandlers.get("check_treasure_chest").foreach(_())

  /** Start dialogue if not already active */
  private def startDialogue(dialogueType: String): Unit =
    if !dialogueSystem.isActive then
      dialogueType match
        case "tutorial" => dialogueSystem.startDialogue(0)
        case "controls" => dialogueSystem.startDialogue(1)
        case "help"     => dialogueSystem.startDialogue(2)
        case _          => println(s"Unknown dialogue type: $dialogueType")
      updateContext()

  /** Handle movement input */
  private def handleMovementInput(key: String): Unit =
    val keyCode = mapStringToKeyCode(key)
    player.onKeyPressed(keyCode)

  /** Handle movement key release */
  private def handleMovementRelease(key: String): Unit =
    val keyCode = mapStringToKeyCode(key)
    player.onKeyReleased(keyCode)

  /** Map string key to KeyCode */
  private def mapStringToKeyCode(key: String): KeyCode =
    key match
      case "W" => KeyCode.W
      case "A" => KeyCode.A
      case "S" => KeyCode.S
      case "D" => KeyCode.D
      case "E" => KeyCode.E
      case "R" => KeyCode.R
      // Arrow keys - handle getName() format (typically returns "Up", "Down", etc.)
      case "Up"    => KeyCode.Up
      case "Down"  => KeyCode.Down
      case "Left"  => KeyCode.Left
      case "Right" => KeyCode.Right
      case _       => KeyCode.Space

  /** Check and update context based on dialogue state */
  private def checkDialogueState(): Unit =
    if !dialogueSystem.isActive && currentContext == InputContext.Dialogue then
      currentContext = InputContext.Normal

  /** Check and update context based on inventory state */
  private def checkInventoryState(): Unit =
    if !farmingInventorySystem.isOpen && currentContext == InputContext.Inventory
    then currentContext = InputContext.Normal

  /** Update context based on current game state */
  def updateContext(): Unit =
    currentContext =
      if dialogueSystem.isActive then InputContext.Dialogue
      else if farmingInventorySystem.isOpen then InputContext.Inventory
      else InputContext.Normal

  /** Get current input context (for debugging) */
  def getCurrentContext: InputContext = currentContext

  /** Check if movement is allowed in current context */
  def isMovementAllowed: Boolean =
    currentContext != InputContext.Inventory && currentContext != InputContext.Paused
