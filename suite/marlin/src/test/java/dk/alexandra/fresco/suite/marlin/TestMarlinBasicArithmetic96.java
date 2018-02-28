package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kOpenedValueStoreImpl;
import java.util.function.Supplier;

public class TestMarlinBasicArithmetic96 extends MarlinTestSuite<Spdz2kResourcePool<CompUInt96>> {

  @Override
  protected Spdz2kResourcePool<CompUInt96> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt96> factory = new CompUInt96Factory();
    Spdz2kResourcePool<CompUInt96> resourcePool =
        new Spdz2kResourcePoolImpl<>(
            playerId,
            noOfParties, null,
            new Spdz2kOpenedValueStoreImpl<>(),
            new Spdz2kDummyDataSupplier<>(playerId, noOfParties, factory.createRandom(), factory),
            factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt96>> createProtocolSuite() {
    return new Spdz2kProtocolSuite96();
  }

}
