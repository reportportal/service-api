package com.epam.ta.reportportal.ws.param;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class HistoryProviderFactory {

	private Map<ItemHistoryBaselineEnum, HistoryProvider> historyProviderMapping;

	@Autowired
	public HistoryProviderFactory(Map<ItemHistoryBaselineEnum, HistoryProvider> historyProviderMapping) {
		this.historyProviderMapping = historyProviderMapping;
	}

	public Optional<HistoryProvider> getProvider(HistoryRequestParams historyRequestParams) {
		return ItemHistoryBaselineEnum.resolveType(historyRequestParams).map(this.historyProviderMapping::get);
	}
}
