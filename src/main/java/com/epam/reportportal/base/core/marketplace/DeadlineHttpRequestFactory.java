/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.marketplace;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * Gives every registry exchange a deadline covering the whole of it.
 *
 * <p>The socket timeout Apache applies is per read. A registry that drips one byte just inside
 * every window therefore never trips it and holds the calling thread for as long as it likes,
 * however small the timeout. Only aborting the underlying request unblocks a thread already parked
 * in a socket read, so the abort is what bounds the total time; the deadline holds whatever the
 * drip rate is, and covers the header phase as well as the body.
 */
public class DeadlineHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

  private final Duration deadline;
  private final ScheduledExecutorService watchdog;
  // Handed over within one synchronous createRequest call on the calling thread.
  private final ThreadLocal<HttpUriRequestBase> pending = new ThreadLocal<>();

  /**
   * Creates the factory.
   *
   * @param httpClient the marketplace HTTP client
   * @param deadline   how long one exchange may take in total
   * @param watchdog   scheduler that fires the aborts; not owned by this factory
   */
  public DeadlineHttpRequestFactory(HttpClient httpClient, Duration deadline,
      ScheduledExecutorService watchdog) {
    super(httpClient);
    this.deadline = deadline;
    this.watchdog = watchdog;
  }

  @Override
  protected ClassicHttpRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
    // The abortable request has to be kept: it is the only handle on the connection.
    var request = new HttpUriRequestBase(httpMethod.name(), uri);
    pending.set(request);
    return request;
  }

  @Override
  public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
    try {
      var delegate = super.createRequest(uri, httpMethod);
      var abortable = pending.get();
      return abortable == null ? delegate : new DeadlineRequest(delegate, abortable);
    } finally {
      pending.remove();
    }
  }

  private InterruptedIOException deadlineExceeded(String uri, IOException cause) {
    var exceeded = new InterruptedIOException(
        "Marketplace registry exceeded the " + deadline + " request deadline for " + uri);
    exceeded.initCause(cause);
    return exceeded;
  }

  /**
   * Arms the deadline when the exchange starts and disarms it when the response is closed.
   */
  private final class DeadlineRequest implements ClientHttpRequest {

    private final ClientHttpRequest delegate;
    private final HttpUriRequestBase abortable;

    private DeadlineRequest(ClientHttpRequest delegate, HttpUriRequestBase abortable) {
      this.delegate = delegate;
      this.abortable = abortable;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
      Future<?> abort = watchdog.schedule(abortable::abort, deadline.toMillis(),
          TimeUnit.MILLISECONDS);
      try {
        return new DeadlineResponse(delegate.execute(), abortable, abort);
      } catch (IOException e) {
        abort.cancel(false);
        // Aborting mid-headers surfaces as a closed socket; say what it really was.
        throw abortable.isAborted() ? deadlineExceeded(getURI().toString(), e) : e;
      }
    }

    @Override
    public HttpMethod getMethod() {
      return delegate.getMethod();
    }

    @Override
    public URI getURI() {
      return delegate.getURI();
    }

    @Override
    public Map<String, Object> getAttributes() {
      return delegate.getAttributes();
    }

    @Override
    public HttpHeaders getHeaders() {
      return delegate.getHeaders();
    }

    @Override
    public OutputStream getBody() throws IOException {
      return delegate.getBody();
    }
  }

  /**
   * Reports a body cut short by the deadline as the timeout it is. Without this the message
   * converter would see a closed socket and the caller would be told the registry spoke a broken
   * protocol rather than that it stalled.
   */
  private final class DeadlineResponse implements ClientHttpResponse {

    private final ClientHttpResponse delegate;
    private final HttpUriRequestBase abortable;
    private final Future<?> abort;

    private DeadlineResponse(ClientHttpResponse delegate, HttpUriRequestBase abortable,
        Future<?> abort) {
      this.delegate = delegate;
      this.abortable = abortable;
      this.abort = abort;
    }

    @Override
    public InputStream getBody() throws IOException {
      return new FilterInputStream(delegate.getBody()) {
        @Override
        public int read() throws IOException {
          try {
            return super.read();
          } catch (IOException e) {
            throw translate(e);
          }
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
          try {
            return super.read(buffer, offset, length);
          } catch (IOException e) {
            throw translate(e);
          }
        }
      };
    }

    private IOException translate(IOException e) {
      return abortable.isAborted() ? deadlineExceeded(abortable.getRequestUri(), e) : e;
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
      return delegate.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
      return delegate.getStatusText();
    }

    @Override
    public HttpHeaders getHeaders() {
      return delegate.getHeaders();
    }

    @Override
    public void close() {
      abort.cancel(false);
      delegate.close();
    }
  }
}
