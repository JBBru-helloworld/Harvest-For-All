package harvestforall.gui.scenes

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ListView, Alert}
import scalafx.scene.layout.{BorderPane, VBox, HBox, GridPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.geometry.{Insets, Pos}
import scalafx.collections.ObservableBuffer
import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType
import harvestforall.gui.managers.SceneManager
import harvestforall.core.{GameState, SustainabilityAction}
import harvestforall.gui.utils.FontManager

/** Village Scene for selling crops and trading
  *
  * Handles crop sales, trading, and progress tracking
  */
class VillageScene(
    sceneManager: SceneManager,
    gameState: GameState,
    onReturnToFarm: Option[() => Unit] = None
):

  private val SCENE_WIDTH = 1200
  private val SCENE_HEIGHT = 800

  // Store references to UI components that need updating
  private var inventoryListView: ListView[String] = _
  private var moneyLabel: Label = _
  private var villageInfoSection: VBox = _
  private var salesSection: VBox = _
  private var satisfactionLabel: Label = _
  private var salesLabel: Label = _
  private var sustainabilityLabel: Label = _
  private var sellAllButtons: Map[String, Button] = Map.empty

  // Crop prices for village market (using lowercase names to match harvested crops)
  private val cropPrices: Map[String, Double] = Map(
    "wheat" -> 12.0,
    "rice" -> 15.0,
    "corn" -> 10.0,
    "soybean" -> 18.0,
    "lentils" -> 20.0,
    "chickpeas" -> 16.0,
    "spinach" -> 8.0,
    "tomato" -> 14.0,
    "carrot" -> 8.0,
    "lettuce" -> 10.0
  )

  def getScene: Scene =
    val root = createVillageLayout()
    updateVillageInfo()
    new Scene(root, SCENE_WIDTH, SCENE_HEIGHT)

  private def createVillageLayout(): BorderPane =
    val root = new BorderPane()

    // Set village theme styling
    root.style = "-fx-background-color: #8B4513; -fx-padding: 10px;"

    // Title
    val titleLabel = new Label("Village Market")
    titleLabel.font = FontManager.headerFont
    titleLabel.textFill = Color.White
    titleLabel.alignment = Pos.Center

    // Main content area
    val contentArea = new HBox(20)
    contentArea.alignment = Pos.Center
    contentArea.padding = Insets(20)

    // Inventory section
    val inventorySection = createInventorySection()

    // Sales section
    val salesSection = createSalesSection()

    // Village info section
    val villageInfoSection = createVillageInfoSection()

    contentArea.children.addAll(
      inventorySection,
      salesSection,
      villageInfoSection
    )

    // Bottom controls
    val bottomControls = createBottomControls()

    root.top = titleLabel
    root.center = contentArea
    root.bottom = bottomControls

    root

  private def createInventorySection(): VBox =
    val section = new VBox(10)
    section.style =
      "-fx-background-color: #D2B48C; -fx-padding: 15px; -fx-border-color: #8B4513; -fx-border-width: 2px;"

    val titleLabel = new Label("Your Sellable Crops")
    titleLabel.font = FontManager.subtitleFont
    titleLabel.textFill = Color.SaddleBrown

    inventoryListView = new ListView[String]()
    inventoryListView.prefWidth = 200
    inventoryListView.prefHeight = 250
    // Apply game font to ListView
    inventoryListView.style =
      s"-fx-font-family: '${FontManager.labelFont.family}'; -fx-font-size: ${FontManager.labelFont.size}px;"
    updateInventoryList(inventoryListView)

    // Add sell selected button
    val sellSelectedButton = new Button("Sell Selected")
    sellSelectedButton.font = FontManager.buttonFont
    sellSelectedButton.style =
      "-fx-background-color: #228B22; -fx-text-fill: white; -fx-padding: 8px 16px;"
    sellSelectedButton.onAction = _ => sellSelectedCrop(inventoryListView)

    // Initially disable if no selection
    sellSelectedButton.disable = true

    // Enable/disable button based on selection
    inventoryListView.selectionModel().selectedItem.onChange {
      (_, _, newValue) =>
        sellSelectedButton.disable =
          newValue == null || newValue == "No sellable crops in inventory"
    }

    section.children.addAll(titleLabel, inventoryListView, sellSelectedButton)
    section

  private def createSalesSection(): VBox =
    val section = new VBox(10)
    section.style =
      "-fx-background-color: #F5DEB3; -fx-padding: 15px; -fx-border-color: #8B4513; -fx-border-width: 2px;"

    val titleLabel = new Label("Market Prices")
    titleLabel.font = FontManager.subtitleFont
    titleLabel.textFill = Color.SaddleBrown

    val pricesGrid = new GridPane()
    pricesGrid.hgap = 10
    pricesGrid.vgap = 5

    var row = 0
    val buttons = scala.collection.mutable.Map[String, Button]()

    for (crop, price) <- cropPrices do
      val cropLabel = new Label(crop)
      cropLabel.font = FontManager.labelFont

      val priceLabel = new Label(f"$$${price}%.2f each")
      priceLabel.font = FontManager.labelFont

      val sellButton = new Button("Sell All")
      sellButton.onAction = _ => sellCrop(crop)
      sellButton.disable =
        !gameState.inventory.contains(crop) || gameState.inventory(crop) <= 0

      buttons(crop) = sellButton

      pricesGrid.add(cropLabel, 0, row)
      pricesGrid.add(priceLabel, 1, row)
      pricesGrid.add(sellButton, 2, row)
      row += 1

    sellAllButtons = buttons.toMap
    section.children.addAll(titleLabel, pricesGrid)
    section

  private def createVillageInfoSection(): VBox =
    villageInfoSection = new VBox(10)
    villageInfoSection.style =
      "-fx-background-color: #DEB887; -fx-padding: 15px; -fx-border-color: #8B4513; -fx-border-width: 2px;"

    val titleLabel = new Label("Village Status")
    titleLabel.font = FontManager.subtitleFont
    titleLabel.textFill = Color.SaddleBrown

    satisfactionLabel = new Label()
    satisfactionLabel.font = FontManager.labelFont
    satisfactionLabel.textFill = Color.DarkGreen

    salesLabel = new Label()
    salesLabel.font = FontManager.labelFont
    salesLabel.textFill = Color.DarkGreen

    sustainabilityLabel = new Label()
    sustainabilityLabel.font = FontManager.labelFont
    sustainabilityLabel.textFill = Color.DarkGreen

    // Populate with actual data
    updateVillageLabels()

    villageInfoSection.children.addAll(
      titleLabel,
      satisfactionLabel,
      salesLabel,
      sustainabilityLabel
    )
    villageInfoSection

  private def createBottomControls(): HBox =
    val controls = new HBox(20)
    controls.alignment = Pos.Center
    controls.padding = Insets(15)

    val backToFarmButton = new Button("Return to Farm")
    backToFarmButton.font = FontManager.buttonFont
    backToFarmButton.style =
      "-fx-background-color: #228B22; -fx-text-fill: white; -fx-padding: 10px 20px;"
    backToFarmButton.onAction = _ => returnToFarm()

    moneyLabel = new Label()
    moneyLabel.font = FontManager.buttonFont
    moneyLabel.textFill = Color.White
    updateMoneyLabel(moneyLabel)

    controls.children.addAll(backToFarmButton, moneyLabel)
    controls

  private def sellSelectedCrop(inventoryList: ListView[String]): Unit =
    val selectedItem = inventoryList.selectionModel().selectedItem.value
    if (
      selectedItem != null && selectedItem != "No sellable crops in inventory"
    ) then
      val parts = selectedItem.split(": ")
      if (parts.length == 2) then
        val cropName = parts(0)
        val quantity = parts(1).toIntOption.getOrElse(0)

        if (quantity > 0 && cropPrices.contains(cropName)) then
          val pricePerUnit = cropPrices(cropName)
          val totalPrice = pricePerUnit * quantity

          // Use GameState methods to sell the crop
          if gameState.sellCrop(cropName, quantity, pricePerUnit) then
            // Update sustainability for selling to village
            gameState.updateSustainability(
              SustainabilityAction.SellToVillage,
              quantity * 0.1
            )

            // Update display
            refreshUIComponents()

            println(s"Sold $quantity $cropName for $$${totalPrice}")
          else println(s"Failed to sell $cropName - insufficient inventory")
        else
          println(s"Cannot sell $cropName: not a sellable crop or no quantity")
      else println(s"Invalid item format: $selectedItem")

  private def updateInventoryList(listView: ListView[String]): Unit =
    val inventoryItems = ObservableBuffer[String]()
    for (crop, quantity) <- gameState.inventory do
      // Only show crops (not seeds) that can be sold
      if quantity > 0 && !crop.endsWith("_seeds") && cropPrices.contains(crop)
      then inventoryItems += s"$crop: $quantity"

    if inventoryItems.isEmpty then
      inventoryItems += "No sellable crops in inventory"

    listView.items = inventoryItems

    // Apply custom font to ListView items
    listView.style =
      s"-fx-font-family: '${FontManager.labelFont.family}'; -fx-font-size: ${FontManager.labelFont.size}px;"

  private def updateMoneyLabel(label: Label): Unit =
    label.text = f"Money: $$${gameState.currency}%.2f"

  private def updateVillageInfo(): Unit =
    updateVillageLabels()

  /** Update village status labels with current game state data */
  private def updateVillageLabels(): Unit =
    if satisfactionLabel != null then
      satisfactionLabel.text =
        f"Village Satisfaction: ${gameState.villageSatisfaction}%.1f%%"
      // Color based on satisfaction level
      if gameState.villageSatisfaction >= 75 then
        satisfactionLabel.textFill = Color.DarkGreen
      else if gameState.villageSatisfaction >= 50 then
        satisfactionLabel.textFill = Color.Orange
      else satisfactionLabel.textFill = Color.DarkRed

    if salesLabel != null then
      salesLabel.text = f"Total Sales: $$${gameState.totalSales}%.2f"
      salesLabel.textFill = Color.DarkGreen

    if sustainabilityLabel != null then
      sustainabilityLabel.text =
        f"Sustainability: ${gameState.globalSustainabilityRating}%.1f%%"
      // Color based on sustainability level
      if gameState.globalSustainabilityRating >= 80 then
        sustainabilityLabel.textFill = Color.DarkGreen
      else if gameState.globalSustainabilityRating >= 60 then
        sustainabilityLabel.textFill = Color.Orange
      else sustainabilityLabel.textFill = Color.DarkRed

  private def sellCrop(cropType: String): Unit =
    val quantity = gameState.inventory.getOrElse(cropType, 0)
    if quantity <= 0 then
      showAlert("No Stock", s"You don't have any $cropType to sell!")
      return

    val pricePerUnit = cropPrices.getOrElse(cropType, 0.0)
    val totalPrice = quantity * pricePerUnit

    // Update game state
    gameState.sellCrop(cropType, quantity, pricePerUnit)

    // Update sustainability for selling to village
    gameState.updateSustainability(
      SustainabilityAction.SellToVillage,
      quantity * 0.1
    )

    // Refresh UI components
    refreshUIComponents()

    // Show success message
    showAlert(
      "Sale Successful",
      f"Sold $quantity $cropType for $$${totalPrice}%.2f!\n" +
        f"Village satisfaction increased!\n" +
        f"Current money: $$${gameState.currency}%.2f"
    )

  private def showAlert(title: String, message: String): Unit =
    val alert = new Alert(AlertType.Information)
    alert.title = title
    alert.headerText = Some(null)
    alert.contentText = message
    alert.showAndWait()

  private def returnToFarm(): Unit =
    gameState.setLocation("Farm")
    // Call the callback before switching scenes to sync inventory
    onReturnToFarm.foreach(_())
    sceneManager.switchTo(sceneManager.getScene("FarmGame").get, "FarmGame")

  /** Refresh all UI components after a sale */
  private def refreshUIComponents(): Unit =
    // Update money display
    if moneyLabel != null then updateMoneyLabel(moneyLabel)

    // Update inventory list
    if inventoryListView != null then updateInventoryList(inventoryListView)

    // Update village info section (for satisfaction display)
    if villageInfoSection != null then updateVillageInfo()

    // Update sell all buttons based on current inventory
    for (crop, button) <- sellAllButtons do
      button.disable =
        !gameState.inventory.contains(crop) || gameState.inventory(crop) <= 0

end VillageScene

def cleanup(): Unit =
  // Clean up any resources if needed
  ()
