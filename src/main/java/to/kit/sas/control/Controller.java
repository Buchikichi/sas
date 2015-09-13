package to.kit.sas.control;

/**
 * Controller.
 * @param <T> form type
 * @author Hidetaka Sasai
 */
public interface Controller<T> {
	/**
	 * Execute process.
	 * @param form requested parameters
	 * @return Object
	 */
	Object execute(T form);
}
