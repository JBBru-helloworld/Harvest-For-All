package harvestforall.agriculture

import harvestforall.core.Season
import harvestforall.gui.managers.AssetManager
import harvestforall.graphics.CropSpriteManager
import scalafx.scene.image.Image

/** Plant type enumeration for categorization - expanded to match sprite pack */
enum PlantType:
  case Wheat, Rice, Corn, Soybean, Lentils, Chickpeas, Spinach, Tomato, Carrot,
    Beetroot, Cabbage, Onion, Peas, Beans, Cucumber, Strawberry,
    Grapes, Pumpkin, Broccoli, Barley, Rye

/** Abstract Plant hierarchy demonstrating inheritance and polymorphism
  *
  * This is where the OOP magic happens imo - each plant type inherits from this
  * base but has its own unique characteristics. Spent ages balancing the growth
  * times!
  */
abstract class Plant(
    val name: String,
    val plantType: PlantType,
    val baseYield: Double,
    val growthTime: Int
):
  protected var _currentGrowthStage: Int = 0
  protected var _plantedSeason: Season = Season.SPRING
  protected var _waterLevel: Double =
    50.0 // Start at half-water so players need to care for crops
  protected var _healthStatus: Double = 100.0
  protected var _growthProgress: Double = 0.0

  // Track if this is a multi-harvest crop (like tomatoes that keep producing)
  protected var _isMultiHarvest: Boolean = false
  protected var _harvestCount: Int = 0

  // Abstract methods for polymorphism - each plant type implements these differently
  def getOptimalSeasons: List[Season]
  def getSpecialBonus: Double
  def getPlantCategory: String

  // Concrete methods that all plants share
  def currentGrowthStage: Int = _currentGrowthStage
  def waterLevel: Double = _waterLevel
  def healthStatus: Double = _healthStatus
  def getGrowthStage: Double = _growthProgress / growthTime
  def isMultiHarvest: Boolean = _isMultiHarvest
  def harvestCount: Int = _harvestCount

  // === SPRITE MANAGEMENT ===

  /** Get current sprite based on growth stage */
  def getCurrentSprite: Image =
    val spriteStage =
      math.min(4, _currentGrowthStage) // Map to 0-4 sprite range
    AssetManager.getCropSprite(getSpriteKey, spriteStage)

  /** Get crop icon for UI/inventory */
  def getCropIcon: Image =
    AssetManager.getCropIcon(getSpriteKey)

  /** Get seeds sprite for planting */
  def getSeedsSprite: Image =
    AssetManager.getSeedsSprite(getSpriteKey)

  /** Get sign sprite for farm decoration */
  def getSignSprite: Image =
    AssetManager.getSignSprite(getSpriteKey)

  /** Map plant type to sprite key */
  private def getSpriteKey: String =
    plantType.toString.toLowerCase match
      case "wheat"      => "wheat"
      case "rice"       => "wheat" // Use wheat as substitute
      case "corn"       => "corn"
      case "soybean"    => "beans"
      case "lentils"    => "peas"
      case "chickpeas"  => "peas"
      case "spinach"    => "spinach"
      case "tomato"     => "tomato"
      case "carrot"     => "carrot"
      case "beetroot"   => "beetroot"
      case "cabbage"    => "cabbage"
      case "onion"      => "onion"
      case "peas"       => "peas"
      case "beans"      => "beans"
      case "cucumber"   => "cucumber"
      case "strawberry" => "strawberry"
      case "grapes"     => "grapes"
      case "pumpkin"    => "pumpkin"
      case "broccoli"   => "broccoli"
      case "barley"     => "barley"
      case "rye"        => "rye"
      case _            => "wheat" // Default fallback

  /** Check if this plant supports multi-harvest */
  def setupMultiHarvest(): Unit =
    // Based on sprite pack documentation: corn, peas, beans, tomatoes, cucumber, strawberries, grapes
    _isMultiHarvest = plantType match
      case PlantType.Corn | PlantType.Peas | PlantType.Beans |
          PlantType.Tomato | PlantType.Cucumber | PlantType.Strawberry |
          PlantType.Grapes =>
        true
      case _ => false

  def water(): Unit =
    _waterLevel = math.min(100.0, _waterLevel + 15.0)
    _healthStatus = math.min(100.0, _healthStatus + 5.0)

  def grow(deltaTime: Double): Unit =
    if _healthStatus > 0 then
      val growthRate = if _waterLevel > 30.0 then 1.0 else 0.5
      val seasonBonus =
        if getOptimalSeasons.contains(_plantedSeason) then 1.2 else 1.0
      _growthProgress += deltaTime * growthRate * seasonBonus
      _currentGrowthStage = math.min(growthTime, _growthProgress.toInt)

  def isReadyToHarvest: Boolean = _currentGrowthStage >= growthTime

  def harvest(): Double =
    if isReadyToHarvest then
      val seasonalBonus =
        if getOptimalSeasons.contains(_plantedSeason) then 1.2 else 1.0
      val waterBonus = _waterLevel / 100.0
      val healthMultiplier = _healthStatus / 100.0
      val specialBonus = getSpecialBonus

      val harvestYield =
        baseYield * seasonalBonus * waterBonus * healthMultiplier * specialBonus

      // Handle multi-harvest crops
      if _isMultiHarvest && _harvestCount < 3 then
        _harvestCount += 1
        _currentGrowthStage = growthTime - 1 // Reset to stage 4 for regrowth
        _growthProgress = (growthTime - 1).toDouble

      harvestYield
    else 0.0

  def setPlantedSeason(season: Season): Unit =
    _plantedSeason = season

  def fertilize(): Unit =
    _healthStatus = math.min(100.0, _healthStatus + 10.0)

  def updateSeason(season: Season): Unit =
    _plantedSeason = season

