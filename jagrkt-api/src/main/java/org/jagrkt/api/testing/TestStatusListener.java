package org.jagrkt.api.testing;

import org.junit.platform.engine.TestSource;

public interface TestStatusListener {

  boolean succeeded(TestSource source);

  boolean failed(TestSource source);
}
