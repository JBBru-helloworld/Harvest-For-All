package harvestforall.audio

import scalafx.scene.media.{Media, MediaPlayer}

/** Manages background music for the farming game - I wanted to add some nice
  * background music so players can really get immersed in the farming
  * experience. This handles all the music stuff - playing, stopping, volume
  * control etc. Pretty straightforward really.
  */
class BackgroundMusicManager:
  private var mediaPlayer: MediaPlayer | Null = null
  private var isPlaying: Boolean = false

  /** Play background music with specified volume
    *
    * @param resourcePath
    *   Path to the music resource
    * @param volume
    *   Volume level (0.0 to 1.0)
    * @param loop
    *   Whether to loop the music
    */
  def playMusic(
      resourcePath: String = "/assets/music/background.mp3",
      volume: Double = 0.5, // Default to half volume so it's not overwhelming
      loop: Boolean = true // Obviously want it to loop - nobody wants silence!
  ): Unit =
    try
      stopMusic() // Stop any existing music first - don't want overlapping tracks!

      val musicUrl = getClass.getResource(resourcePath).toExternalForm
      val media = new Media(musicUrl)
      mediaPlayer = new MediaPlayer(media)

      mediaPlayer.nn.cycleCount = if loop then MediaPlayer.Indefinite else 1
      mediaPlayer.nn.volume = volume
        .max(0.0)
        .min(
          1.0
        ) // Make sure volume stays reasonable (learned this the hard way)
      mediaPlayer.nn.play()

      isPlaying = true
      println(s"Background music started: $resourcePath")

    catch
      case ex: Exception =>
        println(s"Couldn't play background music: ${ex.getMessage}")
        mediaPlayer = null
        isPlaying = false

  /** Stop whatever music is playing right now */
  def stopMusic(): Unit =
    if mediaPlayer != null then
      mediaPlayer.nn.stop()
      mediaPlayer = null
      isPlaying = false
      println("Background music stopped")

  /** Pause the music - handy for when player opens inventory */
  def pauseMusic(): Unit =
    if mediaPlayer != null && isPlaying then
      mediaPlayer.nn.pause()
      isPlaying = false

  /** Resume paused music - back to farming! */
  def resumeMusic(): Unit =
    if mediaPlayer != null && !isPlaying then
      mediaPlayer.nn.play()
      isPlaying = true

  /** Set volume for currently playing music - dont want to wake the neighbors
    */
  def setVolume(volume: Double): Unit =
    if mediaPlayer != null then mediaPlayer.nn.volume = volume.max(0.0).min(1.0)

  /** Check if music is currently playing */
  def isMusicPlaying: Boolean = isPlaying && mediaPlayer != null

  /** Clean up resources */
  def cleanup(): Unit =
    stopMusic()

// Extension method for null-safe access (Scala 3 feature)
extension [T](value: T | Null)
  def notNull: Option[T] =
    if value == null then None
    else Some(value.asInstanceOf[T])