end Plant

/** Here is my 3+ Level Inheritance Hierarchy for Plants */

/** Second level: Abstract plant categories */
abstract class Cereal(
    name: String,
    plantType: PlantType,
    baseYield: Double,
    growthTime: Int
) extends Plant(name, plantType, baseYield, growthTime):
  override def getPlantCategory: String = "Cereal Crop"
  def getGrainType: String
  def getProteinContent: Double

abstract class Legume(
    name: String,
    plantType: PlantType,
    baseYield: Double,
    growthTime: Int
) extends Plant(name, plantType, baseYield, growthTime):
  override def getPlantCategory: String = "Legume"
  def getNitrogenFixation: Double
  def getProteinContent: Double

abstract class Vegetable(
    name: String,
    plantType: PlantType,
    baseYield: Double,
    growthTime: Int
) extends Plant(name, plantType, baseYield, growthTime):
  override def getPlantCategory: String = "Vegetable"
  def getNutrientProfile: Map[String, Double]
  def getVitaminContent: String

abstract class Fruit(
    name: String,
    plantType: PlantType,
    baseYield: Double,
    growthTime: Int
) extends Plant(name, plantType, baseYield, growthTime):
  def getSweetness: Double
  def getFruitCategory: String
  def getPlantCategory: String = "Fruit"

/** Third level: Concrete plant implementations
  *
  * I made a last minute design decision where only core gameplay crops get
  * companion objects!
  *
  * I've selectively added companion objects for the crops that are actually
  * used in gameplay (wheat, corn, carrot, tomato, spinach). These are the ones
  * players interact with most, so they benefit from:
  *   - clean factory methods (Wheat(), Tomato.forSummer())
  *   - centralised configuration (I dont want to deal with the magic numbers)
  *   - and spcefic season creation helpers
  *
  * Less-used crops (rice, soybean, etc.) keep the simpler approach to avoid
  * over-engineering just for no reason la.
  */

/** Wheat - Primary staple crop, heavily used in gameplay */
class Wheat
    extends Cereal(
      "Wheat",
      PlantType.Wheat,
      Wheat.BASE_YIELD,
      Wheat.GROWTH_TIME
    ):
  def getOptimalSeasons: List[Season] = Wheat.OPTIMAL_SEASONS
  def getSpecialBonus: Double = Wheat.SPECIAL_BONUS
  def getGrainType: String = Wheat.GRAIN_TYPE
  def getProteinContent: Double = Wheat.PROTEIN_CONTENT

object Wheat:
  // Configuration constants - much cleaner than hardcoded values!
  val BASE_YIELD = 45.0
  val GROWTH_TIME = 4
  val OPTIMAL_SEASONS = List(Season.SPRING, Season.SUMMER)
  val SPECIAL_BONUS = 1.1
  val GRAIN_TYPE = "Wheat grain"
  val PROTEIN_CONTENT = 12.0

  // Factory methods for convenient creation
  def apply(): Wheat = new Wheat()
  def withSeason(season: Season): Wheat =
    val wheat = new Wheat()
    wheat.setPlantedSeason(season)
    wheat

/** Rice - Less commonly used, so kept simple */

