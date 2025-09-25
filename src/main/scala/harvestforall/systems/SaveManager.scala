package harvestforall.systems

import harvestforall.core._
import harvestforall.graphics.FarmPlayer
import harvestforall.game.ui.FarmingInventorySystem
import harvestforall.game.systems.InteractiveFarmingSystem
import java.io.{File, FileWriter, BufferedWriter}
import java.nio.file.{Files, Paths, Path}
import scala.io.Source
import scala.util.{Try, Success, Failure}
import play.api.libs.json.*

class SaveManager {

  // Figuring out where to put save files was trickier than expected lol
  // Need to handle both running from project root AND parent directory
  private val SAVE_DIRECTORY = {
    val currentDir = System.getProperty("user.dir")
    val projectDir = new File(currentDir, "final-project-JBBru-helloworld")

    // Always use the saves directory inside the project folder
    if (projectDir.exists() && projectDir.isDirectory) {
      // We're running from parent directory - use project-relative path
      new File(projectDir, "saves").getAbsolutePath
    } else {
      // We're already in project directory - use local saves
      new File(currentDir, "saves").getAbsolutePath
    }
  }
  private val SAVE_EXTENSION = ".json"

  ensureSaveDirectoryExists()

  // JSON formatters - Play's JSON library makes this pretty painless
  // These implicit vals let Scala automatically serialize/deserialize our case classes
  implicit val playerSaveDataFormat: Format[PlayerSaveData] =
    Json.format[PlayerSaveData]
  implicit val farmPlotSaveDataFormat: Format[FarmPlotSaveData] =
    Json.format[FarmPlotSaveData]
  implicit val inventorySaveDataFormat: Format[InventorySaveData] =
    Json.format[InventorySaveData]
  implicit val gameStatsSaveDataFormat: Format[GameStatsSaveData] =
    Json.format[GameStatsSaveData]
  implicit val gameSaveDataFormat: Format[GameSaveData] =
    Json.format[GameSaveData]

  def saveGame(
      saveName: String,
      gameState: GameState,
      player: FarmPlayer,
      farmingSystem: InteractiveFarmingSystem,
      inventorySystem: FarmingInventorySystem
  ): Try[String] = {
    try {
      println(s"[SaveManager] Starting save process for: $saveName")

      val playerData = PlayerSaveData(
        worldX = player.worldX.toInt,
        worldY = player.worldY.toInt,
        currentHealth = 6,
        maxHealth = 6
      )

      val farmPlots = extractFarmPlotData(farmingSystem)
      val inventoryData = extractInventoryData(inventorySystem)

      val saveData = GameSaveData.fromGameState(
        saveName,
        gameState,
        playerData,
        farmPlots,
        inventoryData
      )

      val json = Json.prettyPrint(Json.toJson(saveData))
      val fileName = saveData.getFileName
      val filePath = Paths.get(SAVE_DIRECTORY, fileName)

      val writer = new BufferedWriter(new FileWriter(filePath.toFile))
      try {
        writer.write(json)
        writer.flush()
        println(s"[SaveManager] Successfully saved game to: $filePath")
        Success(filePath.toString)
      } finally {
        writer.close()
      }

    } catch {
      case ex: Exception =>
        println(s"[SaveManager] Error saving game: ${ex.getMessage}")
        ex.printStackTrace()
        Failure(ex)
    }
  }

  def loadGame(fileName: String): Try[GameSaveData] = {
    try {
      val filePath = Paths.get(SAVE_DIRECTORY, fileName)

      if (!Files.exists(filePath)) {
        return Failure(new RuntimeException(s"Save file not found: $fileName"))
      }

      println(s"[SaveManager] Loading game from: $filePath")

      val source = Source.fromFile(filePath.toFile, "UTF-8")
      val jsonContent =
        try source.mkString
        finally source.close()

      val json = Json.parse(jsonContent)
      val saveData = json.as[GameSaveData]

      println(s"[SaveManager] Successfully loaded game: ${saveData.saveName}")
      Success(saveData)

    } catch {
      case ex: Exception =>
        println(s"[SaveManager] Error loading game: ${ex.getMessage}")
        ex.printStackTrace()
        Failure(ex)
    }
  }

  def getAvailableSaves: List[GameSaveData] = {
    try {
      val saveDir = new File(SAVE_DIRECTORY)
      if (!saveDir.exists()) return List.empty

      saveDir
        .listFiles()
        .filter(_.getName.endsWith(SAVE_EXTENSION))
        .flatMap { file =>
          loadGame(file.getName) match {
            case Success(saveData) => Some(saveData)
            case Failure(ex) =>
              println(
                s"[SaveManager] Failed to load save metadata for ${file.getName}: ${ex.getMessage}"
              )
              None
          }
        }
        .sortBy(_.saveDate)(Ordering.String.reverse)
        .toList

    } catch {
      case ex: Exception =>
        println(
          s"[SaveManager] Error getting available saves: ${ex.getMessage}"
        )
        List.empty
    }
  }

