package com.trading.sim.ui;

import com.trading.sim.market.Market;
import com.trading.sim.model.Company;
import com.trading.sim.order.Trade;
import com.trading.sim.trader.AccountingTrader;
import com.trading.sim.trader.strategies.MeanReversionStrategy;
import com.trading.sim.trader.strategies.RandomStrategy;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JavaFX dashboard that shows live prices, trades and per-trader P&L.
 * Compatible with JavaFX 17.
 */
public class DashboardApp extends Application {

    private Market market;
    private ExecutorService pool;
    private final List<AccountingTrader> traders = new ArrayList<>();

    private final ObservableList<PriceRow> priceRows = FXCollections.observableArrayList();
    private final ObservableList<Trade> tradeRows = FXCollections.observableArrayList();
    private final ObservableList<TraderRow> traderRows = FXCollections.observableArrayList();

    private final DecimalFormat df2 = new DecimalFormat("#,##0.00");

    @Override
    public void start(Stage stage) {
        // --- Build simulation ---
        Random rng = new Random(42);
        market = new Market(rng);
        market.listCompany(new Company("Acme Robotics", "ACME", 100.00, 0.25));
        market.listCompany(new Company("Nimbus Cloud", "NIMB", 55.50, 0.30));
        market.listCompany(new Company("Solaris Energy", "SOLR", 22.15, 0.40));
        market.start();

        // Traders
        pool = Executors.newFixedThreadPool(6);
        traders.add(new AccountingTrader("T-001", new RandomStrategy(),        market, new Random(rng.nextLong()), 100_000));
        traders.add(new AccountingTrader("T-002", new RandomStrategy(),        market, new Random(rng.nextLong()), 100_000));
        traders.add(new AccountingTrader("T-003", new MeanReversionStrategy(), market, new Random(rng.nextLong()), 100_000));
        traders.add(new AccountingTrader("T-004", new MeanReversionStrategy(), market, new Random(rng.nextLong()), 100_000));
        traders.forEach(t -> pool.submit(t));

        // --- UI tables ---
        TableView<PriceRow> priceTable = buildPriceTable();
        TableView<Trade> tradeTable = buildTradeTable();
        TableView<TraderRow> pnlTable = buildTraderTable();

        // init rows
        for (String sym : market.symbols()) {
            priceRows.add(new PriceRow(sym, market.lastPrice(sym)));
        }
        for (AccountingTrader t : traders) {
            traderRows.add(new TraderRow(t.getId(), 100_000.0, 0.0));
        }

        // Trade live feed
        market.addTradeListener(t -> Platform.runLater(() -> {
            tradeRows.add(0, t);
            if (tradeRows.size() > 300) tradeRows.remove(300, tradeRows.size());
        }));

        // Timers to refresh data
        Timeline priceTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> refreshPrices()));
        priceTimer.setCycleCount(Animation.INDEFINITE);
        priceTimer.play();

        Timeline pnlTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshPnL()));
        pnlTimer.setCycleCount(Animation.INDEFINITE);
        pnlTimer.play();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        TabPane tabs = new TabPane();
        Tab tPrices = new Tab("Prices", priceTable); tPrices.setClosable(false);
        Tab tTrades = new Tab("Trades", tradeTable); tTrades.setClosable(false);
        Tab tPnL   = new Tab("Traders P&L", pnlTable); tPnL.setClosable(false);
        tabs.getTabs().addAll(tPrices, tTrades, tPnL);

        Label title = new Label("Stock Market Simulator — Live Dashboard");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox top = new HBox(title);
        top.setPadding(new Insets(0, 0, 10, 0));

        root.setTop(top);
        root.setCenter(tabs);

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("Market Dashboard");
        stage.setOnCloseRequest(e -> stopApp());
        stage.show();
    }

    private TableView<PriceRow> buildPriceTable() {
        TableView<PriceRow> table = new TableView<>(priceRows);
        TableColumn<PriceRow, String> cSym = new TableColumn<>("Symbol");
        cSym.setCellValueFactory(data -> data.getValue().symbolProperty());
        cSym.setPrefWidth(120);

        TableColumn<PriceRow, Number> cPx = new TableColumn<>("Last Price");
        cPx.setCellValueFactory(data -> data.getValue().priceProperty());
        cPx.setPrefWidth(120);

        table.getColumns().clear();
        table.getColumns().add(cSym);
        table.getColumns().add(cPx);
        // JavaFX 17: use the classic constrained policy
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<Trade> buildTradeTable() {
        TableView<Trade> table = new TableView<>(tradeRows);
        TableColumn<Trade, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()));
        TableColumn<Trade, String> cSym = new TableColumn<>("Symbol");
        cSym.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSymbol()));
        TableColumn<Trade, Number> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getQuantity()));
        TableColumn<Trade, Number> cPx = new TableColumn<>("Price");
        cPx.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrice()));
        TableColumn<Trade, String> cB  = new TableColumn<>("Buyer");
        cB.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBuyTraderId()));
        TableColumn<Trade, String> cS  = new TableColumn<>("Seller");
        cS.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSellTraderId()));

        table.getColumns().clear();
        table.getColumns().add(cId);
        table.getColumns().add(cSym);
        table.getColumns().add(cQty);
        table.getColumns().add(cPx);
        table.getColumns().add(cB);
        table.getColumns().add(cS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<TraderRow> buildTraderTable() {
        TableView<TraderRow> table = new TableView<>(traderRows);
        TableColumn<TraderRow, String> cId = new TableColumn<>("Trader");
        cId.setCellValueFactory(d -> d.getValue().idProperty());
        TableColumn<TraderRow, String> cCash = new TableColumn<>("Cash");
        cCash.setCellValueFactory(d -> new SimpleStringProperty(df2.format(d.getValue().getCash())));
        TableColumn<TraderRow, String> cPnl = new TableColumn<>("PnL");
        cPnl.setCellValueFactory(d -> new SimpleStringProperty(df2.format(d.getValue().getPnl())));

        table.getColumns().clear();
        table.getColumns().add(cId);
        table.getColumns().add(cCash);
        table.getColumns().add(cPnl);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private void refreshPrices() {
        for (PriceRow r : priceRows) {
            r.setPrice(market.lastPrice(r.getSymbol()));
        }
    }

    private void refreshPnL() {
        for (TraderRow row : traderRows) {
            AccountingTrader t = traders.stream()
                    .filter(tr -> tr.getId().equals(row.getId()))
                    .findFirst().orElse(null);
            if (t != null) {
                double cash = t.getAccount().getCash();
                double pnl  = t.getAccount().unrealizedPnL(market::lastPrice);
                row.setCash(cash);
                row.setPnl(pnl);
            }
        }
    }

    private void stopApp() {
        traders.forEach(AccountingTrader::stop);
        if (pool != null) pool.shutdownNow();
        if (market != null) market.close();
        Platform.exit();
    }

    // --- Row models for JavaFX tables ---
    public static class PriceRow {
        private final StringProperty symbol = new SimpleStringProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();
        public PriceRow(String symbol, double price) { setSymbol(symbol); setPrice(price); }
        public String getSymbol() { return symbol.get(); }
        public void setSymbol(String s) { symbol.set(s); }
        public StringProperty symbolProperty() { return symbol; }
        public double getPrice() { return price.get(); }
        public void setPrice(double p) { price.set(p); }
        public DoubleProperty priceProperty() { return price; }
    }

    public static class TraderRow {
        private final StringProperty id = new SimpleStringProperty();
        private final DoubleProperty cash = new SimpleDoubleProperty();
        private final DoubleProperty pnl = new SimpleDoubleProperty();
        public TraderRow(String id, double cash, double pnl) { setId(id); setCash(cash); setPnl(pnl); }
        public String getId() { return id.get(); }
        public void setId(String s) { id.set(s); }
        public StringProperty idProperty() { return id; }
        public double getCash() { return cash.get(); }
        public void setCash(double v) { cash.set(v); }
        public DoubleProperty cashProperty() { return cash; }
        public double getPnl() { return pnl.get(); }
        public void setPnl(double v) { pnl.set(v); }
        public DoubleProperty pnlProperty() { return pnl; }
    }
}
