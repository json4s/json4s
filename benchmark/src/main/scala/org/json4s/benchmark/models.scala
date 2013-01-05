package org.json4s
package benchmark

import java.util.Date

case class Project(name: String, startDate: Date, lang: Option[Language], teams: List[Team])
case class Language(name: String, version: Double)
case class Team(role: String, members: List[Employee])
case class Employee(name: String, experience: Int)