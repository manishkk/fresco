package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.eq.EqualityConstRounds;
import dk.alexandra.fresco.lib.compare.gt.LessThanOrEquals;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTest;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestLogRounds;
import java.math.BigInteger;

/**
 * Default way of producing the protocols within the interface. This default class can be
 * overwritten when implementing {@link BuilderFactoryNumeric} if the protocol suite has a better
 * and more efficient way of constructing the protocols.
 */
public class DefaultComparison implements Comparison {

  // Security parameter used by protocols using rightshifts and/or additive masks.
  private final int magicSecureNumber = 60;
  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;

  public DefaultComparison(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  @Override
  public DRes<SInt> compareLEQLong(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength() * 2;
    LessThanOrEquals leqProtocol = new LessThanOrEquals(
        bitLength, magicSecureNumber, x, y);
    return builder.seq(leqProtocol);

  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y) {
    int maxBitLength = builder.getBasicNumericContext().getMaxBitLength();
    return equals(maxBitLength, x, y);
  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y, ComparisonAlgorithm algorithm) {
    int maxBitLength = builder.getBasicNumericContext().getMaxBitLength();
    return equals(maxBitLength, x, y, algorithm);
  }

  @Override
  public DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y,
      ComparisonAlgorithm algorithm) {
    if (algorithm == ComparisonAlgorithm.CONST_ROUNDS) {
      return builder.seq(new EqualityConstRounds(bitLength, x, y));
    } else {
      DRes<SInt> diff = builder.numeric().sub(x, y);
      return builder.seq(new ZeroTestLogRounds(diff, bitLength));
    }
  }

  @Override
  public DRes<SInt> compareLEQ(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength();
    return builder.seq(
        new LessThanOrEquals(bitLength, magicSecureNumber, x, y));
  }

  @Override
  public DRes<SInt> sign(DRes<SInt> x) {
    Numeric input = builder.numeric();
    // TODO create a compareLeqOrEqZero on comparison builder
    DRes<SInt> compare =
        compareLEQ(input.known(BigInteger.ZERO), x);
    BigInteger oint = BigInteger.valueOf(2);
    Numeric numericBuilder = builder.numeric();
    DRes<SInt> twice = numericBuilder.mult(oint, compare);
    return numericBuilder.sub(twice, BigInteger.valueOf(1));
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength) {
    return builder.seq(new ZeroTest(bitLength, x, magicSecureNumber));
  }

  protected ProtocolBuilderNumeric getBuilder() {
    return builder;
  }

}
