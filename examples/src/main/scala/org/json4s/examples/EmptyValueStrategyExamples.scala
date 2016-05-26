package org.json4s
package examples

case class EventAsCaseClass(eventType: String, duration: Option[Int] = None)

object EmptyValueTreatmentExamples {

  import JsonDSL._

  private val weekPlans = Seq(Some("event for monday"), None, None, None, Some("friday boogie night!"), Some("uh, headaches.."), Some("sunday laziness"))

  private val eventAsMap = Map(
    ("eventType" -> Some("event")),
    ("duration" -> None))

  private val eventAsCaseClass = EventAsCaseClass("dinner")

  def main(args: Array[String]): Unit = {
    jacksonWaySkippingNulls
    jacksonWayPreservingNulls
    nativeWaySkippingNulls
    nativeWayPreservingNulls
  }

  def jacksonWaySkippingNulls(): Unit = {
    import jackson.JsonMethods._
    import jackson.Serialization._

    implicit val formats = jackson.Serialization.formats(NoTypeHints).skippingEmptyValues

    // perform the show
    println("\n\n### Jackson way, skipping nulls ###")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday","friday boogie night!","uh, headaches..","sunday laziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event"}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner"}
  }

  def jacksonWayPreservingNulls(): Unit = {
    import jackson.JsonMethods._
    import jackson.Serialization._
    implicit val formats = jackson.Serialization.formats(NoTypeHints).preservingEmptyValues

    // perform the show
    println("\n\n### Jackson way, preserving nulls ###")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday",null,null,null,"friday boogie night!","uh, headaches..","sunday laziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event","duration":null}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner","duration":null}
  }

  def nativeWaySkippingNulls(): Unit = {
    import native.JsonMethods._
    import native.Serialization._

    implicit val formats = native.Serialization.formats(NoTypeHints).skippingEmptyValues

    // perform the show
    println("\n\n### Native way, skipping nulls ###")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday","friday boogie night!","uh, headaches..","sunday laziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event"}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner"}
  }

  def nativeWayPreservingNulls(): Unit = {
    import native.JsonMethods._
    import native.Serialization._
    implicit val formats = native.Serialization.formats(NoTypeHints).preservingEmptyValues

    // perform the show
    println("\n\n### Native way, preserving nulls ###\n")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday",null,null,null,"friday boogie night!","uh, headaches..","sunday laziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event","duration":null}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner","duration":null}
  }

}
