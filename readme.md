# Stock Market Simulator (Java 17 + JavaFX)

A moderately advanced Java project demonstrating **Object-Oriented Design** and **Multithreading** with a live **JavaFX dashboard**.  
Multiple trader threads place orders on independent order books; a matching engine executes trades; a price engine jitters quotes; and a dashboard shows prices, trades, and per-trader P&L in real time.

## Features
- **OOP architecture**: clear separation (`market`, `engine`, `model`, `order`, `trader`, `ui`).
- **Concurrency**: `ScheduledExecutorService` for price ticks; multiple trader threads with pluggable strategies.
- **Matching engine**: price–time priority for limit orders; simple market order handling.
- **P&L tracking**: each trader has an `Account` (cash, positions, unrealized P&L).
- **JavaFX dashboard**: live tables for Prices, Trades, and Traders’ P&L.
- **Deterministic-ish**: seeded RNG for reproducible demos.


## Project Structure
pom.xml
src/main/java/com/trading/sim/App.java # Console runner (headless)
src/main/java/com/trading/sim/ui/DashboardApp.java # JavaFX dashboard (GUI)
src/main/java/com/trading/sim/market/Market.java
src/main/java/com/trading/sim/engine/{PriceEngine,OrderBook,MatchingEngine}.java
src/main/java/com/trading/sim/model/{Company,Stock}.java
src/main/java/com/trading/sim/order/{Order,OrderType,Side,Trade}.java
src/main/java/com/trading/sim/trader/{Strategy,Trader,AccountingTrader,Account}.java
src/main/java/com/trading/sim/trader/strategies/{RandomStrategy,MeanReversionStrategy}.java


## Prerequisites
- **JDK 17 (LTS)**  
- **Maven 3.8+**

### macOS quick install
```bash
# Homebrew (if you don't have it)
#/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

brew install --cask temurin17
brew install maven

# Use JDK 17 in this shell
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```


## Run
1) Console simulation (headless)
mvn -q -DskipTests exec:java
You’ll see trade fills and periodic P&L logs, then Simulation finished.
2) JavaFX dashboard (GUI)
```bash
# Make sure this shell uses JDK 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

mvn -q -DskipTests javafx:run
```

### Tabs:
Prices – last price per symbol (auto-refresh)
Trades – live feed (recent trades at the top)
Traders P&L – cash and unrealized P&L per trader (1s refresh)


## How it works (high level)
PriceEngine ticks prices via a small random walk.
Traders (AccountingTrader) run on their own threads and call a Strategy to generate orders.
MatchingEngine matches orders per symbol using price–time priority against an OrderBook.
Executed Trades update trader accounts and are broadcast to the dashboard via listeners.

## Customization
Add symbols: edit DashboardApp.start (or App.main) and add more Company entries.
Change strategies: implement Strategy and use it when creating traders.
Change tick speed: adjust period in Market.start() → priceEngine.start(200) (ms).
Initial cash: change the last parameter when constructing AccountingTrader.

## Troubleshooting
Maven not found: install with brew install maven (macOS) or use SDKMAN.
Java version mismatch: ensure JAVA_HOME points to 17 and VS Code workspace JDK is 17.
JavaFX errors about resize policy: you’re on JavaFX 17; the code uses the classic
TableView.CONSTRAINED_RESIZE_POLICY (compatible). If you upgrade to FX 21+, the flexible policy is available.

## Improvements
Line charts for each symbol (price over time) and order-book depth view.
Additional order types (stop, iceberg), fees, and full execution reports.
Persistence (H2/PostgreSQL) for trades and daily P&L.
WebSocket market data + web UI.

## License
MIT