/** Rice - Less commonly used, so kept simple */
class Rice extends Cereal("Rice", PlantType.Rice, 50.0, 5):
  def getOptimalSeasons: List[Season] = List(Season.SPRING, Season.SUMMER)
  def getSpecialBonus: Double = 1.2
  def getGrainType: String = "Rice grain"
  def getProteinContent: Double = 7.0

/** Corn - Major gameplay crop with companion object for enhanced usability */
class Corn
    extends Cereal("Corn", PlantType.Corn, Corn.BASE_YIELD, Corn.GROWTH_TIME):
  def getOptimalSeasons: List[Season] = Corn.OPTIMAL_SEASONS
  def getSpecialBonus: Double = Corn.SPECIAL_BONUS
  def getGrainType: String = Corn.GRAIN_TYPE
  def getProteinContent: Double = Corn.PROTEIN_CONTENT

object Corn:
  // High-yield summer crop - popular choice for players
  val BASE_YIELD = 70.0
  val GROWTH_TIME = 6
  val OPTIMAL_SEASONS = List(Season.SUMMER, Season.AUTUMN)
  val SPECIAL_BONUS = 1.2
  val GRAIN_TYPE = "Corn kernels"
  val PROTEIN_CONTENT = 9.0

  def apply(): Corn = new Corn()
  def withSeason(season: Season): Corn =
    val corn = new Corn()
    corn.setPlantedSeason(season)
    corn

/** Soybean - Background crop, simple implementation */

/** Soybean - Background crop, simple implementation */
class Soybean extends Legume("Soybean", PlantType.Soybean, 35.0, 4):
  def getOptimalSeasons: List[Season] = List(Season.SPRING, Season.SUMMER)
  def getSpecialBonus: Double = 1.3
  def getNitrogenFixation: Double = 15.0
  def getProteinContent: Double = 36.0

/** Lentils - Supporting cast crop */
class Lentils extends Legume("Lentils", PlantType.Lentils, 25.0, 3):
  def getOptimalSeasons: List[Season] = List(Season.SPRING, Season.AUTUMN)
  def getSpecialBonus: Double = 1.1
  def getNitrogenFixation: Double = 12.0
  def getProteinContent: Double = 25.0

/** Tomato - Star vegetable crop! Multi-harvest and summer specialty */
class Tomato
    extends Vegetable(
      "Tomatoes",
      PlantType.Tomato,
      Tomato.BASE_YIELD,
      Tomato.GROWTH_TIME
    ):
  setupMultiHarvest() // Enable multi-harvest for tomatoes

  def getOptimalSeasons: List[Season] = Tomato.OPTIMAL_SEASONS
  def getSpecialBonus: Double = Tomato.SPECIAL_BONUS
  def getNutrientProfile: Map[String, Double] = Tomato.NUTRIENT_PROFILE
  def getVitaminContent: String = Tomato.VITAMIN_CONTENT

object Tomato:
  // Multi-harvest summer crop - player favorite!
  val BASE_YIELD = 30.0
  val GROWTH_TIME = 3
  val OPTIMAL_SEASONS = List(Season.SUMMER)
  val SPECIAL_BONUS = 1.15
  val NUTRIENT_PROFILE = Map("Carbohydrates" -> 3.9, "Protein" -> 0.9)
  val VITAMIN_CONTENT = "Vitamin C, K, Lycopene"

  def apply(): Tomato = new Tomato()
  def forSummer(): Tomato = withSeason(Season.SUMMER) // Convenience method!
  def withSeason(season: Season): Tomato =
    val tomato = new Tomato()
    tomato.setPlantedSeason(season)
    tomato

/** Carrot - Essential root vegetable for gameplay variety */
class Carrot
    extends Vegetable(
      "Carrots",
      PlantType.Carrot,
      Carrot.BASE_YIELD,
      Carrot.GROWTH_TIME
    ):
  def getOptimalSeasons: List[Season] = Carrot.OPTIMAL_SEASONS
  def getSpecialBonus: Double = Carrot.SPECIAL_BONUS
  def getNutrientProfile: Map[String, Double] = Carrot.NUTRIENT_PROFILE
  def getVitaminContent: String = Carrot.VITAMIN_CONTENT

