package harvestforall.game.systems

import harvestforall.graphics.FarmTileManager
import harvestforall.agriculture.{Plant, PlantType}
import harvestforall.agriculture.PlantFactory
import harvestforall.game.ui.FarmingInventorySystem
import scala.collection.mutable

/** Interactive farming system for tile-based crop management features: opaque
  * types, given instances, enums, extension methods
  */
class InteractiveFarmingSystem(
    tileManager: FarmTileManager,
    inventorySystem: FarmingInventorySystem
) {

  // Opaque types for type safety
  opaque type TilePosition = (Int, Int)
  opaque type GrowthStage = Int

  // Extension methods for the opaque types above
  extension (pos: TilePosition)
    def x: Int = pos._1
    def y: Int = pos._2
    def worldX: Double = pos.x * tileManager.tileSize
    def worldY: Double = pos.y * tileManager.tileSize

  extension (stage: GrowthStage)
    def value: Int = stage
    def isMaxStage: Boolean = stage >= 4
    def canAdvance: Boolean = stage < 4

  // Enum for crop states
  enum CropState:
    case Empty
    case Planted(
        plant: Plant,
        stage: GrowthStage,
        waterLevel: Int,
        lastWatered: Long
    )
    case ReadyToHarvest(plant: Plant)

  // Crop management with mutable map for performance
  private val farmPlots = mutable.Map[TilePosition, CropState]()
  private var lastUpdateTime = System.currentTimeMillis()

  // Configuration
  private val FarmableTileType = 17 // My farming tile in my resources
  private val GrowthTimePerStage = 10000L // 10 seconds per stage
  private val WaterDecayTime = 15000L // 15 seconds before water evaporates
  private val MaxWaterLevel = 3

  /** Get crop information for rendering at specified tile position Returns
    * tuple with: Option[(cropType: String, stage: Int, isReady: Boolean,
    * waterLevel: Int)]
    */
  def getCropRenderInfo(
      col: Int,
      row: Int
  ): Option[(String, Int, Boolean, Int)] =
    farmPlots.get((col, row)) match
      case Some(CropState.Planted(plant, stage, waterLevel, _)) =>
        Some(
          (plant.plantType.toString.toLowerCase, stage.value, false, waterLevel)
        )
      case Some(CropState.ReadyToHarvest(plant)) =>
        Some((plant.plantType.toString.toLowerCase, 4, true, 0))
      case _ => None

  def initialize(): Unit =
    println(
      "[InteractiveFarmingSystem] Initialized with Scala 3 modern features"
    )

  /** Update all planted crops */
  def update(): Unit =
    val currentTime = System.currentTimeMillis()
    val deltaTime = currentTime - lastUpdateTime
    lastUpdateTime = currentTime

    // Update all planted crops using pattern matching
    farmPlots.foreachEntry { (position, state) =>
      state match
        case CropState.Planted(plant, stage, waterLevel, lastWatered) =>
          // Check if water has evaporated
          val newWaterLevel =
            if (currentTime - lastWatered) > WaterDecayTime then 0
            else waterLevel

          // Check if crop can advance growth stage
          if newWaterLevel > 0 && (currentTime - lastWatered) >= GrowthTimePerStage
          then
            if stage.canAdvance then
              val newStage: GrowthStage = (stage.value + 1)
              if newStage.isMaxStage then
                farmPlots(position) = CropState.ReadyToHarvest(plant)
                println(
                  s"[Farming] Crop at (${position.x}, ${position.y}) is ready for harvest!"
                )
              else
                farmPlots(position) = CropState.Planted(
                  plant,
                  newStage,
                  newWaterLevel - 1,
                  currentTime
                )
                println(
                  s"[Farming] Crop at (${position.x}, ${position.y}) grew to stage ${newStage.value}"
                )
            end if
          else if newWaterLevel != waterLevel then
            farmPlots(position) =
              CropState.Planted(plant, stage, newWaterLevel, lastWatered)
          end if
        case _ => // No update needed for empty or ready crops
    }

  /** Attempt to plant a crop at player's current position */
  def plantCrop(
      playerWorldX: Double,
      playerWorldY: Double,
      cropType: String
  ): Boolean =
    val tilePos = getTilePosition(playerWorldX, playerWorldY)

    // Check if tile is farmable (tile 17)
    if !isFarmableTile(tilePos) then
      println(s"[Farming] Cannot plant here - not farmable soil")
      return false

    // Check if tile is empty
    farmPlots.get(tilePos) match
      case Some(CropState.Empty) | None =>
        // Look for seeds in inventory (seeds have "_seeds" suffix)
        val seedName = s"${cropType}_seeds"

        if inventorySystem.hasItem(seedName) then
          // Convert string to PlantType
          val plantType = cropType.toLowerCase match
            case "wheat"   => PlantType.Wheat
            case "corn"    => PlantType.Corn
            case "carrot"  => PlantType.Carrot
            case "tomato"  => PlantType.Tomato
            case "spinach" => PlantType.Spinach
            case _         => PlantType.Wheat // Default fallback

          PlantFactory.createPlant(plantType) match
            case Some(plant) =>
              farmPlots(tilePos) =
                CropState.Planted(plant, 0, 0, System.currentTimeMillis())
              inventorySystem.removeItem(seedName, 1) // Remove seeds, not crop
              println(
                s"[Farming] Planted $cropType using $seedName at (${tilePos.x}, ${tilePos.y})"
              )
              true
            case None =>
              println(s"[Farming] Failed to create plant: $cropType")
              false
        else
          println(s"[Farming] No $seedName in inventory")
          false
      case Some(_) =>
        println(s"[Farming] Tile already occupied")
        false

  /** Water the crop at player's current position */
  def waterCrop(playerWorldX: Double, playerWorldY: Double): Boolean =
    val tilePos = getTilePosition(playerWorldX, playerWorldY)

    farmPlots.get(tilePos) match
      case Some(CropState.Planted(plant, stage, waterLevel, _))
          if waterLevel < MaxWaterLevel =>
        val newWaterLevel = math.min(MaxWaterLevel, waterLevel + 1)
        farmPlots(tilePos) = CropState.Planted(
          plant,
          stage,
          newWaterLevel,
          System.currentTimeMillis()
        )
        println(
          s"[Farming] Watered crop at (${tilePos.x}, ${tilePos.y}) - Water level: $newWaterLevel/$MaxWaterLevel"
        )
        true
      case Some(CropState.Planted(_, _, waterLevel, _)) =>
        println(
          s"[Farming] Crop is already well watered ($waterLevel/$MaxWaterLevel)"
        )
        false
      case Some(CropState.ReadyToHarvest(_)) =>
        println(s"[Farming] Crop is ready for harvest, no need to water")
        false
      case _ =>
        println(s"[Farming] No crop to water here")
        false

  /** Harvest the crop at player's current position */
  def harvestCrop(playerWorldX: Double, playerWorldY: Double): Boolean =
    val tilePos = getTilePosition(playerWorldX, playerWorldY)

    farmPlots.get(tilePos) match
      case Some(CropState.ReadyToHarvest(plant)) =>
        val harvestYield =
          1 + scala.util.Random.nextInt(2) // 1-2 crops (more balanced)
        val cropName = plant.plantType.toString.toLowerCase

        inventorySystem.addItem(cropName, harvestYield)
        farmPlots(tilePos) = CropState.Empty

        println(
          s"[Farming] Harvested $harvestYield x $cropName from (${tilePos.x}, ${tilePos.y})"
        )
        true

      case Some(CropState.Planted(plant, stage, _, _)) =>
        println(s"[Farming] Crop is not ready yet (stage ${stage.value}/4)")
        false

      case _ =>
        println(s"[Farming] Nothing to harvest here")
        false

  /** Get crop information at position */
  def getCropInfo(playerWorldX: Double, playerWorldY: Double): String =
    val tilePos = getTilePosition(playerWorldX, playerWorldY)

    farmPlots.get(tilePos) match
      case Some(CropState.Planted(plant, stage, waterLevel, lastWatered)) =>
        val timeSinceWatered = (System.currentTimeMillis() - lastWatered) / 1000
        val cropName = plant.plantType.toString
        s"$cropName - Stage ${stage.value}/4 - Water: $waterLevel/$MaxWaterLevel - Last watered: ${timeSinceWatered}s ago"

      case Some(CropState.ReadyToHarvest(plant)) =>
        s"${plant.plantType.toString} - READY TO HARVEST!"

      case Some(CropState.Empty) | None =>
        if isFarmableTile(tilePos) then "Empty farmable soil"
        else "Not farmable"

  /** Check if player is standing on a farmable tile */
  def canFarmHere(playerWorldX: Double, playerWorldY: Double): Boolean =
    val tilePos = getTilePosition(playerWorldX, playerWorldY)
    isFarmableTile(tilePos)

  // Helper methods
  private def getTilePosition(worldX: Double, worldY: Double): TilePosition =
    val tileX = (worldX / tileManager.tileSize).toInt
    val tileY = (worldY / tileManager.tileSize).toInt
    (tileX, tileY)

  private def isFarmableTile(pos: TilePosition): Boolean =
    val col = pos.x
    val row = pos.y

    if row >= 0 && row < tileManager.maxWorldRow && col >= 0 && col < tileManager.maxWorldCol
    then
      val tileNum = tileManager.mapTileNum(tileManager.currentMap)(col)(row)
      tileNum == FarmableTileType
    else false

  /** Get all farm plot statuses for debugging */
  def getAllPlotStatuses: Map[String, String] =
    farmPlots.map { case (pos, state) =>
      val key = s"(${pos.x}, ${pos.y})"
      val value = state match
        case CropState.Empty => "Empty"
        case CropState.Planted(plant, stage, water, _) =>
          s"${plant.plantType.toString} Stage:${stage.value} Water:$water"
        case CropState.ReadyToHarvest(plant) =>
          s"${plant.plantType.toString} READY!"
      key -> value
    }.toMap

  /** Restore farm plots from save data */
  def restoreFarmPlots(
      plotsData: Map[String, harvestforall.core.FarmPlotSaveData]
  ): Unit = {
    println(
      s"[InteractiveFarmingSystem] Restoring ${plotsData.size} farm plots..."
    )

    plotsData.foreach { case (positionStr, plotData) =>
      // Parse position string like "(25, 21)" to extract coordinates
      val positionPattern = """\((\d+), (\d+)\)""".r
      positionStr match {
        case positionPattern(xStr, yStr) =>
          val position: TilePosition = (xStr.toInt, yStr.toInt)

          plotData.cropType match {
            case Some(cropType) =>
              // Create plant from crop type (cropType is already in proper case like "Wheat")
              val plantType = PlantType.valueOf(cropType)
              PlantFactory.createPlant(plantType) match {
                case Some(plant) =>
                  val cropState = if (plotData.isReadyToHarvest) {
                    CropState.ReadyToHarvest(plant)
                  } else {
                    CropState.Planted(
                      plant,
                      plotData.growthStage,
                      plotData.waterLevel,
                      plotData.plantedTime
                    )
                  }

                  farmPlots(position) = cropState
                  println(
                    s"[InteractiveFarmingSystem] Restored $cropType at $position (stage ${plotData.growthStage})"
                  )

                case None =>
                  println(
                    s"[InteractiveFarmingSystem] Failed to create plant for crop type: $cropType"
                  )
                  farmPlots(position) = CropState.Empty
              }

            case None =>
              farmPlots(position) = CropState.Empty
              println(
                s"[InteractiveFarmingSystem] Restored empty plot at $position"
              )
          }

        case _ =>
          println(
            s"[InteractiveFarmingSystem] Failed to parse position: $positionStr"
          )
      }
    }

    println(s"[InteractiveFarmingSystem] Farm plot restoration complete")
  }

}
