package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * A protocol producer that only produces a single protocol.
 */
public class SingleProtocolProducer<T> implements ProtocolProducer, DRes<T> {

  private NativeProtocol<T, ?> protocol;
  private boolean evaluated = false;
  private T result;

  public SingleProtocolProducer(NativeProtocol<T, ?> protocol) {
    this.protocol = protocol;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    evaluated = true;
    protocolCollection.addProtocol(protocol);
  }

  @Override
  public boolean hasNextProtocols() {
    return !evaluated;
  }

  @Override
  public String toString() {
    return "SingleProtocolProducer{"
        + "protocol=" + protocol
        + '}';
  }

  @Override
  public T out() {
    if (result == null) {
      result = protocol.out();
      // Break chain of native protocols to ensure garbage collection
      protocol = null;
    }
    return result;
  }
}