object Carrot:
  // Reliable spring/autumn crop - good for beginners
  val BASE_YIELD = 25.0
  val GROWTH_TIME = 3
  val OPTIMAL_SEASONS = List(Season.SPRING, Season.AUTUMN)
  val SPECIAL_BONUS = 1.0
  val NUTRIENT_PROFILE = Map("Carbohydrates" -> 9.6, "Protein" -> 0.9)
  val VITAMIN_CONTENT = "Vitamin A, K, Beta-Carotene"

  def apply(): Carrot = new Carrot()
  def withSeason(season: Season): Carrot =
    val carrot = new Carrot()
    carrot.setPlantedSeason(season)
    carrot

/** Beetroot - Minor vegetable, basic implementation */

/** Beetroot - Minor vegetable, basic implementation */
class Beetroot extends Vegetable("Beetroot", PlantType.Beetroot, 20.0, 3):
  def getOptimalSeasons: List[Season] = List(Season.AUTUMN)
  def getSpecialBonus: Double = 1.0
  def getNutrientProfile: Map[String, Double] =
    Map("Fiber" -> 2.8, "Folate" -> 0.15)
  def getVitaminContent: String = "Folate, Manganese"

/** Cabbage - Background vegetable */
class Cabbage extends Vegetable("Cabbage", PlantType.Cabbage, 35.0, 4):
  def getOptimalSeasons: List[Season] = List(Season.AUTUMN, Season.WINTER)
  def getSpecialBonus: Double = 1.05
  def getNutrientProfile: Map[String, Double] =
    Map("Vitamin C" -> 36.6, "Vitamin K" -> 76.0)
  def getVitaminContent: String = "Vitamin C, K, Folate"

/** Onion - Supporting vegetable */
class Onion extends Vegetable("Onion", PlantType.Onion, 28.0, 4):
  def getOptimalSeasons: List[Season] = List(Season.SPRING, Season.SUMMER)
  def getSpecialBonus: Double = 1.1
  def getNutrientProfile: Map[String, Double] =
    Map("Quercetin" -> 0.3, "Sulfur" -> 0.15)
  def getVitaminContent: String = "Vitamin C, B6"

/** Peas & Beans - Multi-harvest legumes, kept simple */
class Peas extends Legume("Peas", PlantType.Peas, 22.0, 3):
  setupMultiHarvest() // Enable multi-harvest for peas

  def getOptimalSeasons: List[Season] = List(Season.SPRING)
  def getSpecialBonus: Double = 1.2
  def getProteinContent: Double = 5.4
  def getNitrogenFixation: Double = 8.0

class Beans extends Legume("Beans", PlantType.Beans, 25.0, 4):
  setupMultiHarvest() // Enable multi-harvest for beans

  def getOptimalSeasons: List[Season] = List(Season.SUMMER)
  def getSpecialBonus: Double = 1.25
  def getProteinContent: Double = 8.7
  def getNitrogenFixation: Double = 12.0

class Cucumber extends Vegetable("Cucumber", PlantType.Cucumber, 30.0, 3):
  setupMultiHarvest() // Enable multi-harvest for cucumber

  def getOptimalSeasons: List[Season] = List(Season.SUMMER)
  def getSpecialBonus: Double = 1.1
  def getNutrientProfile: Map[String, Double] =
    Map("Water" -> 95.0, "Vitamin K" -> 16.4)
  def getVitaminContent: String = "Vitamin K, Potassium"

/** Spinach - Fast-growing leafy green, frequently used in gameplay */
class Spinach
    extends Vegetable(
      "Spinach",
      PlantType.Spinach,
      Spinach.BASE_YIELD,
      Spinach.GROWTH_TIME
    ):
  def getOptimalSeasons: List[Season] = Spinach.OPTIMAL_SEASONS
  def getSpecialBonus: Double = Spinach.SPECIAL_BONUS
  def getNutrientProfile: Map[String, Double] = Spinach.NUTRIENT_PROFILE
  def getVitaminContent: String = Spinach.VITAMIN_CONTENT

object Spinach:
  // Quick-growing crop - great for new players!
  val BASE_YIELD = 18.0
  val GROWTH_TIME = 2 // Fastest growing crop
  val OPTIMAL_SEASONS = List(Season.SPRING, Season.AUTUMN)
  val SPECIAL_BONUS = 1.15
  val NUTRIENT_PROFILE = Map("Iron" -> 2.7, "Vitamin K" -> 483.0)
  val VITAMIN_CONTENT = "Iron, Vitamin K, Folate"

  def apply(): Spinach = new Spinach()
  def forSpring(): Spinach = withSeason(Season.SPRING)
  def forAutumn(): Spinach = withSeason(Season.AUTUMN)
  def withSeason(season: Season): Spinach =
    val spinach = new Spinach()
    spinach.setPlantedSeason(season)
    spinach

