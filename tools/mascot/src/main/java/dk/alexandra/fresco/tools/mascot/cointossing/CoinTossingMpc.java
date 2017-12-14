package dk.alexandra.fresco.tools.mascot.cointossing;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.commit.CommitmentBasedProtocol;
import java.util.List;

public class CoinTossingMpc extends CommitmentBasedProtocol<StrictBitVector> {

  /**
   * Creates new coin-tossing protocol.
   * 
   * @param resourcePool {@inheritDoc}
   */
  public CoinTossingMpc(MascotResourcePool resourcePool, Network network) {
    super(resourcePool, network, resourcePool.getStrictBitVectorSerializer());
  }

  /**
   * Computes all parties seeds into one by xoring.
   * 
   * @param seeds all parties' seeds
   * @return shared seed
   */
  StrictBitVector combine(List<StrictBitVector> seeds) {
    StrictBitVector acc = seeds.get(0);
    for (StrictBitVector seed : seeds.subList(1, seeds.size())) {
      acc.xor(seed);
    }
    return acc;
  }

  /**
   * Generates random seed and calls {@link CoinTossingMpc#generateJointSeed(StrictBitVector)}.
   * 
   * @param bitLengthSeed bit length of seed
   * @return shared seed
   */
  public StrictBitVector generateJointSeed(int bitLengthSeed) {
    // generate own seed
    return generateJointSeed(new StrictBitVector(bitLengthSeed, resourcePool.getRandomGenerator()));
  }

  /**
   * Distribute seeds and combine into single, joint seed.
   * 
   * @param ownSeed own random seed
   * @return shared seed
   */
  public StrictBitVector generateJointSeed(StrictBitVector ownSeed) {
    // distribute seeds
    List<StrictBitVector> allSeeds = allCommit(ownSeed);
    // combine seeds
    return combine(allSeeds);
  }

}
