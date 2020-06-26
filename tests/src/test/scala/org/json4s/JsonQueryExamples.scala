/*
* Copyright 2009-2010 WorldWide Conferencing, LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.json4s

import org.specs2.mutable.Specification
import org.json4s.native.Document

class NativeJsonQueryExamples extends JsonQueryExamples[Document]("Native") with native.JsonMethods
class JacksonJsonQueryExamples extends JsonQueryExamples[JValue]("Jackson") with jackson.JsonMethods

/**
* System under specification for JSON Query Examples.
*/
abstract class JsonQueryExamples[T](mod: String) extends Specification with JsonMethods[T] {

  (mod+" JSON Query Examples") should {
    "List of IPs" in {
      val ips = for { JString(ip) <- json \\ "ip" } yield ip
      ips must_== List("192.168.1.125", "192.168.1.126", "192.168.1.127", "192.168.2.125", "192.168.2.126")
    }

    "List of IPs converted to XML" in {
      val ips = <ips>{ for { JString(ip) <- json \\ "ip" } yield <ip>{ ip }</ip> }</ips>
      ips must_== <ips><ip>192.168.1.125</ip><ip>192.168.1.126</ip><ip>192.168.1.127</ip><ip>192.168.2.125</ip><ip>192.168.2.126</ip></ips>
    }

    "List of IPs in cluster2" in {
      val ips = for {
        // NOTE: the following warning given by Scala compiler is incorrect
        // pattern var cluster in value $anonfun is never used; `cluster@_' suppresses this warning
        cluster @ JObject(x) <- json \ "data_center" if (x contains JField("name", JString("cluster2")))
        JString(ip) <- cluster \\ "ip"
      } yield ip
      ips must_== List("192.168.2.125", "192.168.2.126")
    }

    "Total cpus in data center" in {
      (for { JInt(x) <- json \\ "cpus" } yield x) reduceLeft (_ + _) must_== 40
    }

    "Servers sorted by uptime" in {
      case class Server(ip: String, uptime: Long)

      val servers = for {
        JArray(servers) <- json \\ "servers"
        JObject(server) <- servers
        JField("ip", JString(ip)) <- server
        JField("uptime", JInt(uptime)) <- server
      } yield Server(ip, uptime.longValue)

      servers sortWith (_.uptime > _.uptime) must_== List(Server("192.168.1.127", 901214), Server("192.168.2.125", 453423), Server("192.168.2.126", 214312), Server("192.168.1.126", 189822), Server("192.168.1.125", 150123))
    }

    "Clusters administered by liza" in {
      val clusters = for {
        JObject(cluster) <- json
        JField("admins", JArray(admins)) <- cluster
        if admins contains JString("liza")
        JField("name", JString(name)) <- cluster
      } yield name

      clusters must_== List("cluster2")
    }
  }

  lazy val json = parse("""
    { "data_center": [
      {
        "name": "cluster1",
        "servers": [
          {"ip": "192.168.1.125", "uptime": 150123, "specs": {"cpus":  8, "ram": 2048}},
          {"ip": "192.168.1.126", "uptime": 189822, "specs": {"cpus": 16, "ram": 4096}},
          {"ip": "192.168.1.127", "uptime": 901214, "specs": {"cpus":  8, "ram": 4096}}
        ],
        "links": [
          {"href": "http://www.example.com/admin", "name": "admin"},
          {"href": "http://www,example.com/home", "name": "home"}
        ],
        "admins": ["jim12", "joe", "maddog"]
      },
      {
        "name": "cluster2",
        "servers": [
          {"ip": "192.168.2.125", "uptime": 453423, "specs": {"cpus":  4, "ram": 2048}},
          {"ip": "192.168.2.126", "uptime": 214312, "specs": {"cpus":  4, "ram": 2048}}
        ],
        "links": [
          {"href": "http://www.example2.com/admin", "name": "admin"},
          {"href": "http://www,example2.com/home", "name": "home"}
        ],
        "admins": ["joe", "liza"]
      }
   ]}
  """)
}