class Strawberry
    extends Fruit(
      "Strawberry",
      PlantType.Strawberry,
      Strawberry.BASE_YIELD,
      Strawberry.GROWTH_TIME
    ):
  setupMultiHarvest() // Enable multi-harvest for strawberries

  def getOptimalSeasons: List[Season] = Strawberry.OPTIMAL_SEASONS
  def getSpecialBonus: Double = Strawberry.SPECIAL_BONUS
  def getSweetness: Double = Strawberry.SWEETNESS
  def getFruitCategory: String = Strawberry.FRUIT_CATEGORY

object Strawberry:
  val BASE_YIELD = 15.0
  val GROWTH_TIME = 3
  val OPTIMAL_SEASONS = List(Season.SPRING, Season.SUMMER)
  val SPECIAL_BONUS = 1.3
  val SWEETNESS = 4.9
  val FRUIT_CATEGORY = "Berry"

  def apply(): Strawberry = new Strawberry()
  def forSpring(): Strawberry = withSeason(Season.SPRING)
  def forSummer(): Strawberry = withSeason(Season.SUMMER)
  def withSeason(season: Season): Strawberry =
    val strawberry = new Strawberry()
    strawberry.setPlantedSeason(season)
    strawberry

class Grapes extends Fruit("Grapes", PlantType.Grapes, 20.0, 5):
  setupMultiHarvest() // Enable multi-harvest for grapes

  def getOptimalSeasons: List[Season] = List(Season.SUMMER, Season.AUTUMN)
  def getSpecialBonus: Double = 1.4
  def getSweetness: Double = 16.0
  def getFruitCategory: String = "Vine Fruit"

class Pumpkin
    extends Vegetable(
      "Pumpkin",
      PlantType.Pumpkin,
      Pumpkin.BASE_YIELD,
      Pumpkin.GROWTH_TIME
    ):
  def getOptimalSeasons: List[Season] = Pumpkin.OPTIMAL_SEASONS
  def getSpecialBonus: Double = Pumpkin.SPECIAL_BONUS
  def getNutrientProfile: Map[String, Double] = Pumpkin.NUTRIENT_PROFILE
  def getVitaminContent: String = Pumpkin.VITAMIN_CONTENT

object Pumpkin:
  val BASE_YIELD = 50.0
  val GROWTH_TIME = 6
  val OPTIMAL_SEASONS = List(Season.AUTUMN)
  val SPECIAL_BONUS = 1.2
  val NUTRIENT_PROFILE = Map("Beta-carotene" -> 3100.0, "Fiber" -> 1.0)
  val VITAMIN_CONTENT = "Vitamin A, C, Potassium"

  def apply(): Pumpkin = new Pumpkin()
  def forAutumn(): Pumpkin = withSeason(Season.AUTUMN)
  def withSeason(season: Season): Pumpkin =
    val pumpkin = new Pumpkin()
    pumpkin.setPlantedSeason(season)
    pumpkin

class Broccoli extends Vegetable("Broccoli", PlantType.Broccoli, 25.0, 3):
  def getOptimalSeasons: List[Season] = List(Season.SPRING, Season.AUTUMN)
  def getSpecialBonus: Double = 1.25
  def getNutrientProfile: Map[String, Double] =
    Map("Vitamin C" -> 89.2, "Vitamin K" -> 101.6)
  def getVitaminContent: String = "Vitamin C, K, Folate"

class Barley extends Cereal("Barley", PlantType.Barley, 42.0, 4):
  def getOptimalSeasons: List[Season] = List(Season.SPRING)
  def getSpecialBonus: Double = 1.05
  def getGrainType: String = "Barley grain"
  def getProteinContent: Double = 12.5

class Rye extends Cereal("Rye", PlantType.Rye, 38.0, 4):
  def getOptimalSeasons: List[Season] = List(Season.SPRING, Season.AUTUMN)
  def getSpecialBonus: Double = 1.0
  def getGrainType: String = "Rye grain"
  def getProteinContent: Double = 10.3

/** Plant factory for creating different plant types
  *
  * Notice how the factory uses companion objects for core crops but falls back
  * to basic constructors for less-used plants. This is practical OOP design -
  * sophisticated where it matters, simple where it doesn't!
  */
