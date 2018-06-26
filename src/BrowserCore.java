import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import java.util.ResourceBundle;

public class BrowserCore {
	// English resource package found online
	public static final String DEFAULT_RESOURCES = "resources/Errors";
	public static final String PREFIX = "http://";
	
	private URL home;
	private URL currentURL;
	private int currentIndex;
	private List<URL> history;
	private HashMap<String, URL> favorites;
	
	private ResourceBundle resources;
	
	public BrowserCore() {
		home = null;
		currentURL = null;
		currentIndex = -1;
		favorites = new HashMap<String, URL>();
		history = new ArrayList<>();
		resources = ResourceBundle.getBundle(DEFAULT_RESOURCES);
	}
	
	public URL goForward() {
		if(hasNext()) {
			currentIndex++;
			assert currentIndex < -1 : "current index is under 0";
			return history.get(currentIndex);
		} else {
			throw new BrowserException(resources.getString("NoNext"));
		}
	}
	
	public URL goBack() {
		if(hasPrevious()) {
			currentIndex--;
			return history.get(currentIndex);
		} else {
			throw new BrowserException(resources.getString("NoPrevious"));
		}
	}
	
	public URL go (String url) {
		try {
			URL tmp = completeURL(url);
			tmp.openStream();
			currentURL = tmp;
			if(hasNext()) {
				history = history.subList(0, currentIndex + 1);
			}
			history.add(currentURL);
			currentIndex++;
			return currentURL;
		} catch (Exception e) {
			throw new BrowserException(e, resources.getString("NoLoad"), url);
		}
	}
	
	public boolean hasNext() {
		return currentIndex < (history.size() - 1);
	}
	
	public boolean hasPrevious() {
		return currentIndex > 0;
	}
	
	public URL getHome() {
		return home;
	}
	
	public void setHome() {
		if(currentURL != null) {
			home = currentURL;
		}
	}
	
	public void addFavorite(String name) {
		if(name != null && !name.equals("") && currentURL != null) {
			try{
				favorites.put(name, currentURL);
			}catch(NullPointerException e) {
				System.out.println("Nullpointer Exception");
			}
		}
	}
	
	public URL getFavorite(String name) {
		if(name != null && !name.equals("") && favorites.containsKey(name)) {
			return favorites.get(name);
		} else {
			throw new BrowserException(resources.getString("BadFavorite"), name);
		}
	}
	
	// fills the URL if it isn't complete
	// it's a bit wonky right now, but works
	private URL completeURL(String possible) throws MalformedURLException{
		try {
			assert possible != null : possible;
			return new URL(possible);
		} catch (MalformedURLException e1) {
			try {
				assert possible != null : possible;
				assert currentURL != null : currentURL;
				return new URL(currentURL.toString() + "/" + possible);
			} catch (MalformedURLException e2) {
				try {
					assert possible != null : possible;
					return new URL(PREFIX + possible);
				} catch (MalformedURLException e3){
					throw e3;
				}
			}
		}
	}
}
