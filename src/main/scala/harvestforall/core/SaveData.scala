package harvestforall.core

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Case classes for serializing game state to JSON
  */

/** Player data for save system */
case class PlayerSaveData(
    worldX: Int,
    worldY: Int,
    currentHealth: Int,
    maxHealth: Int
)

/** Farm plot data for save system */
case class FarmPlotSaveData(
    cropType: Option[String],
    growthStage: Int,
    waterLevel: Int,
    plantedTime: Long,
    isReadyToHarvest: Boolean
)

/** Inventory data for save system */
case class InventorySaveData(
    items: Map[String, Int],
    selectedIndex: Int
)

/** Game statistics for save system */
case class GameStatsSaveData(
    currency: Double,
    sustainabilityRating: Double,
    totalCropsHarvested: Int,
    totalMoneyEarned: Double,
    gamesPlayed: Int
)

/** Complete save data structure */
case class GameSaveData(
    // Save metadata
    saveName: String,
    saveDate: String,
    gameVersion: String,

    // Core game data
    playerData: PlayerSaveData,
    farmPlots: Map[String, FarmPlotSaveData], // Key format: "x,y"
    inventoryData: InventorySaveData,
    gameStats: GameStatsSaveData,

    // Additional metadata
    totalPlayTime: Long, // in seconds
    lastSaved: String
) {

  /** Generate a formatted display name for the save file */
  def getDisplayName: String = s"$saveName"

  /** Generate filename for this save */
  def getFileName: String =
    s"${saveName.replaceAll("[^a-zA-Z0-9\\-_]", "_")}.json"

  /** Get formatted save date for display */
  def getFormattedDate: String = {
    try {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      val dateTime = LocalDateTime.parse(saveDate, formatter)
      dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    } catch {
      case _: Exception => saveDate
    }
  }
}

object GameSaveData {

  /** Create a new save data from current game state */
  def fromGameState(
      saveName: String,
      gameState: GameState,
      playerData: PlayerSaveData,
      farmPlots: Map[String, FarmPlotSaveData],
      inventoryData: InventorySaveData
  ): GameSaveData = {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    GameSaveData(
      saveName = saveName,
      saveDate = now.format(formatter),
      gameVersion = "1.0.0",
      playerData = playerData,
      farmPlots = farmPlots,
      inventoryData = inventoryData,
      gameStats = GameStatsSaveData(
        currency = gameState.currency,
        sustainabilityRating = gameState.globalSustainabilityRating,
        totalCropsHarvested =
          gameState.totalSales.toInt, // Use total sales as proxy for crops harvested
        totalMoneyEarned = gameState.totalSales,
        gamesPlayed =
          gameState.currentDay // Use current day as games played metric
      ),
      totalPlayTime = 0L, // TODO: Implement play time tracking
      lastSaved = now.format(formatter)
    )
  }
}
