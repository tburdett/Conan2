package uk.ac.ebi.fgpt.conan.utils;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class ProcessListener {
  private List<String> progressMessages = new ArrayList<String>();
  private List<String> errorMessages = new ArrayList<String>();
  private boolean finished;
  private boolean started;

  private LineBreak lineBreak = LineBreak.STANDARD;

  public ProcessListener() {
  }

  public String getProgressMessage() {
    StringBuffer sb = new StringBuffer();
    for(String progressMessage : progressMessages) {
      sb.append(progressMessage);
    }
    return sb.toString();
  }

  public void setProgressMessage(String s) {
    progressMessages = Collections.emptyList();
    progressMessages.add(s);
  }

  public void appendProgressMessage(String s) {
    progressMessages.add(s);
  }

  public void appendProgressMessageLine(String s) {
    appendProgressMessage(s + lineBreak.getBreak());
  }

  public void setErrorMessage(String s) {
    errorMessages = Collections.emptyList();
    errorMessages.add(s);
  }

  public void appendErrorMessage(String s) {
    errorMessages.add(s);
  }

  public void appendErrorMessageLine(String s) {
    appendErrorMessage(s + lineBreak.getBreak());
  }

  public boolean isStarted() {
    return started;
  }

  public void setStarted(boolean s) {
    started = s;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean f) {
    finished = f;
  }

  public void setLineBreak(LineBreak l) {
    lineBreak = l;
  }

  public void reset() {
    started = false;
    finished = false;
    progressMessages = Collections.emptyList();
    errorMessages = Collections.emptyList();
  }
}
