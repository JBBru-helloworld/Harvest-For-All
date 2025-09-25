package harvestforall.agriculture

import harvestforall.core.Season
import harvestforall.utilities.Observable
import scala.collection.mutable.{Map, ListBuffer}
import scalafx.scene.image.ImageView

/** Simple tile sprite placeholder */
class TileSprite(spriteName: String):
  def getSpriteName: String = spriteName

/** Simple sprite factory */
object SpriteFactory:
  def createTileSprite(name: String): TileSprite =
    new TileSprite(name)

/** Farm plot representing a single cultivated area
  *
  * Contains plant and visual representation
  */
class FarmPlot(
    val x: Int,
    val y: Int,
    val width: Int = 64,
    val height: Int = 64
):
  private var _plant: Option[Plant] = None
  private var _tileSprite: TileSprite =
    SpriteFactory.createTileSprite("soil_dry")
  private var _lastWatered: Long = 0

  // Getters
  def plant: Option[Plant] = _plant
  def tileSprite: TileSprite = _tileSprite
  def position: (Int, Int) = (x, y)
  def dimensions: (Int, Int) = (width, height)

  /** Plant a crop in this plot
    */
  def plantCrop(newPlant: Plant): Boolean =
    if _plant.isEmpty then
      _plant = Some(newPlant)
      updateTileVisual()
      true
    else false

  /** Harvest the crop from this plot
    */
  def harvestCrop(): Option[Double] =
    _plant match
      case Some(plant) if plant.isReadyToHarvest =>
        val harvestYield = plant.harvest()
        _plant = None
        updateTileVisual()
        Some(harvestYield)
      case Some(plant) => None
      case None        => None

  /** Water the plot
    */
  def water(): Unit =
    _plant.foreach(_.water())
    _lastWatered = System.currentTimeMillis()
    updateTileVisual()

  /** Fertilize the plot (simplified)
    */
  def fertilize(): Unit =
    _plant.foreach(_.fertilize())
    updateTileVisual()

  /** Update tile visual based on current state
    */
  private def updateTileVisual(): Unit =
    val tileType = _plant match
      case Some(_) => "soil_average"
      case None    => "soil_dry"

    _tileSprite = SpriteFactory.createTileSprite(tileType)

  /** Update plot state (called each season)
    */
  def updateSeason(season: Season): Unit =
    _plant.foreach(_.updateSeason(season))
    updateTileVisual()

  /** Check if plot is ready for harvest
    */
  def isReadyForHarvest: Boolean =
    _plant.exists(_.isReadyToHarvest)

  /** Get plot status summary
    */
  def getStatus: String =
    _plant match
      case Some(plant) =>
        s"${plant.name} (${plant.getGrowthStage}) - Basic soil"
      case None => "Empty plot - Basic soil"

  override def toString: String =
    s"FarmPlot(($x, $y), ${getStatus})"

/** Farm management system for agricultural operations
  */
class Farm(
    val name: String,
    val size: Double,
    val location: String,
    val gridWidth: Int = 10,
    val gridHeight: Int = 10
) extends Observable[Farm]:

  private var _farmPlots: Map[(Int, Int), FarmPlot] = Map.empty
  private var _totalProduction: Double = 0.0
  private var _currentSeason: Season = Season.SPRING

  // Initialize farm plots
  initializeFarmPlots()

  /** Initialize the farm plot grid
    */
  private def initializeFarmPlots(): Unit =
    for
      x <- 0 until gridWidth
      y <- 0 until gridHeight
    do
      val plot = new FarmPlot(x * 64, y * 64)
      _farmPlots((x, y)) = plot

  // Getters
  def farmPlots: Map[(Int, Int), FarmPlot] = _farmPlots
  def totalProduction: Double = _totalProduction
  def currentSeason: Season = _currentSeason

  /** Get a specific farm plot
    */
  def getPlot(x: Int, y: Int): Option[FarmPlot] =
    _farmPlots.get((x, y))

  /** Plant a crop at specific coordinates
    */
  def plantCrop(x: Int, y: Int, plant: Plant): Boolean =
    getPlot(x, y) match
      case Some(plot) =>
        val success = plot.plantCrop(plant)
        if success then notifyObservers(this)
        success
      case None => false

  /** Harvest crop at specific coordinates
    */
  def harvestCrop(x: Int, y: Int): Option[Double] =
    getPlot(x, y) match
      case Some(plot) =>
        val harvestResult = plot.harvestCrop()
        harvestResult.foreach { amount =>
          _totalProduction += amount
          notifyObservers(this)
        }
        harvestResult
      case None => None

  /** Water all plots or specific plot
    */
  def waterPlots(coordinates: List[(Int, Int)] = List.empty): Unit =
    val plotsToWater =
      if coordinates.isEmpty then _farmPlots.keys.toList
      else coordinates.filter(_farmPlots.contains)

    plotsToWater.foreach { case (x, y) =>
      _farmPlots((x, y)).water()
    }
    notifyObservers(this)

  /** Fertilize all plots or specific plots
    */
  def fertilizePlots(coordinates: List[(Int, Int)] = List.empty): Unit =
    val plotsToFertilize =
      if coordinates.isEmpty then _farmPlots.keys.toList
      else coordinates.filter(_farmPlots.contains)

    plotsToFertilize.foreach { case (x, y) =>
      _farmPlots((x, y)).fertilize()
    }
    notifyObservers(this)

  /** Advance to next season
    */
  def advanceSeason(): Unit =
    _currentSeason = _currentSeason match
      case Season.SPRING => Season.SUMMER
      case Season.SUMMER => Season.AUTUMN
      case Season.AUTUMN => Season.WINTER
      case Season.WINTER => Season.SPRING

    _farmPlots.values.foreach(_.updateSeason(_currentSeason))
    notifyObservers(this)

  /** Get basic farm statistics
    */
  def getFarmStats: Map[String, Any] =
    val totalPlots = _farmPlots.size
    val occupiedPlots = _farmPlots.values.count(_.plant.isDefined)
    val readyForHarvest = _farmPlots.values.count(_.isReadyForHarvest)

    Map(
      "totalPlots" -> totalPlots,
      "occupiedPlots" -> occupiedPlots,
      "readyForHarvest" -> readyForHarvest,
      "totalProduction" -> _totalProduction
    )

  /** Get all plots that are ready for harvest
    */
  def getHarvestablePlots: List[(Int, Int)] =
    _farmPlots.filter(_._2.isReadyForHarvest).keys.toList

  /** Get all empty plots available for planting
    */
  def getEmptyPlots: List[(Int, Int)] =
    _farmPlots.filter(_._2.plant.isEmpty).keys.toList

  override def toString: String =
    s"Farm(name=$name, size=$size, location=$location, production=$_totalProduction)"

end Farm
