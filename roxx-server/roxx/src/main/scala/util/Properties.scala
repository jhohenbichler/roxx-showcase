package util

class Properties(path: String) {
  private val props: java.util.Properties = new java.util.Properties()
  props.load(Thread.currentThread.getContextClassLoader.getResourceAsStream(path))

  def apply(key: String): Option[String] = Option(props.getProperty(key))
}

object Properties {
  def apply(path: String) = new Properties(path)
}