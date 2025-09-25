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
import scala.util.Random

/** Quiz scene for treasure chest - SDG 2: Zero Hunger questions */
class TreasureQuizScene(sceneManager: SceneManager, gameState: GameState)
    extends Scene(1000, 700) {

  // Quiz data structure
  case class QuizQuestion(
      question: String,
      options: List[String],
      correctAnswer: Int,
      explanation: String
  )

  // 5 different quiz sets about SDG 2: Zero Hunger and Agricultural Management
  private val quizSets = List(
    // Quiz Set 1: Basic SDG 2 Knowledge
    List(
      QuizQuestion(
        "What is the main goal of SDG 2: Zero Hunger?",
        List(
          "End hunger and ensure access to safe, nutritious food for all",
          "Reduce poverty in urban areas",
          "Improve education quality",
          "Promote renewable energy"
        ),
        0,
        "SDG 2 aims to end hunger, achieve food security, improve nutrition, and promote sustainable agriculture."
      ),
      QuizQuestion(
        "Which farming practice helps maintain soil health for sustainable agriculture?",
        List(
          "Continuous monoculture",
          "Crop rotation",
          "Excessive pesticide use",
          "Removing all organic matter"
        ),
        1,
        "Crop rotation helps maintain soil nutrients, reduces pest buildup, and improves soil structure."
      ),
      QuizQuestion(
        "What percentage of the world's hungry people live in rural areas?",
        List(
          "25%",
          "50%",
          "75%",
          "90%"
        ),
        2,
        "About 75% of the world's hungry people live in rural areas, often dependent on agriculture for their livelihoods."
      )
    ),

    // Quiz Set 2: Sustainable Agriculture
    List(
      QuizQuestion(
        "What is precision agriculture?",
        List(
          "Farming without any technology",
          "Using technology to optimize field-level management",
          "Growing only one type of crop",
          "Farming in urban areas only"
        ),
        1,
        "Precision agriculture uses GPS, sensors, and data analytics to optimize farming practices and resource use."
      ),
      QuizQuestion(
        "Which nutrient is most commonly deficient in soils worldwide?",
        List(
          "Calcium",
          "Nitrogen",
          "Iron",
          "Magnesium"
        ),
        1,
        "Nitrogen is the most commonly deficient nutrient in agricultural soils, essential for plant growth and protein synthesis."
      ),
      QuizQuestion(
        "What is agroforestry?",
        List(
          "Cutting down all trees for farming",
          "Integrating trees and shrubs into agricultural systems",
          "Farming only forest products",
          "Using synthetic fertilizers exclusively"
        ),
        1,
        "Agroforestry combines agriculture and forestry to create more sustainable and productive land-use systems."
      )
    ),

    // Quiz Set 3: Food Security and Nutrition
    List(
      QuizQuestion(
        "What does food security include?",
        List(
          "Only having enough calories",
          "Access to safe, nutritious, and culturally appropriate food",
          "Eating only expensive foods",
          "Growing your own food exclusively"
        ),
        1,
        "Food security encompasses availability, access, utilization, and stability of safe, nutritious food for all people."
      ),
      QuizQuestion(
        "Which farming method typically uses the least water?",
        List(
          "Flood irrigation",
          "Sprinkler systems",
          "Drip irrigation",
          "Manual watering"
        ),
        2,
        "Drip irrigation delivers water directly to plant roots, reducing water waste and improving efficiency."
      ),
      QuizQuestion(
        "What is the 'hidden hunger'?",
        List(
          "Eating in secret",
          "Micronutrient deficiency despite adequate calories",
          "Hunger during night time",
          "Emotional eating"
        ),
        1,
        "Hidden hunger refers to micronutrient deficiencies that occur even when caloric intake appears adequate."
      )
    ),

    // Quiz Set 4: Climate and Agriculture
    List(
      QuizQuestion(
        "How does climate change affect agriculture?",
        List(
          "It only improves crop yields",
          "It has no effect on farming",
          "It changes precipitation patterns and increases extreme weather",
          "It only affects tropical regions"
        ),
        2,
        "Climate change alters weather patterns, increases droughts and floods, and affects crop productivity globally."
      ),
      QuizQuestion(
        "What is carbon sequestration in agriculture?",
        List(
          "Releasing carbon into the atmosphere",
          "Storing carbon in soil and plants",
          "Using more fossil fuels",
          "Removing all organic matter"
        ),
        1,
        "Carbon sequestration in agriculture involves practices that store carbon in soil and vegetation, helping mitigate climate change."
      ),
      QuizQuestion(
        "Which crop is known as a 'climate-smart' option for many regions?",
        List(
          "Cotton",
          "Tobacco",
          "Legumes (beans, peas)",
          "Sugar cane"
        ),
        2,
        "Legumes fix nitrogen in soil, require less fertilizer, and are more resilient to climate variations."
      )
    ),

    // Quiz Set 5: Technology and Innovation
    List(
      QuizQuestion(
        "What role does biotechnology play in achieving Zero Hunger?",
        List(
          "It has no role in agriculture",
          "It can develop drought-resistant and nutrient-rich crops",
          "It only increases costs",
          "It replaces all traditional farming"
        ),
        1,
        "Biotechnology can develop crops that are more resilient, nutritious, and suitable for challenging environments."
      ),
      QuizQuestion(
        "What is vertical farming?",
        List(
          "Farming on mountain slopes",
          "Growing crops in vertically stacked layers",
          "Planting trees vertically",
          "Traditional field farming"
        ),
        1,
        "Vertical farming grows crops in vertically stacked layers, often in controlled environments, maximizing space efficiency."
      ),
      QuizQuestion(
        "How can mobile technology help smallholder farmers?",
        List(
          "It cannot help farmers",
          "Only for entertainment",
          "Access to market prices, weather info, and farming advice",
          "Only for wealthy farmers"
        ),
        2,
        "Mobile technology provides farmers access to market information, weather forecasts, agricultural advice, and financial services."
      )
    )
  )

  // Current quiz state
  private var currentQuizSet: List[QuizQuestion] = selectRandomQuizSet()
  private var currentQuestionIndex = 0
  private var correctAnswers = 0
  private var selectedAnswer: Option[Int] = None

  // UI Components
  private var questionLabel: Label = _
  private var optionButtons: List[RadioButton] = _
  private var toggleGroup: ToggleGroup = _
  private var submitButton: Button = _
  private var nextButton: Button = _
  private var resultLabel: Label = _
  private var progressLabel: Label = _
  private var explanationLabel: Label = _

  initializeUI()

  private def selectRandomQuizSet(): List[QuizQuestion] = {
    val random = new Random()
    quizSets(random.nextInt(quizSets.length))
  }

  private def initializeUI(): Unit = {
    // Main container
    val mainVBox = new VBox {
      spacing = 25
      padding = Insets(40)
      alignment = Pos.Center
      style = s"""
        -fx-background-color: linear-gradient(to bottom, #8B4513, #654321);
      """
    }

    // Title
    val titleLabel = new Label("Treasure Chest Quiz") {
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
      "Answer 3 questions about SDG 2: Zero Hunger to unlock the treasure!"
    ) {
      font = FontManager.labelFont
      textFill = Color.web("#FFE4B5")
      textAlignment = scalafx.scene.text.TextAlignment.Center
    }

    // Progress indicator
    progressLabel = new Label(
      s"Question ${currentQuestionIndex + 1} of 3 | Correct: $correctAnswers/3"
    ) {
      font = FontManager.buttonFont
      textFill = Color.White
      style = s"""
        -fx-background-color: rgba(0, 0, 0, 0.6);
        -fx-background-radius: 10px;
        -fx-padding: 10px 20px;
      """
    }

    // Question container
    val questionContainer = new VBox {
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

    // Question label
    questionLabel = new Label {
      font = FontManager.subHeaderFont
      textFill = Color.DarkBlue
      wrapText = true
      maxWidth = 750
    }

    // Options
    toggleGroup = new ToggleGroup()
    val radioToggleGroup = toggleGroup
    optionButtons = (0 until 4).map { i =>
      val radioButton = new RadioButton {
        font = FontManager.buttonFont
        textFill = Color.Black
        wrapText = true
        maxWidth = 700
        onAction = (_: ActionEvent) => {
          selectedAnswer = Some(i)
          submitButton.disable = false
        }
      }
      radioButton.toggleGroup = radioToggleGroup
      radioButton
    }.toList

    val optionsVBox = new VBox {
      spacing = 15
      children = optionButtons
    }

    questionContainer.children = List(questionLabel, optionsVBox)

    // Explanation label (initially hidden)
    explanationLabel = new Label {
      font = FontManager.labelFont
      textFill = Color.DarkGreen
      wrapText = true
      maxWidth = 750
      visible = false
      style = s"""
        -fx-background-color: rgba(144, 238, 144, 0.8);
        -fx-background-radius: 10px;
        -fx-padding: 15px;
        -fx-border-color: green;
        -fx-border-width: 1px;
        -fx-border-radius: 10px;
      """
    }

    // Buttons
    val buttonHBox = new HBox {
      spacing = 20
      alignment = Pos.Center
    }

    submitButton = new Button("Submit Answer") {
      font = FontManager.buttonFont
      prefWidth = 150
      prefHeight = 40
      disable = true
      style = s"""
        -fx-background-color: #4CAF50;
        -fx-text-fill: white;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
      """
      onAction = (_: ActionEvent) => handleSubmitAnswer()
    }

    nextButton = new Button("Next Question") {
      font = FontManager.buttonFont
      prefWidth = 150
      prefHeight = 40
      visible = false
      style = s"""
        -fx-background-color: #2196F3;
        -fx-text-fill: white;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
      """
      onAction = (_: ActionEvent) => handleNextQuestion()
    }

    val retryButton = new Button("Retry Quiz") {
      font = FontManager.buttonFont
      prefWidth = 120
      prefHeight = 40
      style = s"""
        -fx-background-color: #FF9800;
        -fx-text-fill: white;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
      """
      onAction = (_: ActionEvent) => resetQuiz()
    }

    val quitButton = new Button("Quit") {
      font = FontManager.buttonFont
      prefWidth = 100
      prefHeight = 40
      style = s"""
        -fx-background-color: #f44336;
        -fx-text-fill: white;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
      """
      onAction = (_: ActionEvent) => sceneManager.goBack()
    }

    buttonHBox.children =
      List(submitButton, nextButton, retryButton, quitButton)

    // Result label
    resultLabel = new Label {
      font = FontManager.subHeaderFont
      textFill = Color.Red
      visible = false
    }

    // Add all components
    mainVBox.children = List(
      titleLabel,
      subtitleLabel,
      progressLabel,
      questionContainer,
      explanationLabel,
      resultLabel,
      buttonHBox
    )

    fill = Color.web("#8B4513")
    root = mainVBox

    // Load first question
    loadCurrentQuestion()
  }

  private def loadCurrentQuestion(): Unit = {
    val question = currentQuizSet(currentQuestionIndex)

    questionLabel.text = question.question

    for (i <- optionButtons.indices) {
      optionButtons(i).text = s"${('A' + i).toChar}. ${question.options(i)}"
      optionButtons(i).selected = false
    }

    progressLabel.text =
      s"Question ${currentQuestionIndex + 1} of 3 | Correct: $correctAnswers/3"

    selectedAnswer = None
    submitButton.disable = true
    nextButton.visible = false
    explanationLabel.visible = false
    resultLabel.visible = false
  }

  private def handleSubmitAnswer(): Unit = {
    selectedAnswer.foreach { answer =>
      val question = currentQuizSet(currentQuestionIndex)
      val isCorrect = answer == question.correctAnswer

      if (isCorrect) {
        correctAnswers += 1
        resultLabel.text = "Correct!"
        resultLabel.textFill = Color.Green
      } else {
        resultLabel.text =
          s"Incorrect. The correct answer was ${('A' + question.correctAnswer).toChar}."
        resultLabel.textFill = Color.Red
      }

      resultLabel.visible = true
      explanationLabel.text = s"Explanation: ${question.explanation}"
      explanationLabel.visible = true

      submitButton.visible = false

      if (currentQuestionIndex < 2) {
        nextButton.visible = true
      } else {
        // Quiz completed
        if (correctAnswers == 3) {
          // All correct - unlock treasure
          val successButton = new Button("Open Treasure Chest!") {
            font = FontManager.buttonFont
            prefWidth = 200
            prefHeight = 50
            style = s"""
              -fx-background-color: gold;
              -fx-text-fill: black;
              -fx-border-radius: 8px;
              -fx-background-radius: 8px;
              -fx-font-weight: bold;
            """
            onAction = (_: ActionEvent) => {
              println("Treasure unlocked! Opening shop...")
              val treasureShop = new TreasureShopScene(sceneManager, gameState)
              sceneManager.switchTo(treasureShop, "TreasureShop")
            }
          }

          // Replace next button with success button
          val buttonContainer =
            nextButton.parent.value.asInstanceOf[javafx.scene.layout.HBox]
          val children = buttonContainer.getChildren
          val nextIndex = children.indexOf(nextButton.delegate)
          if (nextIndex >= 0) {
            children.set(nextIndex, successButton.delegate)
          }
        } else {
          resultLabel.text =
            resultLabel.text.value + s"\n\nYou got $correctAnswers/3 correct. You need all 3 correct to unlock the treasure!"
        }
      }
    }
  }

  private def handleNextQuestion(): Unit = {
    currentQuestionIndex += 1
    loadCurrentQuestion()
    submitButton.visible = true
  }

  private def resetQuiz(): Unit = {
    currentQuizSet = selectRandomQuizSet()
    currentQuestionIndex = 0
    correctAnswers = 0
    selectedAnswer = None
    submitButton.visible = true
    loadCurrentQuestion()
  }
}
