package org.json4s
package examples

case class EventAsCaseClass(eventType: String, duration: Option[Int] = None)

object EmptyValueTreatmentExamples {

  import JsonDSL._

  private val weekPlans = Seq(Some("event for monday"), None, None, None, Some("friday boogie night!"), Some("uh, headaches.."), Some("sunday layziness"))

  private val eventAsMap = Map(
    ("eventType" -> Some("event")),
    ("duration" -> None))

  private val eventAsCaseClass = EventAsCaseClass("dinner")

  def main(args: Array[String]) {
    jacksonWaySkippingNulls
    jacksonWayPreservingNulls
    nativeWaySkippingNulls
    nativeWayPreservingNulls
  }

  def jacksonWaySkippingNulls() {
    import jackson.JsonMethods._
    import jackson.Serialization._

    // this import is optional, as long as default strategy points to Skip
    import prefs.EmptyValueStrategy.Skip._
    implicit val formats = jackson.Serialization.formats(NoTypeHints)

    // perform the show
    println("\n\n### Jackson way, skipping nulls ###")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday","friday boogie night!","uh, headaches..","sunday layziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event"}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner"}
  }

  def jacksonWayPreservingNulls() {
    import jackson.JsonMethods._
    import jackson.Serialization._

    import prefs.EmptyValueStrategy.Preserve._
    implicit val formats = jackson.Serialization.formats(NoTypeHints)

    // perform the show
    println("\n\n### Jackson way, preserving nulls ###")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday",null,null,null,"friday boogie night!","uh, headaches..","sunday layziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event","duration":null}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner","duration":null}
  }

  def nativeWaySkippingNulls() {
    import native.JsonMethods._
    import native.Serialization._

    // this import is optional, as long as default strategy points to Skip
    import prefs.EmptyValueStrategy.Skip._
    implicit val formats = native.Serialization.formats(NoTypeHints)

    // perform the show
    println("\n\n### Native way, skipping nulls ###")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday","friday boogie night!","uh, headaches..","sunday layziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event"}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner"}
  }

  def nativeWayPreservingNulls() {
    import native.JsonMethods._
    import native.Serialization._

    import prefs.EmptyValueStrategy.Preserve._
    implicit val formats = native.Serialization.formats(NoTypeHints)

    // perform the show
    println("\n\n### Native way, preserving nulls ###\n")
    println("\nserializing " + weekPlans)
    println("\t" + compact(render(weekPlans))) // = ["event for monday",null,null,null,"friday boogie night!","uh, headaches..","sunday layziness"]

    println("\nserializing " + eventAsMap)
    println("\t" + compact(render(eventAsMap))) // = {"eventType":"event","duration":null}

    println("\nserializing " + eventAsCaseClass)
    println("\t" + write(eventAsCaseClass)) // = {"eventType":"dinner","duration":null}
  }

}