object PlantFactory:
  def createPlant(plantType: PlantType): Option[Plant] =
    plantType match
      // Core gameplay crops with companion objects
      case PlantType.Wheat      => Some(Wheat())
      case PlantType.Corn       => Some(Corn())
      case PlantType.Tomato     => Some(Tomato())
      case PlantType.Carrot     => Some(Carrot())
      case PlantType.Spinach    => Some(Spinach())
      case PlantType.Strawberry => Some(Strawberry())
      case PlantType.Pumpkin    => Some(Pumpkin())

      // Supporting crops with basic constructors
      case PlantType.Rice      => Some(new Rice())
      case PlantType.Soybean   => Some(new Soybean())
      case PlantType.Lentils   => Some(new Lentils())
      case PlantType.Chickpeas => Some(new Chickpeas())
      case PlantType.Beetroot  => Some(new Beetroot())
      case PlantType.Cabbage   => Some(new Cabbage())
      case PlantType.Onion     => Some(new Onion())
      case PlantType.Peas      => Some(new Peas())
      case PlantType.Beans     => Some(new Beans())
      case PlantType.Cucumber  => Some(new Cucumber())
      case PlantType.Grapes    => Some(new Grapes())
      case PlantType.Broccoli  => Some(new Broccoli())
      case PlantType.Barley    => Some(new Barley())
      case PlantType.Rye       => Some(new Rye())

  /** Create plant with optimal season set - enhanced for core crops */
  def createPlantForSeason(
      plantType: PlantType,
      season: Season
  ): Option[Plant] =
    plantType match
      // Use companion object factory methods for main crops
      case PlantType.Wheat      => Some(Wheat.withSeason(season))
      case PlantType.Corn       => Some(Corn.withSeason(season))
      case PlantType.Tomato     => Some(Tomato.withSeason(season))
      case PlantType.Carrot     => Some(Carrot.withSeason(season))
      case PlantType.Spinach    => Some(Spinach.withSeason(season))
      case PlantType.Strawberry => Some(Strawberry.withSeason(season))
      case PlantType.Pumpkin    => Some(Pumpkin.withSeason(season))
      case _ =>
        createPlant(plantType).map { plant =>
          plant.setPlantedSeason(season)
          plant
        }

  /** Create plant with optimal season automatically selected
    *
    * This method shows off the convenience methods from companion objects!
    */
  def createOptimalPlant(
      plantType: PlantType,
      currentSeason: Season
  ): Option[Plant] =
    plantType match
      case PlantType.Tomato if currentSeason == Season.SUMMER =>
        Some(Tomato.forSummer())
      case PlantType.Spinach if currentSeason == Season.SPRING =>
        Some(Spinach.forSpring())
      case PlantType.Spinach if currentSeason == Season.AUTUMN =>
        Some(Spinach.forAutumn())
      case PlantType.Strawberry if currentSeason == Season.SPRING =>
        Some(Strawberry.forSpring())
      case PlantType.Strawberry if currentSeason == Season.SUMMER =>
        Some(Strawberry.forSummer())
      case PlantType.Pumpkin if currentSeason == Season.AUTUMN =>
        Some(Pumpkin.forAutumn())
      case _ => createPlantForSeason(plantType, currentSeason)

  def getAvailablePlants: List[PlantType] =
    List(
      PlantType.Wheat,
      PlantType.Rice,
      PlantType.Corn,
      PlantType.Soybean,
      PlantType.Lentils,
      PlantType.Chickpeas,
      PlantType.Spinach,
      PlantType.Tomato,
      PlantType.Carrot,
      PlantType.Beetroot,
      PlantType.Cabbage,
      PlantType.Onion,
      PlantType.Peas,
      PlantType.Beans,
      PlantType.Cucumber,
      PlantType.Strawberry,
      PlantType.Grapes,
      PlantType.Pumpkin,
      PlantType.Broccoli,
      PlantType.Barley,
      PlantType.Rye
    )

  def getCerealPlants: List[PlantType] =
    List(PlantType.Wheat, PlantType.Rice, PlantType.Corn)

  def getLegumePlants: List[PlantType] =
    List(PlantType.Soybean, PlantType.Lentils, PlantType.Chickpeas)

  def getVegetablePlants: List[PlantType] =
    List(PlantType.Spinach, PlantType.Tomato, PlantType.Carrot)

end PlantFactory

/** Enhanced Plant implementations with simple design */

class Chickpeas extends Legume("Chickpeas", PlantType.Chickpeas, 28.0, 4):
  def getOptimalSeasons: List[Season] = List(Season.SPRING, Season.AUTUMN)
  def getSpecialBonus: Double = 1.2
  def getNitrogenFixation: Double = 10.0
  def getProteinContent: Double = 20.0
