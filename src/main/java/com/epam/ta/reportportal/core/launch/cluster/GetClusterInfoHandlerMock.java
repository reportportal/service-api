package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
public class GetClusterInfoHandlerMock implements GetClusterInfoHandler {

	public static final String LINE_BREAK = "\n";

	private final Function<Long, List<ClusterInfoResource>> contentProvider = this::getClusterContent;

	@Override
	public Iterable<ClusterInfoResource> getResources(Launch launch, Pageable pageable) {
		final List<ClusterInfoResource> content = contentProvider.apply(launch.getId());

		final List<ClusterInfoResource> foundContent = content.stream()
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize())
				.collect(Collectors.toList());

		final Page<ClusterInfoResource> contentPage = new PageImpl<>(foundContent, pageable, content.size());
		return PagedResourcesAssembler.<ClusterInfoResource>pageConverter().apply(contentPage);
	}

	private List<ClusterInfoResource> getClusterContent(Long launchId) {
		return LongStream.range(1, 11).mapToObj(i -> {
			final StringBuilder clusterMessageBuilder = new StringBuilder();
			LongStream.range(0, i)
					.forEach(logLineNumber -> clusterMessageBuilder.append("This is the error log line with index: ").append(logLineNumber).append(LINE_BREAK));
			final String message = clusterMessageBuilder.toString();
			final ClusterInfoResource clusterInfoResource = new ClusterInfoResource();
			clusterInfoResource.setId(i);
			clusterInfoResource.setLaunchId(launchId);
			clusterInfoResource.setMessage(message);
			return clusterInfoResource;
		}).collect(Collectors.toList());
	}

}
