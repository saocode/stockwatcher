package com.google.gwt.sample.stockwatcher.client;


import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StockWatcher implements EntryPoint {

	private static final int REFRESH_INTERVAL = 5000;
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable(); // this is the table
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> stocks = new ArrayList<String>();

	// Login
	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label("XXX Please sign in to your Google Account to access the StockWatcher application.");
	private Anchor signInLink = new Anchor("Sign In");
	private Anchor signOutLink = new Anchor("Sign Out");

	// Stock Service
	private final StockServiceAsync stockService = GWT.create(StockService.class);

	// Gnode Service
	private final GnodeServiceAsync gnodeService = GWT.create(GnodeService.class);


	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		// Check login status using login service.
		LoginServiceAsync loginService = GWT.create(LoginService.class);

		loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(LoginInfo result) {
				loginInfo = result;
				if(loginInfo.isLoggedIn()) {
					loadStockWatcher();
				} else {
					loadLogin();
				}
			}
		}
				);
	}

	private void loadLogin() {
		// Assemble login panel.
		signInLink.setHref(loginInfo.getLoginUrl());
		loginPanel.add(loginLabel);
		loginPanel.add(signInLink);
		RootPanel.get("stockList").add(loginPanel);
	}

	private void loadStockWatcher() {

		// Set up sign out hyperlink.
		signOutLink.setHref(loginInfo.getLogoutUrl());

		// Create table for stock data. 
		stocksFlexTable.setText(0, 0, "Symbol"); // here we are adding headers to the table
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");    

		loadStocks();


		// Assemble Add Stock panel.
		addPanel.add(newSymbolTextBox);
		addPanel.add(addStockButton);

		// Assemble Main panel.
		mainPanel.add(signOutLink);
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("stockList").add(mainPanel);

		// Move cursor focus to the input box.
		newSymbolTextBox.setFocus(true);    

		// Setup timer to refresh list automatically.
		Timer refreshTimer = new Timer() {
			@Override
			public void run() {
				refreshWatchList();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

		// Listen for mouse events on the Add button.
		addStockButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addStock();
				//addGnode("hi");
			}
		});
	}

	private void loadStocks() {
		stockService.getStocks(new AsyncCallback<String[]>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(String[] symbols) {
				displayStocks(symbols);
			}
		});
	}

	private void displayStocks(String[] symbols) {
		for (String symbol : symbols) {
			displayStock(symbol);
		}
	}


	/**
	 * Add stock to FlexTable. Executed when the user clicks the addStockButton or
	 * presses enter in the newSymbolTextBox.
	 */
	private void addStock() {
		final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
		newSymbolTextBox.setFocus(true);

		// Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
		if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
			Window.alert("'" + symbol + "' is not a valid symbol.");
			newSymbolTextBox.selectAll();
			return;
		}

		// blank the input box
		newSymbolTextBox.setText("");

		// Don't add the stock if it's already in the table.
		if (stocks.contains(symbol))
			return;

		addStock(symbol);
		addGnode(symbol);
		parseVnodes();
	}

	// Testing Gnode JDO
	private void addGnode(final String symbol) {

		gnodeService.addGnode(symbol, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Void ignore) {
				Window.alert("Gnode " + symbol + " should be added.");			}

		});
	}

	private void parseVnodes() {

		gnodeService.parseVnodes(new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Void ignore) {
				Window.alert("Gnodes should be parsed.");			
			}

		});


	}



	private void addStock(final String symbol) {

		stockService.addStock(symbol, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Void ignore) {
				displayStock(symbol);
			}

		});
	}

	private void displayStock(final String symbol) {

		// If the stock doesn't exist, add it.
		// When you call the setText method, the FlexTable automatically creates new cells as needed; therefore, you don't need to resize the table explicitly.
		int row = stocksFlexTable.getRowCount(); // how many rows?
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol); // add new row at end


		// So that users can delete a specific stock from the list, insert a Remove button in the last cell of the table row. 

		// Subscribe to click events with the addClickHandler method. 
		// If the Remove Stock button publishes a click event, remove the stock from the FlexTable and the ArrayList.

		// Add a button to remove this stock from the table.
		Button removeStock = new Button("x");
		removeStock.addStyleDependentName("remove");

		removeStock.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				removeStock(symbol);
			}
		});
		stocksFlexTable.setWidget(row, 3, removeStock);

		// Get the stock price.
		refreshWatchList();

	}

	private void removeStock(final String symbol) {
		stockService.removeStock(symbol, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Void ignore) {
				undisplayStock(symbol);
			}
		});
	}

	private void undisplayStock(String symbol) {
		int removedIndex = stocks.indexOf(symbol);
		stocks.remove(removedIndex);
		stocksFlexTable.removeRow(removedIndex+1);
	}

	/**
	 * Generate random stock prices.
	 */
	private void refreshWatchList() {
		final double MAX_PRICE = 100.0; // $100.00
		final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

		StockPrice[] prices = new StockPrice[stocks.size()]; // create array of StockPrices of size stocks array
		for (int i = 0; i < stocks.size(); i++) {
			double price = Random.nextDouble() * MAX_PRICE;
			double change = price * MAX_PRICE_CHANGE
					* (Random.nextDouble() * 2.0 - 1.0);

			prices[i] = new StockPrice(stocks.get(i), price, change);
		}

		updateTable(prices); // send fake price array to updateTable		
	}

	/**
	 * Update the Price and Change fields all the rows in the stock table.
	 *
	 * @param prices Stock data for all rows.
	 */
	private void updateTable(StockPrice[] prices) { // update table w/ array
		for (int i = 0; i < prices.length; i++) {
			updateTable(prices[i]);
		}			

		// Display timestamp showing last refresh.
		lastUpdatedLabel.setText("Last update : "
				+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
	}

	/**
	 * Update a single row in the stock table.  Called by updateTable(array).
	 *
	 * @param price Stock data for a single row.
	 */
	private void updateTable(StockPrice price) {  // update table w/ StockPrice
		// Make sure the stock is still in the stock table.
		if (!stocks.contains(price.getSymbol())) {
			return;
		}

		int row = stocks.indexOf(price.getSymbol()) + 1;

		// Format the data in the Price and Change fields.
		String priceText = NumberFormat.getFormat("#,##0.00").format(
				price.getPrice());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(price.getChange());
		String changePercentText = changeFormat.format(price.getChangePercent());

		// Populate the Price and Change fields with new data.
		stocksFlexTable.setText(row, 1, priceText);
		stocksFlexTable.setText(row, 2, changeText + " (" + changePercentText
				+ "%)");
	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(loginInfo.getLogoutUrl());
		}
	}



}