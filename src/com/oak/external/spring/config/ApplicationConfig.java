package com.oak.external.spring.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oak.external.finance.app.marketdata.api.DataConnector;
import com.oak.external.finance.app.marketdata.api.MarketDataProvider;
import com.oak.external.finance.app.marketdata.api.impl.MarketDataPollingProviderImpl;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.BalanceSheetDao;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.CashFlowStatementDao;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.IncomeStatementDao;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.YahooDataConnector;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.YahooDataConverter;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.YahooWebDataBalanceSheetDao;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.YahooWebDataCashFlowStatementDao;
import com.oak.external.finance.app.marketdata.api.impl.yahoo.YahooWebDataIncomeStatementDao;
import com.oak.external.finance.app.marketdata.api.yahoo.YahooDataConverterImpl;
import com.oak.external.utils.input.api.StreamProvider;
import com.oak.external.utils.input.api.impl.FileStreamProvider;
import com.oak.finance.app.dao.SymbolsDao;
import com.oak.finance.app.dao.impl.files.SymbolsFileDao;
import com.oak.finance.app.main.server.ApplicationServer;
import com.oak.finance.app.main.server.ApplicationServerImpl;
import com.oak.finance.app.monitor.MarketDataMonitorsController;
import com.oak.finance.app.monitor.MarketDataMonitorsControllerImpl;
import com.oak.finance.app.monitor.analysis.FinanceAnalysisController;
import com.oak.finance.app.monitor.analysis.FinanceFundamentalAnalysisControllerImpl;
import com.oak.finance.interest.SymbolProviderImpl;
import com.oak.finance.interest.SymbolsProvider;

@Configuration
public class ApplicationConfig {

	private int waitInMilliseconds = 500;
	private long historyBackInMilliSeconds = 7 * 24 * 60 * 60 * 1000;
	// private String stocksFilename = "/stocks/yahoo.csv";
	private String stocksFilename = "C:\\Users\\charb_000\\Documents\\invest\\data\\yahoo.csv";
	private String interestingCompaniesSymbolsFileName = "C:\\Users\\charb_000\\Documents\\invest\\data\\interestingCompaniesSymbolsFileName.csv";
	// private String stocksFilename =
	// "C:\\Users\\charb_000\\Documents\\invest\\data\\yahoo.us.csv";
	// private String stocksFilename =
	// "C:\\Users\\charb_000\\Documents\\invest\\data\\test_yahoo.csv";
	// private String stocksStocksWithBadPrices =
	// "/stocks/yahooStocksWithoutPrices.csv";
	// private String stocksWithNoPriceFileName=
	// "/stocks/stocksWithNoPrice.txt";
	private String stocksWithNoPriceFileName = "C:\\Users\\charb_000\\Documents\\invest\\data\\stocksWithNoPrice.txt";
	private String stocksToWatch = "C:\\Users\\charb_000\\Documents\\invest\\data\\watchList.csv";
	// private String stocksToWatch =
	// "C:\\Users\\charb_000\\Documents\\invest\\data\\test_watchList.csv";
	private Double targetMinCurrentRatio = 2.0;
	private Double targetMinQuickRatio = 1.0;
	private Double targetMinAssetToDebtRatio = 3.0;
	
	private Logger log = LogManager.getLogger(ApplicationConfig.class);

	@Bean
	ApplicationServer app() {
		log.debug("creating app...");
		ApplicationServerImpl applicationServer = new ApplicationServerImpl(
				symbolProvider(), marketDataMonitorsController(),
				LogManager.getFormatterLogger(ApplicationServerImpl.class));
		log.debug("creating app...done");
		return applicationServer;
	}

	@Bean
	SymbolsProvider symbolProvider() {
		log.debug("creating stockListProvider...");
		SymbolProviderImpl symbolProvider = new SymbolProviderImpl(
				symbolsDao(),
				LogManager.getFormatterLogger(SymbolProviderImpl.class));
		log.debug("creating stockListProvider...done");
		return symbolProvider;
	}

	@Bean
	StreamProvider streamProvider() {
		log.debug("creating streamProvider...");
		// ResourceFileStreamProvider resourceFileStreamProvider = new
		// ResourceFileStreamProvider(stocksFilename,
		// LogManager.getFormatterLogger(ResourceFileStreamProvider.class));
		StreamProvider streamProvider = new FileStreamProvider(stocksFilename,
				LogManager.getFormatterLogger(FileStreamProvider.class));
		log.debug("creating streamProvider...done");
		return streamProvider;
	}

