package harvestforall.game.ui

import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.scene.image.Image
import scala.collection.mutable.ArrayBuffer
import harvestforall.gui.utils.FontManager

/** Dialogue system for farming gameplay - Manages character-by-character text
  * animation and farming gameplay instructions
  *
  * This handles both the tutorial messages AND acts as the pause screen too!
  * The typewriter effect was surprisingly tricky to get feeling right
  */
class DialogueSystem:

  // Dialogue configuration - had to play with these sizes a lot
  private val SUB_WINDOW_WIDTH = 600
  private val SUB_WINDOW_HEIGHT = 140
  private var dialogueFont: Font = _

  // Character animation - the typewriter effect that makes it feel retro
  private var combinedText = ""
  private var charIndex = 0
  private var counter = 0
  private val charAnimationSpeed = 3 // Lower = faster typing

  // Dialogue state tracking
  private var currentDialogueSet = 0
  private var currentDialogueIndex = 0
  private var dialogueEndIndex = 0
  private var isDialogueActive = false

  // Farming tutorial dialogues - wrote these to be actually helpful
  private val dialogues = Array(
    // Tutorial dialogue set (0)
    Array(
      "Welcome to Harvest for All!",
      "This is a farming simulation game where\nyou grow crops to feed the world.",
      "Use WASD or Arrow Keys to move your\ncharacter around the farm areas.",
      "Press E to plant seeds when standing\non empty soil with seeds in inventory.",
      "After planting, press R to water your\ncrops so they can grow properly.",
      "Once crops are fully grown (you'll see\nthem change appearance), press E to harvest.",
      "Press I to open your inventory and\nview seeds, tools, and harvested crops.",
      "Watch your crops grow through different\nstages! Water overlay shows watered plots.",
      "Your goal is to maximize food production\nto help feed as many people as possible.",
      "Press C anytime for controls help!\nGood luck, farmer!"
    ),

    // Controls reminder dialogue set (1)
    Array(
      "Game Controls:",
      "WASD or Arrow Keys - Move character",
      "E - Plant seeds or Harvest crops\n(context-sensitive)",
      "R - Water crops with watering can",
      "I - Open/Close inventory system",
      "T - View tutorial messages",
      "C - Show this controls help",
      "H - Get farming tips and advice",
      "P or ESC - Pause game",
      "ENTER - Enter village (when near road)",
      "ENTER/SPACE - Continue dialogue"
    ),

    // Tips dialogue set (2)
    Array(
      "Farming Tips:",
      "Different crops have different growth stages.\nWatch them progress from seeds to harvest!",
      "Wheat grows quickly and is perfect\nfor beginners.",
      "Corn takes longer but produces more food\nper harvest.",
      "Plan your farm layout efficiently\nto maximize production!"
    )
  )

  def initialize(): Unit =
    try
      // Load custom pixel art font
      dialogueFont = FontManager.subHeaderFont // 20pt upheaval font
      println("[DialogueSystem] Initialized successfully with custom font")
    catch
      case ex: Exception =>
        println(s"[DialogueSystem] Error initializing: ${ex.getMessage}")
        dialogueFont = FontManager.getCustomFont(20) // Fallback font

  def startDialogue(dialogueSet: Int): Unit =
    if dialogueSet >= 0 && dialogueSet < dialogues.length then
      currentDialogueSet = dialogueSet
      currentDialogueIndex = 0
      dialogueEndIndex = dialogues(dialogueSet).length - 1
      isDialogueActive = true

      // Reset character animation
      combinedText = ""
      charIndex = 0
      counter = 0

      println(s"[DialogueSystem] Started dialogue set $dialogueSet")
    else println(s"[DialogueSystem] Invalid dialogue set: $dialogueSet")

  def handleInput(key: String): Unit =
    if isDialogueActive then
      key match
        case "ENTER" | "Enter" | "Return" | "SPACE" | "Space" | " " =>
          if charIndex < getCurrentDialogue().length then
            // Skip animation, show full text
            charIndex = getCurrentDialogue().length
          else
          // Move to next dialogue
          if currentDialogueIndex < dialogueEndIndex then
            currentDialogueIndex += 1
            charIndex = 0
            combinedText = ""
            counter = 0
          else
            // End dialogue
            isDialogueActive = false
            println("[DialogueSystem] Dialogue ended")
        case _ => // Ignore other keys during dialogue
  def update(): Unit =
    if isDialogueActive then
      val currentText = getCurrentDialogue()

      if charIndex < currentText.length then
        counter += 1
        if counter >= charAnimationSpeed then
          charIndex += 1
          counter = 0

          // Update combined text for rendering
          combinedText = currentText.substring(0, charIndex)

  def render(
      gc: GraphicsContext,
      screenWidth: Double,
      screenHeight: Double
  ): Unit =
    if isDialogueActive then drawDialogueWindow(gc, screenWidth, screenHeight)

  private def drawDialogueWindow(
      gc: GraphicsContext,
      screenWidth: Double,
      screenHeight: Double
  ): Unit =
    // Calculate window position (moved higher from bottom of screen)
    val x = (screenWidth - SUB_WINDOW_WIDTH) / 2
    val y =
      screenHeight - SUB_WINDOW_HEIGHT - 80 // Increased from 20 to 80 pixels

    // Draw dialogue background window
    gc.fill = Color.Black
    gc.fillRect(x, y, SUB_WINDOW_WIDTH, SUB_WINDOW_HEIGHT)

    // Draw border
    gc.stroke = Color.White
    gc.lineWidth = 3
    gc.strokeRect(x, y, SUB_WINDOW_WIDTH, SUB_WINDOW_HEIGHT)

    // Draw text
    gc.fill = Color.White
    gc.font = dialogueFont

    // Split text by newlines and render each line
    val lines = combinedText.split("\n")
    val lineHeight = 25
    val textStartY = y + 40

    for (i <- lines.indices) do
      gc.fillText(lines(i), x + 20, textStartY + (i * lineHeight))

    // Draw progression indicator
    if charIndex >= getCurrentDialogue().length then
      gc.fillText(
        "Press ENTER to continue...",
        x + 20,
        y + SUB_WINDOW_HEIGHT - 20
      )

  private def getCurrentDialogue(): String =
    if isDialogueActive && currentDialogueSet < dialogues.length &&
      currentDialogueIndex < dialogues(currentDialogueSet).length
    then dialogues(currentDialogueSet)(currentDialogueIndex)
    else ""

  def isActive: Boolean = isDialogueActive

  def forceEnd(): Unit =
    isDialogueActive = false
    println("[DialogueSystem] Dialogue force ended")

end DialogueSystem
