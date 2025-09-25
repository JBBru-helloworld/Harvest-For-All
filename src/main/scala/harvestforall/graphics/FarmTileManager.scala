package harvestforall.graphics

import scalafx.scene.image.Image
import scalafx.scene.canvas.GraphicsContext
import scala.io.Source
import scala.util.{Try, Success, Failure}

/** Farm Tile Manager - Handles tile loading, collision detection, and rendering
  * for the farming game Uses a tile-based architecture for efficient 2D
  * rendering and game logic
  *
  * This is basically the backbone of the whole map system - handles everything
  * from loading tile images to figuring out where the player can walk
  */
class FarmTileManager:

  // Game panel settings for tile-based rendering - updated for full-screen
  val originalTileSize = 16
  val scale = 3
  val tileSize = originalTileSize * scale // 48x48
  val maxScreenCol = 25 // Increased for full-screen width
  val maxScreenRow = 15 // Increased for full-screen height
  val screenWidth = tileSize * maxScreenCol // 1200 pixels
  val screenHeight = tileSize * maxScreenRow // 720 pixels

  // World settings - made it pretty big so players have room to explore
  var maxWorldCol = 50
  var maxWorldRow = 50
  val maxMap = 10
  var currentMap = 0

  /** Tile data structures (converted from Java tutorial but works great - again
    * Ryisnow is a legend) Tutorial was from this playlist:
    * https://youtube.com/playlist?list=PL_QPQmz5C6WUF-pOQDsbsKbaBZqXj4qSq&feature=shared
    */
  var tiles: Array[FarmTile] = Array.empty
  var mapTileNum: Array[Array[Array[Int]]] =
    Array.empty // [map][col][row] - 3D array was confusing at first
  var drawPath = true

  // File data from tiledata.txt - external config makes it easy to add new tiles
  private var fileNames: Array[String] = Array.empty
  private var collisionStatus: Array[String] = Array.empty

  // Player position (will be set externally)
  var playerWorldX: Double = 0
  var playerWorldY: Double = 0
  var playerScreenX: Double = screenWidth / 2 - tileSize / 2
  var playerScreenY: Double = screenHeight / 2 - tileSize / 2

  /** Initialize the tile manager
    */
  def initialize(): Unit =
    println("Initializing Farm Tile Manager...")

    // READ TILE DATA FILE
    readTileDataFile()

    // INITIALIZE THE TILE ARRAY
    tiles = new Array[FarmTile](fileNames.length)
    getTileImages()

    // GET THE maxWorldCol & Row from worldmap
    getWorldDimensions("/assets/maps/worldmap.txt")

    // Initialize map array
    mapTileNum = Array.ofDim[Int](maxMap, maxWorldCol, maxWorldRow)

    // Load maps
    loadMap("/assets/maps/worldmap.txt", 0)

    println(
      s"Farm Tile Manager initialized: ${maxWorldCol}x${maxWorldRow}, ${tiles.length} tiles"
    )

  /** Read tile data file to configure tile properties
    */
  private def readTileDataFile(): Unit =
    try
      val tileDataStream =
        getClass.getResourceAsStream("/assets/maps/tiledata.txt")
      if tileDataStream != null then
        val lines = Source.fromInputStream(tileDataStream).getLines().toArray
        tileDataStream.close()

        // Extract file names and collision status (alternating lines)
        val fileNamesList = scala.collection.mutable.ArrayBuffer[String]()
        val collisionList = scala.collection.mutable.ArrayBuffer[String]()

        var i = 0
        while i < lines.length - 1 do
          fileNamesList += lines(i) // file name
          collisionList += lines(i + 1) // collision status
          i += 2

        fileNames = fileNamesList.toArray
        collisionStatus = collisionList.toArray

        println(s"Read ${fileNames.length} tile definitions from tiledata.txt")
      else
        println("Could not find tiledata.txt, using defaults")
        createDefaultTileData()
    catch
      case e: Exception =>
        println(s"Error reading tiledata.txt: ${e.getMessage}")
        createDefaultTileData()

  /** Create default tile data if file loading fails
    */
  private def createDefaultTileData(): Unit =
    fileNames =
      Array("000.png", "001.png", "002.png", "003.png", "018.png", "024.png")
    collisionStatus = Array("false", "false", "false", "true", "true", "true")

  /** Load and prepare tile images for rendering
    */
  private def getTileImages(): Unit =
    for i <- fileNames.indices do
      val fileName = fileNames(i)
      val collision = collisionStatus(i).toLowerCase == "true"
      setup(i, fileName, collision)

  /** Setup individual tile with image and properties
    */
  private def setup(index: Int, imageName: String, collision: Boolean): Unit =
    try
      tiles(index) = new FarmTile()

      val imagePath = s"/assets/tiles/ryisnow/$imageName"
      val imageStream = getClass.getResourceAsStream(imagePath)

      if imageStream != null then
        tiles(index).image = Some(
          new Image(imageStream, tileSize, tileSize, false, false)
        )
        tiles(index).collision = collision
        imageStream.close()
        println(s"✓ Loaded tile $index: $imageName (collision: $collision)")
      else
        println(s"✗ Could not load tile: $imagePath")
        tiles(index).image = None
        tiles(index).collision = collision
    catch
      case e: Exception =>
        println(s"Error setting up tile $index ($imageName): ${e.getMessage}")
        tiles(index) = new FarmTile()
        tiles(index).collision = collision

  /** Get world dimensions from map file
    */
  private def getWorldDimensions(mapPath: String): Unit =
    try
      val mapStream = getClass.getResourceAsStream(mapPath)
      if mapStream != null then
        val lines = Source.fromInputStream(mapStream).getLines().toArray
        mapStream.close()

        if lines.nonEmpty then
          val firstLine = lines.head
          val maxTile = firstLine.split(" ")

          maxWorldCol = maxTile.length
          maxWorldRow = lines.length

          println(s"World dimensions: ${maxWorldCol}x${maxWorldRow}")
        else
          println("Map file is empty, using default dimensions")
          maxWorldCol = 50
          maxWorldRow = 50
      else
        println(s"Could not find map file: $mapPath")
        maxWorldCol = 50
        maxWorldRow = 50
    catch
      case e: Exception =>
        println(s"Error reading world dimensions: ${e.getMessage}")
        maxWorldCol = 50
        maxWorldRow = 50

  /** Load world map from file
    */
  private def loadMap(filePath: String, map: Int): Unit =
    try
      val mapStream = getClass.getResourceAsStream(filePath)
      if mapStream != null then
        val lines = Source.fromInputStream(mapStream).getLines().toArray
        mapStream.close()

        var col = 0
        var row = 0

        for line <- lines if row < maxWorldRow do
          col = 0
          val numbers = line.split(" ")

          for numStr <- numbers if col < maxWorldCol do
            val num = numStr.trim.toInt
            mapTileNum(map)(col)(row) = num
            col += 1

          row += 1

        println(s"Loaded map $map from $filePath")
      else println(s"Could not load map: $filePath")
    catch
      case e: Exception =>
        println(s"Error loading map $filePath: ${e.getMessage}")
        e.printStackTrace()

  /** Update player position
    */
  def updatePlayerPosition(worldX: Double, worldY: Double): Unit =
    playerWorldX = worldX
    playerWorldY = worldY

  /** Render tiles and crops to the graphics context */
  def draw(
      gc: GraphicsContext,
      farmingSystem: harvestforall.game.systems.InteractiveFarmingSystem,
      cropSpriteManager: harvestforall.graphics.CropSpriteManager
  ): Unit =
    var worldCol = 0
    var worldRow = 0

    while worldCol < maxWorldCol && worldRow < maxWorldRow do
      val tileNum = mapTileNum(currentMap)(worldCol)(worldRow)
      val worldX = worldCol * tileSize
      val worldY = worldRow * tileSize
      val screenX = worldX - playerWorldX + playerScreenX
      val screenY = worldY - playerWorldY + playerScreenY

      // Only draw tiles that are visible on screen for optimization
      if worldX + tileSize > playerWorldX - playerScreenX &&
        worldX - tileSize < playerWorldX + playerScreenX &&
        worldY + tileSize > playerWorldY - playerScreenY &&
        worldY - tileSize < playerWorldY + playerScreenY
      then
        if tileNum >= 0 && tileNum < tiles.length then
          tiles(tileNum).image match
            case Some(image) =>
              gc.drawImage(image, screenX, screenY)
            case None =>
              renderTileFallback(gc, screenX, screenY, tileNum)
        else renderTileFallback(gc, screenX, screenY, -1)

        // Draw crop if present on this tile using farmingSystem.getCropRenderInfo
        farmingSystem.getCropRenderInfo(worldCol, worldRow) match
          case Some((cropType, stage, _, waterLevel)) =>
            cropSpriteManager.getCropSprite(cropType, stage).foreach { sprite =>
              val spriteSize = tileSize * 0.9
              val spriteX = screenX + (tileSize - spriteSize) / 2
              val spriteY = screenY + (tileSize - spriteSize) / 2
              gc.drawImage(sprite, spriteX, spriteY, spriteSize, spriteSize)

              // Draw water overlay if crop is watered
              if waterLevel > 0 then
                val originalGlobalAlpha = gc.globalAlpha
                gc.globalAlpha = 0.3 // Semi-transparent overlay
                gc.fill = scalafx.scene.paint.Color.CornflowerBlue
                val overlaySize = spriteSize * 0.8
                val overlayX = screenX + (tileSize - overlaySize) / 2
                val overlayY = screenY + (tileSize - overlaySize) / 2
                gc.fillRect(overlayX, overlayY, overlaySize, overlaySize)
                gc.globalAlpha = originalGlobalAlpha // Restore original alpha
            }
          case None => ()

      worldCol += 1
      if worldCol == maxWorldCol then
        worldCol = 0
        worldRow += 1

  /** Render fallback colored tile if image is missing
    */
  private def renderTileFallback(
      gc: GraphicsContext,
      screenX: Double,
      screenY: Double,
      tileNum: Int
  ): Unit =
    tileNum match
      case 0  => gc.fill = scalafx.scene.paint.Color.LightGreen // grass
      case 1  => gc.fill = scalafx.scene.paint.Color.SaddleBrown // dirt
      case 2  => gc.fill = scalafx.scene.paint.Color.Blue // water
      case 3  => gc.fill = scalafx.scene.paint.Color.Gray // stone
      case 18 => gc.fill = scalafx.scene.paint.Color.DarkGreen // trees
      case 24 => gc.fill = scalafx.scene.paint.Color.Brown // wall
      case _  => gc.fill = scalafx.scene.paint.Color.Magenta // unknown

    gc.fillRect(screenX, screenY, tileSize, tileSize)

    // Add subtle border
    gc.stroke = scalafx.scene.paint.Color.Black
    gc.lineWidth = 1
    gc.strokeRect(screenX, screenY, tileSize, tileSize)

  /** Check collision at world coordinates
    */
  def checkCollision(worldX: Double, worldY: Double): Boolean =
    val col = (worldX / tileSize).toInt
    val row = (worldY / tileSize).toInt

    if row >= 0 && row < maxWorldRow && col >= 0 && col < maxWorldCol then
      val tileNum = mapTileNum(currentMap)(col)(row)
      if tileNum >= 0 && tileNum < tiles.length then tiles(tileNum).collision
      else false
    else true // Out of bounds = collision

  /** Check if a specific tile type exists at world coordinates
    */
  def getTileTypeAt(worldX: Double, worldY: Double): Int =
    val col = (worldX / tileSize).toInt
    val row = (worldY / tileSize).toInt

    if row >= 0 && row < maxWorldRow && col >= 0 && col < maxWorldCol then
      mapTileNum(currentMap)(col)(row)
    else -1 // Out of bounds

  /** Check if player is near a village tile (tile 33) Returns true if any of
    * the tiles around the player position contain a village home
    */
  def isNearVillage(
      playerWorldX: Double,
      playerWorldY: Double,
      proximityRange: Int = 2
  ): Boolean =
    val playerCol = (playerWorldX / tileSize).toInt
    val playerRow = (playerWorldY / tileSize).toInt

    // Check in a square around the player position
    (for
      col <- (playerCol - proximityRange) to (playerCol + proximityRange)
      row <- (playerRow - proximityRange) to (playerRow + proximityRange)
      if row >= 0 && row < maxWorldRow && col >= 0 && col < maxWorldCol
    yield mapTileNum(currentMap)(col)(row) == 33).exists(identity)

/** Tile class for farming game
  */
class FarmTile:
  var image: Option[Image] = None
  var collision: Boolean = false
