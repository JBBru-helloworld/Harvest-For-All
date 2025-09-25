package harvestforall.gui.scenes

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{
  VBox,
  StackPane,
  Background,
  BackgroundFill,
  CornerRadii,
  Border,
  BorderStroke,
  BorderWidths,
  BorderStrokeStyle
}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.TextAlignment
import scalafx.scene.paint.{Color, LinearGradient, CycleMethod, Stop}
import scalafx.scene.effect.DropShadow
import scalafx.Includes._
import harvestforall.gui.managers.SceneManager
import harvestforall.core.GameState
import harvestforall.gui.utils.FontManager

class AboutScene(sceneManager: SceneManager, gameState: GameState):

  private val SCENE_WIDTH = 1200
  private val SCENE_HEIGHT = 800

  def getScene: Scene =
    new Scene(SCENE_WIDTH, SCENE_HEIGHT):
      fill = Color.web("#1B4F20")
      root = createAboutLayout()

  private def createAboutLayout(): StackPane =
    val container = new StackPane()
    container.background = Background(
      Array(
        BackgroundFill(
          LinearGradient(
            startX = 0,
            startY = 0,
            endX = 0,
            endY = 1,
            proportional = true,
            cycleMethod = CycleMethod.NoCycle,
            stops = List(
              Stop(0, Color.web("#2E7D32")),
              Stop(0.5, Color.web("#1B5E20")),
              Stop(1, Color.web("#0D4F14"))
            )
          ),
          CornerRadii.Empty,
          Insets.Empty
        )
      )
    )

    val contentBox = new VBox():
      spacing = 25
      alignment = Pos.TopCenter
      padding = Insets(40, 60, 40, 60)
      maxWidth = 900
      background = Background(
        Array(
          BackgroundFill(
            Color.web("#000000", 0.25),
            CornerRadii(15),
            Insets.Empty
          )
        )
      )
      border = Border(
        BorderStroke(
          Color.web("#4CAF50"),
          BorderStrokeStyle.Solid,
          CornerRadii(15),
          BorderWidths(3)
        )
      )

    val title = new Label("About Harvest for All"):
      font = FontManager.titleFont
      textFill = Color.web("#E8F5E8")
      effect = new DropShadow:
        blurType = scalafx.scene.effect.BlurType.Gaussian
        color = Color.Black
        offsetX = 3
        offsetY = 3
        radius = 4

    val subtitle = new Label(
      "UN Sustainable Development Goal 2 - Agricultural Management System"
    ):
      font = FontManager.subHeaderFont
      textFill = Color.web("#81C784")
      wrapText = true
      maxWidth = 800
      textAlignment = TextAlignment.Center

    val contentText = new Label:
      text = s"""
        |${"-" * 80}
        | HARVEST FOR ALL 
        |A Sustainable Farming Adventure
        |
        |Author: Joshua Bonham (23020050)
        |Course: PRG2104 - Object-Oriented Programming
        |University Assignment: Semester 6
        |
        |Mission Statement:
        |Harvest for All is dedicated to UN Sustainable Development Goal 2: 
        |"End hunger, achieve food security, improve nutrition, and promote 
        |sustainable agriculture." Experience the challenges and rewards of 
        |modern sustainable farming!
        |
        | How to Play:
        |• Use WASD keys to move your farmer around the field
        |• Press E to plant seeds or harvest mature crops
        |• Press R to water your growing plants
        |• Press I to open your inventory and manage resources
        |• Visit the village market to buy seeds and sell crops
        |• Monitor your sustainability rating and earnings
        |
        | Game Features:
        |• Multiple crop varieties: wheat, corn, carrots, tomatoes, spinach
        |• Dynamic crop growth system with realistic watering needs
        |• Village marketplace with economic simulation
        |• Sustainability tracking - make eco-friendly choices!
        |• Resource management and strategic planning
        |• Beautiful pixel art graphics and smooth gameplay
        |
        | Educational Goals:
        |Learn about sustainable agriculture, resource management, and 
        |the importance of food security while having fun!
        |
        | Tips for Success:
        |• Plant diverse crops for sustainability bonuses
        |• Water regularly but don't over-water
        |• Save your progress frequently
        |• Experiment with different farming strategies
        |${"-" * 80}
        |""".stripMargin
      font = FontManager.labelFont
      textFill = Color.web("#E0F7E0")
      wrapText = true
      maxWidth = 800
      lineSpacing = 1.5
      textAlignment = TextAlignment.Left

    val backButton = new Button("Back to Main Menu"):
      prefWidth = 250
      prefHeight = 50
      font = FontManager.buttonFont
      textFill = Color.White
      background = Background(
        Array(
          BackgroundFill(
            LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = CycleMethod.NoCycle,
              stops = List(
                Stop(0, Color.web("#4CAF50")),
                Stop(1, Color.web("#388E3C"))
              )
            ),
            CornerRadii(5),
            Insets.Empty
          )
        )
      )
      onAction = _ => sceneManager.switchToMainMenu()

      // Button hover effects
      onMouseEntered = _ =>
        background = Background(
          Array(
            BackgroundFill(
              LinearGradient(
                startX = 0,
                startY = 0,
                endX = 0,
                endY = 1,
                proportional = true,
                cycleMethod = CycleMethod.NoCycle,
                stops = List(
                  Stop(0, Color.web("#66BB6A")),
                  Stop(1, Color.web("#4CAF50"))
                )
              ),
              CornerRadii(5),
              Insets.Empty
            )
          )
        )

      onMouseExited = _ =>
        background = Background(
          Array(
            BackgroundFill(
              LinearGradient(
                startX = 0,
                startY = 0,
                endX = 0,
                endY = 1,
                proportional = true,
                cycleMethod = CycleMethod.NoCycle,
                stops = List(
                  Stop(0, Color.web("#4CAF50")),
                  Stop(1, Color.web("#388E3C"))
                )
              ),
              CornerRadii(5),
              Insets.Empty
            )
          )
        )

    val scrollPane = new ScrollPane:
      content = contentText
      fitToWidth = true
      maxHeight = 450
      style = "-fx-background: transparent; -fx-background-color: transparent;"
      vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded

    contentBox.children.addAll(title, subtitle, scrollPane, backButton)
    container.children.add(contentBox)
    StackPane.setAlignment(contentBox, Pos.Center)
    container
end AboutScene
