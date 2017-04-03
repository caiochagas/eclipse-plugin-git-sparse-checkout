package handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import views.MainView;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MainHandler extends AbstractHandler {
	
	private static MainView mainView;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(mainView == null) {
			mainView = new MainView();
			mainView.setVisible(true);
		}else {
			mainView.setState(java.awt.Frame.NORMAL);
			mainView.setVisible(true);
		}
		
		return null;
	}
}
