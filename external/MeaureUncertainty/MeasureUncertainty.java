package org.broadinstitute.sting.gatk.walkers.qc;

import org.broadinstitute.sting.commandline.Output;
import org.broadinstitute.sting.gatk.CommandLineGATK;
import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.walkers.LocusWalker;
import org.broadinstitute.sting.gatk.walkers.NanoSchedulable;
import org.broadinstitute.sting.gatk.walkers.TreeReducible;
import org.broadinstitute.sting.utils.help.DocumentedGATKFeature;

import java.io.PrintStream;

/**
 * Walks over the input data set, calculating the uncertainty of results at the position.
 *
 * <p>
 * This is used to evaluate the combination of refseq, aligner and variant detector
 * by measuring where in the reference misalignment does not translate into confident variants.
 *
 *
 * <h2>Input</h2>
 * <p>
 * One or more BAM files.
 * </p>
 *
 * <h2>Output</h2>
 * <p>
 *  A .bam.uncertainty file with the following structure:
 *
 *  For each position, we record the number of reads which cross the position, 
 *  broken down by category:
 *    1 matches to the reference
 *    2 mismatches with base A
 *    3 mismatches with base C
 *    4 mismatches with base G
 *    5 mismatches with base T
 *    6 deletion at the position
 *    7 insertions at the following position
 *
 *  The output of this tool does not vary with variant detection results.
 *  It is passed to the companion tool ReportUncertainty, which will take
 *  the uncertainty file from this process, and the confident variant detection
 *  results, and "subtract":
 *    - Each high-confidence SNV moves reads from category 2-5 to 1
 *    - Each high-confidence indel moves reads from category 6 or 7 to 1
 *
 *  The final output of the companion tool ReportUncertainty, is a probability
 *  that the align/detect process is failing at the position in question.
 *
 * </p>
 *
 * <h2>Examples</h2>
 * <pre>
 * java -Xmx2g -jar GenomeAnalysisTK.jar \
 *   -T MeasureUncertainty \
 *   -R ref.fasta \
 *   -o output.txt \
 *   -I input.bam \
 *   [-L input.intervals]
 * </pre>
 *
 */
@DocumentedGATKFeature( groupName = "The Genome Institute at Washington University Genome Modeling System Extensions", extraDocs = {CommandLineGATK.class} )
public class MeasureUncertainty extends LocusWalker<Integer, Long> implements TreeReducible<Long>, NanoSchedulable {
    @Output(doc="Write count to this file instead of STDOUT")
    PrintStream out;

    public Integer map(RefMetaDataTracker tracker, ReferenceContext ref, AlignmentContext context) {
        return 1;
    }

    public Long reduceInit() { return 0l; }

    public Long reduce(Integer value, Long sum) {
        return value + sum;
    }

    /**
     * Reduces two subtrees together.  In this case, the implementation of the tree reduce
     * is exactly the same as the implementation of the single reduce.
     */
    public Long treeReduce(Long lhs, Long rhs) {
        return lhs + rhs;
    }

    public void onTraversalDone( Long c ) {
        out.println(c);
    }
}


