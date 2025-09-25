package harvestforall.gui.scenes

import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import scalafx.event.ActionEvent
import harvestforall.gui.managers.SceneManager
import harvestforall.gui.utils.FontManager
import harvestforall.core.GameState

/** Treasure Shop Scene for purchasing seeds after completing the quiz */
class TreasureShopScene(sceneManager: SceneManager, gameState: GameState)
    extends Scene(1000, 700) {

  // Seed shop data
  case class SeedItem(
      name: String,
      displayName: String,
      price: Double,
      description: String,
      quantity: Int = 5 // How many seeds you get for the price
  )

  // Available seeds in the treasure shop
  private val seedItems = List(
    SeedItem(
      "wheat_seeds",
      "Wheat Seeds",
      25.00,
      "Fast-growing grain crop. Perfect for beginners.",
      5
    ),
    SeedItem(
      "corn_seeds",
      "Corn Seeds",
      35.00,
      "High-yield crop that grows tall and strong.",
      4
    ),
    SeedItem(
      "tomato_seeds",
      "Tomato Seeds",
      45.00,
      "Nutritious fruit crop with vibrant red color.",
      3
    ),
    SeedItem(
      "carrot_seeds",
      "Carrot Seeds",
      30.00,
      "Root vegetable rich in vitamins and minerals.",
      4
    ),
    SeedItem(
      "spinach_seeds",
      "Spinach Seeds",
      40.00,
      "Leafy green superfood packed with iron.",
      4
    ),
    SeedItem(
      "rice_seeds",
      "Rice Seeds",
      50.00,
      "Staple grain crop that feeds billions worldwide.",
      3
    ),
    SeedItem(
      "cabbage_seeds",
      "Cabbage Seeds",
      35.00,
      "Hardy vegetable perfect for sustainable farming.",
      4
    )
  )

  // UI State
  private var selectedSeedIndex: Int = 0
  private var moneyLabel: Label = _
  private var seedButtons: List[Button] = _
  private var purchaseButton: Button = _
  private var quantityLabel: Label = _
  private var descriptionLabel: Label = _

  initializeUI()

  private def initializeUI(): Unit = {
    // Main container
    val mainVBox = new VBox {
      spacing = 30
      padding = Insets(40)
      alignment = Pos.Center
      style = s"""
        -fx-background-color: linear-gradient(to bottom, #8B4513, #654321);
      """
    }

    // Title
    val titleLabel = new Label("Treasure Seed Shop") {
      font = FontManager.titleFont
      textFill = Color.Gold
      style = s"""
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 4, 0.5, 2, 2);
        -fx-background-color: rgba(139, 69, 19, 0.8);
        -fx-background-radius: 15px;
        -fx-padding: 15px 30px;
        -fx-border-color: gold;
        -fx-border-width: 2px;
        -fx-border-radius: 15px;
      """
    }

    val subtitleLabel = new Label(
      "Special seeds unlocked by completing the SDG 2 quiz!"
    ) {
      font = FontManager.labelFont
      textFill = Color.web("#FFE4B5")
      textAlignment = scalafx.scene.text.TextAlignment.Center
    }

    // Money display
    moneyLabel = new Label(f"Money: $$${gameState.currency}%.2f") {
      font = FontManager.subHeaderFont
      textFill = Color.LightGreen
      style = s"""
        -fx-background-color: rgba(0, 0, 0, 0.6);
        -fx-background-radius: 10px;
        -fx-padding: 10px 20px;
      """
    }

    // Shop container
    val shopContainer = new VBox {
      spacing = 20
      padding = Insets(30)
      style = s"""
        -fx-background-color: rgba(255, 255, 255, 0.9);
        -fx-background-radius: 15px;
        -fx-border-color: #8B4513;
        -fx-border-width: 3px;
        -fx-border-radius: 15px;
      """
      maxWidth = 800
    }

    // Seeds grid
    val seedsGrid = new GridPane {
      hgap = 15
      vgap = 15
      alignment = Pos.Center
    }

    // Create seed buttons
    seedButtons = seedItems.zipWithIndex.map { case (seed, index) =>
      val button = new Button(
        f"${seed.displayName}\n$$${seed.price}%.2f\n(${seed.quantity} seeds)"
      ) {
        prefWidth = 180
        prefHeight = 90
        font = FontManager.buttonFont
        wrapText = true
        style = if (index == selectedSeedIndex) {
          s"""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-border-color: gold;
            -fx-border-width: 2px;
          """
        } else {
          s"""
            -fx-background-color: #2196F3;
            -fx-text-fill: white;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
          """
        }
        onAction = (_: ActionEvent) => selectSeed(index)
      }

      // Add to grid (4 columns)
      val col = index % 4
      val row = index / 4
      seedsGrid.add(button, col, row)

      button
    }

    // Selected seed details
    val detailsContainer = new VBox {
      spacing = 15
      padding = Insets(20)
      style = s"""
        -fx-background-color: rgba(144, 238, 144, 0.3);
        -fx-background-radius: 10px;
        -fx-border-color: green;
        -fx-border-width: 1px;
        -fx-border-radius: 10px;
      """
    }

    descriptionLabel = new Label(seedItems(selectedSeedIndex).description) {
      font = FontManager.labelFont
      textFill = Color.DarkBlue
      wrapText = true
      maxWidth = 750
      textAlignment = scalafx.scene.text.TextAlignment.Center
    }

    quantityLabel = new Label(
      s"You will receive: ${seedItems(selectedSeedIndex).quantity} seeds"
    ) {
      font = FontManager.buttonFont
      textFill = Color.DarkGreen
    }

    detailsContainer.children = List(descriptionLabel, quantityLabel)

    shopContainer.children = List(
      new Label("Choose your seeds:") {
        font = FontManager.subHeaderFont
        textFill = Color.DarkBlue
      },
      seedsGrid,
      detailsContainer
    )

    // Purchase controls
    val purchaseContainer = new HBox {
      spacing = 20
      alignment = Pos.Center
    }

    purchaseButton = new Button("Purchase Selected") {
      font = FontManager.buttonFont
      prefWidth = 200
      prefHeight = 50
      style = s"""
        -fx-background-color: #4CAF50;
        -fx-text-fill: white;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
        -fx-font-weight: bold;
      """
      onAction = (_: ActionEvent) => purchaseSeeds()
    }

    val backButton = new Button("Leave Shop") {
      font = FontManager.buttonFont
      prefWidth = 150
      prefHeight = 50
      style = s"""
        -fx-background-color: #f44336;
        -fx-text-fill: white;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
      """
      onAction = (_: ActionEvent) => {
        // Return to previous scene (farm map)
        sceneManager.goBack()
      }
    }

    purchaseContainer.children = List(purchaseButton, backButton)

    // Add all components
    mainVBox.children = List(
      titleLabel,
      subtitleLabel,
      moneyLabel,
      shopContainer,
      purchaseContainer
    )

    fill = Color.web("#8B4513")
    root = mainVBox

    // Update initial selection
    updateSelectedSeed()
  }

  private def selectSeed(index: Int): Unit = {
    selectedSeedIndex = index
    updateSelectedSeed()
  }

  private def updateSelectedSeed(): Unit = {
    val selectedSeed = seedItems(selectedSeedIndex)

    // Update button styles
    seedButtons.zipWithIndex.foreach { case (button, index) =>
      button.style = if (index == selectedSeedIndex) {
        s"""
          -fx-background-color: #4CAF50;
          -fx-text-fill: white;
          -fx-border-radius: 8px;
          -fx-background-radius: 8px;
          -fx-border-color: gold;
          -fx-border-width: 2px;
        """
      } else {
        s"""
          -fx-background-color: #2196F3;
          -fx-text-fill: white;
          -fx-border-radius: 8px;
          -fx-background-radius: 8px;
        """
      }
    }

    // Update details
    descriptionLabel.text = selectedSeed.description
    quantityLabel.text = s"You will receive: ${selectedSeed.quantity} seeds"

    // Update purchase button state
    val canAfford = gameState.currency >= selectedSeed.price
    purchaseButton.disable = !canAfford

    if (!canAfford) {
      purchaseButton.style = s"""
        -fx-background-color: #CCCCCC;
        -fx-text-fill: #666666;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
      """
      purchaseButton.text = "Not Enough Money"
    } else {
      purchaseButton.style = s"""
        -fx-background-color: #4CAF50;
        -fx-text-fill: white;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
        -fx-font-weight: bold;
      """
      purchaseButton.text = "Purchase Selected"
    }
  }

  private def purchaseSeeds(): Unit = {
    val selectedSeed = seedItems(selectedSeedIndex)

    if (gameState.spendCurrency(selectedSeed.price)) {
      // Add seeds to inventory
      gameState.addToInventory(selectedSeed.name, selectedSeed.quantity)

      // Update money display
      moneyLabel.text = f"Money: $$${gameState.currency}%.2f"

      // Show success message
      val alert = new Alert(Alert.AlertType.Information) {
        title = "Purchase Successful!"
        headerText = "Seeds Purchased"
        contentText =
          f"You bought ${selectedSeed.quantity} ${selectedSeed.displayName} for $$${selectedSeed.price}%.2f!\nCheck your inventory (Press I) to see your new seeds."
      }
      alert.showAndWait()

      println(
        f"[TreasureShop] Purchased ${selectedSeed.quantity} ${selectedSeed.name} for $$${selectedSeed.price}%.2f"
      )
      println(f"[TreasureShop] New balance: $$${gameState.currency}%.2f")

      // Update button states
      updateSelectedSeed()

    } else {
      val alert = new Alert(Alert.AlertType.Warning) {
        title = "Insufficient Funds"
        headerText = "Not Enough Money"
        contentText =
          s"You need $$${selectedSeed.price} but only have $$${gameState.currency}.\nEarn more money by harvesting and selling crops!"
      }
      alert.showAndWait()
    }
  }
}
