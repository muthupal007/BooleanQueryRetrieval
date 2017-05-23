

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class CSE535Assignment2 {
	
	private HashMap<String, LinkedList<Integer>> invIndex;
	private PrintWriter post;
	private FileInputStream fin;
	private BufferedReader buf;
	private String currline;
	private int dcount;
	
	public static void main(String[] args) throws IOException
	{
	CSE535Assignment2 l = new CSE535Assignment2();
	if(args.length != 3)
	{
		System.out.println("Arguments must be <Index> <Output> <Input>");
		return;
	}
	String index_path = args[0];
	String outputFile = args[1];
	String inputFile = args[2];
	l.readIndex(index_path);
	l.getPostings(outputFile, inputFile);
	}
	
	public void getPostings(String outputFile, String inputFile) throws IOException {
		post = new PrintWriter(outputFile,"UTF-8");	
		fin = new FileInputStream(new File(inputFile));
		buf = new BufferedReader(new InputStreamReader(fin,"UTF-8"));
		
		while ((currline = buf.readLine())!=null)
		{
			getPosting();
			taatAnd();
			taatOr();
			daatAndOr();
		}
    	post.close();
}
	private void getPosting()
	{
		String[] words = currline.split(" ");
		for(String key : invIndex.keySet())
		{
		for(String w : words)
		{
			if (key.equals(w))
		{    		
		List<Integer> prdoc = new ArrayList<Integer>(); 
		prdoc = invIndex.get(key);
		post.write("GetPostings\n" +key+ "\nPostings list: ");
		for(int docid : prdoc)
		{
		post.write(docid + " ");
		}
		post.write("\n");
		}
		}
	}
	}
	public void readIndex(String index_path) throws IOException {
	invIndex = new HashMap<String, LinkedList<Integer>>();
	Terms terms;
	File path = new File(index_path);
	Directory index = FSDirectory.open(path.toPath());
	IndexReader reader = DirectoryReader.open(index);	
	Fields fields = MultiFields.getFields(reader);
	
	
	  for (String f : fields) 
	  {
		if(f.startsWith("text_"))
		{
		terms = fields.terms(f);
		TermsEnum t = terms.iterator();
		BytesRef term;
	    int i = 1;
	    while((term = t.next())!=null)
	    {	
	       	i++;
	    	String termString = term.utf8ToString();
	    	PostingsEnum p = MultiFields.getTermDocsEnum(reader, f, term);
	    	int docId;
	    	LinkedList <Integer> docIdList = new LinkedList<Integer>();
	        while ((docId = p.nextDoc()) != PostingsEnum.NO_MORE_DOCS) 
	        {	
	        	docIdList.add(docId);
	        }
	        invIndex.put(termString, docIdList);
	    }	
	   	}
	  }
	}
	  
private void taatOr() throws IOException
	  {
			int count=0;
			post.println("TaatOr");
			post.println(currline);
			post.print("Results:");
			String[] words = currline.split(" ");
			LinkedList<Integer> list = new LinkedList<Integer>();
			for(String word : words)
			{
				if(invIndex.containsKey(word))
				{
					if(list.isEmpty())
					{
						list.addAll((LinkedList<Integer>)invIndex.get(word));
					}
					else if(!list.isEmpty())
					{
						ListIterator<Integer> itr = list.listIterator();
						for(int j : invIndex.get(word))
						{
							while(itr.hasNext())
							{
								count++;
								int k = itr.next();
								if(j==k)
								{
									break;
								}
								else if(j<k)
								{	
									itr.previous();
									itr.add(j);
									break;
								}
							}
							if(!itr.hasNext())
							{
								itr.previous();
								if(itr.next()!=j)
								{
									itr.add(j);
								}
							}	
						}
					}
				}
			}
			if(!list.isEmpty())
			{
				for(int i : list)
				{
					post.print(" "+i);
				}
			}
			else
			{
				post.print(" empty");
			}
			post.println();
			post.println("Number of documents in results: "+list.size());
			post.println("Number of comparisons: "+count);
			
		}
private void taatAnd() {

	int count=0;
	post.println("TaatAnd");
	post.println(currline);
	post.print("Results:");
	String[] words = currline.split(" ");
	LinkedList<Integer> list = new LinkedList<Integer>();
	LinkedList<Integer> set = new LinkedList<Integer>();
	for(String word : words)
	{
		
		if(invIndex.containsKey(word))
		{
			
			if(list.isEmpty())
			{
				list.addAll(invIndex.get(word));
			}
			else if(!list.isEmpty())
			{
				LinkedList<Integer> temp = (LinkedList<Integer>) invIndex.get(word).clone();
				ListIterator<Integer> itr = temp.listIterator();
				for(int j : list)
				{	
					//ListIterator<Integer> itr = temp.listIterator();
					while(itr.hasNext())
					{
						
						int k = itr.next();
						if(k==j)
						{
							set.add(k);
							//System.out.println(k);
							count++;
							break;
						}
						else if(k>j)
						{
							itr.previous();
							count++;
							break;
						}
					}
				}			
			}
		} 
	}
	
	if(!set.isEmpty())
	{
		for(int i : set)
		{
			post.print(" "+i);
		}
	}
	else
	{
		post.print(" empty");
	}
	post.println();
	post.println("Number of documents in results: "+set.size());
	post.println("Number of comparisons: "+count);
	
}

private void daatAndOr() 
{
	post.println("DaatAnd");
	post.println(currline);
	post.print("Results:");
	String[] words = currline.split(" ");

	TreeMap<Integer,Integer> m = new TreeMap<Integer, Integer>();
	PriorityQueue<Integer[]> pq = new PriorityQueue<Integer[]>(new MyComp());
	ArrayList<Iterator<Integer>> itr = new ArrayList<Iterator<Integer>>();
	int max = 0;
	for(String word : words)
	{
		if(invIndex.containsKey(word))
		{
			LinkedList<Integer> temp = invIndex.get(word);
			itr.add(temp.iterator());
			if(max<=invIndex.get(word).size())
			{
				max = invIndex.get(word).size();
			}
		}
	}
	for(int i = 0;i<max;i++)
	{
		for(int j = 0;j<itr.size();j++)
		{
			if(itr.get(j).hasNext())
			{
				int k =itr.get(j).next();
				dcount++;
				if(m.containsKey(k))
				{
					m.put(k, m.get(k)+1);
				}
				else{m.put(k, 1);
				}
			}
		}
	}
	
	if(m.isEmpty())
	{
		post.print(" empty");
	}
	else
	{
		for(Entry<Integer,Integer> e :m.entrySet())
		{
			Integer[] temp = {e.getKey(),e.getValue()};
			pq.add(temp);
		}
	}
	
	while(!pq.isEmpty())
	{
		if(pq.peek()[1].equals(words.length))
		{
			post.print(" "+pq.poll()[0]);
			
		}
		else
		{
			break;
		}
	}
	
	post.println();
	post.println("Number of documents in results: "+(m.size()-pq.size()));
	post.println("Number of comparisons: "+dcount);
	daatOr(m);
	
}

private void daatOr(TreeMap<Integer, Integer> m) 
{
	post.println("DaatOr");
	post.println(currline);
	post.print("Results:");
	
	if(m.isEmpty())
	{
		post.print(" empty");
	}
	else
	{
		for(Entry<Integer,Integer> e :m.entrySet())
		{
			post.print(" "+e.getKey());
		}
	}
	post.println();
	post.println("Number of documents in results: "+m.size());
	post.println("Number of comparisons: "+dcount);
}

class MyComp implements Comparator<Integer[]>
{
	public int compare(Integer[] arg0, Integer[] arg1) 
	{	
		if(arg0[1]>arg1[1])
		{
			return -1;
		}
		else if(arg0[1]<arg1[1])
		{
			return 1;
		}
		
		else
		{
			return arg0[0].compareTo(arg1[0]);
		}	
	}
}
}