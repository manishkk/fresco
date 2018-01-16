package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiplyRightHelper extends MultiplySharedHelper {

  public MultiplyRightHelper(MascotResourcePool resourcePool,
      Network network, Integer otherId, int numLeftFactors) {
    super(resourcePool, network, otherId, numLeftFactors);
  }

  public MultiplyRightHelper(MascotResourcePool resourcePool,
      Network network, Integer otherId) {
    super(resourcePool, network, otherId, 1);
  }

  /**
   * Generate random seed pairs using OT. <br> The seed pairs are correlated with the
   * multiplication factors of the other party. If the other party's factors (represented as a bit
   * vector) is 010, this party will receive seed pairs <i>(a<sub>0</sub>, a<sub>1</sub>),
   * (b<sub>0</sub>, b<sub>1</sub>), (c<sub>0</sub>, c<sub>1</sub>)</i> whereas the other party
   * will receive seeds <i>a<sub>0</sub>, b<sub>1</sub>, c<sub>0</sub></i>. The parties can use
   * the resulting seeds to compute the shares of the product of their factors.
   *
   * @param numMults the number of total multiplications
   * @param seedLength the bit length of the seeds
   * @return the seed pairs
   */
  public List<Pair<StrictBitVector, StrictBitVector>> generateSeeds(int numMults,
      int seedLength) {
    // perform rots for each bit, for each left factor, for each multiplication
    int numRots = getModBitLength() * getNumLeftFactors() * numMults;
    List<Pair<StrictBitVector, StrictBitVector>> seeds = getRot().send(numRots, seedLength);
    // TODO temporary fix until big-endianness issue is resolved
    Collections.reverse(seeds);
    return seeds;
  }

  public FieldElement computeDiff(Pair<FieldElement, FieldElement> feSeedPair,
      FieldElement factor) {
    FieldElement left = feSeedPair.getFirst();
    FieldElement right = feSeedPair.getSecond();
    FieldElement diff = left.subtract(right).add(factor);
    return diff;
  }

  /**
   * Computes "masked" share of each bit of each of this party's factors. <br> For each seed pair
   * (q0, q1)_n compute q0 - q1 + bn where bn is the n-th bit of this party's factor.
   *
   * @param feSeedPairs seed pairs as field elements
   * @param rightFactors this party's factors
   * @return masked shares of this party's factor's bits.
   */
  public List<FieldElement> computeDiffs(List<Pair<FieldElement, FieldElement>> feSeedPairs,
      List<FieldElement> rightFactors) {
    List<FieldElement> diffs = new ArrayList<FieldElement>(feSeedPairs.size());
    int rightFactorIdx = 0;
    int seedPairIdx = 0;
    for (Pair<FieldElement, FieldElement> feSeedPair : feSeedPairs) {
      FieldElement rightFactor = rightFactors.get(rightFactorIdx);
      FieldElement diff = computeDiff(feSeedPair, rightFactor);
      diffs.add(diff);
      seedPairIdx++;
      rightFactorIdx = seedPairIdx / (getNumLeftFactors() * getModBitLength());
    }
    return diffs;
  }

  /**
   * Computes this party's shares of the final products. <br> For each seed pair (q0, q1) this
   * party holds, uses q0 to recombine into field elements representing the product shares.
   *
   * @param feZeroSeeds the zero choice seeds
   * @param numRightFactors number of total right factors
   * @return shares of products
   */
  public List<FieldElement> computeProductShares(List<FieldElement> feZeroSeeds,
      int numRightFactors) {
    int groupBitLength = getNumLeftFactors() * getModBitLength();
    List<FieldElement> productShares = new ArrayList<FieldElement>(numRightFactors);
    for (int rightFactIdx = 0; rightFactIdx < numRightFactors; rightFactIdx++) {
      for (int leftFactIdx = 0; leftFactIdx < getNumLeftFactors(); leftFactIdx++) {
        int from = rightFactIdx * groupBitLength + leftFactIdx * getModBitLength();
        int to = rightFactIdx * groupBitLength + (leftFactIdx + 1) * getModBitLength();
        List<FieldElement> subFactors = feZeroSeeds.subList(from, to);
        FieldElement recombined = getFieldElementUtils().recombine(subFactors);
        productShares.add(recombined.negate());
      }
    }
    return productShares;
  }
}
