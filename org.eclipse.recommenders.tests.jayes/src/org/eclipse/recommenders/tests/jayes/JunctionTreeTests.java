/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.tests.jayes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.jayes.io.XDSLReader;
import org.eclipse.recommenders.jayes.util.BayesUtils;
import org.eclipse.recommenders.tests.jayes.LBP.LoopyBeliefPropagation;
import org.eclipse.recommenders.tests.jayes.testgeneration.TestCase;
import org.eclipse.recommenders.tests.jayes.testgeneration.TestcaseDeserializer;
import org.eclipse.recommenders.tests.jayes.util.NetExamples;
import org.junit.Ignore;
import org.junit.Test;

public class JunctionTreeTests {

    @Test
    public void testInference1() {
        BayesNet net = NetExamples.testNet1();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        IBayesInferer inference = new JunctionTreeAlgorithm();
        inference.addEvidence(a, "false");
        inference.addEvidence(b, "lu");
        inference.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes())
            assertArrayEquals(compare.getBeliefs(n), inference.getBeliefs(n), 0.01);
    }

    @Test
    public void testLogScale() {
        BayesNet net = NetExamples.testNet1();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        JunctionTreeAlgorithm inferer = new JunctionTreeAlgorithm();
        inferer.setLogThreshold(0);
        inferer.addEvidence(a, "false");
        inferer.addEvidence(b, "lu");
        inferer.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes())
            assertArrayEquals(compare.getBeliefs(n), inferer.getBeliefs(n), 0.01);
    }

    @Test
    public void testMixedScale() {
        BayesNet net = NetExamples.testNet1();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        JunctionTreeAlgorithm inferer = new JunctionTreeAlgorithm();
        // a treshold of two will make the a,b,c clique log scale but the
        // c,d clique normal
        inferer.setLogThreshold(2);
        inferer.addEvidence(a, "false");
        inferer.addEvidence(b, "lu");
        inferer.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes())
            assertArrayEquals(compare.getBeliefs(n), inferer.getBeliefs(n), 0.01);
    }

    @Test
    public void testFailedCase1() {
        BayesNet net = NetExamples.testNet1();

        BayesNode b = net.getNode("b");

        JunctionTreeAlgorithm inferer = new JunctionTreeAlgorithm();
        inferer.setNetwork(net);

        Map<Integer, Integer> evidence = new HashMap<Integer, Integer>();
        evidence.put(0, 1);
        evidence.put(2, 0);
        inferer.setEvidence(BayesUtils.toNodeMap(net, evidence));
        assertEquals(0.22, inferer.getBeliefs(b)[0], 0.01);
    }

    @Test
    public void testUnconnected() {
        BayesNet net = NetExamples.unconnectedNet();
        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        IBayesInferer inference = new JunctionTreeAlgorithm();
        inference.addEvidence(a, "false");
        inference.addEvidence(b, "true");
        inference.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "true");

        for (BayesNode n : net.getNodes())
            assertArrayEquals(inference.getBeliefs(n), compare.getBeliefs(n), 0.01);
    }

    @Test
    public void testSparseFactors() {
        BayesNet net = NetExamples.sparseNet();

        BayesNode a = net.getNode("a");
        BayesNode b = net.getNode("b");

        IBayesInferer inference = new JunctionTreeAlgorithm();
        inference.addEvidence(a, "false");
        inference.addEvidence(b, "lu");
        inference.setNetwork(net);

        IBayesInferer compare = new LoopyBeliefPropagation();
        compare.setNetwork(net);
        compare.addEvidence(a, "false");
        compare.addEvidence(b, "lu");

        for (BayesNode n : net.getNodes())
            assertArrayEquals(compare.getBeliefs(n), inference.getBeliefs(n), 0.01);
    }
    
    @Test
    @Ignore
    public void testLargerScaleCorrectness() throws IOException{
    	BayesNet net = new XDSLReader().read("alarm.xdsl");//FIXME: use recommender model
    	TestcaseDeserializer deser = new TestcaseDeserializer(net);
    	Reader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("testcases_alarm.json")));
    	StringBuffer buf = new StringBuffer();
    	CharBuffer cbuff = CharBuffer.allocate(1024);
    	while(rdr.read(cbuff) != -1){
    		cbuff.flip();
    		buf.append(cbuff);
    		cbuff.clear();
    	}
    	rdr.close();
    	
    	List<TestCase> testcases = deser.deserialize(buf.toString());
    	
    	JunctionTreeAlgorithm algo = new JunctionTreeAlgorithm();
    	algo.setNetwork(net);
    	
    	for(TestCase tc: testcases){
    		algo.setEvidence(tc.evidence);
    		for(BayesNode node: net.getNodes()){
    			assertArrayEquals(tc.beliefs.get(node),algo.getBeliefs(node),0.00001);
    		}
    	}
    }

}
