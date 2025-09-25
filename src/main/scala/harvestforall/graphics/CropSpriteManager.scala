package harvestforall.graphics

import scalafx.scene.image.{Image, WritableImage}
import scala.collection.mutable.Map

/** Manages the Mana Seed Farming Crops sprite sheets
  *
  * Based on the crop reference documentation:
  *   - Sheet dimensions: 144x512 pixels
  *   - Sprite size: 16x32 pixels
  *   - 9 columns x 16 rows per sheet
  *   - Column layout: icon, seedbag, seeds, stage1-5, sign_icon, sign
  *
  * This was a pain to get right - had to figure out the sprite layout by trial
  * and error!
  */
class CropSpriteManager:

  // Sprite sheet dimensions - these are fixed by the asset pack
  private val SPRITE_WIDTH = 16
  private val SPRITE_HEIGHT = 32
  private val COLUMNS_PER_ROW = 9

  // Column indices for different sprite types (took forever to map these correctly)
  private val CROP_ICON_COL = 0
  private val SEEDBAG_COL = 1
  private val SEEDS_COL = 2
  private val GROWTH_START_COL = 3 // Stages 1-5 are columns 3-7
  private val SIGN_ICON_COL = 8
  private val SIGN_COL = 9

  // Loaded sprite sheets
  private var spriteSheetA: Option[Image] = None
  private var spriteSheetB: Option[Image] = None
  private var spriteSheetC: Option[Image] = None

  // Cached extracted sprites - using mutable Map for performance
  private val cropSprites: Map[String, Array[Image]] = Map.empty
  private val cropIcons: Map[String, Image] = Map.empty
  private val seedSprites: Map[String, Image] = Map.empty
  private val signSprites: Map[String, Image] = Map.empty
  private val signIcons: Map[String, Image] = Map.empty

  /** Load all crop sprite sheets */
  def initialize(): Unit =
    try
      println("[CropSpriteManager] Loading crop sprite sheets...")

      spriteSheetA = Some(
        new Image(
          getClass.getResourceAsStream(
            "/assets/crops/20.02a - Farming Crops #1 3.0/farming crops 1-A 16x32.png"
          )
        )
      )
      spriteSheetB = Some(
        new Image(
          getClass.getResourceAsStream(
            "/assets/crops/20.02a - Farming Crops #1 3.0/farming crops 1-B 16x32.png"
          )
        )
      )
      spriteSheetC = Some(
        new Image(
          getClass.getResourceAsStream(
            "/assets/crops/20.02a - Farming Crops #1 3.0/farming crops 1-C 16x32.png"
          )
        )
      )

      // Extract all crop sprites
      extractAllCropSprites()

      println(
        s"[CropSpriteManager] Loaded ${cropSprites.size} crop types successfully"
      )

    catch
      case ex: Exception =>
        println(
          s"[CropSpriteManager] Failed to load sprite sheets: ${ex.getMessage}"
        )

  /** Extract a single sprite from a sheet */
  private def extractSprite(sheet: Image, col: Int, row: Int): Image =
    val x = col * SPRITE_WIDTH
    val y = row * SPRITE_HEIGHT

    val pixelReader = sheet.pixelReader.get
    val writableImage = new WritableImage(SPRITE_WIDTH, SPRITE_HEIGHT)
    val pixelWriter = writableImage.pixelWriter

    for {
      i <- 0 until SPRITE_WIDTH
      j <- 0 until SPRITE_HEIGHT
    } {
      val color = pixelReader.getColor(x + i, y + j)
      pixelWriter.setColor(i, j, color)
    }
    writableImage

  /** Extract all crop data from sprite sheets */
  private def extractAllCropSprites(): Unit =
    // Extract from Sheet A (main crops)
    extractCropFromSheet(spriteSheetA.get, "beetroot", 0)
    extractCropFromSheet(spriteSheetA.get, "cabbage", 1)
    extractCropFromSheet(spriteSheetA.get, "carrot", 2)
    extractCropFromSheet(spriteSheetA.get, "corn", 3)
    extractCropFromSheet(spriteSheetA.get, "onion", 4)
    extractCropFromSheet(spriteSheetA.get, "peas", 6)
    extractCropFromSheet(spriteSheetA.get, "beans", 7)
    extractCropFromSheet(spriteSheetA.get, "tomato", 8)
    extractCropFromSheet(spriteSheetA.get, "wheat", 9)
    extractCropFromSheet(spriteSheetA.get, "cucumber", 10)
    extractCropFromSheet(spriteSheetA.get, "spinach", 11)
    extractCropFromSheet(spriteSheetA.get, "strawberry", 12)
    extractCropFromSheet(spriteSheetA.get, "grapes", 13)
    extractCropFromSheet(spriteSheetA.get, "pumpkin", 14)
    extractCropFromSheet(spriteSheetA.get, "broccoli", 15)

    // Extract from Sheet C (additional grains)
    spriteSheetC.foreach { sheet =>
      extractCropFromSheet(sheet, "barley", 0)
      extractCropFromSheet(sheet, "rye", 1)
    }

  /** Extract all sprites for a single crop type */
  private def extractCropFromSheet(
      sheet: Image,
      cropName: String,
      row: Int
  ): Unit =
    try
      // Extract crop icon
      cropIcons(cropName) = extractSprite(sheet, CROP_ICON_COL, row)

      // Extract seeds sprite
      seedSprites(cropName) = extractSprite(sheet, SEEDS_COL, row)

      // Extract growth stages (5 stages: columns 3-7)
      val growthStages = Array.ofDim[Image](5)
      for stage <- 0 until 5 do
        growthStages(stage) =
          extractSprite(sheet, GROWTH_START_COL + stage, row)
      cropSprites(cropName) = growthStages

      // Extract sign sprites
      signIcons(cropName) = extractSprite(sheet, SIGN_ICON_COL, row)
      signSprites(cropName) = extractSprite(sheet, SIGN_COL, row)

    catch
      case ex: Exception =>
        println(
          s"[CropSpriteManager] Failed to extract $cropName: ${ex.getMessage}"
        )

  /** Get crop sprite for specific growth stage (0-4) */
  def getCropSprite(cropType: String, growthStage: Int): Option[Image] =
    cropSprites.get(cropType.toLowerCase).flatMap { stages =>
      if growthStage >= 0 && growthStage < stages.length then
        Some(stages(growthStage))
      else stages.headOption // Default to first stage if invalid
    }

  /** Get crop icon for inventory/UI */
  def getCropIcon(cropType: String): Option[Image] =
    cropIcons.get(cropType.toLowerCase)

  /** Get seeds sprite for planting */
  def getSeedsSprite(cropType: String): Option[Image] =
    seedSprites.get(cropType.toLowerCase)

  /** Get sign sprite for farm decoration */
  def getSignSprite(cropType: String): Option[Image] =
    signSprites.get(cropType.toLowerCase)

  /** Get sign icon for inventory */
  def getSignIcon(cropType: String): Option[Image] =
    signIcons.get(cropType.toLowerCase)

  /** Get all growth stages for a crop */
  def getAllGrowthStages(cropType: String): Array[Image] =
    cropSprites.getOrElse(cropType.toLowerCase, Array.empty)

  /** Check if a crop type is available */
  def hasCrop(cropType: String): Boolean =
    cropSprites.contains(cropType.toLowerCase)

  /** Get all available crop names */
  def getAvailableCrops: List[String] =
    cropSprites.keys.toList.sorted

  /** Map game plant types to sprite names */
  def mapPlantTypeToSprite(plantType: String): String =
    plantType.toLowerCase match
      case "wheat"     => "wheat"
      case "rice"      => "wheat" // Use wheat as substitute for rice
      case "corn"      => "corn"
      case "soybean"   => "beans"
      case "lentils"   => "peas" // Use peas as substitute for lentils
      case "chickpeas" => "peas" // Use peas as substitute for chickpeas
      case "spinach"   => "spinach"
      case "tomato"    => "tomato"
      case "carrot"    => "carrot"
      case _           => "wheat" // Default fallback

end CropSpriteManager
