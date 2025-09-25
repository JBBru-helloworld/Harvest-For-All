package harvestforall.core

import harvestforall.agriculture.Farm
import harvestforall.utilities.Observable
import scala.collection.mutable.ListBuffer

/** Sustainability actions that affect the sustainability rating */
enum SustainabilityAction:
  case PlantCrop, HarvestCrop, WaterCrop, OverFarming, SellToVillage,
    WasteCrops, DiverseCrops

/** Game state management for Harvest for All
  *
  * Manages game progression, timing, and global state
  */
class GameState extends Observable[GameState]:

  // Starting stats - balanced so players don't get frustrated early on
  private var _currentDay: Int = 1
  private var _currentSeason: Season = Season.SPRING
  private var _isPaused: Boolean = false
  private var _gameSpeed: Double = 1.0
  private var _playerName: String = "Farmer"
  private var _playerLevel: Int = 1
  private var _playerExperience: Int = 0
  private var _currency: Double = 1000.0
  private var _globalSustainabilityRating: Double =
    0.0 // Start at 0% - farmers must build sustainability
  private var _totalFoodsecurity: Double = 0.0
  private var _farms: ListBuffer[Farm] = ListBuffer.empty
  private var _gameStartTime: Long = System.currentTimeMillis()
  private var _totalGameTime: Long = 0
  private var _isGameOver: Boolean = false
  private var _gameObjectives: Map[String, Boolean] = Map(
    "Plant first crop" -> false,
    "Harvest first crop" -> false,
    "Achieve 80% sustainability" -> false,
    "Feed 1000 people" -> false,
    "Reach level 10" -> false
  )

  // Inventory system for harvested crops - using mutable map cuz performance matters
  private var _inventory: scala.collection.mutable.Map[String, Int] =
    scala.collection.mutable.Map.empty
  private var _totalSales: Double = 0.0
  private var _villageSatisfaction: Double =
    50.0 // Start neutral, can go up/down
  private var _currentLocation: String = "Farm" // "Farm" or "Village"
  private var _lastSalePrice: Double = 0.0
  private var _currentSaveName: Option[String] = None // For save/load system

  // Getters
  def currentDay: Int = _currentDay
  def currentSeason: Season = _currentSeason
  def isPaused: Boolean = _isPaused
  def gameSpeed: Double = _gameSpeed
  def playerName: String = _playerName
  def playerLevel: Int = _playerLevel
  def playerExperience: Int = _playerExperience
  def currency: Double = _currency
  def globalSustainabilityRating: Double = _globalSustainabilityRating
  def totalFoodSecurity: Double = _totalFoodsecurity
  def farms: List[Farm] = _farms.toList
  def gameStartTime: Long = _gameStartTime
  def totalGameTime: Long = _totalGameTime
  def isGameOver: Boolean = _isGameOver
  def gameObjectives: Map[String, Boolean] = _gameObjectives

  // New getters for inventory and village system
  def inventory: Map[String, Int] = _inventory.toMap
  def totalSales: Double = _totalSales
  def villageSatisfaction: Double = _villageSatisfaction
  def currentLocation: String = _currentLocation
  def lastSalePrice: Double = _lastSalePrice
  def currentSaveName: Option[String] = _currentSaveName

  // Inventory management methods
  def addToInventory(cropType: String, quantity: Int): Unit =
    val current = _inventory.getOrElse(cropType, 0)
    _inventory(cropType) = current + quantity
    notifyObservers(this)

  def removeFromInventory(cropType: String, quantity: Int): Boolean =
    val current = _inventory.getOrElse(cropType, 0)
    if current >= quantity then
      _inventory(cropType) = current - quantity
      if _inventory(cropType) <= 0 then _inventory.remove(cropType)
      notifyObservers(this)
      true
    else false

  def getInventoryCount(cropType: String): Int =
    _inventory.getOrElse(cropType, 0)

  def hasInInventory(cropType: String, quantity: Int): Boolean =
    getInventoryCount(cropType) >= quantity

  // Village and selling methods
  def setLocation(location: String): Unit =
    _currentLocation = location
    notifyObservers(this)

  def sellCrop(cropType: String, quantity: Int, pricePerUnit: Double): Boolean =
    if hasInInventory(cropType, quantity) then
      removeFromInventory(cropType, quantity)
      val saleAmount = quantity * pricePerUnit
      addCurrency(saleAmount)
      _totalSales += saleAmount
      _lastSalePrice = saleAmount

      // Improve village satisfaction based on sale
      _villageSatisfaction =
        math.min(100.0, _villageSatisfaction + (quantity * 0.5))

      // Update objectives
      if !_gameObjectives.getOrElse("Harvest first crop", false) then
        _gameObjectives = _gameObjectives + ("Harvest first crop" -> true)

      notifyObservers(this)
      true
    else false

  def getCropSellPrice(cropType: String): Double =
    // Dynamic pricing based on village satisfaction and crop type
    val basePrice = cropType match
      case "Wheat"   => 3.0
      case "Rice"    => 4.0
      case "Corn"    => 3.5
      case "Soybean" => 5.0
      case "Lentils" => 4.5
      case "Tomato"  => 6.0
      case "Carrot"  => 2.5
      case _         => 2.0

    val satisfactionMultiplier = 0.5 + (_villageSatisfaction / 100.0)
    basePrice * satisfactionMultiplier

  /** Advance to next day
    */
  def advanceDay(): Unit =
    if !_isPaused && !_isGameOver then
      _currentDay += 1

      // Check if season should change (every 90 days)
      if _currentDay % _currentSeason.durationInDays == 0 then advanceSeason()

      // Update all farms
      _farms.foreach(_.advanceSeason())

      // Update global metrics
      updateGlobalMetrics()

      // Check for level up
      checkLevelUp()

      // Update total game time
      _totalGameTime = System.currentTimeMillis() - _gameStartTime

      // Check game objectives
      checkObjectives()

      notifyObservers(this)

  /** Advance to next season
    */
  def advanceSeason(): Unit =
    _currentSeason = _currentSeason.next
    // Logger.info(s"Season changed to ${_currentSeason}")

  /** Pause/unpause game
    */
  def pause(): Unit =
    _isPaused = true
    notifyObservers(this)

  def unpause(): Unit =
    _isPaused = false
    notifyObservers(this)

  def togglePause(): Unit =
    _isPaused = !_isPaused
    notifyObservers(this)

  /** Set game speed
    */
  def setGameSpeed(speed: Double): Unit =
    _gameSpeed = Math.max(0.1, Math.min(5.0, speed))
    notifyObservers(this)

  /** Player management
    */
  def setPlayerName(name: String): Unit =
    _playerName = name
    notifyObservers(this)

  def setCurrentSaveName(name: Option[String]): Unit =
    _currentSaveName = name
    notifyObservers(this)

  // Convenience method for setting save name with String
  def currentSaveName_=(name: String): Unit = setCurrentSaveName(Some(name))

  def addExperience(amount: Int): Unit =
    _playerExperience += amount
    checkLevelUp()
    notifyObservers(this)

  def addCurrency(amount: Double): Unit =
    _currency += amount
    notifyObservers(this)

  /** Set currency directly (for save loading) */
  def setCurrency(amount: Double): Unit =
    _currency = amount
    notifyObservers(this)

  /** Set sustainability rating directly (for save loading) */
  def setSustainabilityRating(rating: Double): Unit =
    _globalSustainabilityRating = rating.max(0).min(100) // Clamp between 0-100
    notifyObservers(this)

  /** Update sustainability based on farming practices */
  def updateSustainability(
      action: SustainabilityAction,
      amount: Double = 1.0
  ): Unit =
    val sustainabilityChange = action match
      case SustainabilityAction.PlantCrop =>
        1.5 * amount // Planting helps sustainability (increased from 0.5)
      case SustainabilityAction.HarvestCrop =>
        1.0 * amount // Harvesting maintains sustainability (increased from 0.2)
      case SustainabilityAction.WaterCrop =>
        0.5 * amount // Proper watering helps (increased from 0.1)
      case SustainabilityAction.OverFarming =>
        -3.0 * amount // Overfarming hurts sustainability
      case SustainabilityAction.SellToVillage =>
        2.0 * amount // Selling to village helps community (increased from 1.0)
      case SustainabilityAction.WasteCrops =>
        -5.0 * amount // Wasting crops hurts sustainability
      case SustainabilityAction.DiverseCrops =>
        3.0 * amount // Crop diversity helps sustainability (increased from 1.5)

    _globalSustainabilityRating = math.max(
      0.0,
      math.min(100.0, _globalSustainabilityRating + sustainabilityChange)
    )

    // Check sustainability achievement
    if _globalSustainabilityRating >= 80.0 then
      _gameObjectives =
        _gameObjectives.updated("Achieve 80% sustainability", true)

    notifyObservers(this)

  def spendCurrency(amount: Double): Boolean =
    if _currency >= amount then
      _currency -= amount
      notifyObservers(this)
      true
    else false

  def incrementTotalFoodSecurity(amount: Double): Unit =
    _totalFoodsecurity += amount
    notifyObservers(this)

  /** Farm management
    */
  def addFarm(farm: Farm): Unit =
    _farms += farm
    // Add observer to farm for updates
    farm.addObserver(_ => updateGlobalMetrics())
    notifyObservers(this)

  def removeFarm(farm: Farm): Unit =
    _farms -= farm
    notifyObservers(this)

  def getFarm(name: String): Option[Farm] =
    _farms.find(_.name == name)

  /** Update global sustainability and food security metrics
    */
  private def updateGlobalMetrics(): Unit =
    if _farms.nonEmpty then
      // Calculate total food security (production capacity)
      _totalFoodsecurity = _farms.map(_.totalProduction).sum

      // Global sustainability is managed through player actions via updateSustainability()
      // Actions like PlantCrop, HarvestCrop, SellToVillage increase sustainability
      // Actions like OverFarming, WasteCrops decrease sustainability

  /** Check for level up
    */
  private def checkLevelUp(): Unit =
    val requiredExp = _playerLevel * 100
    if _playerExperience >= requiredExp then
      _playerLevel += 1
      _playerExperience -= requiredExp
      // Award currency for leveling up
      _currency += _playerLevel * 50
      // Logger.info(s"Level up! Now level ${_playerLevel}")

  /** Check and update game objectives
    */
  private def checkObjectives(): Unit =
    // Check if any crops have been planted
    if _farms.exists(_.farmPlots.values.exists(_.plant.isDefined)) then
      _gameObjectives = _gameObjectives.updated("Plant first crop", true)

    // Check if any crops have been harvested
    if _farms.exists(_.totalProduction > 0) then
      _gameObjectives = _gameObjectives.updated("Harvest first crop", true)

    // Check sustainability
    if _globalSustainabilityRating >= 80.0 then
      _gameObjectives =
        _gameObjectives.updated("Achieve 80% sustainability", true)

    // Check food security
    if _totalFoodsecurity >= 1000.0 then
      _gameObjectives = _gameObjectives.updated("Feed 1000 people", true)

    // Check level
    if _playerLevel >= 10 then
      _gameObjectives = _gameObjectives.updated("Reach level 10", true)

  /** Get game statistics
    */
  def getGameStats: Map[String, Any] =
    Map(
      "currentDay" -> _currentDay,
      "currentSeason" -> _currentSeason,
      "playerLevel" -> _playerLevel,
      "playerExperience" -> _playerExperience,
      "currency" -> _currency,
      "globalSustainabilityRating" -> _globalSustainabilityRating,
      "totalFoodSecurity" -> _totalFoodsecurity,
      "totalFarms" -> _farms.size,
      "totalGameTime" -> _totalGameTime,
      "completedObjectives" -> _gameObjectives.count(_._2),
      "totalObjectives" -> _gameObjectives.size
    )

  /** Get current season information
    */
  def getCurrentSeasonInfo: Map[String, Any] =
    Map(
      "season" -> _currentSeason,
      "description" -> _currentSeason.description,
      "growthModifier" -> _currentSeason.growthModifier,
      "waterRequirement" -> _currentSeason.waterRequirement,
      "daysInSeason" -> _currentSeason.durationInDays,
      "daysLeftInSeason" -> (_currentSeason.durationInDays - (_currentDay % _currentSeason.durationInDays))
    )

  /** Reset game state
    */
  def resetGame(): Unit =
    _currentDay = 1
    _currentSeason = Season.SPRING
    _isPaused = false
    _gameSpeed = 1.0
    _playerLevel = 1
    _playerExperience = 0
    _currency = 1000.0
    _globalSustainabilityRating =
      0.0 // Start at 0% - farmers must build sustainability
    _totalFoodsecurity = 0.0
    _farms.clear()
    _inventory.clear()
    _totalSales = 0.0
    _villageSatisfaction = 50.0
    _currentLocation = "Farm"
    _lastSalePrice = 0.0
    _gameStartTime = System.currentTimeMillis()
    _totalGameTime = 0
    _isGameOver = false
    _gameObjectives = _gameObjectives.map((k, _) => k -> false)
    notifyObservers(this)

  override def toString: String =
    s"GameState(Day $_currentDay, ${_currentSeason}, Level $_playerLevel, Sustainability ${_globalSustainabilityRating}%)"

end GameState