  def deleteSave(fileName: String): Try[Unit] = {
    try {
      val filePath = Paths.get(SAVE_DIRECTORY, fileName)
      if (Files.exists(filePath)) {
        Files.delete(filePath)
        println(s"[SaveManager] Deleted save file: $fileName")
        Success(())
      } else {
        Failure(new RuntimeException(s"Save file not found: $fileName"))
      }
    } catch {
      case ex: Exception =>
        println(s"[SaveManager] Error deleting save: ${ex.getMessage}")
        Failure(ex)
    }
  }

  def saveExists(saveName: String): Boolean = {
    val fileName = s"${saveName.replaceAll("[^a-zA-Z0-9\\-_]", "_")}.json"
    Files.exists(Paths.get(SAVE_DIRECTORY, fileName))
  }

  private def extractFarmPlotData(
      farmingSystem: InteractiveFarmingSystem
  ): Map[String, FarmPlotSaveData] = {
    println("[SaveManager] Extracting farm plot data...")

    val plotStatuses = farmingSystem.getAllPlotStatuses
    println(s"[SaveManager] Found ${plotStatuses.size} farm plots:")
    plotStatuses.foreach { case (pos, status) =>
      println(s"[SaveManager]   Plot $pos: $status")
    }

    plotStatuses.map { case (positionStr, statusStr) =>
      val positionKey = positionStr // Use the position string as key

      val farmPlotData = if (statusStr == "Empty") {
        FarmPlotSaveData(None, 0, 0, 0L, false)
      } else if (statusStr.endsWith("READY!")) {
        val cropType = statusStr.replace(" READY!", "")
        FarmPlotSaveData(Some(cropType), 4, 0, System.currentTimeMillis(), true)
      } else {
        // Parse status like "Wheat Stage:2 Water:1"
        val stagePattern = """(\w+)\s+Stage:(\d+)\s+Water:(\d+)""".r
        statusStr match {
          case stagePattern(cropType, stageStr, waterStr) =>
            FarmPlotSaveData(
              Some(cropType),
              stageStr.toInt,
              waterStr.toInt,
              System.currentTimeMillis(),
              false
            )
          case _ =>
            // Fallback for unknown format
            FarmPlotSaveData(None, 0, 0, 0L, false)
        }
      }

      positionKey -> farmPlotData
    }.toMap
  }

  private def extractInventoryData(
      inventorySystem: FarmingInventorySystem
  ): InventorySaveData = {
    println("[SaveManager] Extracting inventory data...")

    val inventoryContents = inventorySystem.getInventoryContents
    val selectedRow = inventorySystem.getSelectedSlotRow
    val selectedCol = inventorySystem.getSelectedSlotCol
    val selectedIndex = selectedRow * 8 + selectedCol

    InventorySaveData(
      items = inventoryContents,
      selectedIndex = selectedIndex
    )
  }

  /** Restore game state from loaded save data */
  def restoreGameState(
      saveData: GameSaveData,
      player: FarmPlayer,
      farmingSystem: InteractiveFarmingSystem,
      inventorySystem: FarmingInventorySystem
  ): Unit = {
    println(s"[SaveManager] Restoring game state from: ${saveData.saveName}")

    // Restore player position
    player.worldX = saveData.playerData.worldX.toDouble
    player.worldY = saveData.playerData.worldY.toDouble

    // TODO: Restore player health when health system is available
    // player.health = saveData.playerData.currentHealth

    // Restore inventory - clear existing and add saved items
    saveData.inventoryData.items.foreach { case (itemName, quantity) =>
      inventorySystem.addItem(itemName, quantity)
    }

    // Restore farm plots using the new restoration method
    farmingSystem.restoreFarmPlots(saveData.farmPlots)

    println(s"[SaveManager] Game state restored successfully")
  }

  private def ensureSaveDirectoryExists(): Unit = {
    val saveDir = new File(SAVE_DIRECTORY)
    if (!saveDir.exists()) {
      saveDir.mkdirs()
      println(
        s"[SaveManager] Created save directory: ${saveDir.getAbsolutePath}"
      )
    } else {
      println(
        s"[SaveManager] Using save directory: ${saveDir.getAbsolutePath}"
      )
    }
  }
}

object SaveManager {
  private var instance: Option[SaveManager] = None

  def getInstance: SaveManager = {
    instance match {
      case Some(manager: SaveManager) => manager
      case None =>
        val manager = new SaveManager()
        instance = Some(manager)
        manager
    }
  }
}
