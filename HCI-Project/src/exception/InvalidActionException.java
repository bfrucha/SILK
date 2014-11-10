package exception;

// exception used in ProjectController class
public class InvalidActionException extends Exception {

	public InvalidActionException() {
		super("Action should be Draw or Erase");
	}
}
