package harvestforall.graphics

import scalafx.scene.image.Image
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.input.KeyCode
import scala.collection.mutable

/** Farm Player - Handles player movement, collision, and rendering Handles
  * player movement, animation, and collision detection for the farming game
  */
class FarmPlayer(private val tileManager: FarmTileManager):

  // Game panel settings - updated for full-screen
  val originalTileSize = 16
  val scale = 3
  val tileSize = originalTileSize * scale // 48x48
  val maxScreenCol = 25 // Increased for full-screen width
  val maxScreenRow = 15 // Increased for full-screen height
  val screenWidth = tileSize * maxScreenCol // 1200 pixels
  val screenHeight = tileSize * maxScreenRow // 720 pixels

  // Player position and settings
  var worldX: Double = tileSize * 23
  var worldY: Double = tileSize * 21
  var speed: Double = 4

  // Solid area (hitbox) for collision detection
  // Had to tweak these values a bunch to get collisions feeling right
  var solidAreaDefaultX = 8
  var solidAreaDefaultY = 16
  var solidAreaX = solidAreaDefaultX
  var solidAreaY = solidAreaDefaultY
  var solidAreaWidth = 32
  var solidAreaHeight = 32

  // Player screen position - keeps player centered
  val screenX: Double = screenWidth / 2 - tileSize / 2
  val screenY: Double = screenHeight / 2 - tileSize / 2

  // Player sprites for animation - we got 2 frames per direction for walkin'
  private var up1: Option[Image] = None
  private var up2: Option[Image] = None
  private var down1: Option[Image] = None
  private var down2: Option[Image] = None
  private var left1: Option[Image] = None
  private var left2: Option[Image] = None
  private var right1: Option[Image] = None
  private var right2: Option[Image] = None

  // Animation and direction
  var direction: String = "down"
  var spriteCounter: Int = 0
  var spriteNum: Int = 1

  // Key handler with failsafe mechanism
  // I added this because sometimes the up arrow key would get "stuck" - the press event
  // would fire but the release event wouldn't, so the player would keep moving up forever!
  // Now I track when each key was pressed and auto-release any key that's been held too long
  private val pressedKeys = mutable.Set[KeyCode]()
  private val keyPressTime = mutable.Map[KeyCode, Long]()
  private val maxKeyHoldTime = 5000 // 5 seconds max hold time as failsafe

  /** Initialize the player (equivalent to constructor in Java tutorial that I
    * followed on YouTube - thank you Ryisnow you are a legend)
    */
  def initialize(): Unit =
    println("Initializing Farm Player...")

    // Get player images
    getPlayerImage()

    // Update tile manager with initial position
    tileManager.updatePlayerPosition(worldX, worldY)

    println(s"Farm Player initialized at ($worldX, $worldY)")

  /** Load player sprite images
    */
  private def getPlayerImage(): Unit =
    try
      up1 = setup("boy_up_1")
      up2 = setup("boy_up_2")
      down1 = setup("boy_down_1")
      down2 = setup("boy_down_2")
      left1 = setup("boy_left_1")
      left2 = setup("boy_left_2")
      right1 = setup("boy_right_1")
      right2 = setup("boy_right_2")

      println("✓ All player sprites loaded successfully")
    catch
      case e: Exception =>
        println(s"Error loading player sprites: ${e.getMessage}")

  /** Setup individual sprite with image
    */
  private def setup(imageName: String): Option[Image] =
    try
      val imagePath = s"/assets/sprites/walking/$imageName.png"
      val imageStream = getClass.getResourceAsStream(imagePath)

      if imageStream != null then
        val image = new Image(imageStream, tileSize, tileSize, false, false)
        imageStream.close()
        println(s"✓ Loaded player sprite: $imageName")
        Some(image)
      else
        println(s"✗ Could not load player sprite: $imagePath")
        None
    catch
      case e: Exception =>
        println(s"Error setting up sprite $imageName: ${e.getMessage}")
        None

  /** Key press handler - now with timestamp tracking
    */
  def onKeyPressed(keyCode: KeyCode): Unit =
    pressedKeys += keyCode
    keyPressTime(keyCode) =
      System.currentTimeMillis() // Remember when this key was pressed

  /** Key release handler - cleans up our tracking
    */
  def onKeyReleased(keyCode: KeyCode): Unit =
    pressedKeys -= keyCode
    keyPressTime.remove(keyCode) // Stop tracking this key since it's released

  /** Update player position and animation
    */
  def update(): Unit =
    // This is my fix for the "sticky key" bug - sometimes JavaFX doesn't send the key release event
    // So I check if any key has been "pressed" for more than 5 seconds and auto-release it
    // It's like having a safety net - no key should realistically be held that long during normal play
    val currentTime = System.currentTimeMillis()
    val stuckKeys = keyPressTime
      .filter { case (_, pressTime) =>
        currentTime - pressTime > maxKeyHoldTime
      }
      .keys
      .toSet

    stuckKeys.foreach { key =>
      pressedKeys -= key
      keyPressTime.remove(key)
    }

    // Only move and animate if keys are pressed
    if pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.A) ||
      pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.D) ||
      pressedKeys.contains(KeyCode.Up) || pressedKeys.contains(KeyCode.Down) ||
      pressedKeys.contains(KeyCode.Left) || pressedKeys.contains(KeyCode.Right)
    then

      // Determine direction based on input (support both WASD and arrow keys)
      if pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.Up)
      then direction = "up"
      else if pressedKeys.contains(KeyCode.S) || pressedKeys.contains(
          KeyCode.Down
        )
      then direction = "down"
      else if pressedKeys.contains(KeyCode.A) || pressedKeys.contains(
          KeyCode.Left
        )
      then direction = "left"
      else if pressedKeys.contains(KeyCode.D) || pressedKeys.contains(
          KeyCode.Right
        )
      then direction = "right"

      // CHECK TILE COLLISION
      var collisionOn = false

      // Calculate the tile position we're trying to move to
      val nextWorldX = direction match
        case "left"  => worldX - speed
        case "right" => worldX + speed
        case _       => worldX

      val nextWorldY = direction match
        case "up"   => worldY - speed
        case "down" => worldY + speed
        case _      => worldY

      // Check collision at the intended position
      val entityLeftWorldX = nextWorldX + solidAreaX
      val entityRightWorldX = nextWorldX + solidAreaX + solidAreaWidth
      val entityTopWorldY = nextWorldY + solidAreaY
      val entityBottomWorldY = nextWorldY + solidAreaY + solidAreaHeight

      val entityLeftCol = (entityLeftWorldX / tileSize).toInt
      val entityRightCol = (entityRightWorldX / tileSize).toInt
      val entityTopRow = (entityTopWorldY / tileSize).toInt
      val entityBottomRow = (entityBottomWorldY / tileSize).toInt

      direction match
        case "up" =>
          if tileManager.checkCollision(
              entityLeftCol * tileSize,
              entityTopRow * tileSize
            ) ||
            tileManager.checkCollision(
              entityRightCol * tileSize,
              entityTopRow * tileSize
            )
          then collisionOn = true
        case "down" =>
          if tileManager.checkCollision(
              entityLeftCol * tileSize,
              entityBottomRow * tileSize
            ) ||
            tileManager.checkCollision(
              entityRightCol * tileSize,
              entityBottomRow * tileSize
            )
          then collisionOn = true
        case "left" =>
          if tileManager.checkCollision(
              entityLeftCol * tileSize,
              entityTopRow * tileSize
            ) ||
            tileManager.checkCollision(
              entityLeftCol * tileSize,
              entityBottomRow * tileSize
            )
          then collisionOn = true
        case "right" =>
          if tileManager.checkCollision(
              entityRightCol * tileSize,
              entityTopRow * tileSize
            ) ||
            tileManager.checkCollision(
              entityRightCol * tileSize,
              entityBottomRow * tileSize
            )
          then collisionOn = true

      // IF COLLISION IS FALSE, PLAYER CAN MOVE
      if !collisionOn then
        direction match
          case "up"    => worldY -= speed
          case "down"  => worldY += speed
          case "left"  => worldX -= speed
          case "right" => worldX += speed

        // Update tile manager with new position
        tileManager.updatePlayerPosition(worldX, worldY)

      // Sprite animation
      spriteCounter += 1
      if spriteCounter > 12 then
        spriteNum = if spriteNum == 1 then 2 else 1
        spriteCounter = 0

  /** Render player to screen
    */
  def draw(gc: GraphicsContext): Unit =
    var image: Option[Image] = None

    // Select the appropriate sprite based on direction and animation frame
    direction match
      case "up" =>
        image = if spriteNum == 1 then up1 else up2
      case "down" =>
        image = if spriteNum == 1 then down1 else down2
      case "left" =>
        image = if spriteNum == 1 then left1 else left2
      case "right" =>
        image = if spriteNum == 1 then right1 else right2

    // Draw the sprite or fallback
    image match
      case Some(img) =>
        gc.drawImage(img, screenX, screenY)
      case None =>
        // Fallback colored rectangle (like a classic RPG character)
        gc.fill = scalafx.scene.paint.Color.Blue
        gc.fillRect(screenX, screenY, tileSize, tileSize)

        // Add simple directional indicator
        gc.fill = scalafx.scene.paint.Color.White
        direction match
          case "up"    => gc.fillRect(screenX + 20, screenY + 5, 8, 15)
          case "down"  => gc.fillRect(screenX + 20, screenY + 28, 8, 15)
          case "left"  => gc.fillRect(screenX + 5, screenY + 20, 15, 8)
          case "right" => gc.fillRect(screenX + 28, screenY + 20, 15, 8)

  /** Get current world coordinates
    */
  def getWorldX: Double = worldX
  def getWorldY: Double = worldY

  /** Set world position (for debugging or teleporting)
    */
  def setWorldPosition(x: Double, y: Double): Unit =
    worldX = x
    worldY = y
    tileManager.updatePlayerPosition(worldX, worldY)
