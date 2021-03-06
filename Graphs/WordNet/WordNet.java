import java.util.HashMap;
import java.util.Map; 

public class WordNet {
   
   private Map<Integer, String> id2SynsetDefinition; // for quick search ancestor
	// for quick search noun in WordNet. Use bag for values, as there can be more then 1 id correspond to the word. Key - noun
	private Map<String, Bag<Integer>> synset2id;
	private SAP sap;

	
   // constructor takes the name of the two input files
   public WordNet(String synsets, String hypernyms) {
	   id2SynsetDefinition = new HashMap<Integer, String>();
	   synset2id = new HashMap<String, Bag<Integer>>();
	   createMaps(synsets);
	   createSAP(hypernyms);
   }


   // returns all WordNet nouns
   public Iterable<String> nouns() {
	   return synset2id.keySet();
   }


   // is the word a WordNet noun?
   public boolean isNoun(String word) {
	   return synset2id.containsKey(word)
   }


   // distance between nounA and nounB 
   public int distance(String nounA, String nounB) {
	   if (!isNoun(nounA) || !isNoun(nounB)) throw new java.lang.IllegalArgumentException("No such nouns in WordNet!");
	   return sap.length(synset2id.get(nounA), synset2id.get(nounB));
   }


   // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB in a shortest ancestral path 
   public String sap(String nounA, String nounB) {
	   if (!isNoun(nounA) || !isNoun(nounB)) throw new java.lang.IllegalArgumentException("No such nouns in WordNet!");
	   int ancestorId = sap.ancestor(synset2id.get(nounA), synset2id.get(nounB));
	   String valueFields[] = id2SynsetDefinition.get(ancestorId).split(",");
	   return valueFields[0];
   }   
      
	   
   private void createMaps(String synsets) {
	   In in = new In(synsets);
	   while (in.hasNextLine()) {
		   String curString = in.readLine();
		   String[] fields = curString.split(",");
		   for (int i = 0; i < fields.length; i++) {
			   fields[i] = fields[i].trim();
		   }
		   
		   int id = Integer.parseInt(fields[0]);
		   String synsetDefinition = fields[1] + "," + fields[2];
		   id2SynsetDefinition.put(id, synsetDefinition);
		   
		   String synonyms[] = fields[1].split(" ");
		   for (int i = 0; i < synonyms.length; i++) {
			   synonyms[i] = synonyms[i].trim();
			   Bag<Integer> bag = synset2id.get(synonyms[i]);
			   if (bag == null) {
				   Bag<Integer> newBag = new Bag<Integer>();
		             	   newBag.add(id);
		             	   synset2id.put(synonyms[i], newBag);
		          }
		          else {
				  bag.add(id);
		          }
		   }
	   }
   }
	   
      
      
   private void createSAP(String hypernyms) {
	   In in = new In(hypernyms);
	   Digraph diG = new Digraph(id2SynsetDefinition.size());
	   while (in.hasNextLine()) {
		   String curString = in.readLine();
		   String[] fields = curString.split(",");
		   for (int i = 0; i < fields.length; i++) {
			   fields[i] = fields[i].trim();
		   }
		   for (int i = 1; i < fields.length; i++) {
			   diG.addEdge(Integer.parseInt(fields[0]), Integer.parseInt(fields[i]));
		   }
	   } 
	   if(!isRootedDAG(diG)) {
		   throw new java.lang.IllegalArgumentException("Not rooted DAG!");
	   } 
	   sap = new SAP(diG);
   }
	   
   
   private boolean isRootedDAG(Digraph diG) {
	   // check if there is no Cycle 
	   DirectedCycle diCycle = new DirectedCycle(diG);
	   if (diCycle.hasCycle()) {
		   return false;
	   }
	   
	   // check if there is one root (root = vertex with no outgoing edges)
	   int roots = 0;
	   for (int vertex = 0; vertex < diG.V(); vertex++) {
		   if (!diG.adj(vertex).iterator().hasNext()) roots++;
	   }
	   if (roots != 1) return false;
	   return true;
   }	   

	
   // do unit testing of this class
   public static void main(String[] args) {
	   WordNet wordnet = new WordNet(args[0], args[1]);
	   for (String s : wordnet.nouns()) {
		   StdOut.println(s);
	   }
	   
	   while (!StdIn.isEmpty()) {
		   String nounX = StdIn.readLine();
	           String nounY = StdIn.readLine();
	           int distance   = wordnet.distance(nounX, nounY);
	           String ancestor = wordnet.sap(nounX, nounY);
	           StdOut.println("length = " + distance);
	           StdOut.println("ancestor = " + ancestor);
	   }
    } 	
	
}
