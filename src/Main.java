import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String TITLE = "Simple Browser";
	public static final String START_PAGE = "http://google.com";
	
	@Override
	public void start (Stage stage) {
		// create components
		BrowserCore core = new BrowserCore();
		BrowserView display = new BrowserView(core, "English"); // for test purposes
		// window title
		stage.setTitle(TITLE);
		// add user interface components
		stage.setScene(display.getScene());
		stage.show();
		// start page
		display.loadPage(START_PAGE);
	}
	
	// start browser
	public static void main(String[] args) {
		launch(args);
	}
	
}
