import java.io.IOException;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

public class BM25LSimilarity extends Similarity {
  
  private final float k1;
  private final float b;
  private final float d;

  public BM25LSimilarity(float k1, float b, float d) {
    this.k1 = k1;
    this.b  = b;
    this.d  = d;
  }

  public BM25LSimilarity(float d) {
	this.k1 = 1.2f;
	this.b  = 0.75f;
    this.d  = d;
  }
  
  public BM25LSimilarity() {
    this.k1 = 1.2f;
    this.b  = 0.75f;
    this.d  = 0.5f;
  }
  
  protected float idf(long docFreq, long numDocs) {
    return (float) Math.log((numDocs + 1)/(docFreq + 0.5D));
  }
  
  protected float avgFieldLength(CollectionStatistics collectionStats) {
    final long sumTotalTermFreq = collectionStats.sumTotalTermFreq();
    if (sumTotalTermFreq <= 0) {
      return 1f;       // field does not exist, or stat is unsupported
    } else {
      return (float) (sumTotalTermFreq / (double) collectionStats.maxDoc());
    }
  }
  
  protected byte encodeNormValue(float boost, int fieldLength) {
    return SmallFloat.floatToByte315(boost / (float) Math.sqrt(fieldLength));
  }

  protected float decodeNormValue(byte b) {
    return NORM_TABLE[b & 0xFF];
  }
  
  protected boolean discountOverlaps = true;
  
  private static final float[] NORM_TABLE = new float[256];

  static {
    for (int i = 0; i < 256; i++) {
      float f = SmallFloat.byte315ToFloat((byte)i);
      NORM_TABLE[i] = 1.0f / (f*f);
    }
  }

  @Override
  public final long computeNorm(FieldInvertState state) {
    final int numTerms = discountOverlaps ? state.getLength() - state.getNumOverlap() : state.getLength();
    return encodeNormValue(state.getBoost(), numTerms);
  }

  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
    final long df = termStats.docFreq();
    final long max = collectionStats.maxDoc();
    final float idf = idf(df, max);
    return new Explanation(idf, "idf(docFreq=" + df + ", maxDocs=" + max + ")");
  }

  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats[]) {
    final long max = collectionStats.maxDoc();
    float idf = 0.0f;
    final Explanation exp = new Explanation();
    exp.setDescription("idf(), sum of:");
    for (final TermStatistics stat : termStats ) {
      final long df = stat.docFreq();
      final float termIdf = idf(df, max);
      exp.addDetail(new Explanation(termIdf, "idf(docFreq=" + df + ", maxDocs=" + max + ")"));
      idf += termIdf;
    }
    exp.setValue(idf);
    return exp;
  }

  @Override
  public final SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) {
    Explanation idf = termStats.length == 1 ? idfExplain(collectionStats, termStats[0]) : idfExplain(collectionStats, termStats);

    float avgdl = avgFieldLength(collectionStats);

    // compute freq-independent part of bm25 equation across all norm values
    float cache[] = new float[256];
    for (int i = 0; i < cache.length; i++) {
      cache[i] = (1 - b) + b * decodeNormValue((byte)i) / avgdl;
      cache[i] *= (k1 + d);
    }
    return new BM25Stats(collectionStats.field(), idf, queryBoost, avgdl, cache);
  }

  @Override
  public final SimScorer simScorer(SimWeight stats, AtomicReaderContext context) throws IOException {
    BM25Stats bm25stats = (BM25Stats) stats;
    return new BM25DocScorer(bm25stats, context.reader().getNormValues(bm25stats.field));
  }
  
  private class BM25DocScorer extends SimScorer {
    private final BM25Stats stats;
    private final float weightValue; // boost * idf * (k1 + 1)
    private final NumericDocValues norms;
    private final float[] cache;
    
    BM25DocScorer(BM25Stats stats, NumericDocValues norms) throws IOException {
      this.stats = stats;
      this.weightValue = stats.weight * (k1 + 1);
      this.cache = stats.cache;
      this.norms = norms;
    }
    
    @Override
    public float score(int doc, float freq) {
      // if there are no norms, we act as if b=0
      float norm = norms == null ? k1 : cache[(byte)norms.get(doc) & 0xFF];
      return weightValue * freq / (freq + norm);
    }
    
    @Override
    public Explanation explain(int doc, Explanation freq) {
      return explainScore(doc, freq, stats, norms);
    }

    @Override
    public float computeSlopFactor(int distance) {
      return 1.0f / (distance + 1);
    }

    @Override
    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
      return 1;
    }
  }
  
  /** Collection statistics for the BM25 model. */
  private static class BM25Stats extends SimWeight {
    /** BM25's idf */
    private final Explanation idf;
    /** The average document length. */
    private final float avgdl;
    /** query's inner boost */
    private final float queryBoost;
    /** query's outer boost (only for explain) */
    private float topLevelBoost;
    /** weight (idf * boost) */
    private float weight;
    /** field name, for pulling norms */
    private final String field;
    /** precomputed norm[256] with (k1+d) * ((1 - b) + b * dl / avgdl) */
    private final float cache[];

    BM25Stats(String field, Explanation idf, float queryBoost, float avgdl, float cache[]) {
      this.field = field;
      this.idf = idf;
      this.queryBoost = queryBoost;
      this.avgdl = avgdl;
      this.cache = cache;
    }

    @Override
    public float getValueForNormalization() {
      // we return a TF-IDF like normalization to be nice, but we don't actually normalize ourselves.
      final float queryWeight = idf.getValue() * queryBoost;
      return queryWeight * queryWeight;
    }

    @Override
    public void normalize(float queryNorm, float topLevelBoost) {
      // we don't normalize with queryNorm at all, we just capture the top-level boost
      this.topLevelBoost = topLevelBoost;
      this.weight = idf.getValue() * queryBoost * topLevelBoost;
    } 
  }
  
  private Explanation explainScore(int doc, Explanation freq, BM25Stats stats, NumericDocValues norms) {
    Explanation result = new Explanation();
    result.setDescription("score(doc="+doc+",freq="+freq+"), product of:");
    
    Explanation boostExpl = new Explanation(stats.queryBoost * stats.topLevelBoost, "boost");
    if (boostExpl.getValue() != 1.0f)
      result.addDetail(boostExpl);
    
    result.addDetail(stats.idf);

    Explanation tfNormExpl = new Explanation();
    tfNormExpl.setDescription("tfNorm, computed from:");
    tfNormExpl.addDetail(freq);
    tfNormExpl.addDetail(new Explanation(k1, "parameter k1"));
    if (norms == null) {
      tfNormExpl.addDetail(new Explanation(0, "parameter b (norms omitted for field)"));
      tfNormExpl.setValue((freq.getValue() * (k1 + 1)) / (freq.getValue() + k1));
    } else {
      float doclen = decodeNormValue((byte)norms.get(doc));
      tfNormExpl.addDetail(new Explanation(b, "parameter b"));
      tfNormExpl.addDetail(new Explanation(stats.avgdl, "avgFieldLength"));
      tfNormExpl.addDetail(new Explanation(doclen, "fieldLength"));
      float value = 1 - b + b * doclen/stats.avgdl;
      value *= (k1 + d);
      tfNormExpl.setValue(((freq.getValue() + (1 - b + b * doclen/stats.avgdl)*d) * (k1 + 1)) / (freq.getValue() + value));
    }
    result.addDetail(tfNormExpl);
    result.setValue(boostExpl.getValue() * stats.idf.getValue() * tfNormExpl.getValue());
    return result;
  }
}




