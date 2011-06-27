package uk.ac.ebi.fgpt.conan.utils;

/**
 * An exception that is thrown whenever a system native process is run on the
 * host system encounters a problem, or if it aborts unsucessfully.
 *
 * @author Tony Burdett
 * @date 04-Feb-2009
 */
public class CommandExecutionException extends Exception {
  private static final long serialVersionUID = -7317984422948005141L;

  private String[] errorOutput = new String[0];

  private int exitCode = 0;

  public CommandExecutionException() {
    super();
  }

  /**
   * Constructor that takes a string[] that represents the error report that
   * caused this command to fail.  Normally you would use this exception if a
   * system process failed, in which case the output of the process (which will
   * usually be somewhat descriptive of the error) can be wrapped inside this
   * exception.
   *
   * @param errorOutput the output of the process in error
   */
  public CommandExecutionException(int exitCode, String[] errorOutput) {
    super();
    setErrorOutput(errorOutput);
    setExitCode(exitCode);
  }

  public CommandExecutionException(String s) {
    super(s);
  }

  public CommandExecutionException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public CommandExecutionException(Throwable throwable) {
    super(throwable);
  }

  /**
   * Gets the output from the process in error that caused this exception to be
   * raised.
   *
   * @return the error output
   */
  public String[] getErrorOutput() {
    return errorOutput;
  }

  /**
   * Gets the exit code from the process
   *
   * @return the exit code
   */
  public int getExitCode() {
    return exitCode;
  }


  /**
   * Sets the output from the process in error that caused this exception to be
   * raised.
   *
   * @param errorOutput the error output
   */
  public void setErrorOutput(String[] errorOutput) {
    this.errorOutput = errorOutput;
  }

  /**
   * Sets the exit code from the process
   *
   * @param exitCode the exit code
   */
  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }
  
}
