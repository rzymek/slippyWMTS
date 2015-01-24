package slippyWMTS.webproxy.gae.exceptions;

public class HttpErrorResult extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public final int errorCode;

  public HttpErrorResult(int errorCode) {
    super();
    this.errorCode = errorCode;
  }
  
}
