package slippyWMTS.batch;

public interface StatusListener {

	void progress(double percent);

	void text(String string);

}
