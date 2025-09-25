package harvestforall.game.ui

import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.image.Image
import scala.util.{Try, Success, Failure}

/** Player life system for farming game Displays heart sprites showing player
  * health
  */
class PlayerLifeSystem:

  // Heart configuration
  private val ICON_SIZE = 32
  private val HEARTS_PER_ROW = 8
  private val HEART_SPACING = 4

  // Heart sprites
  private var heartFull: Option[Image] = None
  private var heartHalf: Option[Image] = None
  private var heartBlank: Option[Image] = None

  // Player stats
  private var maxLife = 6 // Default max life (3 hearts = 6 half-hearts)
  private var currentLife = 6 // Current life

  def initialize(): Unit =
    try
      println("[PlayerLifeSystem] Loading heart sprites...")

      // Load heart sprites from game assets
      heartBlank = Some(
        new Image(
          getClass.getResourceAsStream("/assets/hearts/heart_blank.png")
        )
      )

      heartHalf = Some(
        new Image(
          getClass.getResourceAsStream("/assets/hearts/heart_half.png")
        )
      )

      heartFull = Some(
        new Image(
          getClass.getResourceAsStream("/assets/hearts/heart_full.png")
        )
      )

      println("[PlayerLifeSystem] Heart sprites loaded successfully")

    catch
      case ex: Exception =>
        println(
          s"[PlayerLifeSystem] Failed to load heart sprites: ${ex.getMessage}"
        )
        // Try loading from alternative directory as fallback
        loadFromAlternativeLocation()

  private def loadFromAlternativeLocation(): Unit =
    try
      println(
        "[PlayerLifeSystem] Attempting to load from resources directory..."
      )

      val basePath = "/objects/"

      heartBlank = Try(new Image(basePath + "heart_blank.png")).toOption
      heartHalf = Try(new Image(basePath + "heart_half.png")).toOption
      heartFull = Try(new Image(basePath + "heart_full.png")).toOption

      if heartBlank.isDefined && heartHalf.isDefined && heartFull.isDefined then
        println(
          "[PlayerLifeSystem] Successfully loaded hearts from resources"
        )
      else createFallbackHearts()

    catch
      case ex: Exception =>
        println(
          s"[PlayerLifeSystem] Resource loading failed: ${ex.getMessage}"
        )
        createFallbackHearts()

  private def createFallbackHearts(): Unit =
    println("[PlayerLifeSystem] Creating fallback heart representations...")
    // Note: In a real implementation, you would create simple colored rectangles
    // For now, we'll work with None values and handle gracefully in render

  def setMaxLife(max: Int): Unit =
    maxLife = math.max(2, max) // Minimum 1 heart (2 half-hearts)
    if currentLife > maxLife then currentLife = maxLife
    println(s"[PlayerLifeSystem] Max life set to $maxLife")

  def setCurrentLife(life: Int): Unit =
    currentLife = math.max(0, math.min(life, maxLife))
    println(s"[PlayerLifeSystem] Current life set to $currentLife")

  def takeDamage(damage: Int): Unit =
    currentLife = math.max(0, currentLife - damage)
    println(
      s"[PlayerLifeSystem] Took $damage damage, life now: $currentLife/$maxLife"
    )

  def heal(amount: Int): Unit =
    currentLife = math.min(maxLife, currentLife + amount)
    println(
      s"[PlayerLifeSystem] Healed $amount, life now: $currentLife/$maxLife"
    )

  def isDead: Boolean = currentLife <= 0

  def isFullHealth: Boolean = currentLife >= maxLife

  def render(
      gc: GraphicsContext,
      screenWidth: Double,
      screenHeight: Double
  ): Unit =
    if heartBlank.isEmpty || heartHalf.isEmpty || heartFull.isEmpty then
      renderFallbackHearts(gc, screenWidth, screenHeight)
    else renderHeartSprites(gc, screenWidth, screenHeight)

  private def renderHeartSprites(
      gc: GraphicsContext,
      screenWidth: Double,
      screenHeight: Double
  ): Unit =
    val startX = 20.0
    val startY = 20.0

    var currentX = startX
    var currentY = startY

    // Calculate total hearts needed (maxLife / 2, rounded up)
    val totalHearts = (maxLife + 1) / 2

    for i <- 0 until totalHearts do
      // Check if we need to wrap to next row
      if i > 0 && i % HEARTS_PER_ROW == 0 then
        currentX = startX
        currentY += ICON_SIZE + HEART_SPACING

      // Calculate what type of heart to show
      val heartValue = i * 2 // Each heart represents 2 life points

      if currentLife <= heartValue then
        // Empty heart
        heartBlank.foreach(heart =>
          gc.drawImage(heart, currentX, currentY, ICON_SIZE, ICON_SIZE)
        )
      else if currentLife >= heartValue + 2 then
        // Full heart
        heartFull.foreach(heart =>
          gc.drawImage(heart, currentX, currentY, ICON_SIZE, ICON_SIZE)
        )
      else
        // Half heart (currentLife == heartValue + 1)
        heartBlank.foreach(heart =>
          gc.drawImage(heart, currentX, currentY, ICON_SIZE, ICON_SIZE)
        )
        heartHalf.foreach(heart =>
          gc.drawImage(heart, currentX, currentY, ICON_SIZE, ICON_SIZE)
        )

      currentX += ICON_SIZE + HEART_SPACING

  private def renderFallbackHearts(
      gc: GraphicsContext,
      screenWidth: Double,
      screenHeight: Double
  ): Unit =
    val startX = 20.0
    val startY = 20.0
    val heartSize = 24.0
    val spacing = 4.0

    // Draw simple colored rectangles as heart replacements
    var currentX = startX
    var currentY = startY

    val totalHearts = (maxLife + 1) / 2

    for i <- 0 until totalHearts do
      if i > 0 && i % HEARTS_PER_ROW == 0 then
        currentX = startX
        currentY += heartSize + spacing

      val heartValue = i * 2

      if currentLife <= heartValue then
        // Empty heart - gray border
        gc.stroke = scalafx.scene.paint.Color.Gray
        gc.lineWidth = 2
        gc.strokeRect(currentX, currentY, heartSize, heartSize)
      else if currentLife >= heartValue + 2 then
        // Full heart - red fill
        gc.fill = scalafx.scene.paint.Color.Red
        gc.fillRect(currentX, currentY, heartSize, heartSize)
      else
        // Half heart - half red fill
        gc.fill = scalafx.scene.paint.Color.Gray
        gc.fillRect(currentX, currentY, heartSize, heartSize)
        gc.fill = scalafx.scene.paint.Color.Red
        gc.fillRect(currentX, currentY, heartSize / 2, heartSize)

      currentX += heartSize + spacing

  def getCurrentLife: Int = currentLife
  def getMaxLife: Int = maxLife

  // Helper methods to get heart counts (for display purposes)
  def getCurrentHearts: Int = math.ceil(currentLife.toDouble / 2.0).toInt
  def getMaxHearts: Int = math.ceil(maxLife.toDouble / 2.0).toInt

  // Helper method to get life as percentage
  def getLifePercentage: Double =
    if maxLife > 0 then currentLife.toDouble / maxLife.toDouble else 0.0

end PlayerLifeSystem