	@Bean
	SymbolsDao symbolsDao() {
		log.debug("creating symbolsDao...");
		SymbolsFileDao symbolsFileDao = new SymbolsFileDao(stocksFilename,
				stocksWithNoPriceFileName, stocksToWatch,
				interestingCompaniesSymbolsFileName, streamProvider(),
				LogManager.getFormatterLogger(SymbolsFileDao.class));
		log.debug("creating symbolsDao...done");
		return symbolsFileDao;
	}

	@Bean
	MarketDataProvider marketDataProvider() {
		log.debug("creating marketDataProvider...");
		MarketDataProvider marketDataPollingProvider = new MarketDataPollingProviderImpl(
				yahooConnector(),
				waitInMilliseconds,
				historyBackInMilliSeconds,
				LogManager
						.getFormatterLogger(MarketDataPollingProviderImpl.class));
		log.debug("creating marketDataProvider...done");
		return marketDataPollingProvider;
	}

	@Bean
	DataConnector yahooConnector() {
		log.debug("creating yahooConnector...");
		Logger logger = LogManager.getFormatterLogger(YahooDataConnector.class);
		YahooDataConnector yahooDataConnector = new YahooDataConnector(logger,
				yahooDataConverter());
		log.debug("creating yahooConnector...done");
		return yahooDataConnector;
	}

	@Bean
	YahooDataConverter yahooDataConverter() {
		log.debug("creating yahooDataConverter...");
		YahooDataConverterImpl yahooDataConverter = new YahooDataConverterImpl(
				LogManager.getFormatterLogger(YahooDataConverterImpl.class));
		log.debug("creating yahooDataConverter... Done");
		return yahooDataConverter;
	}

	@Bean
	MarketDataMonitorsController marketDataMonitorsController() {
		log.debug("creating marketDataMonitorsController...");
		MarketDataMonitorsController marketDataMonitorsController = new MarketDataMonitorsControllerImpl(
				symbolProvider(),
				financeAnalysisController(),
				marketDataProvider(),
				LogManager
						.getFormatterLogger(MarketDataMonitorsControllerImpl.class));
		log.debug("creating marketDataMonitorsController...done");
		return marketDataMonitorsController;
	}

	@Bean
	BalanceSheetDao balanceSheetDao() {
		log.debug("creating balanceSheetDao...");
		BalanceSheetDao yahooWebDataBalanceSheet = new YahooWebDataBalanceSheetDao(
				LogManager
						.getFormatterLogger(YahooWebDataBalanceSheetDao.class));
		log.debug("creating balanceSheetDao... done");
		return yahooWebDataBalanceSheet;
	}
	@Bean 
	IncomeStatementDao incomeStatementDao(){
		log.debug("creating incomeStatementDao... instance of YahooWebDataIncomeStatementDao");
		YahooWebDataIncomeStatementDao yahooWebDataIncomeStatementDao = new YahooWebDataIncomeStatementDao(LogManager.getFormatterLogger(YahooWebDataIncomeStatementDao.class));
		log.debug("creating incomeStatementDao...done ");
		return yahooWebDataIncomeStatementDao; 
	}
	@Bean 
	CashFlowStatementDao cashFlowStatementDao(){
		log.debug("creating incomeStatementDao... instance of YahooWebDataIncomeStatementDao");
		YahooWebDataCashFlowStatementDao yahooWebDataCashFlowStatementDao = new YahooWebDataCashFlowStatementDao(LogManager.getFormatterLogger(YahooWebDataIncomeStatementDao.class));
		log.debug("creating incomeStatementDao...done ");
		return yahooWebDataCashFlowStatementDao; 
	}
	@Bean
	FinanceAnalysisController financeAnalysisController() {
		log.debug("creating financeAnalysisController...");
		Logger logger = LogManager .getFormatterLogger(FinanceFundamentalAnalysisControllerImpl.class);
		FinanceFundamentalAnalysisControllerImpl financeFundamentalAnalysisController = new FinanceFundamentalAnalysisControllerImpl(
				balanceSheetDao(), incomeStatementDao(), cashFlowStatementDao(),
				targetMinCurrentRatio, targetMinQuickRatio, targetMinAssetToDebtRatio, logger);
		log.debug("creating financeAnalysisController...done");
		return financeFundamentalAnalysisController;
	}
}