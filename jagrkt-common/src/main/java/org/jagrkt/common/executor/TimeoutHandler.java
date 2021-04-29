/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common.executor;

import com.google.inject.Inject;
import org.jagrkt.common.Config;
import org.jagrkt.common.transformer.TimeoutTransformer;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Injected via ASM by {@link TimeoutTransformer}.
 *
 * @see #checkTimeout() for more information.
 */
@SuppressWarnings("unused")
public class TimeoutHandler {

  private static final ThreadLocal<AtomicLong> LAST_TIMEOUT = ThreadLocal.withInitial(AtomicLong::new);
  private static final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

  @Inject
  private static Config config;

  @Inject
  private static Logger logger;

  /**
   * Nested class for lazy initialization.
   */
  private static final class Lazy {
    private static final long INDIVIDUAL_TIMEOUT = config.getTransformers().getTimeout().getIndividualTimeout();
    private static final long TOTAL_TIMEOUT = config.getTransformers().getTimeout().getTotalTimeout();
  }

  /**
   * Checks whether the current {@link Thread} has reached a timeout, throwing an {@link AssertionFailedError} if so.
   *
   * @throws AssertionFailedError If the timeout has been reached.
   */
  public static void checkTimeout() {
    final long userTime = mxBean.getThreadUserTime(Thread.currentThread().getId()) / 1_000_000;
    final long lastTimeout = LAST_TIMEOUT.get().get();
    if (lastTimeout == 0) {
      LAST_TIMEOUT.get().set(userTime);
    } else if (userTime - lastTimeout > Lazy.INDIVIDUAL_TIMEOUT) {
      if (userTime > Lazy.TOTAL_TIMEOUT) {
        logger.error("Total timeout after " + Lazy.TOTAL_TIMEOUT + "ms @ " + Thread.currentThread().getName());
        // do not reset LAST_TIMEOUT
        throw new AssertionFailedError("Total timeout after " + Lazy.TOTAL_TIMEOUT + "ms");
      } else {
        logger.error("Individual timeout after " + Lazy.INDIVIDUAL_TIMEOUT + "ms @ " + Thread.currentThread().getName());
        // reset LAST_TIMEOUT so that the next JUnit test doesn't immediately fail
        LAST_TIMEOUT.get().set(userTime);
        throw new AssertionFailedError("Individual timeout after " + Lazy.INDIVIDUAL_TIMEOUT + "ms");
      }
    }
  }
}
