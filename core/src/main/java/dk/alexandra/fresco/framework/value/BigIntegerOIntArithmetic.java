package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link OIntArithmetic} for the case when open values are represented directly
 * via {@link BigInteger}.
 */
public class BigIntegerOIntArithmetic implements OIntArithmetic {
  private static final BigInteger TWO = new BigInteger("2");
  // TODO wrapping all OInts in DRes seems like a bad idea
  private List<DRes<OInt>> twoPowersList;
  private final OIntFactory factory;

  public BigIntegerOIntArithmetic(OIntFactory factory) {
    this.factory = factory;
    twoPowersList = new ArrayList<>(1);
    twoPowersList.add(() -> new BigIntegerOInt(BigInteger.ONE));
  }

  @Override
  public List<DRes<OInt>> toBits(OInt openValue, int numBits) {
    BigInteger value = factory.toBigInteger(openValue);
    List<DRes<OInt>> bits = new ArrayList<>(numBits);
    for (int b = 0; b < numBits; b++) {
      boolean boolBit = value.testBit(b);
      OInt bit = boolBit ? factory.one() : factory.zero();
      bits.add(() -> bit);
    }
    Collections.reverse(bits);
    return bits;
  }

  @Override
  public List<DRes<OInt>> getPowersOfTwo(int numPowers) {
    // TODO do we need modular reduction in here?
    // TODO taken from MiscBigIntegerGenerators, clean up
    int currentLength = twoPowersList.size();
    if (numPowers > currentLength) {
      ArrayList<DRes<OInt>> newTwoPowersList = new ArrayList<>(numPowers);
      newTwoPowersList.addAll(twoPowersList);
      BigInteger currentValue = ((BigIntegerOInt) newTwoPowersList.get(currentLength - 1).out())
          .getValue();
      while (numPowers > newTwoPowersList.size()) {
        currentValue = currentValue.shiftLeft(1);
        newTwoPowersList.add(new BigIntegerOInt(currentValue));
      }
      twoPowersList = Collections.unmodifiableList(newTwoPowersList);
    }
    return twoPowersList.subList(0, numPowers);
  }

  @Override
  public DRes<OInt> twoTo(int power) {
    return factory.fromBigInteger(TWO.pow(power));
  }

  @Override
  public DRes<OInt> modTwoTo(OInt input, int power) {
    return factory.fromBigInteger(factory.toBigInteger(input).mod(TWO.pow(
        power)));
  }

}
