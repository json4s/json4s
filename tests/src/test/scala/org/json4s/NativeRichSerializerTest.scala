package org.json4s

import org.json4s.native.Document

class NativeRichSerializerTest extends RichSerializerTest[Document] with native.JsonMethods
