/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.configs;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Fixing hack for a envoy proxy issue https://github.com/envoyproxy/envoy/issues/5528
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProxyHeadersFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest originalReq = (HttpServletRequest) request;
		String originalPath = originalReq.getHeader("x-envoy-original-path");
		if (originalPath != null) {
			MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(originalReq);
			mutableRequest.putHeader("x-forwarded-prefix", originalPath);
			chain.doFilter(mutableRequest, response);
		} else {
			chain.doFilter(request, response);
		}
	}
}
