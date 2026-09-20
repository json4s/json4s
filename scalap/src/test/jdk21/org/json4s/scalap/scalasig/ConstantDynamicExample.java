package org.json4s.scalap.scalasig;

public class ConstantDynamicExample {
  public int f(Object s) {
    switch(s) {
      case A.B -> {
        return 1;
      }
      default -> {
        return 2;
      }
    }
  }

  static enum A {
    B
  }
}
