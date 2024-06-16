package org.json4s

private[json4s] object ClassDelta {
  def delta(class1: Class[?], class2: Class[?]): Int = {
    if (class1 == class2) 0
    else if (class1 == null) 1
    else if (class2 == null) -1
    else if (class1.getInterfaces.contains(class2)) 0
    else if (class2.getInterfaces.contains(class1)) 0
    else if (class1.isAssignableFrom(class2)) {
      1 + delta(class1, class2.getSuperclass)
    } else if (class2.isAssignableFrom(class1)) {
      1 + delta(class1.getSuperclass, class2)
    } else sys.error("Don't call delta unless one class is assignable from the other")
  }
}
