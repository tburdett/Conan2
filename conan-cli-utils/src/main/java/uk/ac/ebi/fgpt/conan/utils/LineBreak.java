package uk.ac.ebi.fgpt.conan.utils;

/**
 * An enumeration of the types of linebreak in different document formats
 */
public enum LineBreak {
  HTML("<br/>"),
  STANDARD("\n");

  private final String linebreak;

  LineBreak(String linebreak) {
    this.linebreak = linebreak;
  }

  public String getBreak() {
    return linebreak;
  }
}
