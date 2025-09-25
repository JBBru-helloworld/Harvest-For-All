package harvestforall.gui.managers

import scalafx.scene.image.Image
import scalafx.scene.media.{Media, MediaPlayer}
import scala.collection.mutable.Map
import scala.util.{Try, Success, Failure}
import harvestforall.graphics.CropSpriteManager

/** Asset manager for loading and managing game assets
  *
  * Handles images, sounds, and other resources
  */
object AssetManager:

  private val images: Map[String, Image] = Map.empty
  private val sounds: Map[String, MediaPlayer] = Map.empty
  private var isInitialized: Boolean = false

  // Crop sprite manager
  private val cropSpriteManager = new CropSpriteManager()

  /** Load essential assets required for startup
    */
  def loadEssentialAssets(): Unit =
    try
      println("[AssetManager] Loading essential assets...")

      // Load default placeholder images
      loadDefaultImages()

      // Load UI assets
      loadUIAssets()

      // Initialize crop sprites
      cropSpriteManager.initialize()

      isInitialized = true
      println("[AssetManager] Essential assets loaded successfully")

    catch
      case ex: Exception =>
        println(
          s"[AssetManager] Failed to load essential assets: ${ex.getMessage}"
        )
        // Continue with default assets
        loadDefaultImages()
        isInitialized = true

  /** Load default placeholder images
    */
  private def loadDefaultImages(): Unit =
    // Create simple default images programmatically
    // In a real implementation, you'd load from files
    val defaultImage = new Image(
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
    )

    images("default") = defaultImage
    images("app-icon") = defaultImage
    images("soil_dry") = defaultImage
    images("soil_wet") = defaultImage
    images("plant_seedling") = defaultImage
    images("plant_growing") = defaultImage
    images("plant_mature") = defaultImage

  /** Load UI assets
    */
  private def loadUIAssets(): Unit =
    // Load UI-specific assets
    val defaultImage = images("default")
    images("button_normal") = defaultImage
    images("button_hover") = defaultImage
    images("button_pressed") = defaultImage
    images("background") = defaultImage

  /** Get image by name or path
    */
  def getImage(name: String): Image =
    images.get(name).getOrElse {
      // Try to load image from path if not cached
      val inputStream = getClass.getResourceAsStream(name)
      if inputStream != null then
        val image = new Image(inputStream)
        images(name) = image
        image
      else
        println(s"[AssetManager] Resource not found: $name")
        images.getOrElse("default", createDefaultImage())
    }

  /** Create a default placeholder image
    */
  private def createDefaultImage(): Image =
    new Image(
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
    )

  /** Load image from resources
    */
  def loadImage(name: String, path: String): Boolean =
    Try {
      val inputStream = getClass.getResourceAsStream(path)
      if inputStream != null then
        val image = new Image(inputStream)
        images(name) = image
        true
      else
        println(s"[AssetManager] Resource not found: $path")
        false
    }.getOrElse {
      println(s"[AssetManager] Failed to load image: $name from $path")
      false
    }

  /** Load sound from resources
    */
  def loadSound(name: String, path: String): Boolean =
    Try {
      val resource = getClass.getResource(path)
      if resource != null then
        val media = new Media(resource.toString)
        val mediaPlayer = new MediaPlayer(media)
        sounds(name) = mediaPlayer
        true
      else
        println(s"[AssetManager] Sound resource not found: $path")
        false
    }.getOrElse {
      println(s"[AssetManager] Failed to load sound: $name from $path")
      false
    }

  /** Play sound
    */
  def playSound(name: String): Unit =
    sounds.get(name).foreach(_.play())

  /** Stop sound
    */
  def stopSound(name: String): Unit =
    sounds.get(name).foreach(_.stop())

  /** Check if assets are loaded
    */
  def isLoaded: Boolean = isInitialized

  /** Get all loaded image names
    */
  def getLoadedImages: List[String] = images.keys.toList

  /** Get all loaded sound names
    */
  def getLoadedSounds: List[String] = sounds.keys.toList

  /** Cleanup resources
    */
  def cleanup(): Unit =
    sounds.values.foreach(_.stop())
    sounds.clear()
    images.clear()
    isInitialized = false
    println("[AssetManager] Resources cleaned up")

  // === CROP SPRITE MANAGEMENT ===

  /** Get crop sprite for specific growth stage */
  def getCropSprite(cropType: String, growthStage: Int): Image =
    cropSpriteManager
      .getCropSprite(cropType, growthStage)
      .getOrElse(images.getOrElse("plant_growing", createDefaultImage()))

  /** Get crop icon for UI/inventory */
  def getCropIcon(cropType: String): Image =
    cropSpriteManager
      .getCropIcon(cropType)
      .getOrElse(images.getOrElse("plant_mature", createDefaultImage()))

  /** Get seeds sprite for planting animation */
  def getSeedsSprite(cropType: String): Image =
    cropSpriteManager
      .getSeedsSprite(cropType)
      .getOrElse(images.getOrElse("plant_seedling", createDefaultImage()))

  /** Get sign sprite for farm decoration */
  def getSignSprite(cropType: String): Image =
    cropSpriteManager
      .getSignSprite(cropType)
      .getOrElse(images.getOrElse("default", createDefaultImage()))

  /** Check if crop sprites are available */
  def hasCropSprites(cropType: String): Boolean =
    cropSpriteManager.hasCrop(cropType)

  /** Get all available crop types */
  def getAvailableCropTypes: List[String] =
    cropSpriteManager.getAvailableCrops

end AssetManager
