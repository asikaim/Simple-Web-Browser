import java.awt.Dimension;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javax.imageio.ImageIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;


public class BrowserView {

	public static final Dimension DEFAULT_SIZE = new Dimension(800, 600);
	public static final String DEFAULT_RESOURCES = "resources/";
	public static final String STYLESHEET = "default.css";
	public static final String BLANK = " ";

	private Scene scene;
	private WebView page;
	private Label status;
	private TextField URLDisplay;
	private Button backButton;
	private Button nextButton;
	private Button homeButton;
	
	private ComboBox<String> favorites;
	private ResourceBundle resources;
	private BrowserCore core;
	
	public BrowserView(BrowserCore c, String language) {
		core = c;
		resources = ResourceBundle.getBundle(DEFAULT_RESOURCES + language);
		BorderPane root = new BorderPane();
		
		root.setCenter(createPageDisplay());
		root.setTop(createInputPanel());
		root.setBottom(createInformationPanel());
		
		enableButtons();
		
		scene = new Scene(root, DEFAULT_SIZE.width, DEFAULT_SIZE.height);
	}
	
	public void loadPage(String url) {
		try {
			update(core.go(url));
		} catch (BrowserException e){
			showError(e.getMessage());
		}
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public void showStatus(String message) {
		status.setText(message);
	}
	
	public void showError (String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(resources.getString("ErrorTitle"));
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	private void forward() {
		update(core.goForward());
	}
	
	private void back() {
		update(core.goBack());
	}
	
	private void home() {
		loadPage(core.getHome().toString());
	}
	
    private void showFavorite (String favorite) {
        loadPage(core.getFavorite(favorite).toString());
    }
	
	private void update(URL url) {
		assert url != null : "url = null";
		String urlText = url.toString();
		page.getEngine().load(urlText);
		URLDisplay.setText(urlText);
		enableButtons();
	}
	
	private void addFavorite() {
		TextInputDialog input = new TextInputDialog("");
		input.setTitle(resources.getString("FavoritePromptTitle"));
		input.setContentText(resources.getString("FavoritePrompt"));
		Optional<String> response = input.showAndWait();
		
		if(response.isPresent()) {
			core.addFavorite(response.get());
			favorites.getItems().add(response.get());
		}
	}
	
	private void enableButtons() {
		backButton.setDisable(! core.hasPrevious());
		nextButton.setDisable(! core.hasNext());
		homeButton.setDisable(core.getHome() == null);
	}
	
	private Node createPageDisplay() {
		page = new WebView();
		page.getEngine().getLoadWorker().stateProperty().addListener(new LinkListener());
		return page;
	}
	
	private Node createInputPanel() {
		VBox result = new VBox();
		result.getChildren().addAll(createNavigationPanel(), createPreferencesPanel());
		return result;
	}
	
	private Node createNavigationPanel() {
		HBox result = new HBox();
		backButton = createButton("BackCommand", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				back();
			}
		});
		result.getChildren().add(backButton);
		nextButton = createButton("NextCommand", event -> forward());
		result.getChildren().add(nextButton);
		homeButton = createButton("HomeCommand", event -> home());
		result.getChildren().add(homeButton);
		
		EventHandler<ActionEvent> showHandler = new LoadPage();
		result.getChildren().add(createButton("GoCommand", showHandler));
		URLDisplay = createInputField(40, showHandler);
		result.getChildren().add(URLDisplay);
		return result;
	}
	
	private Node createInformationPanel() {
		status = new Label(BLANK);
		return status;
	}
	
	
	private Node createPreferencesPanel() {
		HBox result = new HBox();
		favorites = new ComboBox<String>();
        favorites.setPromptText(resources.getString("FavoriteFirstItem"));
        favorites.valueProperty().addListener((o, s1, s2) -> showFavorite(s2));
        result.getChildren().add(createButton("AddFavoriteCommand", event -> addFavorite()));
        result.getChildren().add(favorites);
        result.getChildren().add(createButton("SetHomeCommand", event -> {
            core.setHome();
            enableButtons();
        }));
        return result;
	}
	
	private Button createButton (String property, EventHandler<ActionEvent> handler) {
		final String IMAGE_SUFFIXES = String.format(".*\\.(%s)", String.join("|", ImageIO.getReaderFileSuffixes()));
		
		Button result = new Button();
		String label = resources.getString(property);
		if(label.matches(IMAGE_SUFFIXES)) {
			result.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(DEFAULT_RESOURCES + label))));
		} else {
			result.setText(label);
		}
		result.setOnAction(handler);
		return result;
	}
	
	private TextField createInputField (int width, EventHandler<ActionEvent> handler) {
		TextField result = new TextField();
		result.setPrefColumnCount(width);
		result.setOnAction(handler);
		return result;
	}
	
	
	// a bit old school to use inner classes but....
	private class LoadPage implements EventHandler<ActionEvent>{
		@Override
		public void handle (ActionEvent event) {
			loadPage(URLDisplay.getText());
		}
	}
	
	
	private class LinkListener implements ChangeListener<State>{
        public static final String HTML_LINK = "href";
        public static final String EVENT_CLICK = "click";
        public static final String EVENT_MOUSEOVER = "mouseover";
        public static final String EVENT_MOUSEOUT = "mouseout";

        @Override
        public void changed (ObservableValue<? extends State> ov, State oldState, State newState) {	
        	if(newState == Worker.State.SUCCEEDED) {
        		EventListener listener = event -> {
        			final String href = ((Element)event.getTarget()).getAttribute(HTML_LINK);
        			if(href != null) {
        				String domEventType = event.getType();
        				if(domEventType.equals(EVENT_CLICK)) {
        					loadPage(href);
        				} else if(domEventType.equals(EVENT_MOUSEOVER)) {
        					showStatus(href);
        				} else if(domEventType.equals(EVENT_MOUSEOUT)) {
        					showStatus(BLANK);
        				}
        			}
        		};
        		Document doc = page.getEngine().getDocument();
        		NodeList nodes = doc.getElementsByTagName("a");
        		for(int i = 0; i < nodes.getLength(); i++) {
                    EventTarget node = (EventTarget)nodes.item(i);
                    node.addEventListener(EVENT_CLICK, listener, false);
                    node.addEventListener(EVENT_MOUSEOVER, listener, false);
                    node.addEventListener(EVENT_MOUSEOUT, listener, false);
        		}
        	}
        }
	};
}
