package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.Spdz2kInputComputation;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kAddKnownProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kAddProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kMultiplyProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOutputSinglePartyProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kRandomBitProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kRandomElementProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kSubtractFromKnownProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kSubtractProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kTwoPartyInputProtocol;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Basic native builder for the SPDZ2k protocol suite.
 *
 * @param <PlainT> the type representing open values
 */
public class Spdz2kBuilder<PlainT extends CompUInt<?, ?, PlainT>> implements
    BuilderFactoryNumeric {

  private final CompUIntFactory<PlainT> factory;
  private final CompUIntArithmetic<PlainT> arithmetic;
  private final BasicNumericContext numericContext;

  public Spdz2kBuilder(CompUIntFactory<PlainT> factory, BasicNumericContext numericContext) {
    this.factory = factory;
    this.arithmetic = new CompUIntArithmetic<>(factory);
    this.numericContext = numericContext;
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return numericContext;
  }

  @Override
  public Comparison createComparison(ProtocolBuilderNumeric builder) {
    return new Spdz2kComparison<>(this, builder, factory);
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    return new Numeric() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new Spdz2kAddProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return builder.append(new Spdz2kAddKnownProtocol<>(factory.fromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> add(OInt a, DRes<SInt> b) {
        return null;
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new Spdz2kSubtractProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.append(
            new Spdz2kSubtractFromKnownProtocol<>(factory.fromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> sub(OInt a, DRes<SInt> b) {
        return null;
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, OInt b) {
        return null;
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return builder.append(
            new Spdz2kAddKnownProtocol<>(factory.fromBigInteger(b).negate(), a));
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new Spdz2kMultiplyProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return () -> toSpdz2kSInt(b).multiply(factory.fromBigInteger(a));
      }

      @Override
      public DRes<SInt> mult(OInt a, DRes<SInt> b) {
        return null;
      }

      @Override
      public DRes<SInt> randomBit() {
        return builder.append(new Spdz2kRandomBitProtocol<>());
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.append(new Spdz2kRandomElementProtocol<>());
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.append(new Spdz2kKnownSIntProtocol<>(factory.fromBigInteger(value)));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        if (builder.getBasicNumericContext().getNoOfParties() <= 2) {
          return builder.append(
              new Spdz2kTwoPartyInputProtocol<>(factory.fromBigInteger(value),
                  inputParty));
        } else {
          return builder.seq(
              new Spdz2kInputComputation<>(factory.fromBigInteger(value), inputParty));
        }
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return builder.append(new Spdz2kOutputToAllProtocol<>(secretShare));
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare) {
        return null;
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare, int outputParty) {
        return null;
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        return builder.append(new Spdz2kOutputSinglePartyProtocol<>(secretShare, outputParty));
      }
    };
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OIntFactory getOIntFactory() {
    return factory;
  }

  @Override
  public OIntArithmetic getOIntArithmetic() {
    return arithmetic;
  }

  /**
   * Get result from deferred and downcast result to {@link Spdz2kSInt<PlainT>}.
   */
  private Spdz2kSInt<PlainT> toSpdz2kSInt(DRes<SInt> value) {
    return Objects.requireNonNull((Spdz2kSInt<PlainT>) value.out());
  }

  @Override
  public RealNumericContext getRealNumericContext() {
    // TODO Auto-generated method stub
    return null;
  }

}
